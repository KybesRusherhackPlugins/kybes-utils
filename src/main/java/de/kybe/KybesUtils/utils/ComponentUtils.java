package de.kybe.KybesUtils.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.feature.module.IModule;
import org.rusherhack.client.api.setting.ColorSetting;
import org.rusherhack.core.setting.NullSetting;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class ComponentUtils {
    private static final IModule colorsModule = RusherHackAPI.getModuleManager().getFeature("Colors").orElseThrow();
    private static final NullSetting entities = (NullSetting) colorsModule.getSetting("Entities");
    private static final ColorSetting friendsColor = (ColorSetting) entities.getSubSetting("Friends");
    private static final ColorSetting enemiesColor = (ColorSetting) entities.getSubSetting("Enemies");

    public static Component recolorFriends(String message) {
        String[] parts = message.split("((?<= )|(?= ))");
        MutableComponent result = Component.literal("");

        for (String part : parts) {
            String stripped = part.strip();
            if (stripped.isEmpty()) {
                result.append(Component.literal(part));
                continue;
            }

            if (RusherHackAPI.getRelationManager().isFriend(stripped)) {
                result.append(Component.literal(stripped).withColor(friendsColor.getValueRGB()));
            } else if (RusherHackAPI.getRelationManager().isEnemy(stripped)) {
                result.append(Component.literal(stripped).withColor(enemiesColor.getValueRGB()));
            } else {
                result.append(Component.literal(stripped).withStyle(ChatFormatting.WHITE));
            }
        }

        return result;
    }

    public static Component formatTime(OffsetDateTime time) {
        String formatted = time.format(DateTimeFormatter.ofPattern("dd.MM.yy HH:mm:ss"));
        return Component.literal("[").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal(formatted).withStyle(ChatFormatting.GRAY))
                .append(Component.literal("] ").withStyle(ChatFormatting.DARK_GRAY));
    }

    public static String formatTimeStringRaw(OffsetDateTime time) {
        return time.format(DateTimeFormatter.ofPattern("dd.MM.yy HH:mm:ss"));
    }

    public static Component formatChat(String chat) {
        if (chat.startsWith(">")) {
            return Component.literal(chat.substring(1).strip()).withStyle(ChatFormatting.GREEN);
        } else {
            return Component.literal(chat.strip()).withStyle(ChatFormatting.WHITE);
        }
    }

    public static Component formatName(String name) {
        MutableComponent prefix = Component.literal("<").withStyle(ChatFormatting.DARK_GRAY);
        MutableComponent suffix = Component.literal("> ").withStyle(ChatFormatting.DARK_GRAY);
        Component nameComponent;
        if (RusherHackAPI.getRelationManager().isEnemy(name)) {
            nameComponent = Component.literal(name).withColor(enemiesColor.getValueRGB());
        } else if (RusherHackAPI.getRelationManager().isFriend(name)) {
            nameComponent =  Component.literal(name).withColor(friendsColor.getValueRGB());
        } else {
            nameComponent =  Component.literal(name).withStyle(ChatFormatting.GRAY);
        }

        return prefix.append(nameComponent).append(suffix);
    }
}
