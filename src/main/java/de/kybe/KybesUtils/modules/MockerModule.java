package de.kybe.KybesUtils.modules;

import de.kybe.KybesUtils.utils.ChatMessage;
import de.kybe.KybesUtils.utils.ChatMessageParser;
import de.kybe.KybesUtils.utils.DirectMessage;
import de.kybe.KybesUtils.utils.DirectMessageParser;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import org.rusherhack.client.api.events.network.EventPacket;
import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.client.api.feature.module.ToggleableModule;
import org.rusherhack.client.api.utils.ChatUtils;
import org.rusherhack.core.event.stage.Stage;
import org.rusherhack.core.event.subscribe.Subscribe;
import org.rusherhack.core.setting.BooleanSetting;
import org.rusherhack.core.setting.NullSetting;
import org.rusherhack.core.setting.NumberSetting;
import org.rusherhack.core.setting.StringSetting;

import java.util.Arrays;
import java.util.Random;

public class MockerModule extends ToggleableModule {
    private final NumberSetting<Double> triggerChance = new NumberSetting<>("TriggerChance", "0 = never, 100 = always", 100.0, 0.0, 100.0);
    private final NumberSetting<Integer> maxTriggerLength = new NumberSetting<>("MaxTriggerMessageLength", 15, 0, 255);

    private final StringSetting ignoreList = new StringSetting("IgnoreList", "Players to ignore (, separated)", "2kybe3");
    private final StringSetting forceList = new StringSetting("ForceList", "Players who are always cloned (, separated)", "Tilley8");
    private final StringSetting ignoreWords = new StringSetting("IgnoredWords", "Words to filter out messages (, separated, : means AND)", "gay,attracted:men");

    private final BooleanSetting globalMessageSupport = new BooleanSetting("GlobalMessageTrigger", "Should global messages trigger?", true);
    private final BooleanSetting directMessageSupport = new BooleanSetting("DirectMessageTrigger", "Should private messages trigger?", true);
    private final BooleanSetting globalMessageDirectMessageReply = new BooleanSetting("Global -> DirectReply", "Reply to global message privately?", false);
    private final BooleanSetting directMessageDirectMessageReply = new BooleanSetting("Direct -> DirectReply", "Reply to DM privately?", true);
    private final BooleanSetting beforeAntispam = new BooleanSetting("BeforeAntiSpam", false);

    private final StringSetting format = new StringSetting("MessageFormat", "Format ({msg} = message, {rand} = random string)", "\"{msg}\" | {rand}");
    private final NumberSetting<Integer> randomMessagePartLength = new NumberSetting<>("Random Length", "Length of random string", 15, 0, 255);
    private final BooleanSetting derpCaps = new BooleanSetting("DerpCaps", "Random case letters", true);
    private final StringSetting chatRegex = new StringSetting("ChatRegex", "(Group 1 = name, 2 = msg)", "^<([a-zA-Z0-9_]+)>\\s*>*\\s*(.+)$");
    private final StringSetting msgRegex = new StringSetting("MessageRegex", "(Group 1 = name, 2 = msg)", "^([a-zA-Z0-9_]+) whispers: (.+)$");

    private final BooleanSetting debug = new BooleanSetting("debug", false).setHidden(true);

    private final Random random = new Random();

    public MockerModule() {
        super("Mocker", "Mocks Players like \"\"HaLlO HoW Ar u\" | smth\"",ModuleCategory.CHAT);

        NullSetting lists = new NullSetting("Comma-SeparatedLists");
        lists.addSubSettings(ignoreList, forceList, ignoreWords);

        NullSetting msgType = new NullSetting("MessageType");
        msgType.addSubSettings(globalMessageSupport, directMessageSupport, globalMessageDirectMessageReply, directMessageDirectMessageReply);

        NullSetting formats = new NullSetting("Formats");
        formats.addSubSettings(format, randomMessagePartLength, derpCaps, chatRegex, msgRegex);

        registerSettings(triggerChance, maxTriggerLength, beforeAntispam, lists, msgType, formats, debug);
    }

    @Subscribe(stage = Stage.POST, priority = -1, ignoreCancelled = true)
    @SuppressWarnings("unused")
    public void onPacketAfterAntispam(EventPacket.Receive event) {
        if (beforeAntispam.getValue()) return;
        inner(event);
    }

    @Subscribe(stage = Stage.PRE, priority = 1, ignoreCancelled = true)
    @SuppressWarnings("unused")
    public void onPacketBeforeAntispam(EventPacket.Receive event) {
        if (!beforeAntispam.getValue()) return;
        inner(event);
    }

    public void inner(EventPacket.Receive event) {
        if (mc.player == null || mc.getConnection() == null) return;
        if (!(event.getPacket() instanceof ClientboundSystemChatPacket packet)) return;

        String raw = packet.content().getString();
        if (debug.getValue()) ChatUtils.print("Received raw: " + raw);

        MessageData data = parseMessage(raw);

        if (data == null) {
            if (debug.getValue()) ChatUtils.print("No message data parsed");
            return;
        }

        if (debug.getValue())
            ChatUtils.print("Parsed message from: " + data.name + " msg: " + data.message + " direct: " + data.direct);

        if (shouldIgnoreSender(data.name)) {
            if (debug.getValue()) ChatUtils.print("Ignored sender: " + data.name);
            return;
        }

        if (shouldIgnoreMessage(data.message)) {
            if (debug.getValue()) ChatUtils.print("Ignored message content: " + data.message);
            return;
        }

        if (!shouldTrigger(data.name)) {
            if (debug.getValue()) ChatUtils.print("Trigger chance check failed for: " + data.name);
            return;
        }

        String processedMessage = derpCaps.getValue() ? applyDerpCaps(data.message) : data.message;
        String rand = randomString(randomMessagePartLength.getValue());

        String finalMsg = format.getValue()
                .replace("{name}", data.name)
                .replace("{msg}", processedMessage)
                .replace("{rand}", rand);

        if (debug.getValue()) ChatUtils.print("Final message to send: " + finalMsg);

        sendMessage(data.direct, data.name, finalMsg);
    }

    private MessageData parseMessage(String raw) {
        if (globalMessageSupport.getValue()) {
            ChatMessage chat = new ChatMessageParser(chatRegex.getValue()).parse(raw);
            if (chat != null) return new MessageData(chat.name(), chat.msg(), false);
        }

        if (directMessageSupport.getValue()) {
            DirectMessage dm = new DirectMessageParser(msgRegex.getValue()).parse(raw);
            if (dm != null) return new MessageData(dm.name(), dm.msg(), true);
        }

        return null;
    }

    private boolean shouldIgnoreSender(String name) {
        return Arrays.stream(ignoreList.getValue().split(","))
                .map(String::trim)
                .anyMatch(ignore -> ignore.equalsIgnoreCase(name));
    }

    private boolean shouldTrigger(String name) {
        boolean forced = Arrays.stream(forceList.getValue().split(","))
                .map(String::trim)
                .anyMatch(force -> force.equalsIgnoreCase(name));
        if (forced) return true;

        return random.nextDouble() * 100 <= triggerChance.getValue();
    }

    private boolean shouldIgnoreMessage(String message) {
        if (message.length() > maxTriggerLength.getValue()) return true;

        String lowered = message.toLowerCase();
        for (String entry : ignoreWords.getValue().split(",")) {
            String[] terms = entry.split(":");
            boolean allMatch = Arrays.stream(terms).allMatch(term -> lowered.contains(term.trim()));
            if (allMatch) return true;
        }

        return false;
    }

    private void sendMessage(boolean direct, String name, String msg) {
        if (mc.getConnection() == null) return;

        boolean replyPrivately = direct ? directMessageDirectMessageReply.getValue() : globalMessageDirectMessageReply.getValue();

        if (replyPrivately) {
            mc.getConnection().sendCommand("msg " + name + " " + msg);
        } else {
            mc.getConnection().sendChat(msg);
        }
    }

    private String applyDerpCaps(String input) {
        StringBuilder result = new StringBuilder(input.length());
        for (char c : input.toCharArray()) {
            result.append(random.nextBoolean() ? Character.toUpperCase(c) : Character.toLowerCase(c));
        }
        return result.toString();
    }

    private String randomString(int length) {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            result.append(chars.charAt(random.nextInt(chars.length())));
        }
        return result.toString();
    }

    private record MessageData(String name, String message, boolean direct) {
    }
}