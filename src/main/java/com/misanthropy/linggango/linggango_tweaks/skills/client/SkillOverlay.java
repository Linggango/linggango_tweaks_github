package com.misanthropy.linggango.linggango_tweaks.skills.client;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SkillOverlay {

    private static float currentAlpha = 0.0f;
    private static float currentActiveAlpha = 0.0f;

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(ClientSkillEvents.SKILL_KEY);
    }

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("class_skill_hud", SKILL_HUD);
    }

    public static final IGuiOverlay SKILL_HUD = (ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) -> {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.player == null) return;

        String c = ClientSkillEvents.currentClassId;
        String name = "";
        ItemStack icon = ItemStack.EMPTY;

        if (c.equals("north_mage")) {
            name = "Mana Surge";
            icon = new ItemStack(Items.LAPIS_LAZULI);
        } else if (c.equals("machinist")) {
            name = "Quick Repair";
            icon = new ItemStack(Items.ANVIL);
        } else if (c.equals("south_mage")) {
            name = "Limit Break";
            icon = new ItemStack(Items.END_CRYSTAL);
        } else if (c.equals("miner")) {
            name = "Crawl";
            icon = new ItemStack(Items.LADDER);
        } else if (c.equals("vampire")) {
            name = "Night's Embrace";
            icon = new ItemStack(Items.REDSTONE);
        } else if (c.equals("gambler")) {
            if (ClientSkillEvents.isSkillActive) {
                int fakeRoll = (mc.player.tickCount / 4) % 6;
                switch (fakeRoll) {
                    case 0: name = "Rolling... (Repair)"; icon = new ItemStack(Items.ANVIL); break;
                    case 1: name = "Rolling... (Rage)"; icon = new ItemStack(Items.IRON_AXE); break;
                    case 2: name = "Rolling... (Defense)"; icon = new ItemStack(Items.IRON_CHESTPLATE); break;
                    case 3: name = "Rolling... (Invis)"; icon = new ItemStack(Items.POTION); break;
                    case 4: name = "Rolling... (Agility)"; icon = new ItemStack(Items.FEATHER); break;
                    case 5: name = "Rolling... (Sustain)"; icon = new ItemStack(Items.GHAST_TEAR); break;
                }
            } else {
                name = "Roll the Dice";
                icon = new ItemStack(Items.GOLD_NUGGET);
            }
        } else {
            currentAlpha = 0;
            currentActiveAlpha = 0;
            return;
        }

        float targetAlpha = (ClientSkillEvents.combatTimer > 0) ? 1.0f : 0.0f;

        if (ClientSkillEvents.cdRemaining > 0 && !ClientSkillEvents.isSkillActive) {
            targetAlpha = Math.min(targetAlpha, 0.4f);
        }

        float targetActiveAlpha = ClientSkillEvents.isSkillActive ? 1.0f : 0.0f;

        currentAlpha += (targetAlpha - currentAlpha) * 0.1f * (partialTick + 1);
        currentActiveAlpha += (targetActiveAlpha - currentActiveAlpha) * 0.2f * (partialTick + 1);

        if (currentAlpha < 0.05f) return;

        int alphaInt = (int) (currentAlpha * 255);
        int bgAlpha = Math.min((int) (currentAlpha * 180), 255) << 24;
        int textAlpha = alphaInt << 24;

        int x = 15;
        int y = screenHeight / 2 + 60;

        int boxWidth = 35 + mc.font.width(name);

        if (currentActiveAlpha > 0.01f) {
            int borderAlpha = (int) (currentActiveAlpha * currentAlpha * 255) << 24;
            guiGraphics.fill(x - 1, y - 1, x + boxWidth + 1, y + 23, borderAlpha | 0xDDAA00);
        }

        guiGraphics.fill(x, y, x + boxWidth, y + 22, bgAlpha | 0x000000);

        guiGraphics.renderItem(icon, x + 3, y + 3);
        guiGraphics.drawString(mc.font, name, x + 25, y + 7, textAlpha | 0xFFFFFF);

        if (ClientSkillEvents.cdRemaining > 0 && ClientSkillEvents.maxCd > 0) {
            float cdP = (float) ClientSkillEvents.cdRemaining / ClientSkillEvents.maxCd;
            int cdHeight = (int) (16 * cdP);
            guiGraphics.fill(x + 3, y + 3 + (16 - cdHeight), x + 19, y + 19, 0x99000000);
        }
    };
}