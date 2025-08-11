package de.kybe.KybesUtils.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.kybe.KybesUtils.modules.ChamsV2;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;

import static org.lwjgl.opengl.GL11C.*;
import static org.rusherhack.client.api.Globals.mc;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<S extends LivingEntityRenderState, M extends EntityModel<? super S>> {
    @Shadow
    public abstract ResourceLocation getTextureLocation(S livingEntityRenderState);

    @Redirect(
            method = "render(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V"
            )
    )
    private void render(
            M model,
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            int light,
            int overlay,
            int packedColor,
            S livingEntityRenderState,
            PoseStack poseStack2,
            MultiBufferSource multiBufferSource,
            int i2
    ) {
        if (ChamsV2.INSTANCE != null && ChamsV2.INSTANCE.isToggled()) {
            boolean isPlayerRenderer = (LivingEntityRenderer) (Object) this instanceof PlayerRenderer;
            boolean isPlayerState = livingEntityRenderState instanceof PlayerRenderState;

            if (isPlayerRenderer && isPlayerState) {
                PlayerRenderState playerState = (PlayerRenderState) livingEntityRenderState;

                if (mc.player != null && playerState.id == mc.player.getId()) {
                    if (!ChamsV2.INSTANCE.players.getValue()) {
                        model.renderToBuffer(poseStack, vertexConsumer, light, overlay, packedColor);
                        return;
                    }
                    Color color = ChamsV2.INSTANCE.selfPlayerColor.getValue();
                    int newPackedColor = ARGB.color(color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue());
                    model.renderToBuffer(poseStack, vertexConsumer, LightTexture.FULL_BRIGHT, overlay, newPackedColor);
                    return;
                } else {
                    if (!ChamsV2.INSTANCE.otherPlayers.getValue()) {
                        model.renderToBuffer(poseStack, vertexConsumer, light, overlay, packedColor);
                        return;
                    }
                    Color color = ChamsV2.INSTANCE.otherPlayerColor.getValue();
                    int newPackedColor = ARGB.color(color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue());
                    model.renderToBuffer(poseStack, vertexConsumer, LightTexture.FULL_BRIGHT, overlay, newPackedColor);
                    return;
                }
            } else {
                if (!ChamsV2.INSTANCE.mobs.getValue()) {
                    model.renderToBuffer(poseStack, vertexConsumer, light, overlay, packedColor);
                    return;
                }
                Color color = ChamsV2.INSTANCE.mobsColor.getValue();
                int newPackedColor = ARGB.color(color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue());
                model.renderToBuffer(poseStack, vertexConsumer, LightTexture.FULL_BRIGHT, overlay, newPackedColor);
                return;
            }
        }

        model.renderToBuffer(poseStack, vertexConsumer, light, overlay, packedColor);
    }

    @Inject(method = "render(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"))
    private void render$Head(S livingEntityRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci) {
        if (ChamsV2.INSTANCE == null || !ChamsV2.INSTANCE.isToggled()) return;

        //glEnable(GL_POLYGON_OFFSET_FILL);
        //glPolygonOffset(1.0f, -1100000.0f);
    }

    @Inject(method = "render(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("RETURN"))
    private void render$Return(S livingEntityRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci) {
        if (ChamsV2.INSTANCE == null || !ChamsV2.INSTANCE.isToggled()) return;

        //glPolygonOffset(1.0f, 1100000.0f);
        //glDisable(GL_POLYGON_OFFSET_FILL);
    }

    @Inject(method = "getRenderType", at = @At("HEAD"), cancellable = true)
    private void getRenderType$Head(S livingEntityRenderState, boolean bl, boolean bl2, boolean bl3, CallbackInfoReturnable<RenderType> cir) {
        if (ChamsV2.INSTANCE == null || !ChamsV2.INSTANCE.isToggled()) return;

        ResourceLocation texture = this.getTextureLocation(livingEntityRenderState);
        cir.setReturnValue(RenderType.outline(texture));
    }
}