package de.kybe.KybesUtils.modules;

import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.client.api.feature.module.ToggleableModule;

public class DontLimitMyFuckingFpsModule extends ToggleableModule {
    private static DontLimitMyFuckingFpsModule INSTANCE;

    public DontLimitMyFuckingFpsModule() {
        super("DontLimitMyFuckingFps", "Disables the vanilla AFK fps limit", ModuleCategory.MISC);

        INSTANCE = this;
    }

    public static DontLimitMyFuckingFpsModule getInstance() {
        return INSTANCE;
    }
}
