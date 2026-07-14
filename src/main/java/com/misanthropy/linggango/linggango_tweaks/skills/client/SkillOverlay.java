package com.misanthropy.linggango.linggango_tweaks.skills.client;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
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
public class SkillOverlay {

    private static float currentAlpha       = 0.0f;
    private static float currentActiveAlpha = 0.0f;
    private static float flashTimer         = 0.0f;
    private static int   lastRangerStacks   = 0;

    private static final ItemStack ICON_LAPIS       = new ItemStack(Items.LAPIS_LAZULI);
    private static final ItemStack ICON_ANVIL       = new ItemStack(Items.ANVIL);
    private static final ItemStack ICON_CRYSTAL     = new ItemStack(Items.END_CRYSTAL);
    private static final ItemStack ICON_REDSTONE    = new ItemStack(Items.REDSTONE);
    private static final ItemStack ICON_GOLD_NUGGET = new ItemStack(Items.GOLD_NUGGET);
    private static final ItemStack ICON_AXE         = new ItemStack(Items.IRON_AXE);
    private static final ItemStack ICON_CHESTPLATE  = new ItemStack(Items.IRON_CHESTPLATE);
    private static final ItemStack ICON_POTION      = new ItemStack(Items.POTION);
    private static final ItemStack ICON_FEATHER     = new ItemStack(Items.FEATHER);
    private static final ItemStack ICON_GHAST_TEAR  = new ItemStack(Items.GHAST_TEAR);
    private static final ItemStack ICON_ARROW       = new ItemStack(Items.ARROW);
    private static final ItemStack ICON_SHIELD      = new ItemStack(Items.SHIELD);
    private static final ItemStack ICON_AMETHYST    = new ItemStack(Items.AMETHYST_SHARD);

    private static final ResourceLocation TERRAMITY_GOLD_ROUND =
            new ResourceLocation("terramity", "gold_round");
    private static ItemStack cachedGunnerAmmo = null;

    private static String cachedName      = "";
    private static int    cachedNameWidth = 0;

    private static final int PAD       = 5;
    private static final int ICON_SIZE = 16;
    private static final int BOX_H     = 26;

    private static final int COLOR_BG     = 0x1C0808;
    private static final int COLOR_BORDER = 0x5C1614;
    private static final int COLOR_TEXT   = 0xE8D8C0;
    private static final int COLOR_ACTIVE = 0xC87828;

    private static int accentRgb(String classId) {
        return switch (classId) {
            case "north_mage"       -> 0x5599FF;
            case "south_mage"       -> 0xBB44FF;
            case "machinist"        -> 0xFF8833;
            case "miner"            -> 0xCCAA55;
            case "vampire"          -> 0xCC2244;
            case "ranger"           -> 0x44BB66;
            case "gunner"           -> 0xFFCC33;
            case "tank", "tanker"   -> 0x4488CC;
            case "gambler"          -> 0xFFAA22;
            case "berserker"        -> 0xDD2222;
            default                 -> 0xAAAAAA;
        };
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

    @SubscribeEvent
    public static void registerOverlays(@NonNull RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("class_skill_hud", SKILL_HUD);
    }

    public static final IGuiOverlay SKILL_HUD = (ForgeGui gui, GuiGraphics guiGraphics,
                                                 float partialTick, int screenWidth, int screenHeight) -> {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.player == null) return;

        String classId = ClientSkillEvents.currentClassId;

        if ("berserker".equals(classId) && ClientSkillEvents.isSkillActive) {
            int a = 30;
            float pulse = (float) Math.sin(mc.player.tickCount * 0.15f) * 8.0f;
            int tintAlpha = Math.max(15, Math.min(45, (int)(a + pulse)));
            guiGraphics.fill(0, 0, screenWidth, screenHeight, argb(tintAlpha, 0x990000));
            int borderW = 20;
            guiGraphics.fill(0, 0, screenWidth, borderW, argb(tintAlpha + 20, 0x550000));
            guiGraphics.fill(0, screenHeight - borderW, screenWidth, screenHeight, argb(tintAlpha + 20, 0x550000));
            guiGraphics.fill(0, borderW, borderW, screenHeight - borderW, argb(tintAlpha + 20, 0x550000));
            guiGraphics.fill(screenWidth - borderW, borderW, screenWidth, screenHeight - borderW, argb(tintAlpha + 20, 0x550000));
        }

        final String    name;
        final ItemStack icon;

        switch (classId) {
            case "berserker"      -> { name = "Controlled Rage";  icon = ICON_AXE;        }
            case "north_mage"     -> { name = "Mana Surge";       icon = ICON_LAPIS;      }
            case "machinist"      -> { name = "Quick Repair";     icon = ICON_ANVIL;      }
            case "south_mage"     -> { name = "Limit Break";      icon = ICON_CRYSTAL;    }
            case "miner"          -> { name = "Gem Glow";          icon = ICON_AMETHYST;   }
            case "vampire"        -> { name = "Blood Infusion";   icon = ICON_REDSTONE;   }
            case "ranger"         -> { name = "Focus";            icon = ICON_ARROW;      }
            case "gunner"         -> {
                name = "Explosive Shot";
                if (cachedGunnerAmmo == null) {
                    var ammoItem = net.minecraftforge.registries.ForgeRegistries.ITEMS
                            .getValue(TERRAMITY_GOLD_ROUND);
                    cachedGunnerAmmo = new ItemStack(ammoItem != null ? ammoItem : Items.GOLD_NUGGET);
                }
                icon = cachedGunnerAmmo;
            }
            case "tank", "tanker" -> { name = "Kinetic Absorber"; icon = ICON_SHIELD;     }
            case "gambler"        -> {
                if (ClientSkillEvents.isSkillActive) {
                    int roll = (mc.player.tickCount / 4) % 6;
                    switch (roll) {
                        case 0 -> { name = "Rolling... (Repair)";  icon = ICON_ANVIL;      }
                        case 1 -> { name = "Rolling... (Rage)";    icon = ICON_AXE;        }
                        case 2 -> { name = "Rolling... (Defense)"; icon = ICON_CHESTPLATE; }
                        case 3 -> { name = "Rolling... (Invis)";   icon = ICON_POTION;     }
                        case 4 -> { name = "Rolling... (Agility)"; icon = ICON_FEATHER;    }
                        case 5 -> { name = "Rolling... (Sustain)"; icon = ICON_GHAST_TEAR; }
                        default -> { name = "Rolling...";          icon = ICON_GOLD_NUGGET;}
                    }
                } else {
                    name = "Roll the Dice";
                    icon = ICON_GOLD_NUGGET;
                }
            }
            default -> {
                currentAlpha       = 0.0f;
                currentActiveAlpha = 0.0f;
                return;
            }
        }

        boolean isRanger = "ranger".equals(classId);
        float targetAlpha = (ClientSkillEvents.combatTimer > 0) ? 1.0f : 0.0f;

        if (ClientSkillEvents.isSkillActive) {
            targetAlpha = 1.0f;
        } else if (isRanger && ClientSkillEvents.maxCd > 0) {
            targetAlpha = 1.0f;
        } else if (ClientSkillEvents.cdRemaining > 0) {
            targetAlpha = Math.max(targetAlpha, 0.4f);
        }

        float targetActiveAlpha = ClientSkillEvents.isSkillActive ? 1.0f : 0.0f;

        currentAlpha       += (targetAlpha       - currentAlpha)       * 0.06f;
        currentActiveAlpha += (targetActiveAlpha - currentActiveAlpha) * 0.12f;

        if (currentAlpha < 0.01f) return;

        if (!name.equals(cachedName)) {
            cachedName      = name;
            cachedNameWidth = mc.font.width(name);
        }

        int boxW = PAD + ICON_SIZE + PAD + cachedNameWidth + PAD;
        int x    = 14;
        int y    = screenHeight / 2 + 55;

        int a    = (int)(currentAlpha * 255)       & 0xFF;
        int aAct = (int)(currentActiveAlpha * 255) & 0xFF;
        int accent = accentRgb(classId);

        int borderA = Math.min((int)(currentAlpha * 210), 255);
        int bgA     = Math.min((int)(currentAlpha * 200), 255);
        fillRounded(guiGraphics, x, y, boxW, BOX_H, argb(borderA, COLOR_BORDER));
        fillRounded(guiGraphics, x + 1, y + 1, boxW - 2, BOX_H - 2, argb(bgA, COLOR_BG));

        if (aAct > 4) {
            int glowA = (int)(aAct * currentAlpha * 0.85f) & 0xFF;
            fillRounded(guiGraphics, x, y, boxW, BOX_H, argb(glowA, COLOR_ACTIVE));
            fillRounded(guiGraphics, x + 1, y + 1, boxW - 2, BOX_H - 2, argb(bgA, COLOR_BG));
        }

        int iconX = x + PAD - 1;
        int iconY = y + (BOX_H - ICON_SIZE) / 2;
        guiGraphics.renderItem(icon, iconX, iconY);

        if (!ClientSkillEvents.isSkillActive && !isRanger
                && ClientSkillEvents.cdRemaining > 0 && ClientSkillEvents.maxCd > 0) {
            float cdFrac = (float) ClientSkillEvents.cdRemaining / ClientSkillEvents.maxCd;
            int dimA = (int)(cdFrac * currentAlpha * 155) & 0xFF;
            guiGraphics.fill(iconX, iconY, iconX + ICON_SIZE, iconY + ICON_SIZE,
                    argb(dimA, 0x000000));
        }

        int textX = iconX + ICON_SIZE + PAD - 1;
        int textY = y + (BOX_H - 8) / 2;
        guiGraphics.drawString(mc.font, name, textX, textY, argb(a, COLOR_TEXT), false);

        final int barY = y + BOX_H - 4;
        final int barH = 2;
        final int barX = x + 2;
        final int barW = boxW - 4;

        if (isRanger) {
            int stacks = ClientSkillEvents.maxCd;

            if (stacks > lastRangerStacks) flashTimer = 1.0f;
            lastRangerStacks = stacks;
            if (flashTimer > 0f) flashTimer = Math.max(0f, flashTimer - 0.05f);

            final int MAX_STACKS = 5;
            float pipW = (barW - (MAX_STACKS - 1)) / (float) MAX_STACKS;

            for (int i = 0; i < MAX_STACKS; i++) {
                int pipX    = barX + Math.round(i * (pipW + 1));
                int pipEndX = pipX + Math.max(1, Math.round(pipW));

                int pipColor;
                if (i < stacks) {
                    if (flashTimer > 0.2f && i == stacks - 1) {
                        pipColor = argb(a, 0xFFFFFF);
                    } else if (stacks == MAX_STACKS) {
                        pipColor = argb(a, accent);
                    } else {
                        pipColor = argb(a, 0x55DD88);
                    }
                } else {
                    pipColor = argb(Math.min(a, 70), 0x3A1010);
                }
                guiGraphics.fill(pipX, barY, pipEndX, barY + barH, pipColor);
            }

        } else if (ClientSkillEvents.cdRemaining > 0 && ClientSkillEvents.maxCd > 0) {
            float cdFrac  = (float) ClientSkillEvents.cdRemaining / ClientSkillEvents.maxCd;
            int   filledW = Math.round(barW * cdFrac);

            guiGraphics.fill(barX, barY, barX + barW, barY + barH,
                    argb(Math.min(a, 80), 0x3A1010));
            guiGraphics.fill(barX, barY, barX + filledW, barY + barH,
                    argb((int)(currentAlpha * 200) & 0xFF, accent));
        }
    };
}