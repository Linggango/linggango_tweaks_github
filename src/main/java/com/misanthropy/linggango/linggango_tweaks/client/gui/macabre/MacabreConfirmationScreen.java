package com.misanthropy.linggango.linggango_tweaks.client.gui.macabre;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.List;

@SuppressWarnings("unused")
@OnlyIn(Dist.CLIENT)
public class MacabreConfirmationScreen extends Screen {

    private static final int BACKGROUND_DARK = 0x0F0505;
    private static final int PANEL_DARK = 0x1A0909;
    private static final int REDDISH_ACCENT = 0xA33838;
    private static final int ACCENT_DIM_CRIMSON = 0x591B1B;
    private static final int TEXT_LIGHT = 0xEEDDDD;
    private static final int TEXT_DIMMED = 0x996666;
    private static final int TEXT_WARNING = 0xC22E2E;

    private static final SoundEvent DENY_SOUND = SoundEvent.createVariableRangeEvent(new ResourceLocation("linggango_tweaks", "macabre_denial"));
    private static final SoundEvent CONFIRM_SOUND = SoundEvent.createVariableRangeEvent(new ResourceLocation("linggango_tweaks", "macabre_confirmation"));

    private final Runnable onConfirm;

    private float backgroundFade = 0f;
    private float panelScale = 0f;
    private boolean isClosing = false;
    private boolean isConfirmed = false;
    private float delayTimer = 3.0f;

    private long lastFrameTime;

    private final int panelWidth = 280;
    private final int panelHeight = 190;
    private int panelX, panelY;

    private StyledButton confirmBtn;
    private StyledButton cancelBtn;

    private List<FormattedCharSequence> warningLines;
    private List<FormattedCharSequence> infoLines;

    public MacabreConfirmationScreen(Runnable onConfirm) {
        super(Component.literal("The Pit Confirmation"));
        this.onConfirm = onConfirm;
        this.lastFrameTime = Util.getMillis();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    protected void init() {
        panelX = (this.width - panelWidth) / 2;
        panelY = (this.height - panelHeight) / 2;

        int btnWidth = 110;
        int btnHeight = 24;
        int btnY = panelY + panelHeight - btnHeight - 15;

        cancelBtn = new StyledButton(panelX + 20, btnY, btnWidth, btnHeight, "Cancel", false, b -> {
            if (!isClosing && !isConfirmed) {
                playDenySound();
                closeScreen();
            }
        });

        confirmBtn = new StyledButton(panelX + panelWidth - btnWidth - 20, btnY, btnWidth, btnHeight, "Enter The Pit", true, b -> {
        });

        this.addRenderableWidget(cancelBtn);
        this.addRenderableWidget(confirmBtn);

        Component warningLine = Component.literal("Entering The Pit will take everything you have. Remember, you are in THEIR dimension.");
        warningLines = font.split(warningLine, panelWidth - 40);

        Component infoLine = Component.literal("When you escape, your original gear will be restored, and everything earned inside will be packed into a Loot Cache.");
        infoLines = font.split(infoLine, panelWidth - 40);

        try {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(DENY_SOUND, 1.0F, 0.01F));
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(CONFIRM_SOUND, 1.0F, 0.01F));
        } catch (Exception ignored) {}
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (!isConfirmed && !isClosing) {
                playDenySound();
                closeScreen();
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void playDenySound() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(DENY_SOUND, 1.0F));
    }

    public void closeScreen() {
        isClosing = true;
    }

    private void updateAnimations(float dt) {
        float fadeSpeed = 0.08f * dt;

        if (!isClosing && !isConfirmed) {
            backgroundFade = Math.min(1f, backgroundFade + fadeSpeed);
            panelScale = lerp(panelScale, 1f, 1f - (float) Math.pow(0.75f, dt));

            boolean holding = confirmBtn.isHoveredOrFocused()
                    && GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;

            if (holding) {
                confirmBtn.holdProgress = Math.min(1f, confirmBtn.holdProgress + 0.018f * dt);
                if (confirmBtn.holdProgress >= 1f) {
                    isConfirmed = true;
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(CONFIRM_SOUND, 1.0F));
                }
            } else {
                confirmBtn.holdProgress = Math.max(0f, confirmBtn.holdProgress - 0.05f * dt);
            }
        } else if (isConfirmed) {
            delayTimer -= (dt / 60.0f) * 1.05f;
            if (delayTimer <= 0f) {
                if (onConfirm != null) {
                    onConfirm.run();
                }
                assert this.minecraft != null;
                this.minecraft.setScreen(null);
            }
        } else {
            backgroundFade = Math.max(0f, backgroundFade - fadeSpeed * 1.5f);
            panelScale = lerp(panelScale, 0.85f, 1f - (float) Math.pow(0.7f, dt));

            if (backgroundFade <= 0f) {
                assert this.minecraft != null;
                this.minecraft.setScreen(null);
            }
        }
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        long now = Util.getMillis();
        float dt = Math.min((now - lastFrameTime) / 16.666f, 10f);
        lastFrameTime = now;

        updateAnimations(dt);

        g.pose().pushPose();

        renderVignette(g);

        if (panelScale > 0.01f) {
            float cx = panelX + panelWidth / 2f;
            float cy = panelY + panelHeight / 2f;

            g.pose().translate(cx, cy, 0);
            g.pose().scale(panelScale, panelScale, 1f);
            g.pose().translate(-cx, -cy, 0);

            g.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, argb(backgroundFade * 0.95f, PANEL_DARK));

            g.fill(panelX, panelY, panelX + panelWidth, panelY + 1, argb(backgroundFade, REDDISH_ACCENT));
            g.fill(panelX, panelY + panelHeight - 1, panelX + panelWidth, panelY + panelHeight, argb(backgroundFade, REDDISH_ACCENT));

            drawBracket(g, panelX, panelY, false, false, backgroundFade);
            drawBracket(g, panelX + panelWidth, panelY, true, false, backgroundFade);
            drawBracket(g, panelX, panelY + panelHeight, false, true, backgroundFade);
            drawBracket(g, panelX + panelWidth, panelY + panelHeight, true, true, backgroundFade);

            int ty = panelY + 20;
            g.drawCenteredString(font, Component.literal("Be Aware!").withStyle(ChatFormatting.BOLD), panelX + panelWidth / 2, ty, argb(backgroundFade, REDDISH_ACCENT));

            ty += 14;
            g.fill(panelX + 40, ty, panelX + panelWidth - 40, ty + 1, argb(backgroundFade * 0.45f, ACCENT_DIM_CRIMSON));

            ty += 12;
            if (warningLines != null) {
                for (FormattedCharSequence line : warningLines) {
                    g.drawCenteredString(font, line, panelX + panelWidth / 2, ty, argb(backgroundFade, TEXT_WARNING));
                    ty += 11;
                }
            }

            ty += 8;
            if (infoLines != null) {
                for (FormattedCharSequence line : infoLines) {
                    g.drawCenteredString(font, line, panelX + panelWidth / 2, ty, argb(backgroundFade, TEXT_DIMMED));
                    ty += 11;
                }
            }

            if (isConfirmed) {
                String status;
                if (delayTimer > 2.0f) status = "3...";
                else if (delayTimer > 1.0f) status = "2...";
                else status = "1...";

                g.drawCenteredString(font, Component.literal(status).withStyle(ChatFormatting.BOLD), panelX + panelWidth / 2, panelY + panelHeight - 38, argb(backgroundFade, REDDISH_ACCENT));
            } else {
                cancelBtn.globalAlpha = backgroundFade;
                confirmBtn.globalAlpha = backgroundFade;

                cancelBtn.updateAnimation(dt, cancelBtn.isHoveredOrFocused());
                confirmBtn.updateAnimation(dt, confirmBtn.isHoveredOrFocused());

                cancelBtn.render(g, mx, my, pt);
                confirmBtn.render(g, mx, my, pt);
            }
        }

        g.pose().popPose();
    }

    private void renderVignette(@NotNull GuiGraphics g) {
        RenderSystem.enableBlend();
        g.fill(0, 0, this.width, this.height, argb(backgroundFade * 0.8f, BACKGROUND_DARK));
        for (int x = 0; x < 40; x++) {
            float t = (float) Math.pow(1f - (float) x / 40, 2);
            g.fill(x, 0, x + 1, this.height, argb(backgroundFade * t * 0.6f, 0));
            g.fill(this.width - x - 1, 0, this.width - x, this.height, argb(backgroundFade * t * 0.6f, 0));
        }
        RenderSystem.disableBlend();
    }

    private void drawBracket(@NotNull GuiGraphics g, int x, int y, boolean flipX, boolean flipY, float a) {
        int size = 6, dx = flipX ? -1 : 1, dy = flipY ? -1 : 1, c = argb(a, REDDISH_ACCENT);
        g.fill(x, y, x + size * dx, y + dy, c);
        g.fill(x, y, x + dx, y + size * dy, c);
    }

    private static float lerp(float a, float b, float t) { return a + (b - a) * t; }
    private static int argb(float alpha, int rgb) { return ((int) (Math.max(0f, Math.min(1f, alpha)) * 255) << 24) | (rgb & 0xFFFFFF); }

    private static class StyledButton extends Button {
        private final boolean isConfirmType;
        float hoverP = 0f;
        float holdProgress = 0f;
        float globalAlpha = 1f;

        StyledButton(int x, int y, int w, int h, String label, boolean isConfirmType, @NotNull OnPress press) {
            super(x, y, w, h, Component.literal(label).withStyle(ChatFormatting.BOLD), press, DEFAULT_NARRATION);
            this.isConfirmType = isConfirmType;
        }

        public void updateAnimation(float dt, boolean hovered) {
            hoverP = lerp(hoverP, hovered ? 1f : 0f, 1.0f - (float) Math.pow(0.78f, dt));
        }

        @Override
        public void renderWidget(@NotNull GuiGraphics g, int mx, int my, float pt) {
            if (!visible || globalAlpha < 0.05f) return;

            float scale = 1f + hoverP * 0.03f;
            float cx = getX() + width / 2f, cy = getY() + height / 2f;

            g.pose().pushPose();
            g.pose().translate(cx, cy, 0);
            g.pose().scale(scale, scale, 1f);
            g.pose().translate(-cx, -cy, 0);
            g.fill(getX(), getY(), getX() + width, getY() + height, argb(globalAlpha * (0.6f + 0.2f * hoverP), BACKGROUND_DARK));

            if (isConfirmType && holdProgress > 0f) {
                g.fill(getX(), getY(), getX() + (int) (width * holdProgress), getY() + height, argb(globalAlpha * 0.7f, REDDISH_ACCENT));
            }
            if (hoverP > 0.05f) {
                g.fill(getX() - 1, getY() - 1, getX() + width + 1, getY() + height + 1, argb(globalAlpha * hoverP * 0.4f, REDDISH_ACCENT));
            }
            int barW = 3 + (int) (4f * hoverP);
            int barColor = isConfirmType ? REDDISH_ACCENT : TEXT_DIMMED;
            g.fill(getX(), getY(), getX() + barW, getY() + height, argb(globalAlpha, barColor));
            int textColor = isConfirmType ? REDDISH_ACCENT : TEXT_LIGHT;
            if (holdProgress > 0.85f) textColor = 0xFFFFFF;

            g.drawCenteredString(Minecraft.getInstance().font, getMessage(), getX() + width / 2, getY() + (height - 8) / 2, argb(globalAlpha, textColor));

            g.pose().popPose();
        }
    }
}