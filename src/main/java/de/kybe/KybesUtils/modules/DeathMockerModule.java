package de.kybe.KybesUtils.modules;

import de.kybe.KybesUtils.utils.DeathMessage;
import de.kybe.KybesUtils.utils.DeathMessageParser;
import org.rusherhack.client.api.events.client.chat.EventAddChat;
import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.client.api.feature.module.ToggleableModule;
import org.rusherhack.client.api.utils.ChatUtils;
import org.rusherhack.core.event.stage.Stage;
import org.rusherhack.core.event.subscribe.Subscribe;
import org.rusherhack.core.setting.BooleanSetting;
import org.rusherhack.core.setting.EnumSetting;
import org.rusherhack.core.setting.NumberSetting;
import org.rusherhack.core.setting.StringSetting;

import java.util.Arrays;
import java.util.Optional;
import java.util.Random;

public class DeathMockerModule extends ToggleableModule {
    private final StringSetting format = new StringSetting(
            "MessageFormat",
            "Format ({name} = name, {rand} = random string, ; separates entries)",
            "how tf did you die {name} | {rand};how can you be soooo bad {name} | {rand}"
    );

    private final NumberSetting<Integer> randomMessagePartLength = new NumberSetting<>(
            "RandomLength",
            "Length of random string",
            15, 0, 255
    );

    private final EnumSetting<Mode> mode = new EnumSetting<>("Mode", Mode.Direct);

    private final StringSetting ignoreList = new StringSetting("IgnoreList", "(, seperated)", "");

    private final BooleanSetting beforeAntispam = new BooleanSetting("BeforeAntiSpam", false);

    private final BooleanSetting debug = new BooleanSetting("debug", false).setHidden(true);
    private final Random random = new Random();

    public DeathMockerModule() {
        super("DeathMocker", ModuleCategory.CHAT);
        this.registerSettings(format, randomMessagePartLength, mode, ignoreList, beforeAntispam, debug);
    }

    @Subscribe(stage = Stage.POST, priority = -1, ignoreCancelled = true)
    @SuppressWarnings("unused")
    public void onPacketAfterAntispam(EventAddChat event) {
        if (beforeAntispam.getValue()) return;
        inner(event);
    }

    @Subscribe(stage = Stage.PRE, priority = 1, ignoreCancelled = true)
    @SuppressWarnings("unused")
    public void onPacketBeforeAntispam(EventAddChat event) {
        if (!beforeAntispam.getValue()) return;
        inner(event);
    }

    public void inner(EventAddChat event) {
        if (mc.player == null || mc.getConnection() == null) return;

        String raw = event.getChatComponent().getString();
        if (debug.getValue()) ChatUtils.print("raw string: " + raw);

        Optional<DeathMessage> data = DeathMessageParser.parse(raw);
        if (debug.getValue()) ChatUtils.print("data: " + data);

        if (data.isEmpty()) return;

        DeathMessage deathMsg = data.get();

        if (deathMsg.victim() == null) return;

        String[] ignored = ignoreList.getValue().split(",");
        if (debug.getValue()) ChatUtils.print("ignored: " + Arrays.toString(ignored));
        for (String ignoredName : ignored) {
            if (ignoredName.trim().equalsIgnoreCase(deathMsg.victim())) {
                if (debug.getValue()) ChatUtils.print("ignored returning");
                return;
            }
        }

        String[] templates = format.getValue().split(";");
        String template = templates[random.nextInt(templates.length)].trim();


        if (debug.getValue()) ChatUtils.print("using template: " + template);

        String replaced = template.replace("{name}", deathMsg.victim())
                .replace("{rand}", randomString(randomMessagePartLength.getValue()));


        if (mode.getValue() == Mode.Direct) {
            if (debug.getValue()) ChatUtils.print("whispering: " + replaced);
            mc.getConnection().sendCommand("msg " + deathMsg.victim() + " " + replaced);
        } else if (mode.getValue() == Mode.Chat) {
            if (debug.getValue()) ChatUtils.print("chating: " + replaced);
            mc.getConnection().sendChat(replaced);
        }
    }

    private String randomString(int length) {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            result.append(chars.charAt(random.nextInt(chars.length())));
        }
        return result.toString();
    }

    enum Mode {
        Direct,
        Chat
    }
}