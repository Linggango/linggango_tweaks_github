package com.misanthropy.linggango.linggango_tweaks.skills.client;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jspecify.annotations.NonNull;

@Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SkillOverlay {

    private static float currentAlpha = 0.0f;
    private static float currentActiveAlpha = 0.0f;
    private static final ItemStack ICON_LAPIS      = new ItemStack(Items.LAPIS_LAZULI);
    private static final ItemStack ICON_ANVIL      = new ItemStack(Items.ANVIL);
    private static final ItemStack ICON_CRYSTAL    = new ItemStack(Items.END_CRYSTAL);
    private static final ItemStack ICON_LADDER     = new ItemStack(Items.LADDER);
    private static final ItemStack ICON_REDSTONE   = new ItemStack(Items.REDSTONE);
    private static final ItemStack ICON_GOLD_NUGGET= new ItemStack(Items.GOLD_NUGGET);
    private static final ItemStack ICON_AXE        = new ItemStack(Items.IRON_AXE);
    private static final ItemStack ICON_CHESTPLATE = new ItemStack(Items.IRON_CHESTPLATE);
    private static final ItemStack ICON_POTION     = new ItemStack(Items.POTION);
    private static final ItemStack ICON_FEATHER    = new ItemStack(Items.FEATHER);
    private static final ItemStack ICON_GHAST_TEAR = new ItemStack(Items.GHAST_TEAR);

    private static final ResourceLocation TERRAMITY_GOLD_ROUND = new ResourceLocation("terramity", "gold_round");
    private static ItemStack cachedGunnerAmmo = null;
    private static String cachedName = "";
    private static int cachedNameWidth = 0;

    @SubscribeEvent
    public static void registerKeys(@NonNull RegisterKeyMappingsEvent event) {
        event.register(ClientSkillEvents.SKILL_KEY);
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
        final String name;
        final ItemStack icon;

        switch (classId) {
            case "north_mage" -> {
                name = "Mana Surge";
                icon = ICON_LAPIS;
            }
            case "machinist" -> {
                name = "Quick Repair";
                icon = ICON_ANVIL;
            }
            case "south_mage" -> {
                name = "Limit Break";
                icon = ICON_CRYSTAL;
            }
            case "miner" -> {
                name = "Crawl";
                icon = ICON_LADDER;
            }
            case "vampire" -> {
                name = "Night's Embrace";
                icon = ICON_REDSTONE;
            }
            case "gunner" -> {
                name = "Explosive Shot";
                if (cachedGunnerAmmo == null) {
                    var ammoItem = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(TERRAMITY_GOLD_ROUND);
                    cachedGunnerAmmo = new ItemStack(ammoItem != null ? ammoItem : Items.GOLD_NUGGET);
                }
                icon = cachedGunnerAmmo;
            }
            case "gambler" -> {
                if (ClientSkillEvents.isSkillActive) {
                    int fakeRoll = (mc.player.tickCount / 4) % 6;
                    switch (fakeRoll) {
                        case 0 -> { name = "Rolling... (Repair)";   icon = ICON_ANVIL; }
                        case 1 -> { name = "Rolling... (Rage)";     icon = ICON_AXE; }
                        case 2 -> { name = "Rolling... (Defense)";  icon = ICON_CHESTPLATE; }
                        case 3 -> { name = "Rolling... (Invis)";    icon = ICON_POTION; }
                        case 4 -> { name = "Rolling... (Agility)";  icon = ICON_FEATHER; }
                        case 5 -> { name = "Rolling... (Sustain)";  icon = ICON_GHAST_TEAR; }
                        default -> { name = "Rolling...";           icon = ICON_GOLD_NUGGET; }
                    }
                } else {
                    name = "Roll the Dice";
                    icon = ICON_GOLD_NUGGET;
                }
            }
            default -> {
                currentAlpha = 0.0f;
                currentActiveAlpha = 0.0f;
                return;
            }
        }

        float targetAlpha = (ClientSkillEvents.combatTimer > 0) ? 1.0f : 0.0f;
        if (ClientSkillEvents.cdRemaining > 0 && !ClientSkillEvents.isSkillActive) {
            targetAlpha = Math.min(targetAlpha, 0.4f);
        }
        float targetActiveAlpha = ClientSkillEvents.isSkillActive ? 1.0f : 0.0f;
        currentAlpha += (targetAlpha - currentAlpha) * 0.1f;
        currentActiveAlpha += (targetActiveAlpha - currentActiveAlpha) * 0.2f;

        if (currentAlpha < 0.05f) return;
        if (!name.equals(cachedName)) {
            cachedName = name;
            cachedNameWidth = mc.font.width(name);
        }

        int alphaInt  = (int) (currentAlpha * 255);
        int bgAlpha   = Math.min((int) (currentAlpha * 180), 255) << 24;
        int textAlpha = alphaInt << 24;

        int x = 15;
        int y = screenHeight / 2 + 60;
        int boxWidth = 35 + cachedNameWidth;

        if (currentActiveAlpha > 0.01f) {
            int borderAlpha = (int) (currentActiveAlpha * currentAlpha * 255) << 24;
            guiGraphics.fill(x - 1, y - 1, x + boxWidth + 1, y + 23, borderAlpha | 0xDDAA00);
        }

        guiGraphics.fill(x, y, x + boxWidth, y + 22, bgAlpha);
        guiGraphics.renderItem(icon, x + 3, y + 3);
        guiGraphics.drawString(mc.font, name, x + 25, y + 7, textAlpha | 0xFFFFFF);

        if (ClientSkillEvents.cdRemaining > 0 && ClientSkillEvents.maxCd > 0) {
            float cdP = (float) ClientSkillEvents.cdRemaining / ClientSkillEvents.maxCd;
            int cdHeight = (int) (16 * cdP);
            guiGraphics.fill(x + 3, y + 3 + (16 - cdHeight), x + 19, y + 19, 0x99000000);
        }
    };
}