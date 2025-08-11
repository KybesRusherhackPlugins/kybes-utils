package de.kybe.KybesUtils.modules;

import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.client.api.feature.module.ToggleableModule;

public class DontLimitMyFuckingFpsModule extends ToggleableModule {
    private static DontLimitMyFuckingFpsModule INSTANCE;
    public static DontLimitMyFuckingFpsModule getInstance() {
        return INSTANCE;
    }

    public DontLimitMyFuckingFpsModule() {
        super("DontLimitMyFuckingFps", ModuleCategory.MISC);

        INSTANCE = this;
    }
}
