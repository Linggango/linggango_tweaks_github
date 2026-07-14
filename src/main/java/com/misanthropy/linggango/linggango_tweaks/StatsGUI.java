package com.misanthropy.linggango.linggango_tweaks;

import com.misanthropy.linggango.class_enhancement.ClassEnhancement;
import com.misanthropy.linggango.difficulty_enhancement.LinggangoEvents;
import com.misanthropy.linggango.difficulty_enhancement.LinggangoEvents.DifficultyDef;
import com.misanthropy.linggango.linggango_tweaks.config.TweaksConfig;
import com.misanthropy.linggango.linggango_tweaks.ring_selection.RingSelectionScreen.RingDef;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@OnlyIn(Dist.CLIENT)
public class StatsGUI extends Screen {

    private static final int C_BG = 0x030308;
    private static final int C_PANEL = 0x11111F;
    private static final int C_BORDER = 0x2D2D47;
    private static final int C_TEXT = 0xDEDEEC;
    private static final int C_MUTED = 0x7878A8;
    private static final int C_GOLD = 0xD4A843;

    private static final Component SCREEN_TITLE = Component.translatable("gui.linggango_tweaks.stats.title").withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD);

    private static final Component TAB_CLASS = Component.literal("CLASS");
    private static final Component TAB_DIFFICULTY = Component.literal("DIFFICULTY");
    private static final Component TAB_RING = Component.literal("RING");

    private static final Component LABEL_ATTRIBUTES = Component.literal("Attributes").withStyle(ChatFormatting.UNDERLINE, ChatFormatting.GOLD);
    private static final Component LABEL_PASSIVES = Component.literal("Passives").withStyle(ChatFormatting.UNDERLINE, ChatFormatting.GOLD);
    private static final Component LABEL_NO_STATS = Component.literal("No active class stats.").withStyle(ChatFormatting.GRAY);
    private static final Component LABEL_DEFAULT_DIFFICULTY = Component.literal("Default").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY);

    private final Minecraft mc = Minecraft.getInstance();
    private final Quaternionf q1 = new Quaternionf();
    private final Quaternionf qa = new Quaternionf();

    private float uiFade = 0f;
    private float bgFade = 0f;
    private boolean isClosing = false;
    private float closeProgress = 0f;

    private long lastFrameTime;

    private float scaleFactor = 1.0f;
    private int vW;
    private int vH;

    private int activeTab = 0;
    private float tabUnderlineX = 0f;
    private float tabTransition = 0f;
    private int interpolatedGlowColor = C_GOLD;

    private final Queue<Particle> particlePool = new ArrayDeque<>(150);
    private final List<Particle> particles = new ArrayList<>(150);

    private static List<RingDef> ringDefinitions = null;

    private ClassEnhancement.PlayerClass activeClass = null;
    private DifficultyDef activeDifficulty = null;
    private RingDef activeRing = null;

    private ItemStack ringRenderStack = ItemStack.EMPTY;
    private ItemStack ringSecondStack = ItemStack.EMPTY;

    private Component resolvedTitle;
    private Component resolvedClassName;
    private Component resolvedDifficultyName;
    private Component resolvedRingTitle;
    private Component resolvedRingPosName;
    private Component resolvedRingNegName;

    private final List<FormattedCharSequence> cachedClassStatsLines = new ArrayList<>();
    private final List<FormattedCharSequence> cachedClassDescLines = new ArrayList<>();
    private final List<FormattedCharSequence> cachedDifficultyLines = new ArrayList<>();
    private final List<FormattedCharSequence> cachedRingBlessingLines = new ArrayList<>();
    private final List<FormattedCharSequence> cachedRingCurseLines = new ArrayList<>();

    public StatsGUI() {
        super(SCREEN_TITLE);
        this.lastFrameTime = Util.getMillis();
        ensureRingDefinitions();
    }

    private static void ensureRingDefinitions() {
        if (ringDefinitions != null) return;
        ringDefinitions = new ArrayList<>(4);
        ringDefinitions.add(new RingDef("cursed", "Seven Curses",
                "The path of Forbidden Power. To conquer, you must sacrifice yourself.", TweaksConfig.RING_CURSED_ID.get(),
                "Blessings", List.of("+7 Looting Level", "+7 Fortune Level", "+200% Experience dropped", "+24 Enchanting Power", "Obtain unique mob loot for forbidden relics", "Ender Chest lies ever within reach", "Drastically reduced spell cooldowns"),
                "Curses", List.of("Receive double damage from all sources", "Neutral creatures are aggressively hostile", "Armor is 65% less effective", "You cannot sleep", "Fire never wears off naturally", "Death tears your soul apart", "Use Life Force instead of Mana. Exhaust it, and die"),
                0xDD3333));

        ringDefinitions.add(new RingDef("none", "No Ring",
                "No changes.", TweaksConfig.RING_NONE_ID.get(), "Blessings", List.of(), "Curses", List.of(), 0xD4A843));

        ringDefinitions.add(new RingDef("virtue", "Seven Virtues",
                "The path to Divinity. To conquer, you must sacrifice the world.", TweaksConfig.RING_VIRTUE_ID.get(),
                "Virtues", List.of("+7 Luck & +7 Block Reach", "+200% more items from loot chests", "Stack beneficial potion effect durations", "Obtain unique loot for divine relics", "Invoke Divine Recall to teleport to spawn", "Redirect 60% of ANY spell damage to the world"),
                "Burdens", List.of("Melee weapons resist your hand in combat", "Monsters target you to protect nearby entities", "-4% damage per armor point (No armor = 0 dmg)", "Sacred oath denies striking innocent animals", "When chilled, you freeze permanently", "Halt all movement in the absence of light", "Exhausting Natural Energy drains the world"),
                0xFFAA00));

        ringDefinitions.add(new RingDef("both", "Curses & Virtues",
                "Harness both forbidden power and divine virtues. With a cost.", TweaksConfig.RING_CURSED_ID.get(),
                "Boons", List.of("Receive ALL Cursed Blessings & Virtue Virtues simultaneously"),
                "Burdens", List.of("Endure ALL Cursed Curses & Virtue Burdens simultaneously"),
                0x9C27B0));
    }

    @Override
    protected void init() {
        float minReqWidth = 660f;
        float minReqHeight = 440f;
        this.scaleFactor = Math.min(1.0f, Math.min((float)this.width / minReqWidth, (float)this.height / minReqHeight));
        this.vW = (int) (this.width / this.scaleFactor);
        this.vH = (int) (this.height / this.scaleFactor);
        this.tabTransition = 1.0f;

        String classId = com.misanthropy.linggango.class_enhancement.client.ClientAccess.clientClassId;
        this.activeClass = null;
        if (classId != null && !classId.isEmpty()) {
            for (ClassEnhancement.PlayerClass pc : ClassEnhancement.CLASSES) {
                if (pc.id.equals(classId)) {
                    this.activeClass = pc;
                    break;
                }
            }
        }

        if (mc.level != null) {
            this.activeDifficulty = LinggangoEvents.getCurrentDifficulty(mc.level);
        }

        String activeRingId = detectEquippedRingId();
        RingDef foundRing = ringDefinitions.get(1);
        for (RingDef r : ringDefinitions) {
            if (r.id.equals(activeRingId)) {
                foundRing = r;
                break;
            }
        }
        this.activeRing = foundRing;

        this.ringRenderStack = ItemStack.EMPTY;
        this.ringSecondStack = ItemStack.EMPTY;
        if (this.activeRing.id.equals("both")) {
            Item cursedItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(TweaksConfig.RING_CURSED_ID.get()));
            Item virtueItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(TweaksConfig.RING_VIRTUE_ID.get()));
            if (cursedItem != null && cursedItem != Items.AIR) this.ringRenderStack = new ItemStack(cursedItem);
            if (virtueItem != null && virtueItem != Items.AIR) this.ringSecondStack = new ItemStack(virtueItem);
        } else {
            Item item = ForgeRegistries.ITEMS.getValue(this.activeRing.itemId);
            if (item != null && item != Items.AIR) this.ringRenderStack = new ItemStack(item);
        }

        this.interpolatedGlowColor = C_GOLD;

        this.resolvedTitle = SCREEN_TITLE.getString().equals("gui.linggango_tweaks.stats.title")
                ? Component.literal("JOURNEY STATUS").withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD)
                : SCREEN_TITLE;

        this.resolvedClassName = (this.activeClass != null)
                ? Component.translatable(this.activeClass.displayName).withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD)
                : Component.literal("No Class Chosen").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC);

        this.resolvedDifficultyName = (this.activeDifficulty != null)
                ? Component.literal(this.activeDifficulty.name).withStyle(ChatFormatting.BOLD)
                : Component.empty();

        this.resolvedRingTitle = Component.literal(this.activeRing.title.getString()).withStyle(ChatFormatting.BOLD);
        this.resolvedRingPosName = Component.literal(this.activeRing.posName + ":").withStyle(ChatFormatting.UNDERLINE, ChatFormatting.GREEN);
        this.resolvedRingNegName = Component.literal(this.activeRing.negName + ":").withStyle(ChatFormatting.UNDERLINE, ChatFormatting.RED);

        this.cachedClassStatsLines.clear();
        this.cachedClassDescLines.clear();
        if (this.activeClass != null) {
            this.cachedClassStatsLines.addAll(font.split(Component.translatable(this.activeClass.statsInfo), 226));
            this.cachedClassDescLines.addAll(font.split(Component.translatable(this.activeClass.description), 226));
        }

        this.cachedDifficultyLines.clear();
        if (this.activeDifficulty != null) {
            List<Component> diffLines = new ArrayList<>();
            diffLines.add(Component.literal("HP Multiplier: " + activeDifficulty.hpMultiplier + "x").withStyle(ChatFormatting.GRAY));
            diffLines.add(Component.literal("Damage Multiplier: " + activeDifficulty.dmgMultiplier + "x").withStyle(ChatFormatting.GRAY));

            if (activeDifficulty.fallDmgMultiplier > 1.0f) {
                diffLines.add(Component.literal("• High Fall Damage").withStyle(ChatFormatting.RED));
            } else if (activeDifficulty.fallDmgMultiplier < 1.0f) {
                diffLines.add(Component.literal("• Reduced Fall Damage").withStyle(ChatFormatting.GREEN));
            } else {
                diffLines.add(Component.literal("• Normal Fall Damage").withStyle(ChatFormatting.GRAY));
            }

            if (activeDifficulty.enableRegen) {
                diffLines.add(Component.literal("• Passive Regeneration").withStyle(ChatFormatting.GREEN));
            } else {
                diffLines.add(Component.literal("• No Regeneration").withStyle(ChatFormatting.RED));
            }

            if (activeDifficulty.enableBleeding) {
                diffLines.add(Component.literal("• Bleeding Effect Active").withStyle(ChatFormatting.RED));
            }
            if (activeDifficulty.enableCrippling) {
                diffLines.add(Component.literal("• Crippling Effect Active").withStyle(ChatFormatting.RED));
            }
            if (activeDifficulty.enableVengefulAI) {
                diffLines.add(Component.literal("• Hostile Animals & Villagers").withStyle(ChatFormatting.RED));
            }
            if (activeDifficulty.lifestealAmount > 0) {
                diffLines.add(Component.literal("• Hostile Lifesteal: " + (int)(activeDifficulty.lifestealAmount * 100) + "%").withStyle(ChatFormatting.RED));
            }

            for (String trait : activeDifficulty.traits) {
                if (trait != null && !trait.isEmpty()) {
                    diffLines.add(Component.literal("• " + trait).withStyle(ChatFormatting.GOLD));
                }
            }

            for (Component line : diffLines) {
                this.cachedDifficultyLines.addAll(font.split(line, 218));
            }
        }

        this.cachedRingBlessingLines.clear();
        this.cachedRingCurseLines.clear();
        for (String b : this.activeRing.blessings) {
            this.cachedRingBlessingLines.addAll(font.split(Component.literal("• " + b), 226));
        }
        for (String c : this.activeRing.curses) {
            this.cachedRingCurseLines.addAll(font.split(Component.literal("• " + c), 226));
        }
    }

    private String detectEquippedRingId() {
        Player player = mc.player;
        if (player == null) return "none";

        final boolean[] hasCursed = {false};
        final boolean[] hasVirtue = {false};

        try {
            Item cursedItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(TweaksConfig.RING_CURSED_ID.get()));
            Item virtueItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(TweaksConfig.RING_VIRTUE_ID.get()));

            CuriosApi.getCuriosHelper().getEquippedCurios(player).ifPresent(handler -> {
                for (int i = 0; i < handler.getSlots(); i++) {
                    ItemStack stack = handler.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        Item item = stack.getItem();
                        if (item == cursedItem && item != Items.AIR) hasCursed[0] = true;
                        if (item == virtueItem && item != Items.AIR) hasVirtue[0] = true;
                    }
                }
            });
        } catch (Exception ignored) {}

        if (hasCursed[0] && hasVirtue[0]) return "both";
        if (hasCursed[0]) return "cursed";
        if (hasVirtue[0]) return "virtue";
        return "none";
    }

    public void initiateClose() {
        this.isClosing = true;
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0 && uiFade > 0.8f && !isClosing) {
            int panelW = 250;
            int panelH = 345;
            int x = (vW - panelW) / 2;
            int y = (vH - panelH) / 2 + 10;

            double vMx = mx / scaleFactor;
            double vMy = my / scaleFactor;

            int tabW = panelW / 3;
            if (vMy >= y && vMy < y + 25) {
                if (vMx >= x && vMx < x + tabW && activeTab != 0) {
                    activeTab = 0;
                    tabTransition = 0f;
                    mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.2F));
                    spawnTabSwitchBurst(C_GOLD, x + tabW / 2, y + 12);
                    return true;
                }
                if (vMx >= x + tabW && vMx < x + tabW * 2 && activeTab != 1) {
                    activeTab = 1;
                    tabTransition = 0f;
                    int diffColor = activeDifficulty != null ? parseDifficultyColor(activeDifficulty) : C_GOLD;
                    mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.2F));
                    spawnTabSwitchBurst(diffColor, x + tabW + tabW / 2, y + 12);
                    return true;
                }
                if (vMx >= x + tabW * 2 && vMx < x + panelW && activeTab != 2) {
                    activeTab = 2;
                    tabTransition = 0f;
                    mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.2F));
                    spawnTabSwitchBurst(activeRing.themeColor, x + tabW * 2 + tabW / 2, y + 12);
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        long frameNow = Util.getMillis();
        float frameDt = Math.min((frameNow - lastFrameTime) / 16.666f, 10f);
        lastFrameTime = frameNow;

        if (isClosing) {
            closeProgress = Math.min(1f, closeProgress + 0.08f * frameDt);
            bgFade = 1f - closeProgress;
            uiFade = 1f - closeProgress;
            if (closeProgress >= 1f) {
                mc.setScreen(null);
                return;
            }
        } else {
            bgFade = Math.min(1f, bgFade + 0.08f * frameDt);
            uiFade = Math.min(1f, uiFade + 0.08f * frameDt);
        }

        tabTransition = Math.min(1.0f, tabTransition + 0.12f * frameDt);

        int vMx = (int)(mx / scaleFactor);
        int vMy = (int)(my / scaleFactor);

        g.fill(0, 0, width, height, argb(bgFade * 0.70f, C_BG));

        g.pose().pushPose();
        g.pose().scale(scaleFactor, scaleFactor, 1.0f);

        if (closeProgress > 0f) {
            float cx = vW / 2f;
            float cy = vH / 2f;
            float closeScale = 1.0f - (closeProgress * 0.25f);
            float closeYOffset = closeProgress * 220f;
            float closeTiltAngle = closeProgress * 6f;
            g.pose().translate(cx, cy + closeYOffset, 0);
            g.pose().scale(closeScale, closeScale, 1.0f);
            g.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(closeTiltAngle));
            g.pose().translate(-cx, -cy, 0);
        }

        int panelW = 250;
        int panelH = 345;
        int x = (vW - panelW) / 2;
        int y = (vH - panelH) / 2 + 10;

        int targetColor = C_GOLD;
        if (activeTab == 1 && activeDifficulty != null) {
            targetColor = parseDifficultyColor(activeDifficulty);
        } else if (activeTab == 2) {
            targetColor = activeRing.themeColor;
        }
        interpolatedGlowColor = lerpColor(Math.min(1.0f, 0.15f * frameDt), interpolatedGlowColor, targetColor);

        spawnParticles(x, panelW);
        float wind = (float) Math.sin(frameNow / 900.0) * 1.2f;

        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            if (!p.tick(wind, frameDt)) {
                particles.remove(i);
            }
        }

        RenderSystem.enableBlend();
        for (Particle p : particles) {
            p.draw(g, bgFade);
        }
        RenderSystem.disableBlend();

        if (uiFade > 0.05f) {
            if (resolvedTitle != null && !resolvedTitle.getString().isEmpty()) {
                g.drawCenteredString(font, resolvedTitle, vW / 2, y - 20, argb(uiFade, C_GOLD));
            }

            drawCardBase(g, x, y, panelW, panelH, interpolatedGlowColor);

            int tabW = panelW / 3;
            g.fill(x, y, x + panelW, y + 25, argb(uiFade * 0.95f, 0x0E0E1B));
            g.fill(x, y + 24, x + panelW, y + 25, argb(uiFade, C_BORDER));

            boolean hoverTab0 = vMx >= x && vMx < x + tabW && vMy >= y && vMy < y + 25 && !isClosing;
            boolean hoverTab1 = vMx >= x + tabW && vMx < x + tabW * 2 && vMy >= y && vMy < y + 25 && !isClosing;
            boolean hoverTab2 = vMx >= x + tabW * 2 && vMx < x + panelW && vMy >= y && vMy < y + 25 && !isClosing;

            g.drawCenteredString(font, TAB_CLASS, x + tabW / 2, y + 8, argb(uiFade, activeTab == 0 ? C_GOLD : (hoverTab0 ? C_TEXT : C_MUTED)));
            g.drawCenteredString(font, TAB_DIFFICULTY, x + tabW + tabW / 2, y + 8, argb(uiFade, activeTab == 1 ? C_GOLD : (hoverTab1 ? C_TEXT : C_MUTED)));
            g.drawCenteredString(font, TAB_RING, x + tabW * 2 + tabW / 2, y + 8, argb(uiFade, activeTab == 2 ? C_GOLD : (hoverTab2 ? C_TEXT : C_MUTED)));

            tabUnderlineX = lerp(tabUnderlineX, activeTab * tabW, 0.22f * frameDt);
            g.fill(x + (int)tabUnderlineX + 6, y + 23, x + (int)tabUnderlineX + tabW - 6, y + 25, argb(uiFade, interpolatedGlowColor));

            float slideXOffset = (1.0f - easeOutQuad(tabTransition)) * -18.0f;
            float contentAlpha = uiFade * tabTransition;

            g.pose().pushPose();
            g.pose().translate(slideXOffset, 0, 0);

            if (activeTab == 0) {
                renderClassContent(g, x, y + 25, panelW, contentAlpha, frameNow);
            } else if (activeTab == 1) {
                renderDifficultyContent(g, x, y + 25, panelW, contentAlpha);
            } else if (activeTab == 2) {
                renderRingContent(g, x, y + 25, panelW, contentAlpha, frameNow);
            }

            g.pose().popPose();
        }

        super.render(g, vMx, vMy, pt);
        g.pose().popPose();
    }

    private void renderClassContent(GuiGraphics g, int x, int y, int w, float alpha, long frameNow) {
        if (mc.player != null) {
            renderPlayerPreview(g, x + w / 2, y + 105, frameNow);
        }

        g.drawCenteredString(font, resolvedClassName, x + w / 2, y + 120, argb(alpha, C_TEXT));

        if (activeClass != null) {
            int textY = y + 140;
            float listScale = 0.82f;

            g.pose().pushPose();
            g.pose().translate(x + w / 2f, textY, 0);
            g.pose().scale(listScale, listScale, 1f);
            g.pose().translate(-(x + w / 2f), -textY, 0);

            int currentY = textY;
            g.drawCenteredString(font, LABEL_ATTRIBUTES, x + w / 2, currentY, argb(alpha, C_TEXT));
            currentY += 12;
            for (FormattedCharSequence line : cachedClassStatsLines) {
                g.drawCenteredString(font, line, x + w / 2, currentY, argb(alpha, C_TEXT));
                currentY += 10;
            }

            currentY += 6;
            g.drawCenteredString(font, LABEL_PASSIVES, x + w / 2, currentY, argb(alpha, C_TEXT));
            currentY += 12;
            int count = 0;
            for (FormattedCharSequence line : cachedClassDescLines) {
                if (count < 3) {
                    g.drawCenteredString(font, line, x + w / 2, currentY, argb(alpha, C_MUTED));
                    currentY += 10;
                    count++;
                }
            }

            g.pose().popPose();
        } else {
            g.drawCenteredString(font, LABEL_NO_STATS, x + w / 2, y + 145, argb(alpha, C_MUTED));
        }
    }

    private void renderDifficultyContent(GuiGraphics g, int x, int y, int w, float alpha) {
        if (activeDifficulty != null) {
            int themeColor = parseDifficultyColor(activeDifficulty);
            g.drawCenteredString(font, resolvedDifficultyName, x + w / 2, y + 15, argb(alpha, themeColor));

            int drawY = y + 36;
            for (FormattedCharSequence split : cachedDifficultyLines) {
                g.drawString(font, split, x + 18, drawY, argb(alpha, C_TEXT));
                drawY += 13;
            }
        } else {
            g.drawCenteredString(font, LABEL_DEFAULT_DIFFICULTY, x + w / 2, y + 40, argb(alpha, C_MUTED));
        }
    }

    private void renderRingContent(GuiGraphics g, int x, int y, int w, float alpha, long frameNow) {
        int themeColor = activeRing.themeColor;

        double bob = Math.sin(frameNow / 400.0) * 3;
        if (activeRing.id.equals("both") && !ringRenderStack.isEmpty() && !ringSecondStack.isEmpty()) {
            float itemScale = 2.0f;
            g.pose().pushPose();
            g.pose().translate(x + w / 2f - 26, y + 10 + bob, 0);
            g.pose().scale(itemScale, itemScale, 1f);
            g.renderItem(ringRenderStack, 0, 0);
            g.pose().popPose();

            g.pose().pushPose();
            g.pose().translate(x + w / 2f - 6, y + 10 + bob, 10);
            g.pose().scale(itemScale, itemScale, 1f);
            g.renderItem(ringSecondStack, 0, 0);
            g.pose().popPose();
        } else if (!ringRenderStack.isEmpty()) {
            float itemScale = 2.4f;
            g.pose().pushPose();
            g.pose().translate(x + w / 2f - 18, y + 10 + bob, 0);
            g.pose().scale(itemScale, itemScale, 1f);
            g.renderItem(ringRenderStack, 0, 0);
            g.pose().popPose();
        } else {
            g.drawCenteredString(font, "?", x + w / 2, y + 25, argb(alpha * 0.4f, C_MUTED));
        }

        g.drawCenteredString(font, resolvedRingTitle, x + w / 2, y + 55, argb(alpha, themeColor));

        int textY = y + 72;
        int currentY = textY;

        float listScale = activeRing.id.equals("cursed") || activeRing.id.equals("virtue") ? 0.78f : 0.88f;

        g.pose().pushPose();
        g.pose().translate(x + w / 2f, textY, 0);
        g.pose().scale(listScale, listScale, 1f);
        g.pose().translate(-(x + w / 2f), -textY, 0);

        if (!activeRing.blessings.isEmpty()) {
            g.drawString(font, resolvedRingPosName, x + 16, currentY, argb(alpha, 0x55FF55));
            currentY += 12;
            for (FormattedCharSequence line : cachedRingBlessingLines) {
                g.drawString(font, line, x + 16, currentY, argb(alpha * 0.90f, C_TEXT));
                currentY += 11;
            }
        }

        if (!activeRing.curses.isEmpty()) {
            currentY += 5;
            g.drawString(font, resolvedRingNegName, x + 16, currentY, argb(alpha, 0xFF5555));
            currentY += 12;
            for (FormattedCharSequence line : cachedRingCurseLines) {
                g.drawString(font, line, x + 16, currentY, argb(alpha * 0.85f, C_MUTED));
                currentY += 11;
            }
        }

        g.pose().popPose();
    }

    private void drawCardBase(GuiGraphics g, int x, int y, int w, int h, int glowColor) {
        int shadowSteps = 5;
        for (int i = shadowSteps; i > 0; i--) {
            float sa = uiFade * (0.07f * i / shadowSteps);
            g.fill(x + i, y + i, x + w + i, y + h + i, argb(sa, 0x000000));
        }

        for (int i = 1; i <= 4; i++) {
            float sa = uiFade * 1.0f * (0.06f / i);
            g.fill(x - i, y - i, x + w + i, y + h + i, argb(sa, glowColor));
        }

        g.fill(x, y, x + w, y + h, argb(uiFade * 0.82f, C_PANEL));

        g.fill(x, y, x + w, y + 1, argb(uiFade, C_BORDER));
        g.fill(x, y + h - 1, x + w, y + h, argb(uiFade, C_BORDER));
        g.fill(x, y, x + 1, y + h, argb(uiFade, C_BORDER));
        g.fill(x + w - 1, y, x + w, y + h, argb(uiFade, C_BORDER));
    }

    private void renderPlayerPreview(GuiGraphics g, int x, int y, long frameNow) {
        Player p = mc.player;
        if (p == null) return;

        g.pose().pushPose();
        g.pose().translate(x, y - 35, 0);

        float angle = (float)(frameNow * 0.0005);
        g.pose().pushPose();
        g.pose().mulPose(qa.identity().rotationZ(angle));
        drawAuraBracket(g, uiFade * 0.35f);
        g.pose().popPose();

        g.pose().popPose();

        float yb = p.yBodyRot;
        float yr = p.getYRot();
        float xr = p.getXRot();
        float yh0 = p.yHeadRotO;
        float yh = p.yHeadRot;

        float swayAngle = 180f + (float) Math.sin(frameNow / 550.0) * 12f;

        q1.identity().rotateZ((float) Math.PI);
        p.yBodyRot = swayAngle;
        p.setYRot(swayAngle);
        p.setXRot(0f);
        p.yHeadRot = p.getYRot();
        p.yHeadRotO = p.getYRot();

        InventoryScreen.renderEntityInInventory(g, x, y, 32, q1, null, p);

        p.yBodyRot = yb;
        p.setYRot(yr);
        p.setXRot(xr);
        p.yHeadRotO = yh0;
        p.yHeadRot = yh;
    }

    private void drawAuraBracket(GuiGraphics g, float alpha) {
        int r = 26;
        int c = argb(alpha, interpolatedGlowColor);
        g.fill(-r, -1, -r + 6, 1, c);
        g.fill(r - 6, -1, r, 1, c);
        g.fill(-1, -r, 1, -r + 6, c);
        g.fill(-1, r - 6, 1, r, c);
    }

    private int parseDifficultyColor(DifficultyDef def) {
        return switch (def.uiTheme) {
            case 1 -> 0x55FFFF;
            case 2 -> 0xFFAA00;
            case 3 -> 0xFF3333;
            case 4 -> getRainbowColor();
            default -> 0x55FF55;
        };
    }

    private static int getRainbowColor() {
        float hue = (Util.getMillis() % 1200) / 1200f;
        int hi = (int) (hue * 6);
        float f = hue * 6 - hi;
        float p = 0.315f;
        float q = 0.7f * (1.0f - f * 0.55f);
        float t = 0.7f * (1.0f - (1.0f - f) * 0.55f);
        float r, g, b;
        switch (hi) {
            case 0 -> { r = 0.7f; g = t; b = p; }
            case 1 -> { r = q; g = 0.7f; b = p; }
            case 2 -> { r = p; g = 0.7f; b = t; }
            case 3 -> { r = p; g = q; b = 0.7f; }
            case 4 -> { r = t; g = p; b = 0.7f; }
            default -> { r = 0.7f; g = p; b = q; }
        }
        return 0xFF000000 | ((int) (r * 255.0f) << 16) | ((int) (g * 255.0f) << 8) | (int) (b * 255.0f);
    }

    private void spawnParticles(int panelX, int panelW) {
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        if (rand.nextFloat() < 0.05f) {
            Particle p = particlePool.poll();
            if (p == null) p = new Particle();

            float spawnX = rand.nextFloat() * vW;
            int color = interpolatedGlowColor;

            if (rand.nextBoolean()) {
                spawnX = panelX - 10 + rand.nextFloat() * (panelW + 20);
            }

            p.reset(spawnX, vH + 8,
                    (rand.nextFloat() - 0.5f) * 0.25f,
                    -0.4f - rand.nextFloat() * 1.2f,
                    rand.nextInt(2) + 1, color, 110 + rand.nextInt(70));
            particles.add(p);
        }
    }

    private void spawnTabSwitchBurst(int color, int tabCenterX, int tabCenterY) {
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        for (int i = 0; i < 15; i++) {
            Particle p = particlePool.poll();
            if (p == null) p = new Particle();
            p.reset(tabCenterX + (rand.nextFloat() - 0.5f) * 20f,
                    tabCenterY + (rand.nextFloat() - 0.5f) * 10f,
                    (rand.nextFloat() - 0.5f) * 4.0f,
                    -1.2f - rand.nextFloat() * 1.5f,
                    rand.nextInt(2) + 1, color, 70 + rand.nextInt(45));
            particles.add(p);
        }
    }

    private void returnParticle(Particle p) { if (particlePool.size() < 150) particlePool.offer(p); }

    static int argb(float alpha, int rgb) {
        return ((int) (clamp01(alpha) * 255.0f) << 24) | (rgb & 0xFFFFFF);
    }
    static float lerp(float a, float b, float t) { return a + (b - a) * t; }
    static float clamp01(float value) { return Math.max(0f, Math.min(1f, value)); }

    static float easeOutQuad(float x) { return x * (2f - x); }

    static int lerpColor(float t, int a, int b) {
        int ar = (a >> 16) & 255, ag = (a >> 8) & 255, ab = a & 255;
        int br = (b >> 16) & 255, bg = (b >> 8) & 255, bb = b & 255;
        return ((int) (ar + (br - ar) * t) << 16) | ((int) (ag + (bg - ag) * t) << 8) | (int) (ab + (bb - ab) * t);
    }

    private final class Particle {
        float x, y, vx, vy;
        int sz, col, life, age;
        void reset(float x, float y, float vx, float vy, int sz, int c, int l) {
            this.x = x; this.y = y; this.vx = vx; this.vy = vy; this.sz = sz; this.col = c; this.life = l; this.age = 0;
        }
        boolean tick(float w, float dt) {
            x += (vx + w * 0.07f + (float) Math.sin(age * 0.05f) * 0.18f) * dt;
            y += vy * dt;
            vy *= (1.0f - 0.008f * dt);
            boolean alive = age++ < life;
            if (!alive) returnParticle(this);
            return alive;
        }
        void draw(@NotNull GuiGraphics g, float sa) {
            float a = (1f - (float) age / life) * sa * 0.80f;
            float currentSz = sz * (1f - 0.4f * ((float) age / life)) * (1.0f + 0.25f * (float) Math.sin(age * 0.12f));
            if (a > 0.01f) g.fill((int) x, (int) y, (int) (x + currentSz), (int) (y + currentSz), argb(a, col));
        }
    }
}