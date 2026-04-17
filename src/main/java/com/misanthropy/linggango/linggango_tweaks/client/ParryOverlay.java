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

@Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ParryOverlay {

    private static float currentAlpha = 0.0f;
    private static float currentActiveAlpha = 0.0f;
    private static float currentYOffset = 0.0f;

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("parry_hud", PARRY_HUD);
    }

    public static final IGuiOverlay PARRY_HUD = (ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) -> {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.player == null) return;

        ParryEffects.ParryState state = ParryEffects.getStateManager().getCurrentState();
        int cooldown = ParryEffects.getStateManager().getCooldownTicks();

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

        float targetAlpha = (inCombat || state != ParryEffects.ParryState.IDLE || cooldown > 0) ? 1.0f : 0.0f;

        if (cooldown > 0 && state == ParryEffects.ParryState.COOLDOWN) {
            targetAlpha = Math.min(targetAlpha, 0.4f);
        }

        float targetActiveAlpha = (state == ParryEffects.ParryState.ACTIVE || state == ParryEffects.ParryState.SUCCESS) ? 1.0f : 0.0f;
        float targetYOffset = (inCombat && hasSkill) ? 28.0f : 0.0f;

        currentAlpha += (targetAlpha - currentAlpha) * 0.1f * (partialTick + 1);
        currentActiveAlpha += (targetActiveAlpha - currentActiveAlpha) * 0.2f * (partialTick + 1);
        currentYOffset += (targetYOffset - currentYOffset) * 0.2f * (partialTick + 1);

        if (currentAlpha < 0.05f) return;

        int alphaInt = (int) (currentAlpha * 255);
        int bgAlpha = Math.min((int) (currentAlpha * 180), 255) << 24;
        int textAlpha = alphaInt << 24;

        int x = 15;
        int y = (screenHeight / 2 + 60) + (int) currentYOffset;

        int boxWidth = 35 + mc.font.width(name);

        if (currentActiveAlpha > 0.01f) {
            int borderAlpha = (int) (currentActiveAlpha * currentAlpha * 255) << 24;
            int borderColor = state == ParryEffects.ParryState.SUCCESS ? 0x00FF00 : 0xDDAA00;
            guiGraphics.fill(x - 1, y - 1, x + boxWidth + 1, y + 23, borderAlpha | borderColor);
        }

        guiGraphics.fill(x, y, x + boxWidth, y + 22, bgAlpha | 0x000000);

        guiGraphics.renderItem(icon, x + 3, y + 3);
        guiGraphics.drawString(mc.font, name, x + 25, y + 7, textAlpha | 0xFFFFFF);

        if (cooldown > 0) {
            float cdP = (float) cooldown / TweaksConfig.PARRY_COOLDOWN.get();
            int cdHeight = (int) (16 * cdP);
            guiGraphics.fill(x + 3, y + 3 + (16 - cdHeight), x + 19, y + 19, 0x99000000);
        }
    };
}