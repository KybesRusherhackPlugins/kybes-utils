package de.kybe.KybesUtils.mixins;

import de.kybe.KybesUtils.modules.AmbientLightModule;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DimensionSpecialEffects.class)
public abstract class DimensionSpecialEffectsMixin {
    @Inject(method = "constantAmbientLight", at = @At("HEAD"), cancellable = true)
    private static void constantAmbientLight$Head(CallbackInfoReturnable<Boolean> cir) {
        if (AmbientLightModule.INSTANCE == null || !AmbientLightModule.INSTANCE.isToggled()) return;
        if (AmbientLightModule.INSTANCE.mode.getValue() == AmbientLightModule.Mode.NETHER) cir.setReturnValue(true);
        else  cir.setReturnValue(false);
    }
}
