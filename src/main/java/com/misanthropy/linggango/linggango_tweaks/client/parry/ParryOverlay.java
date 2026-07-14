package com.misanthropy.linggango.linggango_tweaks.client.parry;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import com.misanthropy.linggango.linggango_tweaks.config.TweaksConfig;
import com.misanthropy.linggango.linggango_tweaks.skills.client.ClientSkillEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jspecify.annotations.NonNull;

@Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ParryOverlay {

    private static float prevAlpha          = 0.0f;
    private static float currentAlpha       = 0.0f;
    private static float prevActiveAlpha    = 0.0f;
    private static float currentActiveAlpha = 0.0f;
    private static float prevYOffset        = 0.0f;
    private static float currentYOffset     = 0.0f;
    private static float comboDisplayAlpha  = 0.0f;
    private static int   lastReportedCombo  = 0;
    private static float shakeTime          = 0.0f;
    private static float smoothShakeX       = 0.0f;
    private static float smoothShakeY       = 0.0f;
    private static int   lingerTicks        = 0;

    private static final ItemStack ICON_SHIELD = new ItemStack(Items.SHIELD);
    private static final ItemStack ICON_SWORD  = new ItemStack(Items.IRON_SWORD);
    private static final ItemStack ICON_APPLE  = new ItemStack(Items.GOLDEN_APPLE);

    private static final int COLOR_BG      = 0x1C0808;
    private static final int COLOR_BORDER  = 0x5C1614;
    private static final int COLOR_TEXT    = 0xE8D8C0;
    private static final int COLOR_ACTIVE  = 0xC87828;
    private static final int COLOR_SUCCESS = 0x44DD66;
    private static final int COLOR_CD_BAR  = 0xC07030;

    private static final int   PAD       = 5;
    private static final int   ICON_SIZE = 16;
    private static final int   BOX_H     = 26;
    private static final float PARRY_Y_OFFSET = BOX_H + 8f;

    public static void register(IEventBus modBus) {
        modBus.register(new ParryOverlay());
    }

    private static int argb(int alpha, int rgb) {
        return ((alpha & 0xFF) << 24) | (rgb & 0x00FFFFFF);
    }

    private static void fillRounded(GuiGraphics g, int x, int y, int w, int h, int color) {
        if (w <= 2 || h <= 2) { g.fill(x, y, x + w, y + h, color); return; }
        g.fill(x + 1, y,         x + w - 1, y + 1,         color);
        g.fill(x,     y + 1,     x + w,     y + h - 1,     color);
        g.fill(x + 1, y + h - 1, x + w - 1, y + h,         color);
    }

    private static float lerp(float pct, float start, float end) {
        return start + pct * (end - start);
    }

    @SubscribeEvent
    public void registerOverlays(@NonNull RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("parry_hud", PARRY_HUD);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.isPaused() || mc.player == null) return;

        ParryEffects.ParryState state = ParryEffects.getStateManager().getCurrentState();
        int cooldown                  = ParryEffects.getStateManager().getCooldownTicks();
        int comboStage                = ParryEffects.getStateManager().getCurrentComboStage();

        boolean inCombat = ClientSkillEvents.combatTimer > 0;
        String  classId  = ClientSkillEvents.currentClassId;
        boolean hasSkill = classId != null && !classId.isEmpty() && !classId.equals("none");

        boolean isActive = state != ParryEffects.ParryState.IDLE || cooldown > 0 || comboStage > 0;
        if (isActive) {
            lingerTicks = 80;
        } else if (lingerTicks > 0) {
            lingerTicks--;
        }

        float targetAlpha = (inCombat || isActive || lingerTicks > 0) ? 1.0f : 0.0f;

        boolean isActiveOrSuccess = state == ParryEffects.ParryState.STARTUP
                || state == ParryEffects.ParryState.ACTIVE
                || state == ParryEffects.ParryState.SUCCESS
                || state == ParryEffects.ParryState.RECOVERY;
        float targetActiveAlpha = isActiveOrSuccess ? 1.0f : 0.0f;

        float targetYOffset = hasSkill ? PARRY_Y_OFFSET : 0.0f;

        prevAlpha = currentAlpha;
        prevActiveAlpha = currentActiveAlpha;
        prevYOffset = currentYOffset;

        currentAlpha       += (targetAlpha       - currentAlpha)       * 0.15f;
        currentActiveAlpha += (targetActiveAlpha - currentActiveAlpha) * 0.3f;
        currentYOffset     += (targetYOffset     - currentYOffset)     * 0.3f;

        if (comboStage > 0) {
            lastReportedCombo = comboStage;
            comboDisplayAlpha = 1.0f;
        } else {
            comboDisplayAlpha = Math.max(0.0f, comboDisplayAlpha - 0.02f);
        }

        if (state == ParryEffects.ParryState.SUCCESS) {
            shakeTime = 1.0f;
        } else {
            shakeTime = Math.max(0.0f, shakeTime - 0.1f);
        }

        if (shakeTime > 0.05f && mc.level != null) {
            float targetShakeX = (mc.level.random.nextFloat() - 0.5f) * 3.5f * shakeTime;
            float targetShakeY = (mc.level.random.nextFloat() - 0.5f) * 3.5f * shakeTime;
            smoothShakeX += (targetShakeX - smoothShakeX) * 0.4f;
            smoothShakeY += (targetShakeY - smoothShakeY) * 0.4f;
        } else {
            smoothShakeX = 0.0f;
            smoothShakeY = 0.0f;
        }
    }

    public static final IGuiOverlay PARRY_HUD = (ForgeGui gui, GuiGraphics guiGraphics,
                                                 float partialTick, int screenWidth, int screenHeight) -> {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.player == null) return;

        int maxConfigCd = TweaksConfig.PARRY_COOLDOWN.get();

        ParryEffects.ParryState state = ParryEffects.getStateManager().getCurrentState();
        int cooldown                  = ParryEffects.getStateManager().getCooldownTicks();

        final String    intendedName;
        final ItemStack intendedIcon;

        if (state == ParryEffects.ParryState.STARTUP || state == ParryEffects.ParryState.ACTIVE) {
            intendedName = "Parrying...";
            intendedIcon = ICON_SWORD;
        } else if (state == ParryEffects.ParryState.SUCCESS || state == ParryEffects.ParryState.RECOVERY) {
            intendedName = "Deflected!";
            intendedIcon = ICON_APPLE;
        } else if (cooldown > 0) {
            intendedName = "Parry Cooldown";
            intendedIcon = ICON_SHIELD;
        } else {
            intendedName = "Parry";
            intendedIcon = ICON_SHIELD;
        }

        float alpha = lerp(partialTick, prevAlpha, currentAlpha);
        float activeAlpha = lerp(partialTick, prevActiveAlpha, currentActiveAlpha);
        float yOffset = lerp(partialTick, prevYOffset, currentYOffset);

        if (alpha < 0.05f && comboDisplayAlpha < 0.01f) return;

        int nameWidth = mc.font.width(intendedName);
        int boxW      = PAD + ICON_SIZE + PAD + nameWidth + PAD;
        int x         = 14;
        int y         = (screenHeight / 2 + 55) + (int) yOffset;

        int a    = (int)(alpha * 255)       & 0xFF;
        int aAct = (int)(activeAlpha * 255) & 0xFF;

        if (alpha > 0.05f) {
            int borderA = Math.min((int)(alpha * 210), 255);
            int bgA     = Math.min((int)(alpha * 200), 255);

            fillRounded(guiGraphics, x, y, boxW, BOX_H, argb(borderA, COLOR_BORDER));
            fillRounded(guiGraphics, x + 1, y + 1, boxW - 2, BOX_H - 2, argb(bgA, COLOR_BG));

            if (aAct > 4) {
                int glowRgb = (state == ParryEffects.ParryState.SUCCESS
                        || state == ParryEffects.ParryState.RECOVERY)
                        ? COLOR_SUCCESS : COLOR_ACTIVE;
                int glowA = (int)(aAct * alpha * 0.85f) & 0xFF;
                fillRounded(guiGraphics, x, y, boxW, BOX_H, argb(glowA, glowRgb));
                fillRounded(guiGraphics, x + 1, y + 1, boxW - 2, BOX_H - 2, argb(bgA, COLOR_BG));
            }

            int iconX = x + PAD - 1;
            int iconY = y + (BOX_H - ICON_SIZE) / 2;
            guiGraphics.renderItem(intendedIcon, iconX, iconY);

            if (cooldown > 0 && maxConfigCd > 0) {
                float cdFrac = (float) cooldown / maxConfigCd;
                int dimA = (int)(cdFrac * alpha * 155) & 0xFF;
                guiGraphics.fill(iconX, iconY, iconX + ICON_SIZE, iconY + ICON_SIZE,
                        argb(dimA, 0x000000));
            }

            int textX = iconX + ICON_SIZE + PAD - 1;
            int textY = y + (BOX_H - 8) / 2;
            guiGraphics.drawString(mc.font, intendedName, textX, textY, argb(a, COLOR_TEXT), false);

            if (cooldown > 0 && maxConfigCd > 0) {
                float cdFrac  = (float) cooldown / maxConfigCd;
                int   barX    = x + 2;
                int   barY    = y + BOX_H - 4;
                int   barW    = boxW - 4;
                int   filledW = Math.round(barW * cdFrac);

                guiGraphics.fill(barX, barY, barX + barW, barY + 2,
                        argb(Math.min(a, 80), 0x3A1010));
                guiGraphics.fill(barX, barY, barX + filledW, barY + 2,
                        argb((int)(alpha * 210) & 0xFF, COLOR_CD_BAR));
            }
        }

        if (lastReportedCombo > 1 && comboDisplayAlpha > 0.01f) {
            String comboText  = "x" + lastReportedCombo + " Combo!";
            int    comboRgb   = getComboColor(lastReportedCombo);
            int    comboA     = (int)(comboDisplayAlpha * 255) & 0xFF;
            int    comboTextW = mc.font.width(comboText);
            int    comboBoxW  = PAD + comboTextW + PAD;

            int cbX = x + boxW + 4;

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(smoothShakeX, smoothShakeY, 0);

            int comboBorderA = (int)(comboDisplayAlpha * 200) & 0xFF;
            int comboBgA     = (int)(comboDisplayAlpha * 195) & 0xFF;
            fillRounded(guiGraphics, cbX, y, comboBoxW, BOX_H, argb(comboBorderA, comboRgb));
            fillRounded(guiGraphics, cbX + 1, y + 1, comboBoxW - 2, BOX_H - 2, argb(comboBgA, COLOR_BG));

            int ctX = cbX + (comboBoxW - comboTextW) / 2;
            int ctY = y + (BOX_H - 8) / 2;
            guiGraphics.drawString(mc.font, comboText, ctX, ctY, argb(comboA, comboRgb), false);

            guiGraphics.pose().popPose();
        }
    };

    private static int getComboColor(int stage) {
        if (stage >= 8) return 0xDD4444;
        if (stage >= 5) return 0xCC8800;
        if (stage >= 3) return 0xCCCC44;
        return 0xB8B8B8;
    }
}