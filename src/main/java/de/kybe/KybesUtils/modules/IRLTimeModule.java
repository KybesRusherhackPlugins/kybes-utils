package de.kybe.KybesUtils.modules;

import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.client.api.feature.module.ToggleableModule;

import java.time.LocalTime;

public class IRLTimeModule extends ToggleableModule {
    public static IRLTimeModule INSTANCE;


    public IRLTimeModule() {
        super("IRL Time", ModuleCategory.MISC);

        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        if (mc.level == null) return;

        LocalTime now = LocalTime.now();
        int mcTimeTicks = convertToMinecraftTime(now);
        mc.level.setTimeFromServer(mc.level.getGameTime(), mcTimeTicks, false);
    }

    public static int getCurrentMinecraftTime() {
        LocalTime now = LocalTime.now();
        return convertToMinecraftTime(now);
    }

    private static int convertToMinecraftTime(LocalTime time) {
        int h = time.getHour();
        int m = time.getMinute();

        int mcTicks = ((h * 1000) + (m * 1000 / 60) - 6000);

        if (mcTicks < 0) mcTicks += 24000;

        return mcTicks % 24000;
    }

}
