package de.kybe.KybesUtils.shaders;

import net.minecraft.client.renderer.MultiBufferSource;

import static org.rusherhack.client.api.Globals.mc;

public class PostProcessShaders {
    public static EntityShader CHAMS = new EntityShader();

    public static boolean rendering = false;

    public static void beginRender() {
        CHAMS.beginRender();
    }

    public static void endRender() {
        CHAMS.endRender();
    }

    public static void onResized(int width, int height) {
        if (mc == null) return;

        CHAMS.onResized(width, height);
    }

    public static boolean isCustom(MultiBufferSource vcp) {
        return vcp == CHAMS.bufferSource;
    }
}
