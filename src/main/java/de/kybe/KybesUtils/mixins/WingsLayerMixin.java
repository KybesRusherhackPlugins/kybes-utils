package de.kybe.KybesUtils.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.kybe.KybesUtils.modules.BabyElytraModule;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WingsLayer.class)
public abstract class WingsLayerMixin<S extends HumanoidRenderState, M> {
    @WrapOperation(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;isBaby:Z", opcode = Opcodes.GETFIELD)
    )
    private boolean modifyElytraModel(HumanoidRenderState instance, Operation<Boolean> original) {
        if (BabyElytraModule.INSTANCE != null && BabyElytraModule.INSTANCE.isToggled()) {
            if (instance instanceof PlayerRenderState ps) {
                String playerName = ps.name;

                if (BabyElytraModule.INSTANCE.allPlayers.getValue()) {
                    return true;
                } else {
                    String[] players = BabyElytraModule.INSTANCE.players.getValue().split(",");
                    for (String allowedName : players) {
                        if (allowedName.trim().equalsIgnoreCase(playerName)) {
                            return true;
                        }
                    }
                }
            }
        }

        return original.call(instance);
    }
}