package de.kybe.KybesUtils.modules;

import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.client.api.feature.module.ToggleableModule;
import org.rusherhack.core.setting.BooleanSetting;
import org.rusherhack.core.setting.NumberSetting;
import org.rusherhack.core.setting.StringSetting;

public class BabyElytraModule extends ToggleableModule {
    public static BabyElytraModule INSTANCE;

    public final BooleanSetting allPlayers = new BooleanSetting("AllPlayers", true);
    public final StringSetting players = new StringSetting("players", "2kybe3");

    public BabyElytraModule() {
        super("BabyElytra", ModuleCategory.MISC);

        this.registerSettings(allPlayers, players);

        INSTANCE = this;
    }
}
