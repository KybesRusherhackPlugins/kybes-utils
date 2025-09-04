package de.kybe.KybesUtils.modules;

import org.rusherhack.client.api.events.client.chat.EventChatMessage;
import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.client.api.feature.module.ToggleableModule;
import org.rusherhack.core.event.subscribe.Subscribe;
import org.rusherhack.core.setting.BooleanSetting;
import org.rusherhack.core.setting.NullSetting;
import org.rusherhack.core.setting.NumberSetting;
import org.rusherhack.core.setting.StringSetting;

import java.util.Random;

// TODO: remove this when john fixes his own one smh
public class ChatAppendV2 extends ToggleableModule {
    private final Random random = new Random();

    NullSetting prefixRandomSettings = new NullSetting("RandomSettings");
    BooleanSetting prefixRandomIncludeAlpha = new BooleanSetting("IncludeAlpha", true);
    BooleanSetting prefixRandomIncludeNumeric = new BooleanSetting("IncludeNumeric", true);
    BooleanSetting prefixRandomIncludeSpecial = new BooleanSetting("IncludeSpecial", false);
    NumberSetting<Integer> prefixRandomLength = new NumberSetting<>("Length", 5, 0, 20);

    NullSetting betweenRandomSettings = new NullSetting("RandomSettings");
    BooleanSetting betweenRandomIncludeAlpha = new BooleanSetting("IncludeAlpha", true);
    BooleanSetting betweenRandomIncludeNumeric = new BooleanSetting("IncludeNumeric", true);
    BooleanSetting betweenRandomIncludeSpecial = new BooleanSetting("IncludeSpecial", false);
    NumberSetting<Integer> betweenRandomLength = new NumberSetting<>("Length", 5, 0, 20);

    NullSetting postfixRandomSettings = new NullSetting("RandomSettings");
    BooleanSetting postfixRandomIncludeAlpha = new BooleanSetting("IncludeAlpha", true);
    BooleanSetting postfixRandomIncludeNumeric = new BooleanSetting("IncludeNumeric", true);
    BooleanSetting postfixRandomIncludeSpecial = new BooleanSetting("IncludeSpecial", false);
    NumberSetting<Integer> postfixRandomLength = new NumberSetting<>("Length", 5, 0, 20);

    BooleanSetting prefix = new BooleanSetting("Prefix", false);
    StringSetting prefixString = new StringSetting("PrefixString", "kybe");
    BooleanSetting prefixRandom = new BooleanSetting("PrefixRandom", false)
            .onChange(newValue -> {prefixString.setHidden(newValue); prefixRandomSettings.setHidden(!newValue);});

    BooleanSetting between = new BooleanSetting("Between", false);
    StringSetting betweenString = new StringSetting("BetweenString", "kybe");
    BooleanSetting betweenRandom = new BooleanSetting("BetweenRandom", false)
            .onChange(newValue -> {betweenString.setHidden(newValue); betweenRandomSettings.setHidden(!newValue);});

    BooleanSetting postfix = new BooleanSetting("Postfix", true);
    StringSetting postfixString = new StringSetting("PostfixString", "kybe");
    BooleanSetting postfixRandom = new BooleanSetting("PostfixRandom", false)
            .onChange(newValue -> {postfixString.setHidden(newValue); postfixRandomSettings.setHidden(!newValue);});

    BooleanSetting seperator = new BooleanSetting("Separator", true);
    StringSetting seperatorString = new StringSetting("SeperatorString", "|");

    public ChatAppendV2() {
        super("ChatAppendV2", "Allows you to customize sending messages", ModuleCategory.CHAT);

        prefixRandomSettings.addSubSettings(prefixRandomIncludeAlpha, prefixRandomIncludeNumeric, prefixRandomIncludeSpecial, prefixRandomLength);
        betweenRandomSettings.addSubSettings(betweenRandomIncludeAlpha, betweenRandomIncludeNumeric, betweenRandomIncludeSpecial, betweenRandomLength);
        postfixRandomSettings.addSubSettings(postfixRandomIncludeAlpha, postfixRandomIncludeNumeric, postfixRandomIncludeSpecial, postfixRandomLength);

        prefix.addSubSettings(prefixString, prefixRandom, prefixRandomSettings);
        between.addSubSettings(betweenString, betweenRandom, betweenRandomSettings);
        postfix.addSubSettings(postfixString, postfixRandom, postfixRandomSettings);

        seperator.addSubSettings(seperatorString);

        this.registerSettings(prefix, between, postfix, seperator);
    }

    @Subscribe
    public void onChatSend(EventChatMessage event) {
        if (event.getMessage().startsWith("/")) return;

        StringBuilder sb = new StringBuilder();

        if (prefix.getValue()) {
            sb.append(prefixRandom.getValue()
                    ? getRandomString(
                    prefixRandomIncludeAlpha.getValue(),
                    prefixRandomIncludeNumeric.getValue(),
                    prefixRandomIncludeSpecial.getValue(),
                    prefixRandomLength.getValue()
            )
                    : prefixString.getValue());
            if (seperator.getValue()) sb.append(sep());
        }

        String[] words = event.getMessage().split("\\s+");
        for (int i = 0; i < words.length; i++) {
            sb.append(words[i]);

            if (i < words.length - 1 && between.getValue()) {
                if (seperator.getValue()) sb.append(sep());
                sb.append(betweenRandom.getValue()
                        ? getRandomString(
                        betweenRandomIncludeAlpha.getValue(),
                        betweenRandomIncludeNumeric.getValue(),
                        betweenRandomIncludeSpecial.getValue(),
                        betweenRandomLength.getValue()
                )
                        : betweenString.getValue());
                if (seperator.getValue()) sb.append(sep());
            } else if (i < words.length - 1) {
                sb.append(" ");
            }
        }

        if (postfix.getValue()) {
            if (seperator.getValue()) sb.append(sep());
            sb.append(postfixRandom.getValue()
                    ? getRandomString(
                    postfixRandomIncludeAlpha.getValue(),
                    postfixRandomIncludeNumeric.getValue(),
                    postfixRandomIncludeSpecial.getValue(),
                    postfixRandomLength.getValue()
            )
                    : postfixString.getValue());
        }

        event.setMessage(sb.toString());
    }

    private String getRandomString(boolean alpha, boolean numeric, boolean special, int length) {
        StringBuilder chars = new StringBuilder();
        if (alpha) chars.append("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
        if (numeric) chars.append("0123456789");
        if (special) chars.append("!@#$%^&*()-_=+[]{};:,.<>?/");

        if (chars.isEmpty()) return "";

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }


    private String sep() {
        return " " + seperatorString.getValue() + " ";
    }
}
