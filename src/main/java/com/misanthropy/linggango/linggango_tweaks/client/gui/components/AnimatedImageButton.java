package com.misanthropy.linggango.linggango_tweaks.client.gui.components;

import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AnimatedImageButton extends Button {
    private final ResourceLocation texture;
    private final int frameWidth;
    private final int frameHeight;
    private final int totalFrames;
    private final long frameDelayMs;
    private final long startTime;

    public AnimatedImageButton(int x, int y, int width, int height,
                               ResourceLocation texture, int frameWidth, int frameHeight,
                               int totalFrames, long frameDelayMs, OnPress onPress) {
        super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
        this.texture = texture;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.totalFrames = totalFrames;
        this.frameDelayMs = frameDelayMs;
        this.startTime = Util.getMillis();
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        long elapsed = Util.getMillis() - startTime;
        int currentFrame = (int) ((elapsed / frameDelayMs) % totalFrames);
        int vOffset = currentFrame * frameHeight;
        guiGraphics.blit(
                this.texture,
                this.getX(), this.getY(),
                this.width, this.height,
                0, vOffset,
                frameWidth, frameHeight,
                frameWidth, frameHeight * totalFrames
        );
    }
}