package com.misanthropy.linggango.linggango_tweaks.client;

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
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jspecify.annotations.NonNull;

@Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ParryOverlay {

    private static float currentAlpha = 0.0f;
    private static float currentActiveAlpha = 0.0f;
    private static float currentYOffset = 0.0f;

    private static float comboDisplayAlpha = 0.0f;
    private static int lastReportedCombo = 0;
    private static float shakeTime = 0.0f;

    @SubscribeEvent
    public static void registerOverlays(@NonNull RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("parry_hud", PARRY_HUD);
    }

    public static final IGuiOverlay PARRY_HUD = (ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) -> {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.player == null) return;

        ParryEffects.ParryState state = ParryEffects.getStateManager().getCurrentState();
        int cooldown = ParryEffects.getStateManager().getCooldownTicks();
        int comboStage = ParryEffects.getStateManager().getCurrentComboStage();

        String name = "Parry";
        ItemStack icon = new ItemStack(Items.SHIELD);

        if (state == ParryEffects.ParryState.STARTUP || state == ParryEffects.ParryState.ACTIVE) {
            name = "Parrying...";
            icon = new ItemStack(Items.IRON_SWORD);
        } else if (state == ParryEffects.ParryState.SUCCESS || state == ParryEffects.ParryState.RECOVERY) {
            name = "Deflected!";
            icon = new ItemStack(Items.GOLDEN_APPLE);
        } else if (cooldown > 0) {
            name = "Parry Cooldown";
            icon = new ItemStack(Items.SHIELD);
        }

        boolean inCombat = ClientSkillEvents.combatTimer > 0;
        String classId = ClientSkillEvents.currentClassId;
        boolean hasSkill = classId != null && !classId.isEmpty() && !classId.equals("none");

        float targetAlpha = (inCombat || state != ParryEffects.ParryState.IDLE || cooldown > 0 || comboStage > 0) ? 1.0f : 0.0f;

        if (cooldown > 0 && state == ParryEffects.ParryState.COOLDOWN) {
            targetAlpha = 0.4f;
        }

        float targetActiveAlpha = (state == ParryEffects.ParryState.ACTIVE || state == ParryEffects.ParryState.SUCCESS) ? 1.0f : 0.0f;
        float targetYOffset = (inCombat && hasSkill) ? 28.0f : 0.0f;

        currentAlpha += (targetAlpha - currentAlpha) * 0.1f * (partialTick + 1);
        currentActiveAlpha += (targetActiveAlpha - currentActiveAlpha) * 0.2f * (partialTick + 1);
        currentYOffset += (targetYOffset - currentYOffset) * 0.2f * (partialTick + 1);

        if (comboStage > 0) {
            lastReportedCombo = comboStage;
            comboDisplayAlpha = 1.0f;
        } else {

            comboDisplayAlpha = Math.max(0.0f, comboDisplayAlpha - 0.005f * (partialTick + 1));
        }

        if (state == ParryEffects.ParryState.SUCCESS) {
            shakeTime = 1.0f;
        } else {
            shakeTime = Math.max(0.0f, shakeTime - 0.05f * (partialTick + 1));
        }

        if (currentAlpha < 0.05f && comboDisplayAlpha < 0.01f) return;

        int alphaInt = (int) (currentAlpha * 255);
        int bgAlpha = Math.min((int) (currentAlpha * 180), 255) << 24;
        int textAlpha = alphaInt << 24;

        int x = 15;
        int y = (screenHeight / 2 + 60) + (int) currentYOffset;

        int boxWidth = 35 + mc.font.width(name);

        if (currentAlpha > 0.05f) {
            if (currentActiveAlpha > 0.01f) {
                int borderAlpha = (int) (currentActiveAlpha * currentAlpha * 255) << 24;
                int borderColor = state == ParryEffects.ParryState.SUCCESS ? 0x00FF00 : 0xDDAA00;
                guiGraphics.fill(x - 1, y - 1, x + boxWidth + 1, y + 23, borderAlpha | borderColor);
            }

            guiGraphics.fill(x, y, x + boxWidth, y + 22, bgAlpha);
            guiGraphics.renderItem(icon, x + 3, y + 3);
            guiGraphics.drawString(mc.font, name, x + 25, y + 7, textAlpha | 0xFFFFFF);

            if (cooldown > 0) {
                float cdP = (float) cooldown / TweaksConfig.PARRY_COOLDOWN.get();
                int cdHeight = (int) (16 * cdP);
                guiGraphics.fill(x + 3, y + 3 + (16 - cdHeight), x + 19, y + 19, 0x99000000);
            }
        }

        if (lastReportedCombo > 1 && comboDisplayAlpha > 0.01f) {
            String comboText = "x" + lastReportedCombo + " Combo!";
            int comboColor = getComboColor(lastReportedCombo);

            int alphaIntCombo = Math.max(0, Math.min(255, (int)(comboDisplayAlpha * 255)));
            int colorWithAlpha = (alphaIntCombo << 24) | (comboColor & 0xFFFFFF);

            float textShakeX = 0;
            float textShakeY = 0;
            if (shakeTime > 0 && mc.level != null) {
                textShakeX = (mc.level.random.nextFloat() - 0.5f) * 4.0f * shakeTime;
                textShakeY = (mc.level.random.nextFloat() - 0.5f) * 4.0f * shakeTime;
            }

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(textShakeX, textShakeY, 0);

            guiGraphics.drawString(mc.font, comboText, x + boxWidth + 8, y + 7, colorWithAlpha, true);
            guiGraphics.pose().popPose();
        }
    };

    private static int getComboColor(int stage) {
        if (stage >= 8) return 0xFF3333;

        if (stage >= 5) return 0xFFAA00;

        if (stage >= 3) return 0xFFFF55;

        return 0xAAAAAA;

    }
}