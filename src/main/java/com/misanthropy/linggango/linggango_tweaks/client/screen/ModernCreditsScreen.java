package com.misanthropy.linggango.linggango_tweaks.client.screen;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.math.Axis;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jspecify.annotations.NonNull;

import java.util.*;

@OnlyIn(Dist.CLIENT)
public class ModernCreditsScreen extends Screen {

    private static class CreditElement {
        public final Component text;
        public float hoverProgress = 0.0f;
        public CreditElement(Component text) { this.text = text; }
    }

    private record CreditRow(List<CreditElement> columns, float scale, float yOffset) {}

    private static class Star {
        float x, y, size, speed, maxAlpha;
        float rotation, rotationSpeed;
        boolean isExploding = false;
        float explosionProgress = 0.0f;
        float[] particleVx = new float[8];
        float[] particleVy = new float[8];

        Star(float x, float y, float size, float speed, float maxAlpha) {
            this.x = x; this.y = y; this.size = size; this.speed = speed; this.maxAlpha = maxAlpha;
            this.rotation = (float)(Math.random() * 360.0);
            this.rotationSpeed = (float)(Math.random() * 6.0 - 3.0);
            for(int i = 0; i < 8; i++) {
                double angle = Math.random() * Math.PI * 2;
                double vel = Math.random() * 2.0 + 1.0;
                particleVx[i] = (float)(Math.cos(angle) * vel);
                particleVy[i] = (float)(Math.sin(angle) * vel);
            }
        }

        void explode() {
            this.isExploding = true;
            this.explosionProgress = 0.0f;
        }
    }

    private static class ShootingStar {
        float x, y, vx, vy, length, alpha;
    }

    private float scrollPosition = 0;
    private final List<CreditRow> creditRows = new ArrayList<>();
    private final List<Star> backgroundStars = new ArrayList<>();
    private final List<ShootingStar> shootingStars = new ArrayList<>();
    private final Random random = new Random();

    private float currentScrollSpeed = 0.5f;
    private float totalTextHeight = 0;
    private boolean fadingOut = false;
    private float fadeAlpha = 0.0f;
    private long lastFrameMillis = Util.getMillis();
    private float animAccumulator = 0.0f;
    private float spaceProgress = 0.0f;

    private CreditsMusicInstance musicInstance;

    public ModernCreditsScreen() {
        super(Component.literal("Custom Credits"));

        for (int i = 0; i < 350; i++) {
            float size = random.nextFloat() * 2.0f + 0.5f;
            if (random.nextFloat() < 0.03f) size += 4.5f;
            backgroundStars.add(new Star(
                    random.nextFloat(),
                    random.nextFloat(),
                    size,
                    random.nextFloat() * 0.4f + 0.1f,
                    random.nextFloat() * 0.6f + 0.2f
            ));
        }

        addSpace(2.0f);
        addLine(Component.literal("LINGGANGO").withStyle(style -> style.withColor(0xFFD47F).withBold(true)), 3.0f);
        addSpace(1.5f);
        addLine(Component.literal("Pack Developer: Misanthropy (@shuttheirprayer)").withStyle(style -> style.withBold(true)), 1.2f);

        addSpace(2.5f);
        addLine(Component.literal("Contributors:").withStyle(style -> style.withColor(0x7FFFD4).withBold(true)), 2.0f);
        addSpace(1.0f);
        addGrid(Arrays.asList(
                Component.literal("Colander (@jadenft)"), Component.literal("Soot (@sootyboy)"),
                Component.literal("SaloEater (@saloeater)"), Component.literal("d0ugdimadOme (@d0ugdimadOme)"),
                Component.literal("exefer (@.exefer)"), Component.literal("misalover911 (@tookover)"),
                Component.literal("lugu (@1637)")
        ));

        addSpace(2.5f);
        addLine(Component.literal("Special Thanks:").withStyle(style -> style.withColor(0xD47FFF).withBold(true)), 2.0f);
        addSpace(1.0f);
        addGrid(Arrays.asList(
                Component.literal("dtt234 (@_dtt234)"), Component.literal("proprestore (@proprestore)"),
                Component.literal("Whitepyros (@whitepyros)"), Component.literal("Alice (@alice51225)"),
                Component.literal("Rhioost (@Rhioost)"), Component.literal("yui (@yuyui.moe)"),
                Component.literal("WasteAge (@wasteage)")
        ));

        addSpace(2.5f);
        addLine(Component.literal("Tools Used:").withStyle(style -> style.withColor(0xAAAAAA).withBold(true)), 2.0f);
        addSpace(1.0f);
        addLine(Component.literal("IntelliJ IDEA").withStyle(s -> s.withUnderlined(true).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.jetbrains.com/idea/"))), 1.0f);
        addLine(Component.literal("KubeJS").withStyle(s -> s.withUnderlined(true).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.curseforge.com/minecraft/mc-mods/kubejs"))), 1.0f);
        addLine(Component.literal("Visual Studio Code").withStyle(s -> s.withUnderlined(true).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://code.visualstudio.com/"))), 1.0f);

        addSpace(2.5f);
        addLine(Component.literal("Music:").withStyle(style -> style.withColor(0x99FF99).withBold(true)), 2.0f);
        addSpace(1.0f);
        addLine(Component.literal("\"The Deer Who Ran - Credits Music\""), 1.0f);
        addSpace(1.0f);
        addGrid(Arrays.asList(
                Component.literal("\"Scarlet Tour\"").withStyle(s -> s.withColor(0xDDDDDD).withUnderlined(true).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.youtube.com/watch?v=xqNrLvmCiTY"))),
                Component.literal("\"Child Of Light\"").withStyle(s -> s.withColor(0xDDDDDD).withUnderlined(true).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.youtube.com/watch?v=C257HasoluE"))),
                Component.literal("\"wndr\"").withStyle(s -> s.withColor(0xDDDDDD).withUnderlined(true).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.youtube.com/watch?v=uVmi7GAYdGs"))),
                Component.literal("\"Main Theme\"").withStyle(s -> s.withColor(0xDDDDDD).withUnderlined(true).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.youtube.com/watch?v=v6yZhDX9H0E")))
        ));

        addSpace(2.5f);
        addLine(Component.literal("Patreon Members:").withStyle(style -> style.withColor(0xFF6666).withBold(true)), 2.0f);
        addSpace(1.0f);
        addGrid(Arrays.asList(
                Component.literal("prabbit24"), Component.literal("Typode"),
                Component.literal("Alex"), Component.literal("Deta"),
                Component.literal("James Donlon"), Component.literal("Cristoic"),
                Component.literal("Hex"), Component.literal("Lucas Lopes"),
                Component.literal("Zatrok"), Component.literal("Andrew Banh"),
                Component.literal("GengarsGrave"), Component.literal("sfus"),
                Component.literal("xKobalt"), Component.literal("A Koy"),
                Component.literal("Huga Booga"), Component.literal("snickersbabe"),
                Component.literal("Afonso Dias"), Component.literal("ぬこ ぽーち"),
                Component.literal("TeliRod"), Component.literal("Randy Neye"),
                Component.literal("thatpug"), Component.literal("Kaan Gümüs"),
                Component.literal("Jason Harder"), Component.literal("Jin Khya"),
                Component.literal("Nico"), Component.literal("Ahmed"),
                Component.literal("neppudesu"), Component.literal("Eric Wiklund"),
                Component.literal("kuv"), Component.literal("Dual Void"),
                Component.literal("John"), Component.literal("NoName8022"),
                Component.literal("Black Widow"), Component.literal("Sleepyツ"),
                Component.literal("James Locklear"), Component.literal("Laska"),
                Component.literal("Alex Ellen"), Component.literal("Trevor"),
                Component.literal("DreamVFX"), Component.literal("IMANOL"),
                Component.literal("Luca Speeter"), Component.literal("Uci14"),
                Component.literal("ALICEEEEEEEEEE"), Component.literal("Kacper Kamiński"),
                Component.literal("baton"), Component.literal("Mangle Mush"),
                Component.literal("Jey"), Component.literal("kalei liana"),
                Component.literal("Tarrace"), Component.literal("Mr.Brawls"),
                Component.literal("Florian Kramer"), Component.literal("Pál Pongrácz"),
                Component.literal("Black_Buttl3r"), Component.literal("kobayashimaruu"),
                Component.literal("Cross"), Component.literal("Fox Hallows")
        ));

        addSpace(3.0f);
        addLine(Component.literal("All of these people can be found in the official server:"), 1.0f);
        addSpace(0.5f);
        addLine(Component.literal("Linggango Discord").withStyle(style -> style.withColor(0x5865F2).withUnderlined(true).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/linggango"))), 1.2f);

        addSpace(1.5f);

        addLine(Component.literal("Support the ongoing development on:"), 1.0f);
        addSpace(0.5f);
        addLine(Component.literal("Patreon").withStyle(style -> style.withColor(0xFF424D).withUnderlined(true).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.patreon.com/MisanthropyDev"))), 1.2f);

        addSpace(1.5f);

        addLine(Component.literal("Check out the official channel:"), 1.0f);
        addSpace(0.5f);
        addLine(Component.literal("YouTube").withStyle(style -> style.withColor(0xFF0000).withUnderlined(true).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.youtube.com/@LinggDev"))), 1.2f);

        addSpace(4.0f);

        addLine(Component.literal("Developing a pack at this scale is, frankly, exhausting."), 1.2f);
        addLine(Component.literal("I’m just a single developer, with a few sweet people helping along the way."), 1.2f);
        addLine(Component.literal("So if you run into weird bugs, bear with me,"), 1.2f);
        addLine(Component.literal("they’ll get fixed as soon as the pack grows past its early-development stages."), 1.2f);
        addSpace(2.0f);
        addLine(Component.literal("Thanks for being part of the journey."), 1.2f);
        addLine(Component.literal("Now go touch grass… or create a new world and try something else ❤").withStyle(style -> style.withColor(0xFF6666)), 1.2f);

        addSpace(10.0f);
    }

    @Override
    protected void init() {
        super.init();
        assert this.minecraft != null;

        SoundEvent openSound = SoundEvent.createVariableRangeEvent(new ResourceLocation(LinggangoTweaks.MOD_ID, "credits_open"));
        SoundEvent musicSound = SoundEvent.createVariableRangeEvent(new ResourceLocation(LinggangoTweaks.MOD_ID, "the_deer_who_ran"));

        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(openSound, 1.0F, 1.0F));
        this.musicInstance = new CreditsMusicInstance(musicSound);
        this.minecraft.getSoundManager().play(this.musicInstance);
    }

    private void addLine(Component comp, float scale) {
        creditRows.add(new CreditRow(List.of(new CreditElement(comp)), scale, this.totalTextHeight));
        this.totalTextHeight += 16 * scale;
    }

    private void addGrid(List<Component> items) {
        for (int i = 0; i < items.size(); i += 2) {
            List<CreditElement> row = new ArrayList<>();
            row.add(new CreditElement(items.get(i)));
            if (i + 1 < items.size()) {
                row.add(new CreditElement(items.get(i + 1)));
            }
            creditRows.add(new CreditRow(row, (float) 1.0, this.totalTextHeight));
            this.totalTextHeight += 16 * (float) 1.0;
        }
    }

    private void addSpace(float scale) {
        this.totalTextHeight += 16 * scale;
    }

    @Override
    public void tick() {
        super.tick();
        assert this.minecraft != null;

        boolean isSpaceHeld = InputConstants.isKeyDown(this.minecraft.getWindow().getWindow(), InputConstants.KEY_SPACE);

        float baseScrollSpeed = 0.55f;
        this.currentScrollSpeed = isSpaceHeld ? (baseScrollSpeed * 3.5f) : baseScrollSpeed;
        this.scrollPosition += this.currentScrollSpeed;

        if (scrollPosition > this.height + totalTextHeight + 20 && !this.fadingOut) {
            this.fadingOut = true;
            if (this.musicInstance != null) {
                this.musicInstance.fadeOut();
            }
        }

        if (this.fadingOut) {
            this.fadeAlpha += 0.05f;
            if (this.fadeAlpha >= 1.0f) {
                this.minecraft.setScreen(null);
            }
        }

        if (random.nextFloat() < 0.02f) {
            ShootingStar ss = new ShootingStar();
            ss.x = random.nextFloat() * this.width;
            ss.y = -50;
            ss.vx = (random.nextFloat() - 0.5f) * 15.0f;
            ss.vy = random.nextFloat() * 8.0f + 8.0f;
            ss.length = random.nextFloat() * 40.0f + 20.0f;
            ss.alpha = 1.0f;
            shootingStars.add(ss);
        }

        Iterator<ShootingStar> iter = shootingStars.iterator();
        while(iter.hasNext()) {
            ShootingStar ss = iter.next();
            ss.x += ss.vx;
            ss.y += ss.vy;
            ss.alpha -= 0.015f;
            if (ss.alpha <= 0 || ss.y > this.height || ss.x < 0 || ss.x > this.width) {
                iter.remove();
            }
        }

        for (Star s : backgroundStars) {
            if (s.isExploding) {
                s.explosionProgress += 0.05f;
                if (s.explosionProgress >= 1.0f) {
                    s.isExploding = false;
                    s.x = random.nextFloat();
                    s.y = random.nextFloat();
                }
            } else {
                s.rotation += s.rotationSpeed;
            }
        }
    }

    @Override
    public void render(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        long currentMillis = Util.getMillis();
        float dt = (currentMillis - lastFrameMillis) / 1000.0f;
        lastFrameMillis = currentMillis;

        boolean doAnimStep = false;
        this.animAccumulator += dt;
        if (this.animAccumulator >= 0.066f) {
            doAnimStep = true;
            this.animAccumulator %= 0.066f;

            assert this.minecraft != null;
            boolean isSpaceHeld = InputConstants.isKeyDown(this.minecraft.getWindow().getWindow(), InputConstants.KEY_SPACE);
            this.spaceProgress += isSpaceHeld ? 0.2f : -0.2f;
            this.spaceProgress = Math.max(0.0f, Math.min(1.0f, this.spaceProgress));
        }

        guiGraphics.fillGradient(0, 0, this.width, this.height, 0xFF050510, 0xFF120A1A);

        for (Star star : backgroundStars) {
            float starX = star.x * this.width;
            float starY = (star.y * this.height - (this.scrollPosition * star.speed)) % this.height;
            if (starY < 0) starY += this.height;

            if (star.isExploding) {
                int alpha = (int)((1.0f - star.explosionProgress) * 255);
                int color = (alpha << 24) | 0xFFFFFF;
                for (int i = 0; i < 8; i++) {
                    float px = starX + star.particleVx[i] * star.explosionProgress * 60.0f;
                    float py = starY + star.particleVy[i] * star.explosionProgress * 60.0f;
                    guiGraphics.fill((int)px, (int)py, (int)px + 3, (int)py + 3, color);
                }
            } else {
                float twinkle = (float) (Math.sin((currentMillis / 500.0) + star.x * 100) * 0.5 + 0.5);
                int starAlpha = (int) (star.maxAlpha * twinkle * 255.0f);

                if (starAlpha > 5) {
                    int color = (starAlpha << 24) | 0xFFFFFF;
                    guiGraphics.pose().pushPose();
                    guiGraphics.pose().translate(starX + star.size/2, starY + star.size/2, 0);
                    guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(star.rotation));
                    guiGraphics.fill((int)(-star.size/2), (int)(-star.size/2), (int)(star.size/2), (int)(star.size/2), color);
                    guiGraphics.pose().popPose();
                }
            }
        }

        for (ShootingStar ss : shootingStars) {
            int a = (int)(ss.alpha * 255);
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(ss.x, ss.y, 0);
            float angle = (float)Math.atan2(ss.vy, ss.vx);
            guiGraphics.pose().mulPose(Axis.ZP.rotation(angle));

            for (int i = 0; i < 5; i++) {
                int segA = (int)(a * (1.0f - (i / 5.0f)));
                if (segA <= 0) continue;
                int c = (segA << 24) | 0xFFFFFF;
                float startX = -i * (ss.length / 5.0f);
                float endX = -(i + 1) * (ss.length / 5.0f);
                float h = 1.5f * (1.0f - (i / 5.0f));
                guiGraphics.fill((int)startX, (int)-h, (int)endX, (int)h, c);
            }
            guiGraphics.fill(-2, -2, 2, 2, (a << 24) | 0xFFFFFF);

            guiGraphics.pose().popPose();
        }

        int startY = this.height;
        float centerY = this.height / 2.0f;

        for (CreditRow row : creditRows) {
            float baseScale = row.scale();
            float flatY = (startY + row.yOffset()) - (scrollPosition + (currentScrollSpeed * partialTick));

            float distFromCenter = Math.abs(flatY - centerY);
            float normalizedDist = Math.min(distFromCenter / (centerY * 1.1f), 1.0f);

            float fishEyeScale = 1.0f - (normalizedDist * normalizedDist * 0.15f);
            float finalScale = baseScale * fishEyeScale;

            int alphaValue = (int) (255 * (1.0f - Math.pow(normalizedDist, 2.5f)));
            if (alphaValue <= 5) continue;

            int color = (alphaValue << 24) | 0xFFFFFF;

            float baseRenderHeight = 12 * finalScale;

            if (flatY > -baseRenderHeight - 20 && flatY < this.height + 20) {

                if (row.columns().size() == 1) {
                    CreditElement element = row.columns().get(0);
                    int textWidth = this.font.width(element.text);
                    float baseRenderWidth = textWidth * finalScale;
                    float x = (this.width - baseRenderWidth) / 2.0f;

                    boolean isClickable = element.text.getStyle().getClickEvent() != null;
                    boolean isHovered = isClickable && mouseX >= x && mouseX <= x + baseRenderWidth && mouseY >= flatY && mouseY <= flatY + baseRenderHeight;

                    if (doAnimStep) {
                        element.hoverProgress += isHovered ? 0.25f : -0.25f;
                        element.hoverProgress = Math.max(0.0f, Math.min(1.0f, element.hoverProgress));
                    }

                    float renderScale = finalScale * (1.0f + 0.15f * element.hoverProgress);
                    float renderX = (this.width - (textWidth * renderScale)) / 2.0f;
                    float renderY = flatY - ((12 * renderScale) - baseRenderHeight) / 2.0f;

                    guiGraphics.pose().pushPose();
                    guiGraphics.pose().translate(renderX, renderY, 0);
                    guiGraphics.pose().scale(renderScale, renderScale, 1.0f);
                    guiGraphics.drawString(this.font, element.text, 0, 0, color, true);
                    guiGraphics.pose().popPose();

                } else if (row.columns().size() == 2) {
                    float centerX = this.width / 2.0f;

                    CreditElement leftEl = row.columns().get(0);
                    int leftTextWidth = this.font.width(leftEl.text);
                    float leftBaseWidth = leftTextWidth * finalScale;
                    float leftX = centerX - (15.0f * finalScale) - leftBaseWidth;

                    boolean leftClickable = leftEl.text.getStyle().getClickEvent() != null;
                    boolean leftHovered = leftClickable && mouseX >= leftX && mouseX <= leftX + leftBaseWidth && mouseY >= flatY && mouseY <= flatY + baseRenderHeight;

                    if (doAnimStep) {
                        leftEl.hoverProgress += leftHovered ? 0.25f : -0.25f;
                        leftEl.hoverProgress = Math.max(0.0f, Math.min(1.0f, leftEl.hoverProgress));
                    }

                    float renderLeftScale = finalScale * (1.0f + 0.15f * leftEl.hoverProgress);
                    float renderLeftX = centerX - (15.0f * renderLeftScale) - (leftTextWidth * renderLeftScale);
                    float renderLeftY = flatY - ((12 * renderLeftScale) - baseRenderHeight) / 2.0f;

                    guiGraphics.pose().pushPose();
                    guiGraphics.pose().translate(renderLeftX, renderLeftY, 0);
                    guiGraphics.pose().scale(renderLeftScale, renderLeftScale, 1.0f);
                    guiGraphics.drawString(this.font, leftEl.text, 0, 0, color, true);
                    guiGraphics.pose().popPose();

                    CreditElement rightEl = row.columns().get(1);
                    int rightTextWidth = this.font.width(rightEl.text);
                    float rightBaseWidth = rightTextWidth * finalScale;
                    float rightX = centerX + (15.0f * finalScale);

                    boolean rightClickable = rightEl.text.getStyle().getClickEvent() != null;
                    boolean rightHovered = rightClickable && mouseX >= rightX && mouseX <= rightX + rightBaseWidth && mouseY >= flatY && mouseY <= flatY + baseRenderHeight;

                    if (doAnimStep) {
                        rightEl.hoverProgress += rightHovered ? 0.25f : -0.25f;
                        rightEl.hoverProgress = Math.max(0.0f, Math.min(1.0f, rightEl.hoverProgress));
                    }

                    float renderRightScale = finalScale * (1.0f + 0.15f * rightEl.hoverProgress);
                    float renderRightX = centerX + (15.0f * renderRightScale);
                    float renderRightY = flatY - ((12 * renderRightScale) - baseRenderHeight) / 2.0f;

                    guiGraphics.pose().pushPose();
                    guiGraphics.pose().translate(renderRightX, renderRightY, 0);
                    guiGraphics.pose().scale(renderRightScale, renderRightScale, 1.0f);
                    guiGraphics.drawString(this.font, rightEl.text, 0, 0, color, true);
                    guiGraphics.pose().popPose();
                }
            }
        }

        float hintScale = 0.5f + (0.15f * this.spaceProgress);
        Component hint = Component.literal("Hold SPACE to speed up").withStyle(style -> style.withItalic(true));

        int hintR = (int) (170 + (102 - 170) * this.spaceProgress);
        int hintG = (int) (170 + (179 - 170) * this.spaceProgress);
        int hintB = (int) (170 + (255 - 170) * this.spaceProgress);
        int hintColor = (hintR << 16) | (hintG << 8) | hintB;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(hintScale, hintScale, 1.0f);
        int scaledWidth = (int) (this.width / hintScale);
        int scaledHeight = (int) (this.height / hintScale);
        int hintWidth = this.font.width(hint);
        guiGraphics.drawString(this.font, hint, scaledWidth - hintWidth - 20, scaledHeight - 20, hintColor, false);
        guiGraphics.pose().popPose();

        if (this.fadeAlpha > 0.0f) {
            int alphaInt = (int) (Math.min(this.fadeAlpha, 1.0f) * 255.0f);
            guiGraphics.fill(0, 0, this.width, this.height, alphaInt << 24);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (Star s : backgroundStars) {
                if (s.size > 3.5f && !s.isExploding) {
                    float starX = s.x * this.width;
                    float starY = (s.y * this.height - (this.scrollPosition * s.speed)) % this.height;
                    if (starY < 0) starY += this.height;

                    if (mouseX >= starX - s.size - 8 && mouseX <= starX + s.size + 8 &&
                            mouseY >= starY - s.size - 8 && mouseY <= starY + s.size + 8) {
                        s.explode();
                        return true;
                    }
                }
            }

            int startY = this.height;
            float centerY = this.height / 2.0f;

            for (CreditRow row : creditRows) {
                float flatY = (startY + row.yOffset()) - this.scrollPosition;
                float distFromCenter = Math.abs(flatY - centerY);
                float normalizedDist = Math.min(distFromCenter / (centerY * 1.1f), 1.0f);

                float fishEyeScale = 1.0f - (normalizedDist * normalizedDist * 0.15f);
                float finalScale = row.scale() * fishEyeScale;

                if (row.columns().size() == 1) {
                    CreditElement element = row.columns().get(0);
                    float renderScale = finalScale * (1.0f + 0.15f * element.hoverProgress);
                    float scaledWidth = this.font.width(element.text) * renderScale;
                    float scaledHeight = 12 * renderScale;
                    float renderX = (this.width - scaledWidth) / 2.0f;
                    float renderY = flatY - (scaledHeight - (12 * finalScale)) / 2.0f;

                    if (mouseX >= renderX && mouseX <= renderX + scaledWidth && mouseY >= renderY && mouseY <= renderY + scaledHeight) {
                        checkAndFireClick(element.text);
                        return true;
                    }
                } else if (row.columns().size() == 2) {
                    float centerX = this.width / 2.0f;

                    CreditElement leftEl = row.columns().get(0);
                    float renderLeftScale = finalScale * (1.0f + 0.15f * leftEl.hoverProgress);
                    float leftWidth = this.font.width(leftEl.text) * renderLeftScale;
                    float leftHeight = 12 * renderLeftScale;
                    float renderLeftX = centerX - (15.0f * renderLeftScale) - leftWidth;
                    float renderLeftY = flatY - (leftHeight - (12 * finalScale)) / 2.0f;

                    if (mouseX >= renderLeftX && mouseX <= renderLeftX + leftWidth && mouseY >= renderLeftY && mouseY <= renderLeftY + leftHeight) {
                        checkAndFireClick(leftEl.text);
                        return true;
                    }

                    CreditElement rightEl = row.columns().get(1);
                    float renderRightScale = finalScale * (1.0f + 0.15f * rightEl.hoverProgress);
                    float rightWidth = this.font.width(rightEl.text) * renderRightScale;
                    float rightHeight = 12 * renderRightScale;
                    float renderRightX = centerX + (15.0f * renderRightScale);
                    float renderRightY = flatY - (rightHeight - (12 * finalScale)) / 2.0f;

                    if (mouseX >= renderRightX && mouseX <= renderRightX + rightWidth && mouseY >= renderRightY && mouseY <= renderRightY + rightHeight) {
                        checkAndFireClick(rightEl.text);
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void checkAndFireClick(Component text) {
        Style style = text.getStyle();
        if (style.getClickEvent() != null) {
            if (style.getClickEvent().getAction() == ClickEvent.Action.OPEN_URL) {
                try {
                    Util.getPlatform().openUri(new java.net.URI(style.getClickEvent().getValue()));
                } catch (Exception ignored) {}
            }
        }
    }

    @Override
    public void removed() {
        if (this.minecraft != null) {
            if (this.musicInstance != null) {
                this.minecraft.getSoundManager().stop(this.musicInstance);
            }
        }
        super.removed();
    }

    @Override
    public boolean shouldCloseOnEsc() { return false; }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) { return false; }

    private static class CreditsMusicInstance extends AbstractTickableSoundInstance {
        private float targetVolume = 1.0f;

        public CreditsMusicInstance(SoundEvent event) {
            super(event, SoundSource.MUSIC, SoundInstance.createUnseededRandom());
            this.looping = true;
            this.delay = 0;
            this.volume = 1.0f;
            this.pitch = 1.0f;
            this.relative = true;
        }

        @Override
        public void tick() {
            if (this.volume > this.targetVolume) {
                this.volume -= 0.02f;
                if (this.volume < this.targetVolume) {
                    this.volume = this.targetVolume;
                }
            }
            if (this.targetVolume <= 0.0f && this.volume <= 0.0f) {
                this.stop();
            }
        }

        public void fadeOut() {
            this.targetVolume = 0.0f;
        }
    }
}