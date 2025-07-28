package de.kybe.KybesUtils.utils;

import org.rusherhack.client.api.utils.ChatUtils;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class ChatCrypto {
    private static final String START = "k©";
    private static final String END = "$%";
    private static final int SALT_LEN = 16;
    private static final int IV_LEN = 12;
    private static final int TAG_LEN = 128;

    private final List<String> readKeys = Collections.synchronizedList(new ArrayList<>());
    private final AtomicReference<String> writeKey = new AtomicReference<>();
    private final Map<String, StringBuilder> userBuffers = Collections.synchronizedMap(new HashMap<>());

    public void setWriteKey(String key, boolean debug) {
        writeKey.set(key);
        if (debug) ChatUtils.print("Write key set.");
    }

    public void addReadKey(String key, boolean debug) {
        if (key != null && !key.trim().isEmpty()) {
            readKeys.add(key.trim());
            if (debug) ChatUtils.print("Read key added: " + key.trim());
        }
    }

    public void resetReadKeys() {
        readKeys.clear();
    }

    public String encrypt(String plaintext, boolean debug) throws Exception {
        String password = writeKey.get();
        if (password == null || password.isEmpty()) {
            if (debug) ChatUtils.print("Encryption failed: write key is not set.");
            throw new IllegalStateException("Write key is not set.");
        }

        if (debug) ChatUtils.print("Encrypting message...");

        byte[] salt = new byte[SALT_LEN];
        new SecureRandom().nextBytes(salt);

        SecretKeySpec key = deriveKey(password, salt);

        byte[] iv = new byte[IV_LEN];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LEN, iv));
        byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

        byte[] combined = new byte[salt.length + iv.length + ciphertext.length];
        System.arraycopy(salt, 0, combined, 0, salt.length);
        System.arraycopy(iv, 0, combined, salt.length, iv.length);
        System.arraycopy(ciphertext, 0, combined, salt.length + iv.length, ciphertext.length);

        String encoded = Base64.getEncoder().encodeToString(combined);
        if (debug) ChatUtils.print("Encryption complete.");
        return START + encoded + END;
    }

    public String decrypt(String base64, boolean debug) {
        if (debug) ChatUtils.print("Attempting decryption...");

        byte[] data;
        try {
            data = Base64.getDecoder().decode(base64);
        } catch (IllegalArgumentException e) {
            if (debug) ChatUtils.print("Base64 decode failed.");
            return null;
        }

        if (data.length < SALT_LEN + IV_LEN + 1) {
            if (debug) ChatUtils.print("Data too short to decrypt.");
            return null;
        }

        byte[] salt = Arrays.copyOfRange(data, 0, SALT_LEN);
        byte[] iv = Arrays.copyOfRange(data, SALT_LEN, SALT_LEN + IV_LEN);
        byte[] ciphertext = Arrays.copyOfRange(data, SALT_LEN + IV_LEN, data.length);

        synchronized (readKeys) {
            for (String password : readKeys) {
                try {
                    SecretKeySpec key = deriveKey(password, salt);

                    Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                    cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LEN, iv));
                    byte[] plaintext = cipher.doFinal(ciphertext);

                    if (debug) ChatUtils.print("Decryption succeeded.");
                    return new String(plaintext, StandardCharsets.UTF_8);
                } catch (AEADBadTagException e) {
                    if (debug) ChatUtils.print("Decryption failed: bad tag (wrong key or tampered data).");
                } catch (Exception e) {
                    if (debug) ChatUtils.print("Decryption failed with one key, trying next...");
                }
            }
        }

        if (debug) ChatUtils.print("All decryption attempts failed.");
        return null;
    }

    private SecretKeySpec deriveKey(String password, byte[] salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 100000, 128);
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }

    public String handleInput(String user, String input, boolean debug) {
        userBuffers.putIfAbsent(user, new StringBuilder());
        StringBuilder buffer = userBuffers.get(user);

        if (debug) ChatUtils.print("Handling input for user: " + user);

        int startIdx = input.indexOf(START);
        int endIdx = input.indexOf(END);

        if (startIdx != -1 && endIdx != -1 && startIdx < endIdx) {
            if (debug) ChatUtils.print("Full message in one packet.");
            String content = input.substring(startIdx + START.length(), endIdx);
            String result = decrypt(content, debug);
            return result != null ? result : "[Decryption failed]";
        }

        if (startIdx != -1) {
            if (debug) ChatUtils.print("START detected. Resetting buffer.");
            buffer.setLength(0);
            buffer.append(input.substring(startIdx + START.length()));
            return null;
        }

        buffer.append(input);
        int completeEndIdx = buffer.indexOf(END);
        if (completeEndIdx != -1) {
            if (debug) ChatUtils.print("END detected. Finalizing buffer.");
            String content = buffer.substring(0, completeEndIdx);
            buffer.setLength(0);
            String result = decrypt(content, debug);
            return result != null ? result : "[Decryption failed]";
        }

        return null;
    }

    public String getWriteKey() {
        return writeKey.get();
    }

    public ArrayList<String> getReadKeys() {
        synchronized (readKeys) {
            return new ArrayList<>(readKeys);
        }
    }
}
