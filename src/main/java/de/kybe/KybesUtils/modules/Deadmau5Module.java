package de.kybe.KybesUtils.modules;

import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.client.api.feature.module.ToggleableModule;
import org.rusherhack.core.setting.BooleanSetting;
import org.rusherhack.core.setting.StringSetting;

public class Deadmau5Module extends ToggleableModule {
    public static Deadmau5Module INSTANCE;

    public final BooleanSetting allPlayers = new BooleanSetting("AllPlayers", true);
    public final StringSetting players = new StringSetting("Players", "2kybe3");

    public Deadmau5Module() {
        super("Deadmau5", "Allows you or others to have the Deadmau5 ears", ModuleCategory.MISC);

        this.registerSettings(allPlayers, players);

        INSTANCE = this;
    }
}
