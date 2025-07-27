package de.kybe.KybesUtils.utils;

import org.rusherhack.client.api.utils.ChatUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class ChatCrypto {
    private static final String START = "k©";
    private static final String END = "$%";

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

    public void resetReadKeys(List<String> keys, boolean debug) {
        synchronized (readKeys) {
            readKeys.clear();
            for (String key : keys) {
                if (key != null && !key.trim().isEmpty()) {
                    readKeys.add(key.trim());
                    if (debug) ChatUtils.print("Read key added: " + key.trim());
                }
            }
        }
    }

    public String encrypt(String plaintext, boolean debug) throws Exception {
        String key = writeKey.get();
        if (key == null || key.isEmpty()) {
            if (debug) ChatUtils.print("Encryption failed: write key is not set.");
            throw new IllegalStateException("Write key is not set.");
        }

        if (debug) ChatUtils.print("Encrypting message...");

        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, makeKey(key), ivSpec);
        byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

        byte[] combined = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);

        if (debug) ChatUtils.print("Encryption complete.");

        String encoded = Base64.getEncoder().encodeToString(combined);
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

        if (data.length < 16) {
            if (debug) ChatUtils.print("Data too short to contain IV.");
            return null;
        }

        byte[] iv = Arrays.copyOfRange(data, 0, 16);
        byte[] ciphertext = Arrays.copyOfRange(data, 16, data.length);

        synchronized (readKeys) {
            for (String key : readKeys) {
                try {
                    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                    cipher.init(Cipher.DECRYPT_MODE, makeKey(key), new IvParameterSpec(iv));
                    byte[] plaintextBytes = cipher.doFinal(ciphertext);
                    String result = new String(plaintextBytes, StandardCharsets.UTF_8);

                    if (debug) ChatUtils.print("Decryption succeeded.");
                    return result;
                } catch (Exception e) {
                    if (debug) ChatUtils.print("Decryption failed with one key, trying next...");
                }
            }
        }

        if (debug) ChatUtils.print("All decryption attempts failed.");
        return null;
    }

    private SecretKeySpec makeKey(String key) throws Exception {
        byte[] hash = MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(Arrays.copyOf(hash, 16), "AES");
    }

    public String handleInput(String user, String input, boolean debug) {
        userBuffers.putIfAbsent(user, new StringBuilder());
        StringBuilder buffer = userBuffers.get(user);

        if (debug) ChatUtils.print("Handling input for user: " + user);

        int startIdx = input.indexOf(START);
        int endIdx = input.indexOf(END);

        // Case 1: full message in one packet
        if (startIdx != -1 && endIdx != -1 && startIdx < endIdx) {
            if (debug) ChatUtils.print("Full message in one packet.");
            String content = input.substring(startIdx + START.length(), endIdx);
            String result = decrypt(content, debug);
            return result != null ? result : "[Decryption failed]";
        }

        // Case 2: message starts here
        if (startIdx != -1) {
            if (debug) ChatUtils.print("START detected. Resetting buffer.");
            buffer.setLength(0);
            buffer.append(input.substring(startIdx + START.length()));
            return null;
        }

        // Case 3: message ends here
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
