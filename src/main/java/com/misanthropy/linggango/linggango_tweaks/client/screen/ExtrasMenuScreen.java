package com.misanthropy.linggango.linggango_tweaks.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

public class ExtrasMenuScreen extends Screen {
    private final Screen lastScreen;

    public ExtrasMenuScreen(Screen lastScreen) {
        super(Component.literal("Extras"));
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        int buttonWidth = 200;
        int buttonHeight = 20;
        int startX = (this.width - buttonWidth) / 2;
        int startY = this.height / 4 + 48;
        int spacing = 24;
        this.addRenderableWidget(Button.builder(Component.literal("Replay Credits"), button -> {
            assert this.minecraft != null;
            this.minecraft.setScreen(new ModernCreditsScreen());
        }).bounds(startX, startY, buttonWidth, buttonHeight).build());

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, button -> {
            assert this.minecraft != null;
            this.minecraft.setScreen(this.lastScreen);
        }).bounds(startX, this.height - 36, buttonWidth, buttonHeight).build());
    }

    @Override
    public void render(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}