package de.kybe.KybesUtils.modules;

import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.client.api.feature.module.ToggleableModule;
import org.rusherhack.core.setting.EnumSetting;

public class AmbientLightModule extends ToggleableModule {
    public static AmbientLightModule INSTANCE;

    public enum Mode {
        NETHER,
        OVERWORLD
    }

    public EnumSetting<Mode> mode = new EnumSetting<>("Mode", "The mode of the ambient light effect", Mode.NETHER);

    public AmbientLightModule() {
        super("AmbientLight", ModuleCategory.RENDER);

        this.registerSettings(mode);

        INSTANCE = this;
    }
}
