package de.kybe.KybesUtils.shaders;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.OutlineBufferSource;

import static com.mojang.blaze3d.platform.GlConst.GL_TEXTURE0;
import static org.rusherhack.client.api.Globals.mc;

public abstract class PostProcessShader {
    public OutlineBufferSource bufferSource = new OutlineBufferSource(mc.renderBuffers().bufferSource());
    public RenderTarget renderTarget = new TextureTarget(mc.getWindow().getWidth(), mc.getWindow().getHeight(), true);

    protected void preDraw() {}
    protected void postDraw() {}

    protected abstract void setUniforms();

    public void beginRender() {
        renderTarget.clear();
        mc.getMainRenderTarget().bindWrite(false);
    }

    public void endRender(Runnable draw) {
        preDraw();
        draw.run();
        postDraw();

        mc.getMainRenderTarget().bindWrite(false);

        GlStateManager._activeTexture(GL_TEXTURE0);
        GlStateManager._bindTexture(renderTarget.getColorTextureId());

        setUniforms();
    }

    public void onResized(int width, int height) {
        if (renderTarget == null) return;
        renderTarget.resize(width, height);
    }
}
