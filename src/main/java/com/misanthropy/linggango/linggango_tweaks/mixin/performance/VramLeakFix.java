package com.misanthropy.linggango.linggango_tweaks.mixin.performance;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(RenderTarget.class)
public abstract class VramLeakFix {
    @Shadow public int colorTextureId;
    @Shadow public int depthBufferId;
    @Shadow public int frameBufferId;

    @Override
    public void finalize() throws Throwable {
        try {
            if (this.colorTextureId > -1 || this.depthBufferId > -1 || this.frameBufferId > -1) {
                final int color = this.colorTextureId;
                final int depth = this.depthBufferId;
                final int fbo = this.frameBufferId;
                RenderSystem.recordRenderCall(() -> {
                    GlStateManager._bindTexture(0);
                    GlStateManager._glBindFramebuffer(36160, 0);
                    if (color > -1) TextureUtil.releaseTextureId(color);
                    if (depth > -1) TextureUtil.releaseTextureId(depth);
                    if (fbo > -1) {
                        GlStateManager._glBindFramebuffer(36160, 0);
                        GlStateManager._glDeleteFramebuffers(fbo);
                    }
                });
            }
        } finally {
            super.finalize();
        }
    }
}