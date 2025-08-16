package de.kybe.KybesUtils.modules;

import com.mojang.blaze3d.shaders.FogShape;
import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.client.api.feature.module.ToggleableModule;
import org.rusherhack.client.api.setting.ColorSetting;
import org.rusherhack.core.setting.EnumSetting;
import org.rusherhack.core.setting.NumberSetting;

import java.awt.*;

public class FogParametersModule extends ToggleableModule {
    public static FogParametersModule INSTANCE;

    public final NumberSetting<Float> start = new NumberSetting<>("Start", 1000f, 0f, 1000f);
    public final NumberSetting<Float> end = new NumberSetting<>("End", 0f, 0f, 1000f);
    public final EnumSetting<FogShape> fogShape = new EnumSetting<>("FogShape", FogShape.SPHERE);
    public final ColorSetting color = new ColorSetting("Color", new Color(0, 0, 0, 0));


    public FogParametersModule() {
        super("FogParameters", ModuleCategory.CHAT);

        this.registerSettings(start, end, fogShape, color);

        INSTANCE = this;
    }
}
