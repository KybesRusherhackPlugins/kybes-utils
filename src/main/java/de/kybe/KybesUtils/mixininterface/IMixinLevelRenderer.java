package de.kybe.KybesUtils.mixininterface;

import com.mojang.blaze3d.pipeline.RenderTarget;

public interface IMixinLevelRenderer {
    void kybe$pushEntityOutlineFramebuffer(RenderTarget renderTarget);

    void kybe$popEntityOutlineFramebuffer();

}
