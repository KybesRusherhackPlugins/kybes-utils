package de.kybe.KybesUtils.mixins;

import com.mojang.blaze3d.platform.FramerateLimitTracker;
import de.kybe.KybesUtils.modules.DontLimitMyFuckingFpsModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FramerateLimitTracker.class)
public abstract class FramerateLimitTrackerMixin {
    @Shadow private int framerateLimit;

    @Inject(method = "getFramerateLimit", at = @At("HEAD"), cancellable = true)
    private void getFramerateLimit(CallbackInfoReturnable<Integer> cir) {
        if (DontLimitMyFuckingFpsModule.getInstance() != null && DontLimitMyFuckingFpsModule.getInstance().isToggled()) {
            cir.setReturnValue(this.framerateLimit);
        }
    }
}
