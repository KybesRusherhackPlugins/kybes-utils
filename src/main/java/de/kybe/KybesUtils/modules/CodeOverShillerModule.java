package de.kybe.KybesUtils.modules;

import org.rusherhack.client.api.events.client.chat.EventAddChat;
import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.client.api.feature.module.ToggleableModule;
import org.rusherhack.client.api.utils.ChatUtils;
import org.rusherhack.core.event.stage.Stage;
import org.rusherhack.core.event.subscribe.Subscribe;
import org.rusherhack.core.setting.BooleanSetting;
import org.rusherhack.core.setting.NumberSetting;
import org.rusherhack.core.setting.StringSetting;

import java.util.Arrays;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeOverShillerModule extends ToggleableModule {
    private final StringSetting format = new StringSetting(
            "MessageFormat",
            "Format ({name} = name, {rand} = random string, ; separates entries)",
            "code kybe better, trust {name} :-)"
    );

    private final NumberSetting<Integer> randomMessagePartLength = new NumberSetting<>(
            "RandomLength",
            "Length of random string",
            15, 0, 255
    );

    private final StringSetting codeRegex = new StringSetting(
            "CodeRegex",
            "(1 group = name)",
            "(?i)^<([a-zA-Z0-9_]+)>\\s*>*\\s*(?=.*code)(?=.*rusherhack).+$"
    );

    private final StringSetting ignoreList = new StringSetting(
            "IgnoreList",
            "(, seperated)",
            "2kybe3"
    );

    private final BooleanSetting beforeAntispam = new BooleanSetting("BeforeAntiSpam", false);

    private final BooleanSetting debug = new BooleanSetting("debug", false).setHidden(true);

    private final Random random = new Random();


    public CodeOverShillerModule() {
        super("CodeOverShiller", ModuleCategory.CHAT);
        this.registerSettings(format, randomMessagePartLength, beforeAntispam, codeRegex, ignoreList, debug);
    }

    @Subscribe(stage = Stage.POST, priority = -1, ignoreCancelled = true)
    @SuppressWarnings("unused")
    public void onChatMessageAddBeforeAntiSPam(EventAddChat event) {
        if (beforeAntispam.getValue()) return;
        inner(event);
    }

    @Subscribe(stage = Stage.PRE, priority = 1, ignoreCancelled = true)
    @SuppressWarnings("unused")
    public void onPacketBeforeAntiSpam(EventAddChat event) {
        if (!beforeAntispam.getValue()) return;
        inner(event);
    }


    public void inner(EventAddChat event) {
        if (mc.player == null || mc.getConnection() == null) return;

        String raw = event.getChatComponent().getString();

        if (debug.getValue()) ChatUtils.print("raw string: " + raw);

        Pattern pattern = Pattern.compile(codeRegex.getValue());
        Matcher matcher = pattern.matcher(raw);

        if (debug.getValue()) ChatUtils.print("checking matches");
        if (!matcher.matches()) return;

        if (debug.getValue()) ChatUtils.print("matched checking groups");
        if (matcher.groupCount() < 1) return;
        String name = matcher.group(1);

        if (debug.getValue()) ChatUtils.print("name: " + name);

        String[] ignoredNames = ignoreList.getValue().split(",");
        for (String ignored : ignoredNames) {
            if (name.equalsIgnoreCase(ignored.trim())) {
                if (debug.getValue()) ChatUtils.print("ignoring " + name);
                return;
            }
        }

        String[] templates = format.getValue().split(";");
        String selected = templates[random.nextInt(templates.length)];

        if (debug.getValue()) ChatUtils.print("selected template: " + Arrays.toString(templates));

        String replaced = selected
                .replace("{name}", name)
                .replace("{rand}", randomString(randomMessagePartLength.getValue()));

        if (debug.getValue()) ChatUtils.print("sending: " + replaced);
        mc.getConnection().sendChat(replaced);
    }

    private String randomString(int length) {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            result.append(chars.charAt(random.nextInt(chars.length())));
        }
        return result.toString();
    }
}