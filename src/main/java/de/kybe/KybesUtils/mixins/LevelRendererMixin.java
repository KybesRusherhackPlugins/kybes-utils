package de.kybe.KybesUtils.mixins;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.vertex.PoseStack;
import de.kybe.KybesUtils.mixininterface.IMixinLevelRenderer;
import de.kybe.KybesUtils.modules.FogParametersModule;
import de.kybe.KybesUtils.shaders.EntityShader;
import de.kybe.KybesUtils.shaders.PostProcessShaders;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.renderer.FogParameters;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


import java.awt.*;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin implements IMixinLevelRenderer {
    // FogParametersModule
    @ModifyVariable(method = "addMainPass", at = @At("HEAD"), argsOnly = true)
    private FogParameters modifyFogParams(FogParameters original) {
        if (FogParametersModule.INSTANCE != null && FogParametersModule.INSTANCE.isToggled()) {
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
        return original;
    }

    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void onRenderLevel(CallbackInfo ci) {
        PostProcessShaders.beginRender();
    }

    @Inject(method = "renderEntity", at = @At("HEAD"))
    private void renderEntity$head(Entity entity, double camX, double camY, double camZ, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, CallbackInfo ci) {
        draw(entity, camX, camY, camZ, partialTick, bufferSource, poseStack, PostProcessShaders.CHAMS, Color.WHITE);
    }

    @Unique
    private void draw(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, MultiBufferSource multiBufferSource, PoseStack poseStack, EntityShader shader, Color color) {
        if (PostProcessShaders.isCustom(multiBufferSource)) return;
        kybe$pushEntityOutlineFramebuffer(shader.renderTarget);
        PostProcessShaders.rendering = true;

        shader.bufferSource.setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        renderEntity(entity, cameraX, cameraY, cameraZ, tickDelta, poseStack, shader.bufferSource);

        PostProcessShaders.rendering = false;
        kybe$popEntityOutlineFramebuffer();
    }

    @Inject(method = "method_62214", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/OutlineBufferSource;endOutlineBatch()V"))
    private void onRender(CallbackInfo ci) {

        PostProcessShaders.endRender();
    }

    @Inject(method = "resize", at = @At("HEAD"))
    private void onResized(int width, int height, CallbackInfo info) {
        PostProcessShaders.onResized(width, height);
    }


    @Shadow
    private RenderTarget entityOutlineTarget;

    @Shadow
    @Final
    private LevelTargetBundle targets;


    @Shadow protected abstract void renderEntity(Entity entity, double camX, double camY, double camZ, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource);

    @Unique
    private Stack<RenderTarget> targetHandleStack;

    @Unique
    private Stack<ResourceHandle<RenderTarget>> targetStack;


    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(CallbackInfo info) {
        targetHandleStack = new ObjectArrayList<>();
        targetStack = new ObjectArrayList<>();
    }

    @Override
    public void kybe$pushEntityOutlineFramebuffer(RenderTarget renderTarget) {
        targetHandleStack.push(this.entityOutlineTarget);
        this.entityOutlineTarget = renderTarget;

        targetStack.push(this.targets.entityOutline);
        this.targets.entityOutline = () -> renderTarget;
    }

    @Override
    public void kybe$popEntityOutlineFramebuffer() {
        this.entityOutlineTarget = targetHandleStack.pop();
        this.targets.entityOutline = targetStack.pop();
    }
}
