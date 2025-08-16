package de.kybe.KybesUtils.modules;

import de.kybe.KybesUtils.utils.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.rusherhack.client.api.events.client.EventUpdate;
import org.rusherhack.client.api.events.client.chat.EventAddChat;
import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.client.api.feature.module.ToggleableModule;
import org.rusherhack.client.api.utils.ChatUtils;
import org.rusherhack.core.event.subscribe.Subscribe;
import org.rusherhack.core.setting.BooleanSetting;
import org.rusherhack.core.setting.NullSetting;
import org.rusherhack.core.setting.NumberSetting;
import org.rusherhack.core.setting.StringSetting;

import java.util.ArrayList;
import java.util.LinkedList;

public class CryptoChatModule extends ToggleableModule {
    public static CryptoChatModule INSTANCE;

    public final BooleanSetting useChatPrefix = new BooleanSetting("UseChatPrefix", true);
    public final StringSetting chatPrefix = new StringSetting("ChatPrefix", "+");
    public final BooleanSetting allChatMessages = new BooleanSetting("AllChatMessages", false);    private final StringSetting encryptKey = new StringSetting("EncryptKey", "rusherhack")
            .onChange(this::updateCryptoKeys);
    private final StringSetting chatRegex = new StringSetting("ChatRegex", "(Group 1 = name, 2 = msg)",
            "^<([a-zA-Z0-9_]+)>\\s*>*\\s*(.+)$");
    private final StringSetting msgRegex = new StringSetting("MessageRegex", "(Group 1 = name, 2 = msg)",
            "^([a-zA-Z0-9_]+) whispers: (.+)$");
    private final StringSetting outboundDirectMsgRegex = new StringSetting("OutboundDirectMsgRegex", "(Group 1 = target, 2 = msg)",
            "^to ([a-zA-Z0-9_]+): (.+)$");    private final StringSetting decryptKeys = new StringSetting("DecryptKeys", "(comma separated)", "rusherhack")
            .onChange(this::updateCryptoKeys);
    private final NumberSetting<Integer> maxChatLength = new NumberSetting<>("MaxChatLength", 50, 0, 255);
    private final NumberSetting<Integer> sendDelaySeconds = new NumberSetting<>("SendDelay", "in seconds", 5, 0, 255);
    private final BooleanSetting ignoreSelf = new BooleanSetting("IgnoreSelf", false);
    private final BooleanSetting debug = new BooleanSetting("Debug", false).setHidden(true);
    private final ChatCrypto crypto = new ChatCrypto();
    private final LinkedList<SendItem> sendQueue = new LinkedList<>();
    private int sendTimer = 0;
    private SendItem currentSending = null;
    private int currentChunkIndex = 0;
    public CryptoChatModule() {
        super("CryptoChat", ModuleCategory.CHAT);
        NullSetting regexes = new NullSetting("Regular Expressions");
        regexes.addSubSettings(chatRegex, msgRegex, outboundDirectMsgRegex);
        this.registerSettings(encryptKey, decryptKeys, regexes, maxChatLength, sendDelaySeconds, ignoreSelf, useChatPrefix, chatPrefix, allChatMessages, debug);
        INSTANCE = this;
    }

    public static void log(String text) {
        MutableComponent prefix =
                Component.literal("[CryptoChat]").withStyle(ChatFormatting.AQUA)
                        .append(Component.literal(" " + text).withStyle(ChatFormatting.WHITE));
        ChatUtils.print(prefix);
    }

    public void queueMessage(String text) {
        CryptoChatModule.log("Queued encrypted chat: " + text);
        queueInternal(SendItem.Type.CHAT, null, text);
    }

    public void queueDirectMessage(String target, String text) {
        queueInternal(SendItem.Type.DIRECT, target, text);
    }

    private void queueInternal(SendItem.Type type, String target, String text) {
        String encrypted;
        try {
            encrypted = crypto.encrypt(text, debug.getValue());
        } catch (Exception e) {
            log("ERROR ENCRYPTING! " + e.getMessage());
            return;
        }

        if (encrypted == null) {
            log("Encryption failed!");
            return;
        }

        SendItem item = new SendItem();
        item.type = type;
        item.directTarget = target;
        item.fullText = encrypted;

        int maxLen = maxChatLength.getValue();
        for (int i = 0; i < encrypted.length(); i += maxLen) {
            String chunk = encrypted.substring(i, Math.min(i + maxLen, encrypted.length()));
            item.chunks.add(chunk);
            if (debug.getValue()) log("Chunk[" + (i / maxLen) + "]: " + chunk);
        }

        sendQueue.add(item);

        if (debug.getValue()) {
            log("Queued message with " + item.chunks.size() + " chunks. Type: " + type + ", Target: " + target + ", Encrypted: " + encrypted);
        }
    }

    @Override
    public void onEnable() {
        updateCryptoKeys();
    }

    private void updateCryptoKeys() {
        crypto.setWriteKey(encryptKey.getValue(), debug.getValue());
        crypto.resetReadKeys();

        for (String key : decryptKeys.getValue().split(",")) {
            key = key.trim();
            if (!key.isEmpty()) {
                crypto.addReadKey(key, debug.getValue());
            }
        }
    }

    @Subscribe(priority = -1001)
    @SuppressWarnings("unused")
    public void onPacket(EventAddChat event) {
        if (mc.player == null) return;

        String msg = event.getChatComponent().getString();
        String playerName = mc.player.getGameProfile().getName();

        if (debug.getValue()) log("Received chat packet: " + msg);

        ChatMessageParser chatParser = new ChatMessageParser(chatRegex.getValue());
        ChatMessage chat = chatParser.parse(msg);

        if (chat != null) {
            if (debug.getValue()) {
                log("Parsed Chat - Sender: " + chat.name() + ", Msg: " + chat.msg());
            }

            if (currentSending != null &&
                    currentChunkIndex < currentSending.chunks.size() &&
                    chat.name().equals(playerName)) {

                String expected = currentSending.chunks.get(currentChunkIndex);
                if (debug.getValue())
                    log("Matching chunk[" + currentChunkIndex + "]: expected='" + expected + "', actual='" + chat.msg() + "'");

                if (chat.msg().equals(expected)) {
                    currentChunkIndex++;
                    if (debug.getValue()) log("Chunk match. Index now: " + currentChunkIndex);
                } else {
                    log("Chunk mismatch! Ignored.");
                }
            }

            if (ignoreSelf.getValue() && chat.name().equals(playerName)) return;

            String decrypted = crypto.handleInput(chat.name(), chat.msg(), debug.getValue());
            if (decrypted != null) {
                log("<" + chat.name() + "> " + decrypted);
            }
            return;
        }

        DirectMessageParser dmParser = new DirectMessageParser(msgRegex.getValue());
        DirectMessage dm = dmParser.parse(msg);

        if (dm != null) {
            if (debug.getValue()) {
                log("Parsed DM - Sender: " + dm.name() + ", Msg: " + dm.msg());
            }

            if (ignoreSelf.getValue() && dm.name().equals(playerName)) return;

            String decrypted = crypto.handleInput(dm.name(), dm.msg(), debug.getValue());
            if (decrypted != null) {
                log(dm.name() + " -> you: " + decrypted);
            }
            return;
        }

        OutboundDirectMessageParser outboundParser = new OutboundDirectMessageParser(outboundDirectMsgRegex.getValue());
        OutboundDirectMessage outbound = outboundParser.parse(msg);

        if (outbound != null
                && currentSending != null
                && currentSending.type == SendItem.Type.DIRECT
                && currentSending.directTarget.equals(outbound.target())
                && currentChunkIndex < currentSending.chunks.size()) {

            String expected = currentSending.chunks.get(currentChunkIndex);
            if (debug.getValue())
                log("Outbound match check: expected='" + expected + "', actual='" + outbound.msg() + "'");
            if (expected.equals(outbound.msg())) {
                currentChunkIndex++;
                if (debug.getValue()) log("Outbound chunk match. Index now: " + currentChunkIndex);
            } else {
                log("Outbound chunk mismatch!");
            }
        }
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onUpdate(EventUpdate event) {
        if (mc.getConnection() == null) return;

        if (currentSending != null || !sendQueue.isEmpty()) {
            if (sendTimer > 0) {
                sendTimer--;
                return;
            }

            if (currentSending == null) {
                currentSending = sendQueue.removeFirst();
                currentChunkIndex = 0;

                if (debug.getValue()) {
                    log("Now sending new item: " + currentSending.fullText);
                    log("Chunks: " + currentSending.chunks.size());
                }
            }

            if (currentChunkIndex < currentSending.chunks.size()) {
                String chunk = currentSending.chunks.get(currentChunkIndex);
                if (debug.getValue()) log("Sending chunk[" + currentChunkIndex + "]: " + chunk);

                if (currentSending.type == SendItem.Type.CHAT) {
                    MessageUtils.say(chunk);
                } else {
                    mc.getConnection().sendCommand("msg " + currentSending.directTarget + " " + chunk);
                }

                sendTimer = sendDelaySeconds.getValue() * 20;
            } else {
                if (debug.getValue()) log("Finished sending current message.");
                currentSending = null;
            }
        }
    }

    public void addReadKey(String key) {
        crypto.addReadKey(key, debug.getValue());
    }

    public void clearReadKeys() {
        crypto.resetReadKeys();
    }

    public String getWriteKey() {
        return crypto.getWriteKey();
    }

    public void setWriteKey(String key) {
        crypto.setWriteKey(key, debug.getValue());
    }

    public ArrayList<String> getReadKeys() {
        return crypto.getReadKeys();
    }

    public void clearSendQueue() {
        sendQueue.clear();
        currentSending = null;
        currentChunkIndex = 0;
        sendTimer = 0;
        log("Send queue cleared.");
    }

    public static class SendItem {
        public Type type;
        public String directTarget;
        public String fullText;
        public ArrayList<String> chunks = new ArrayList<>();
        public enum Type {DIRECT, CHAT}
    }




}