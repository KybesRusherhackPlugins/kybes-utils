package de.kybe.KybesUtils.mixins;

import de.kybe.KybesUtils.modules.Deadmau5Module;
import net.minecraft.client.renderer.entity.layers.Deadmau5EarsLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Deadmau5EarsLayer.class)
public abstract class Deadmau5EarsLayerMixin {
    @Redirect(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/PlayerRenderState;FF)V",
            at = @At(value = "INVOKE",
                    target = "Ljava/lang/String;equals(Ljava/lang/Object;)Z")
    )
    private boolean render$Invoke(String self, Object other) {
        if (!(other instanceof String name)) return false;

        Deadmau5Module module = Deadmau5Module.INSTANCE;

        if (module != null && module.isToggled()) {
            if (module.allPlayers.getValue()) {
                return true;
            }

            for (String allowed : module.players.getValue().split(",")) {
                if (allowed.trim().equalsIgnoreCase(name)) {
                    return true;
                }
            }
        }

        return self.equals(other);
    }
}
