package de.kybe.KybesUtils.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.kybe.KybesUtils.modules.ChamsV2;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.awt.*;

import static org.rusherhack.client.api.Globals.mc;

@Mixin(CapeLayer.class)
public abstract class CapeLayerMixin {
    @Redirect(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/PlayerRenderState;FF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/HumanoidModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V"
            )
    )
    private void redirectRenderToBuffer(
            HumanoidModel<PlayerRenderState> model,
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            int light,
            int overlay,
            PoseStack poseStackArg,
            MultiBufferSource bufferSource,
            int i,
            PlayerRenderState playerRenderState,
            float f,
            float g
    ) {
        if (ChamsV2.INSTANCE == null || !ChamsV2.INSTANCE.isToggled()) {
            // Default rendering if chams not enabled
            model.renderToBuffer(poseStack, vertexConsumer, light, overlay);
            return;
        }

        // If this is the local player
        if (mc.player != null && playerRenderState.id == mc.player.getId()) {
            if (!ChamsV2.INSTANCE.cape.getValue()) {
                model.renderToBuffer(poseStack, vertexConsumer, light, overlay);
                return;
            }
            Color color = ChamsV2.INSTANCE.selfCapeColor.getValue();
            int packedColor = ARGB.color(color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue());
            model.renderToBuffer(poseStack, vertexConsumer, LightTexture.FULL_BRIGHT, 0, packedColor);
        } else {
            // Other players
            if (!ChamsV2.INSTANCE.otherCapes.getValue()) {
                model.renderToBuffer(poseStack, vertexConsumer, light, overlay);
                return;
            }
            Color color = ChamsV2.INSTANCE.otherCapeColor.getValue();
            int packedColor = ARGB.color(color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue());
            model.renderToBuffer(poseStack, vertexConsumer, LightTexture.FULL_BRIGHT, 0, packedColor);
        }
    }

    @Redirect(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/PlayerRenderState;FF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/MultiBufferSource;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"
            )
    )
    private VertexConsumer redirectGetBuffer(
            MultiBufferSource instance,
            RenderType renderType,
            PoseStack poseStack,
            MultiBufferSource multiBufferSource,
            int i,
            PlayerRenderState playerRenderState,
            float f,
            float g
    ) {
        if (ChamsV2.INSTANCE == null || !ChamsV2.INSTANCE.isToggled()) {
            return instance.getBuffer(renderType);
        }

        PlayerSkin playerSkin = playerRenderState.skin;
        if (playerSkin.capeTexture() != null) {
            return instance.getBuffer(RenderType.entityTranslucent(playerSkin.capeTexture()));
        } else {
            return instance.getBuffer(renderType);
        }
    }
}