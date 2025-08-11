package de.kybe.KybesUtils.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import de.kybe.KybesUtils.modules.ChamsV2;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL11C.GL_POLYGON_OFFSET_FILL;
import static org.lwjgl.opengl.GL11C.glDisable;
import static org.lwjgl.opengl.GL11C.glPolygonOffset;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin<T extends Entity, S extends EntityRenderState> {

    @Inject(
            method = "shouldRender",
            at = @At("HEAD"),
            cancellable = true
    )
    private void shouldRender$Head(T livingEntity, Frustum camera, double camX, double camY, double camZ, CallbackInfoReturnable<Boolean> cir) {
        if (ChamsV2.INSTANCE != null && ChamsV2.INSTANCE.isToggled()) {
            cir.setReturnValue(true);
        }
    }

    /*
    @Inject(method = "render", at = @At("HEAD"))
    private void render$Head(S entityRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci) {
        if (ChamsV2.INSTANCE == null || !ChamsV2.INSTANCE.isToggled()) return;

        glEnable(GL_POLYGON_OFFSET_FILL);
        glPolygonOffset(1.0f, -Float.MAX_VALUE);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void render$Return(S entityRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci) {
        if (ChamsV2.INSTANCE == null || !ChamsV2.INSTANCE.isToggled()) return;

        glPolygonOffset(1.0f, Float.MAX_VALUE);
        glDisable(GL_POLYGON_OFFSET_FILL);
    }
     */
}
