package de.kybe.KybesUtils.shaders;

import de.kybe.KybesUtils.mixininterface.IMixinLevelRenderer;

import static org.rusherhack.client.api.Globals.mc;

public class EntityShader extends PostProcessShader {
    @Override
    protected void preDraw() {
        ((IMixinLevelRenderer) mc.levelRenderer).kybe$pushEntityOutlineFramebuffer(renderTarget);
    }

    @Override
    protected void postDraw() {
        ((IMixinLevelRenderer) mc.levelRenderer).kybe$popEntityOutlineFramebuffer();
    }

    public void endRender() {
        endRender(() -> bufferSource.endOutlineBatch());
    }

    @Override
    protected void setUniforms() {

    }
}
