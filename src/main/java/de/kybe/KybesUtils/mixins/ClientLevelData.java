package de.kybe.KybesUtils.mixins;

import de.kybe.KybesUtils.modules.IRLTimeModule;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.ClientLevelData.class)
public abstract class ClientLevelData {
    @Shadow
    private long dayTime;

    @Inject(method = "setDayTime", at = @At("HEAD"), cancellable = true)
    private void setDayTime$Head(long dayTime, CallbackInfo ci) {
        if (IRLTimeModule.INSTANCE == null || !IRLTimeModule.INSTANCE.isToggled()) return;
        this.dayTime = IRLTimeModule.getCurrentMinecraftTime();
        ci.cancel();
    }
}
