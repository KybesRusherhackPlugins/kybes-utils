package de.kybe.KybesUtils.mixins;

import de.kybe.KybesUtils.modules.FogParametersModule;
import net.minecraft.client.renderer.FogParameters;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;


@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @ModifyVariable(method = "addMainPass", at = @At("HEAD"), argsOnly = true)
    private FogParameters addMainPass$Head(FogParameters original) {
        if (FogParametersModule.INSTANCE == null || !FogParametersModule.INSTANCE.isToggled()) return original;
        FogParametersModule module = FogParametersModule.INSTANCE;
        return new FogParameters(
                module.start.getValue(),
                module.end.getValue(),
                module.fogShape.getValue(),
                module.color.getRed(),
                module.color.getGreen(),
                module.color.getBlue(),
                module.color.getAlpha()
        );
    }
}
