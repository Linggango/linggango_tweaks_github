package com.misanthropy.linggango.linggango_tweaks.ring_selection;

import com.misanthropy.linggango.linggango_tweaks.config.TweaksConfig;
import com.misanthropy.linggango.linggango_tweaks.network.ring.RingSelectionPacket;
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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("unused")
@OnlyIn(Dist.CLIENT)
public class RingSelectionScreen extends Screen {

    private static final int C_BG = 0x080812;
    private static final int C_PANEL = 0x11111F;
    private static final int C_CARD = 0x0E0E1B;
    private static final int C_BORDER = 0x2D2D47;
    private static final int C_TEXT = 0xDEDEEC;
    private static final int C_MUTED = 0x7878A8;

    private static final int THEME_NOTHING = 0xD4A843;
    private static final int THEME_CURSE = 0xDD3333;
    private static final int THEME_VIRTUE = 0xFFAA00;

    private final Minecraft mc = Minecraft.getInstance();

    private float uiFade = 0f;
    private float bgFade = 0f;
    private float titleReveal = 0f;
    private boolean isClosing = false;
    private boolean isLoading = false;

    private long lastFrameTime;
    private float frameDt = 0f;
    private long frameNow = 0L;

    private float scaleFactor = 1.0f;
    private int vW;
    private int vH;

    private final Queue<Particle> particlePool = new ArrayDeque<>(300);
    private final List<Particle> particles = new ArrayList<>(300);
    private final List<RingCard> cards = new ArrayList<>();

    private RingDef hoveredRing = null;
    private RingDef selectedRing = null;
    private RingDef lastTargetDesc = null;
    private long descriptionStartTime = 0;

    private float descriptionAlpha = 0f;
    private float panelSlideUp = 0f;
    private float holdProgress = 0f;
    private int currentThemeColor = THEME_NOTHING;

    private RingDef cachedTargetDesc = null;
    private int cachedChars = -1;
    private String cachedTypePos = "";
    private String cachedTypeNeg = "";
    private final List<FormattedCharSequence> descCache = new ArrayList<>();
    private final List<List<FormattedCharSequence>> posCache = new ArrayList<>();
    private final List<List<FormattedCharSequence>> negCache = new ArrayList<>();

    private int cachedMaxListLines = 0;
    private Component cachedPosComponent = Component.empty();
    private Component cachedNegComponent = Component.empty();
    private String lastDisplayTitle = "";
    private Component cachedTitleComponent = Component.empty();

    private float layoutTransition = 0f;

    public static class RingDef {
        public final String id;
        public final Component title;
        public final String shortDesc;
        public final ResourceLocation itemId;
        public final String posName;
        public final List<String> blessings;
        public final String negName;
        public final List<String> curses;
        public final int themeColor;

        public RingDef(String id, String title, String shortDesc, String itemLoc, String posName, List<String> blessings, String negName, List<String> curses, int themeColor) {
            this.id = id;
            this.title = Component.literal(title).withStyle(ChatFormatting.BOLD);
            this.shortDesc = shortDesc;
            this.itemId = new ResourceLocation(itemLoc);
            this.posName = posName;
            this.blessings = blessings;
            this.negName = negName;
            this.curses = curses;
            this.themeColor = themeColor;
        }
    }

    private final List<RingDef> ringDefinitions = new ArrayList<>();
    private StyledConfirmButton confirmButton;

    public RingSelectionScreen() {
        super(Component.literal("Ring Selection"));
        this.lastFrameTime = Util.getMillis();

        ringDefinitions.add(new RingDef("cursed", "Seven Curses",
                "The path of Forbidden Power. To conquer, you must sacrifice yourself.",
                TweaksConfig.RING_CURSED_ID.get(),
                "Blessings",
                List.of("+7 Looting Level", "+7 Fortune Level", "+200% Experience dropped", "+24 Enchanting Power", "Obtain unique mob loot for forbidden relics", "Ender Chest lies ever within reach", "Drastically reduced spell cooldowns"),
                "Curses",
                List.of("Receive double damage from all sources", "Neutral creatures are aggressively hostile", "Armor is 65% less effective", "You cannot sleep", "Fire never wears off naturally", "Death tears your soul apart", "Use Life Force instead of Mana. Exhaust it, and die"),
                THEME_CURSE));

        ringDefinitions.add(new RingDef("none", "Nothing",
                "No changes.",
                TweaksConfig.RING_NONE_ID.get(), "Blessings", List.of(), "Curses", List.of(), THEME_NOTHING));

        ringDefinitions.add(new RingDef("virtue", "Seven Virtues",
                "The path to Divinity. To conquer, you must sacrifice the world.",
                TweaksConfig.RING_VIRTUE_ID.get(),
                "Virtues",
                List.of("+7 Luck & +7 Block Reach", "+200% more items from loot chests", "Stack beneficial potion effect durations", "Obtain unique loot for divine relics", "Invoke Divine Recall to teleport to spawn", "Redirect 60% of ANY spell damage to the world"),
                "Burdens",
                List.of("Melee weapons resist your hand in combat", "Monsters target you to protect nearby entities", "-4% damage per armor point (No armor = 0 dmg)", "Sacred oath denies striking innocent animals", "When chilled, you freeze permanently", "Halt all movement in the absence of light", "Exhausting Natural Energy drains the world"),
                THEME_VIRTUE));

        boolean isChaosOrTorture = false;
        try {
            var level = Minecraft.getInstance().level;
            if (level != null) {
                var diff = com.misanthropy.linggango.difficulty_enhancement.LinggangoEvents.getCurrentDifficulty(level);
                if (diff != null && (diff.id.equals("chaos") || diff.id.equals("torture"))) {
                    isChaosOrTorture = true;
                }
            }
        } catch (Exception ignored) {}

        if (isChaosOrTorture) {
            ringDefinitions.add(new RingDef("both", "Curses & Virtues",
                    "Harness both forbidden power and divine virtues. With a cost.",
                    TweaksConfig.RING_CURSED_ID.get(),
                    "Combined Boons",
                    List.of("Receive ALL Cursed Blessings & Virtue Virtues simultaneously"),
                    "Combined Burdens",
                    List.of("Endure ALL Cursed Curses & Virtue Burdens simultaneously"),
                    0x9C27B0));
        }
    }

    @Override
    protected void init() {
        float minReqWidth = 660f;
        float minReqHeight = 440f;

        this.scaleFactor = Math.min(1.0f, Math.min((float)this.width / minReqWidth, (float)this.height / minReqHeight));

        this.vW = (int) (this.width / this.scaleFactor);
        this.vH = (int) (this.height / this.scaleFactor);

        cards.clear();
        int cardWidth = 110;
        int cardHeight = 140;
        int spacing = 30;
        int numCards = ringDefinitions.size();
        int totalWidth = (cardWidth * numCards) + (spacing * (numCards - 1));

        int startX = (vW - totalWidth) / 2;
        int startY = (vH / 2) - (cardHeight / 2) - 40;

        for (int i = 0; i < numCards; i++) {
            RingDef def = ringDefinitions.get(i);
            RingCard card = new RingCard(startX + (i * (cardWidth + spacing)), startY, cardWidth, cardHeight, def);
            cards.add(card);
            addRenderableWidget(card);
        }

        int confirmBtnWidth = 140;
        confirmButton = new StyledConfirmButton(vW - confirmBtnWidth - 30, vH - 60, confirmBtnWidth, 28, "Confirm", b -> {});
        confirmButton.active = false;
        confirmButton.visible = false;
        addRenderableWidget(confirmButton);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        return super.mouseClicked(mx / scaleFactor, my / scaleFactor, btn);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int btn) {
        return super.mouseReleased(mx / scaleFactor, my / scaleFactor, btn);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
        return super.mouseDragged(mx / scaleFactor, my / scaleFactor, btn, dx / scaleFactor, dy / scaleFactor);
    }

    @Override
    public void mouseMoved(double mx, double my) {
        super.mouseMoved(mx / scaleFactor, my / scaleFactor);
    }

    @Override
    public boolean isPauseScreen() { return false; }
    @Override
    public boolean shouldCloseOnEsc() { return false; }

    private void updateAnimations(float dt) {
        float fadeSpeed = 0.08f * dt;
        float titleSpeed = 0.012f * dt;

        if (!isClosing) {
            bgFade = Math.min(1f, bgFade + fadeSpeed);
            uiFade = Math.min(1f, uiFade + fadeSpeed);
            titleReveal = clamp(titleReveal + titleSpeed);

            if (ringDefinitions.size() > 3) {
                layoutTransition = lerp(layoutTransition, 1f, 0.05f * dt);
            } else {
                layoutTransition = 0f;
            }

            hoveredRing = null;
            for (RingCard card : cards) {
                if (card.isHoveredOrFocused()) {
                    hoveredRing = card.def;
                }
            }

            RingDef targetDesc = hoveredRing != null ? hoveredRing : selectedRing;
            if (targetDesc != lastTargetDesc) {
                lastTargetDesc = targetDesc;
                descriptionStartTime = Util.getMillis();
            }

            int targetTheme = targetDesc != null ? targetDesc.themeColor : THEME_NOTHING;
            currentThemeColor = lerpColor(Math.min(1f, 0.1f * dt), currentThemeColor, targetTheme);

            if (targetDesc != null) {
                descriptionAlpha = Math.min(1f, descriptionAlpha + fadeSpeed * 1.5f);
                panelSlideUp = lerp(panelSlideUp, 1f, 0.15f * dt);
            } else {
                descriptionAlpha = Math.max(0f, descriptionAlpha - fadeSpeed * 1.5f);
                panelSlideUp = lerp(panelSlideUp, 0f, 0.15f * dt);
            }

            if (selectedRing != null) {
                confirmButton.visible = true;
                confirmButton.active = true;

                boolean holding = confirmButton.isHoveredOrFocused() &&
                        org.lwjgl.glfw.GLFW.glfwGetMouseButton(mc.getWindow().getWindow(), org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT) == org.lwjgl.glfw.GLFW.GLFW_PRESS;

                if (holding) holdProgress = Math.min(1f, holdProgress + 0.025f * dt);
                else holdProgress = Math.max(0f, holdProgress - 0.05f * dt);

                if (holding && holdProgress >= 1f && !isLoading) {
                    confirmSelection();
                }
            }
        } else {
            bgFade = Math.max(0f, bgFade - fadeSpeed);
            uiFade = Math.max(0f, uiFade - fadeSpeed);
            if (bgFade <= 0f) {
                mc.setScreen(null);
            }
        }
    }

    private void confirmSelection() {
        isLoading = true;
        com.misanthropy.linggango.linggango_tweaks.network.handler.NetworkHandler.CHANNEL.sendToServer(new RingSelectionPacket(selectedRing.id));
        cards.forEach(c -> c.active = false);
        confirmButton.active = false;
        isClosing = true;
        mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.PLAYER_LEVELUP, 1.0F));
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        frameNow = Util.getMillis();
        frameDt = Math.min((frameNow - lastFrameTime) / 16.666f, 10f);
        lastFrameTime = frameNow;

        updateAnimations(frameDt);

        int vMx = (int)(mx / scaleFactor);
        int vMy = (int)(my / scaleFactor);

        g.fill(0, 0, width, height, argb(bgFade * 0.94f, C_BG));
        g.fill(0, 0, width, height, argb(bgFade * 0.20f, 0x000000));

        if (uiFade > 0.05f && (hoveredRing != null || selectedRing != null)) {
            g.fill(0, 0, width, height, argb(bgFade * 0.12f, currentThemeColor));
        }

        drawVignette(g);

        g.pose().pushPose();
        g.pose().scale(scaleFactor, scaleFactor, 1.0f);

        float wind = (float) Math.sin(frameNow / 900.0) * 1.2f;
        spawnParticles();
        particles.removeIf(p -> {
            if (!p.tick(wind, frameDt)) {
                returnParticle(p);
                return true;
            }
            return false;
        });

        RenderSystem.enableBlend();
        particles.forEach(p -> p.draw(g, bgFade));
        RenderSystem.disableBlend();

        if (uiFade > 0.05f) {
            String cursor = (frameNow % 1000 < 500) ? "_" : "";
            String fullTitle = "Choose your path!";
            int len = (int) (titleReveal * fullTitle.length());
            String displayTitle = fullTitle.substring(0, len) + (len == fullTitle.length() ? cursor : "");

            if (!displayTitle.equals(lastDisplayTitle)) {
                lastDisplayTitle = displayTitle;
                cachedTitleComponent = Component.literal(displayTitle).withStyle(ChatFormatting.BOLD);
            }

            g.drawCenteredString(font, cachedTitleComponent, vW / 2 + 2, 42, argb(uiFade * 0.5f, 0x000000));
            g.drawCenteredString(font, cachedTitleComponent, vW / 2, 40, argb(uiFade, currentThemeColor));

            float lineAlpha = (1f - titleReveal) * uiFade * 0.35f;
            if (lineAlpha > 0.05f) {
                int lineHalfW = 30;
                int lineCx = vW / 2;
                int lineY = 52;
                for (int i = -lineHalfW; i <= lineHalfW; i++) {
                    float fade = 1.0f - (Math.abs(i) / (float) lineHalfW);
                    g.fill(lineCx + i, lineY, lineCx + i + 1, lineY + 1, argb(lineAlpha * fade, dimColor(currentThemeColor)));
                }
            }

            renderDescriptionPanel(g, vMx, vMy, frameDt);
        }

        int cardWidth = 110;
        int spacing = 30;
        int startY = (vH / 2) - (140 / 2) - 40;

        int totalWidth3 = (cardWidth * 3) + (spacing * 2);
        int startX3 = (vW - totalWidth3) / 2;

        int totalWidth4 = (cardWidth * 4) + (spacing * 3);
        int startX4 = (vW - totalWidth4) / 2;

        for (int i = 0; i < cards.size(); i++) {
            RingCard card = cards.get(i);
            int x3 = startX3 + (i * (cardWidth + spacing));
            if (i == 3) {
                x3 = startX3 + (cardWidth + spacing);
            }
            int x4 = startX4 + (i * (cardWidth + spacing));

            float currentX = lerp(x3, x4, layoutTransition);
            card.setX((int) currentX);
            if (i == 3) {
                card.setY((int) (startY + (1f - layoutTransition) * 30));
            }
        }

        super.render(g, vMx, vMy, pt);

        if (confirmButton.visible) {
            confirmButton.holdProgress = holdProgress;
        }

        g.pose().popPose();
    }

    private void renderDescriptionPanel(@NotNull GuiGraphics g, int mx, int my, float dt) {
        if (descriptionAlpha <= 0.05f) return;
        RingDef target = hoveredRing != null ? hoveredRing : selectedRing;
        if (target == null) return;

        int panelW = Math.min(620, (int)(vW * 0.85f));
        int panelH = 160;
        int panelX = (vW - panelW) / 2;
        int panelY = vH - panelH - 25;

        float a = uiFade * descriptionAlpha;
        int themeColor = target.themeColor;

        int shadowSteps = 6;
        for (int i = shadowSteps; i > 0; i--) {
            float sa = a * (0.06f * i / shadowSteps);
            g.fill(panelX + i, panelY + i, panelX + panelW + i, panelY + panelH + i, argb(sa, 0x000000));
        }

        g.fill(panelX, panelY, panelX + panelW, panelY + panelH, argb(a * 0.90f, C_PANEL));

        float borderGlow = (float) Math.sin(frameNow / 800.0) * 0.05f + 0.88f;
        g.fill(panelX, panelY, panelX + panelW, panelY + 1, argb(a * borderGlow, themeColor));
        g.fill(panelX, panelY, panelX + 1, panelY + panelH, argb(a * 0.35f, C_BORDER));
        g.fill(panelX + panelW - 1, panelY, panelX + panelW, panelY + panelH, argb(a * 0.35f, C_BORDER));
        g.fill(panelX, panelY + panelH - 1, panelX + panelW, panelY + panelH, argb(a * 0.35f, C_BORDER));

        int totalTextLen = target.shortDesc.length() + target.posName.length();
        for (String b : target.blessings) totalTextLen += b.length() + 2;
        totalTextLen += target.negName.length();
        for (String c : target.curses) totalTextLen += c.length() + 2;

        int charsRemaining = (int) ((Util.getMillis() - descriptionStartTime) / 10);
        if (charsRemaining > totalTextLen) {
            charsRemaining = totalTextLen;
        }

        int colW = (panelW / 2) - 30;

        if (target != cachedTargetDesc || charsRemaining != cachedChars) {
            cachedTargetDesc = target;
            cachedChars = charsRemaining;

            descCache.clear();
            posCache.clear();
            negCache.clear();

            int r = charsRemaining;

            String cachedTypeDesc = r > 0 ? target.shortDesc.substring(0, Math.min(target.shortDesc.length(), r)) : "";
            r -= target.shortDesc.length();
            descCache.addAll(font.split(Component.literal(cachedTypeDesc).withStyle(ChatFormatting.ITALIC), panelW - 40));

            cachedTypePos = r > 0 ? target.posName.substring(0, Math.min(target.posName.length(), r)) : "";
            r -= target.posName.length();

            for (String b : target.blessings) {
                if (r <= 0) break;
                String full = "• " + b;
                String sub = full.substring(0, Math.min(full.length(), r));
                r -= full.length();
                posCache.add(font.split(Component.literal(sub), colW));
            }

            cachedTypeNeg = r > 0 ? target.negName.substring(0, Math.min(target.negName.length(), r)) : "";
            r -= target.negName.length();

            for (String c : target.curses) {
                if (r <= 0) break;
                String full = "• " + c;
                String sub = full.substring(0, Math.min(full.length(), r));
                r -= full.length();
                negCache.add(font.split(Component.literal(sub), colW));
            }

            cachedPosComponent = Component.literal(cachedTypePos).withStyle(ChatFormatting.BOLD);
            cachedNegComponent = Component.literal(cachedTypeNeg).withStyle(ChatFormatting.BOLD);

            int posLines = 0;
            for (List<FormattedCharSequence> lines : posCache) posLines += lines.size();
            int negLines = 0;
            for (List<FormattedCharSequence> lines : negCache) negLines += lines.size();
            cachedMaxListLines = Math.max(posLines, negLines);
        }

        int expectedDescY = 12 + (descCache.size() * 10);
        int expectedSeparatorY = Math.max(28, expectedDescY + 4);
        int expectedBottomY = expectedSeparatorY + 10 + 15 + (cachedMaxListLines * 10);

        float textScale = 1.0f;
        if (expectedBottomY > panelH - 10) {
            textScale = (panelH - 10f) / expectedBottomY;
        }

        if (textScale < 1.0f) {
            g.pose().pushPose();
            g.pose().translate(panelX + panelW / 2f, panelY, 0);
            g.pose().scale(textScale, textScale, 1.0f);
            g.pose().translate(-(panelX + panelW / 2f), -panelY, 0);
        }

        int descY = panelY + 12;
        for (FormattedCharSequence line : descCache) {
            g.drawCenteredString(font, line, panelX + panelW / 2, descY, argb(a, C_MUTED));
            descY += 10;
        }

        int separatorY = panelY + expectedSeparatorY;
        g.fill(panelX + 40, separatorY, panelX + panelW - 40, separatorY + 1, argb(a * 0.3f, dimColor(themeColor)));

        if (target.blessings.isEmpty() && target.curses.isEmpty()) {
            if (charsRemaining > 0) {
                String typeNone = "No changes at all.".substring(0, Math.min(11, charsRemaining));
                g.drawCenteredString(font, Component.literal(typeNone).withStyle(ChatFormatting.GRAY), panelX + panelW / 2, panelY + panelH / 2 + 10, argb(a, C_TEXT));
            }
            if (textScale < 1.0f) g.pose().popPose();
            return;
        }

        int leftColX = panelX + 20;
        int rightColX = panelX + (panelW / 2) + 10;
        int listY = separatorY + 10;

        if (!cachedTypePos.isEmpty()) {
            g.drawString(font, cachedPosComponent, leftColX, listY, argb(a, 0x66FF66));
        }
        if (!cachedTypeNeg.isEmpty()) {
            g.drawString(font, cachedNegComponent, rightColX, listY, argb(a, 0xFF5555));
        }

        int dyLeft = listY + 15;
        for (List<FormattedCharSequence> splitLines : posCache) {
            for (FormattedCharSequence line : splitLines) {
                g.drawString(font, line, leftColX, dyLeft, argb(a, C_TEXT));
                dyLeft += 10;
            }
        }

        int dyRight = listY + 15;
        for (List<FormattedCharSequence> splitLines : negCache) {
            for (FormattedCharSequence line : splitLines) {
                g.drawString(font, line, rightColX, dyRight, argb(a * 0.85f, 0xDD9999));
                dyRight += 10;
            }
        }

        g.fill(panelX + (panelW / 2), separatorY + 8, panelX + (panelW / 2) + 1, separatorY + 8 + 15 + (cachedMaxListLines * 10), argb(a * 0.2f, dimColor(themeColor)));

        if (textScale < 1.0f) {
            g.pose().popPose();
        }
    }

    private void drawVignette(@NotNull GuiGraphics g) {
        RenderSystem.enableBlend();
        for (int x = 0; x < 45; x++) {
            float t = (float) Math.pow(1f - (float) x / 45, 3);
            g.fill(x, 0, x + 1, height, argb(bgFade * t * 0.55f, 0x010103));
            g.fill(width - x - 1, 0, width - x, height, argb(bgFade * t * 0.55f, 0x010103));
        }
        g.fill(0, 0, width, 20, argb(bgFade * 0.28f, 0x010103));
        g.fill(0, height - 20, width, height, argb(bgFade * 0.28f, 0x010103));

        float bottomPulse = (float) Math.sin(frameNow / 1600.0) * 0.05f + 0.08f;
        int glowH = 38;
        int cx = width / 2;
        for (int y = 0; y < glowH; y++) {
            float t = 1f - (y / (float) glowH);
            int w = (int) (width * 0.55f * t);
            g.fill(cx - w, height - y, cx + w, height - y + 1, argb(bgFade * bottomPulse * t, dimColor(currentThemeColor)));
        }
        RenderSystem.disableBlend();
    }

    static float clamp(float v) { return v < 0f ? 0f : Math.min(v, (float) 1.0); }
    static float lerp(float a, float b, float t) { return a + (b - a) * t; }
    static int argb(float alpha, int rgb) { return ((int) (Math.max(0f, Math.min(1f, alpha)) * 255) << 24) | (rgb & 0xFFFFFF); }

    static int dimColor(int hex) {
        return (((hex >> 16) & 0xFF) / 2 << 16) | (((hex >> 8) & 0xFF) / 2 << 8) | ((hex & 0xFF) / 2);
    }

    static int lerpColor(float t, int a, int b) {
        int ar = (a >> 16) & 255, ag = (a >> 8) & 255, ab = a & 255;
        int br = (b >> 16) & 255, bg = (b >> 8) & 255, bb = b & 255;
        return ((int) (ar + (br - ar) * t) << 16) | ((int) (ag + (bg - ag) * t) << 8) | (int) (ab + (bb - ab) * t);
    }

    private void spawnParticles() {
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        if (rand.nextFloat() < 0.06f) {
            Particle p = particlePool.poll();
            if (p == null) p = new Particle();

            p.reset(rand.nextFloat() * vW, vH + 8,
                    (rand.nextFloat() - 0.5f) * 0.25f,
                    -0.4f - rand.nextFloat() * 1.2f,
                    rand.nextInt(2) + 1, currentThemeColor, 110 + rand.nextInt(70));
            particles.add(p);
        }
    }

    private void returnParticle(Particle p) { if (particlePool.size() < 300) particlePool.offer(p); }

    private static final class Particle {
        float x, y, vx, vy;
        int sz, col, life, age;
        void reset(float x, float y, float vx, float vy, int sz, int c, int l) {
            this.x = x; this.y = y; this.vx = vx; this.vy = vy; this.sz = sz; this.col = c; this.life = l; this.age = 0;
        }
        boolean tick(float w, float dt) {
            x += (vx + w * 0.07f + (float) Math.sin(age * 0.05f) * 0.18f) * dt;
            y += vy * dt;
            vy *= (float) Math.pow(0.992f, dt);
            return age++ < life;
        }
        void draw(@NotNull GuiGraphics g, float sa) {
            float a = (1f - (float) age / life) * sa * 0.80f;
            float currentSz = sz * (1f - 0.4f * ((float) age / life)) * (1.0f + 0.25f * (float) Math.sin(age * 0.12f));
            if (a > 0.01f) g.fill((int) x, (int) y, (int) (x + currentSz), (int) (y + currentSz), argb(a, col));
        }
    }

    final class RingCard extends Button {
        final RingDef def;
        float hoverP = 0f, selectP = 0f;
        boolean wasHovered = false;
        ItemStack renderStack = ItemStack.EMPTY;
        ItemStack secondStack = ItemStack.EMPTY;

        RingCard(int x, int y, int w, int h, RingDef def) {
            super(x, y, w, h, Component.empty(), b -> {}, DEFAULT_NARRATION);
            this.def = def;
            if (def.id.equals("both")) {
                Item cursedItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(TweaksConfig.RING_CURSED_ID.get()));
                Item virtueItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(TweaksConfig.RING_VIRTUE_ID.get()));
                if (cursedItem != null && cursedItem != Items.AIR) {
                    this.renderStack = new ItemStack(cursedItem);
                }
                if (virtueItem != null && virtueItem != Items.AIR) {
                    this.secondStack = new ItemStack(virtueItem);
                }
            } else {
                Item item = ForgeRegistries.ITEMS.getValue(def.itemId);
                if (item != null && item != Items.AIR) {
                    this.renderStack = new ItemStack(item);
                }
            }
        }

        @Override
        public void onPress() {
            if (!isLoading && selectedRing != def) {
                selectedRing = def;

                String soundStr = switch (def.id) {
                    case "none" -> TweaksConfig.RING_NONE_SOUND.get();
                    case "cursed" -> TweaksConfig.RING_CURSED_SOUND.get();
                    case "virtue" -> TweaksConfig.RING_VIRTUE_SOUND.get();
                    case "both" -> "minecraft:entity.lightning_bolt.thunder";
                    default -> "minecraft:ui.button.click";
                };

                net.minecraft.sounds.SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(soundStr));
                if (sound != null) {
                    mc.getSoundManager().play(SimpleSoundInstance.forUI(sound, 1.1F));
                } else {
                    mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.1F));
                }

                holdProgress = 0f;
            }
        }

        @Override
        public void renderWidget(@NotNull GuiGraphics g, int mx, int my, float pt) {
            float dt = frameDt;
            long now = frameNow;

            boolean hovered = isHoveredOrFocused();

            if (hovered && !wasHovered && !isLoading) {
                mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 0.8F));
            }
            wasHovered = hovered;

            boolean selected = (selectedRing == def);

            hoverP = lerp(hoverP, hovered ? 1f : 0f, 1.0f - (float) Math.pow(0.78f, dt));
            selectP = lerp(selectP, selected ? 1f : 0f, 1.0f - (float) Math.pow(0.82f, dt));

            float scale = 1f + hoverP * 0.05f + selectP * 0.02f;
            if (def.id.equals("both")) {
                scale *= (0.5f + 0.5f * layoutTransition);
            }

            float cx = getX() + width / 2f, cy = getY() + height / 2f;

            g.pose().pushPose();
            g.pose().translate(cx, cy, 0);
            g.pose().scale(scale, scale, 1f);
            g.pose().translate(-cx, -cy, 0);

            float base = uiFade;
            if (def.id.equals("both")) {
                base *= layoutTransition;
                this.active = layoutTransition > 0.8f && !isLoading;
            }

            int borderColor = selected ? def.themeColor : (hovered ? lerpColor(0.5f, C_BORDER, def.themeColor) : C_BORDER);

            int shadowSteps = 5;
            for (int i = shadowSteps; i > 0; i--) {
                float sa = base * (0.07f * i / shadowSteps);
                g.fill(getX() + i, getY() + i, getX() + width + i, getY() + height + i, argb(sa, 0x000000));
            }

            g.fill(getX(), getY(), getX() + width, getY() + height, argb(base * 0.82f, C_CARD));

            if (selected) {
                float pulse = (float) Math.sin(now / 350.0) * 0.18f + 0.82f;
                g.fill(getX() - 2, getY() - 2, getX() + width + 2, getY() + height + 2, argb(base * pulse * 0.45f, def.themeColor));
            }

            g.fill(getX(), getY(), getX() + width, getY() + 1, argb(base, borderColor));
            g.fill(getX(), getY() + height - 1, getX() + width, getY() + height, argb(base, borderColor));
            g.fill(getX(), getY(), getX() + 1, getY() + height, argb(base, borderColor));
            g.fill(getX() + width - 1, getY(), getX() + width, getY() + height, argb(base, borderColor));

            int textColor = selected ? def.themeColor : (hovered ? def.themeColor : C_TEXT);

            g.drawCenteredString(font, def.title, getX() + width / 2, getY() + 15, argb(base, textColor));

            if (def.id.equals("both") && !renderStack.isEmpty() && !secondStack.isEmpty()) {
                float cardScale = 3.2f;
                float halfItemSize = (16f * cardScale) / 2f;
                float horizontalOffset = 11.0f;
                double bob = Math.sin(now / 400.0) * 4 * hoverP;

                g.pose().pushPose();

                g.pose().pushPose();
                g.pose().translate(getX() + width / 2f - halfItemSize - horizontalOffset, getY() + height / 2f - 24 + bob, 5f);
                g.pose().scale(cardScale, cardScale, 1f);
                g.pose().translate(8, 8, 0);
                g.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(-15f));
                g.pose().translate(-8, -8, 0);
                g.renderItem(renderStack, 0, 0);
                g.pose().popPose();

                g.pose().pushPose();
                g.pose().translate(getX() + width / 2f - halfItemSize + horizontalOffset, getY() + height / 2f - 24 + bob, 15f);
                g.pose().scale(cardScale, cardScale, 1f);
                g.pose().translate(8, 8, 0);
                g.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(15f));
                g.pose().translate(-8, -8, 0);
                g.renderItem(secondStack, 0, 0);
                g.pose().popPose();

                g.pose().popPose();
            } else if (!renderStack.isEmpty()) {
                g.pose().pushPose();
                g.pose().translate(getX() + width / 2f - 32, getY() + height / 2f - 24 + (Math.sin(now/400.0) * 4 * hoverP), 0);
                g.pose().scale(4.0f, 4.0f, 1f);
                g.renderItem(renderStack, 0, 0);
                g.pose().popPose();
            } else {
                g.drawCenteredString(font, Component.literal("?").withStyle(ChatFormatting.BOLD), getX() + width / 2, getY() + height / 2 - 4, argb(base * 0.5f, C_MUTED));
            }

            g.pose().popPose();
        }
    }

    final class StyledConfirmButton extends Button {
        float hoverP = 0f, holdProgress = 0f;
        boolean wasHovered = false;

        StyledConfirmButton(int x, int y, int w, int h, String msg, OnPress press) {
            super(x, y, w, h, Component.literal(msg).withStyle(ChatFormatting.BOLD), press, DEFAULT_NARRATION);
        }

        @Override
        public void renderWidget(@NotNull GuiGraphics g, int mx, int my, float pt) {
            boolean hovered = isHoveredOrFocused();

            if (hovered && !wasHovered && this.active && this.visible && !isLoading) {
                mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 0.8F));
            }
            wasHovered = hovered;

            hoverP = lerp(hoverP, hovered ? 1f : 0f, 0.2f);

            float cx = getX() + width / 2f, cy = getY() + height / 2f;
            float scale = 1f + hoverP * 0.02f;

            g.pose().pushPose();
            g.pose().translate(cx, cy, 0);
            g.pose().scale(scale, scale, 1f);
            g.pose().translate(-cx, -cy, 0);

            float base = uiFade;

            int shadowSteps = 4;
            for (int i = shadowSteps; i > 0; i--) {
                float sa = base * (0.08f * i / shadowSteps);
                g.fill(getX() + i, getY() + i, getX() + width + i, getY() + height + i, argb(sa, 0x000000));
            }

            g.fill(getX(), getY(), getX() + width, getY() + height, argb(base * 0.72f, C_PANEL));

            if (holdProgress > 0f) {
                g.fill(getX(), getY(), getX() + (int) (width * holdProgress), getY() + height, argb(base * 0.55f, currentThemeColor));
            }

            int btnBorder = lerpColor(0.4f + hoverP * 0.5f, C_BORDER, currentThemeColor);
            g.fill(getX(), getY(), getX() + width, getY() + 1, argb(base, btnBorder));
            g.fill(getX(), getY() + height - 1, getX() + width, getY() + height, argb(base, btnBorder));
            g.fill(getX(), getY(), getX() + 1, getY() + height, argb(base, btnBorder));
            g.fill(getX() + width - 1, getY(), getX() + width, getY() + height, argb(base, btnBorder));

            g.fill(getX() + 2, getY() + 2, getX() + 2 + (int)(4f * hoverP), getY() + 2 + 1, argb(base, currentThemeColor));

            g.drawCenteredString(font, getMessage(), getX() + width / 2, getY() + height / 2 - 4, argb(base, C_TEXT));
            g.pose().popPose();
        }
    }
}