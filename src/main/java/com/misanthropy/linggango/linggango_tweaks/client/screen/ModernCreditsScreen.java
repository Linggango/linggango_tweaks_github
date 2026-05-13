package com.misanthropy.linggango.linggango_tweaks.client.screen;

import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import com.mojang.blaze3d.platform.InputConstants;

import java.util.ArrayList;
import java.util.List;

public class ModernCreditsScreen extends Screen {
    private record CreditLine(Component text, float scale, float yOffset) {}
    private float scrollPosition = 0;
    private final List<CreditLine> creditLines = new ArrayList<>();
    private float currentScrollSpeed = 0.5f;
    private float totalTextHeight = 0;
    private boolean fadingOut = false;
    private float fadeAlpha = 0.0f;

    public ModernCreditsScreen() {
        super(Component.literal("Custom Credits"));

        addSpace(1.0f);
        addLine(Component.literal("LINGGANGO").withStyle(style -> style.withColor(0xFFD47F).withBold(true)), 3.0f);
        addSpace(1.0f);
        addLine(Component.literal("Developed by Misanthropy (@shuttheirprayer)"), 1.0f);

        addSpace(1.5f);
        addLine(Component.literal("Pack Contributors:").withStyle(style -> style.withColor(0x7FFFD4).withBold(true)), 2.0f);
        addSpace(1.0f);
        addLine(Component.literal("Colander (@jadenft)"), 1.0f);
        addLine(Component.literal("Soot (@sootyboy)"), 1.0f);
        addLine(Component.literal("SaloEater (@saloeater)"), 1.0f);
        addLine(Component.literal("d0ugdimadOme (@d0ugdimadOme)"), 1.0f);
        addLine(Component.literal("exefer (@.exefer)"), 1.0f);
        addLine(Component.literal("misalover911 (@tookover)"), 1.0f);
        addLine(Component.literal("lugu (@1637)"), 1.0f);

        addSpace(1.5f);
        addLine(Component.literal("Special Thanks:").withStyle(style -> style.withColor(0xD47FFF).withBold(true)), 2.0f);
        addSpace(1.0f);
        addLine(Component.literal("dtt234 (@_dtt234)"), 1.0f);
        addLine(Component.literal("proprestore (@proprestore)"), 1.0f);
        addLine(Component.literal("Whitepyros (@whitepyros)"), 1.0f);
        addLine(Component.literal("Alice (@alice51225)"), 1.0f);
        addLine(Component.literal("Rhioost (@Rhioost)"), 1.0f);
        addLine(Component.literal("yui (@yuyui.moe)"), 1.0f);
        addLine(Component.literal("WasteAge (@wasteage)"), 1.0f);

        addSpace(1.5f);
        addLine(Component.literal("Patreon Members:").withStyle(style -> style.withColor(0xFF6666).withBold(true)), 2.0f);
        addSpace(1.0f);
        addLine(Component.literal("Nutlover469 - Nice user"), 1.0f);
        addLine(Component.literal("Huga Booga"), 1.0f);
        addLine(Component.literal("snickersbabe"), 1.0f);
        addLine(Component.literal("Afonso Dias"), 1.0f);
        addLine(Component.literal("Beyblade"), 1.0f);
        addLine(Component.literal("ぬこ ぽーち"), 1.0f);
        addLine(Component.literal("TeliRod"), 1.0f);
        addLine(Component.literal("Randy Neye"), 1.0f);
        addLine(Component.literal("thatpug"), 1.0f);
        addLine(Component.literal("shrug_emoji"), 1.0f);
        addLine(Component.literal("Kaan Gümüs"), 1.0f);
        addLine(Component.literal("Supera04"), 1.0f);
        addLine(Component.literal("Abraham Enríquez"), 1.0f);
        addLine(Component.literal("Jason Harder"), 1.0f);
        addLine(Component.literal("Jin Khya"), 1.0f);
        addLine(Component.literal("Nico"), 1.0f);
        addLine(Component.literal("Ahmed"), 1.0f);
        addLine(Component.literal("필득 김"), 1.0f);
        addLine(Component.literal("neppudesu"), 1.0f);
        addLine(Component.literal("Eric Wiklund"), 1.0f);
        addLine(Component.literal("Kk night"), 1.0f);
        addLine(Component.literal("kuv"), 1.0f);
        addLine(Component.literal("Dual Void"), 1.0f);
        addLine(Component.literal("John"), 1.0f);
        addLine(Component.literal("NoName8022"), 1.0f);
        addLine(Component.literal("Black Widow"), 1.0f);
        addLine(Component.literal("iamsocc"), 1.0f);
        addLine(Component.literal("Sleepyツ"), 1.0f);
        addLine(Component.literal("James Locklear"), 1.0f);
        addLine(Component.literal("Laska"), 1.0f);
        addLine(Component.literal("Alex Ellen"), 1.0f);
        addLine(Component.literal("Trevor"), 1.0f);
        addLine(Component.literal("DreamVFX"), 1.0f);
        addLine(Component.literal("IMANOL"), 1.0f);
        addLine(Component.literal("Luca Speeter"), 1.0f);
        addLine(Component.literal("Isidro Mendoza"), 1.0f);
        addLine(Component.literal("ALICEEEEEEEEEE"), 1.0f);
        addLine(Component.literal("Kacper Kamiński"), 1.0f);
        addLine(Component.literal("baton"), 1.0f);
        addLine(Component.literal("Mangle Mush"), 1.0f);
        addLine(Component.literal("Jey"), 1.0f);
        addLine(Component.literal("kalei liana"), 1.0f);
        addLine(Component.literal("Tarrace"), 1.0f);
        addLine(Component.literal("Fable Jones"), 1.0f);
        addLine(Component.literal("Cross_Sama"), 1.0f);
        addLine(Component.literal("Mr.Brawls"), 1.0f);
        addLine(Component.literal("Florian Kramer"), 1.0f);
        addLine(Component.literal("Pál Pongrácz"), 1.0f);
        addLine(Component.literal("Black_Buttl3r"), 1.0f);
        addLine(Component.literal("kobayashimaruu"), 1.0f);
        addLine(Component.literal("Florian Kramer"), 1.0f);

        addSpace(2.0f);
        Component discordLine = Component.literal("All of these people can be found in the official ")
                .append(Component.literal("Linggango Discord").withStyle(style -> style.withColor(0x5865F2).withUnderlined(true)))
                .append(Component.literal(" server."))
                .withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/linggango")));
        addLine(discordLine, 1.0f);

        addSpace(4.0f);
        addLine(Component.literal("Thanks for playing!").withStyle(style -> style.withColor(0xFFD700).withItalic(true).withBold(true)), 1.5f);
        addSpace(2.0f);
    }

    private void addLine(Component comp, float scale) {
        creditLines.add(new CreditLine(comp, scale, this.totalTextHeight));
        this.totalTextHeight += 12 * scale;
    }

    private void addSpace(float scale) {
        this.totalTextHeight += 12 * scale;
    }

    @Override
    public void tick() {
        super.tick();
        assert this.minecraft != null;

        boolean isSpaceHeld = InputConstants.isKeyDown(this.minecraft.getWindow().getWindow(), InputConstants.KEY_SPACE);

        float baseScrollSpeed = 0.5f;
        this.currentScrollSpeed = isSpaceHeld ? (baseScrollSpeed * 3.5f) : baseScrollSpeed;
        this.scrollPosition += this.currentScrollSpeed;

        if (scrollPosition > this.height + totalTextHeight + 50) {
            this.fadingOut = true;
        }

        if (this.fadingOut) {
            this.fadeAlpha += 0.05f;
            if (this.fadeAlpha >= 1.0f) {
                this.minecraft.setScreen(null);
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fillGradient(0, 0, this.width, this.height, 0xFF050510, 0xFF120A1A);

        int startY = this.height;

        for (CreditLine line : creditLines) {
            float scale = line.scale();

            float y = (startY + line.yOffset()) - (scrollPosition + (currentScrollSpeed * partialTick));
            float scaledHeight = 12 * scale;

            if (y > -scaledHeight - 20 && y < this.height + 20) {
                int textWidth = this.font.width(line.text());
                float x = (this.width - (textWidth * scale)) / 2.0f;

                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(x, y, 0);
                guiGraphics.pose().scale(scale, scale, 1.0f);

                guiGraphics.drawString(this.font, line.text(), 0, 0, 0xFFFFFF, true);

                guiGraphics.pose().popPose();
            }
        }

        float hintScale = 0.5f;
        Component hint = Component.literal("Hold SPACE to speed up").withStyle(style -> style.withItalic(true));
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(hintScale, hintScale, 1.0f);
        int scaledWidth = (int) (this.width / hintScale);
        int scaledHeight = (int) (this.height / hintScale);
        int hintWidth = this.font.width(hint);
        guiGraphics.drawString(this.font, hint, scaledWidth - hintWidth - 20, scaledHeight - 20, 0xAAAAAA, false);
        guiGraphics.pose().popPose();

        if (this.fadeAlpha > 0.0f) {
            int alphaInt = (int) (Math.min(this.fadeAlpha, 1.0f) * 255.0f);
            guiGraphics.fill(0, 0, this.width, this.height, alphaInt << 24);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int startY = this.height;
            for (CreditLine line : creditLines) {
                float y = (startY + line.yOffset()) - this.scrollPosition;
                float scaledHeight = 12 * line.scale();
                float scaledWidth = this.font.width(line.text()) * line.scale();
                float x = (this.width - scaledWidth) / 2.0f;
                if (mouseX >= x && mouseX <= x + scaledWidth && mouseY >= y && mouseY <= y + scaledHeight) {
                    Style style = line.text().getStyle();
                    if (style.getClickEvent() != null) {
                        if (style.getClickEvent().getAction() == ClickEvent.Action.OPEN_URL) {
                            try {
                                Util.getPlatform().openUri(new java.net.URI(style.getClickEvent().getValue()));
                            } catch (Exception ignored) {}
                        } else {
                            this.handleComponentClicked(style);
                        }
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}