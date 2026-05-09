package com.misanthropy.linggango.linggango_tweaks.client.atmosphere;

import com.misanthropy.linggango.linggango_tweaks.config.AtmosphereConfigManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public class AtmosphereEditorScreen extends Screen {
    private final ResourceLocation biomeId;

    private float fogR, fogG, fogB;
    private float skyR, skyG, skyB;
    private boolean hasCustomSky;

    private EditBox fogStartInput;
    private EditBox fogEndInput;
    private Button toggleSkyButton;

    public AtmosphereEditorScreen(ResourceLocation biomeId) {
        super(Component.literal("Atmosphere Editor"));
        this.biomeId = biomeId;
    }

    @Override
    protected void init() {
        super.init();
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        AtmosphereConfigManager.AtmosphereSettings current = AtmosphereConfigManager.ATMOSPHERES.get(biomeId.toString());
        if (current != null) {
            fogR = ((current.fogHex >> 16) & 0xFF) / 255.0f;
            fogG = ((current.fogHex >> 8) & 0xFF) / 255.0f;
            fogB = (current.fogHex & 0xFF) / 255.0f;

            hasCustomSky = current.skyHex != -1;
            if (hasCustomSky) {
                skyR = ((current.skyHex >> 16) & 0xFF) / 255.0f;
                skyG = ((current.skyHex >> 8) & 0xFF) / 255.0f;
                skyB = (current.skyHex & 0xFF) / 255.0f;
            }
        } else {
            fogR = fogG = fogB = 1.0f;

            hasCustomSky = false;
        }

        String startDef = current != null ? String.valueOf(current.fogStart) : "5.0";
        String endDef = current != null ? String.valueOf(current.fogEnd) : "30.0";

        int leftX = centerX - 160;
        int rightX = centerX + 40;
        int topY = centerY - 60;

        this.addRenderableWidget(new ColorSlider(leftX, topY, 120, 20, "Red", fogR, val -> fogR = val.floatValue()));
        this.addRenderableWidget(new ColorSlider(leftX, topY + 25, 120, 20, "Green", fogG, val -> fogG = val.floatValue()));
        this.addRenderableWidget(new ColorSlider(leftX, topY + 50, 120, 20, "Blue", fogB, val -> fogB = val.floatValue()));

        ColorSlider sr = new ColorSlider(rightX, topY, 120, 20, "Red", skyR, val -> skyR = val.floatValue());
        ColorSlider sg = new ColorSlider(rightX, topY + 25, 120, 20, "Green", skyG, val -> skyG = val.floatValue());
        ColorSlider sb = new ColorSlider(rightX, topY + 50, 120, 20, "Blue", skyB, val -> skyB = val.floatValue());

        sr.active = sg.active = sb.active = hasCustomSky;

        this.addRenderableWidget(sr);
        this.addRenderableWidget(sg);
        this.addRenderableWidget(sb);

        this.toggleSkyButton = Button.builder(Component.literal("Custom Sky: " + (hasCustomSky ? "ON" : "OFF")), b -> {
            hasCustomSky = !hasCustomSky;
            b.setMessage(Component.literal("Custom Sky: " + (hasCustomSky ? "ON" : "OFF")));
            sr.active = sg.active = sb.active = hasCustomSky;
            updateLivePreview();
        }).bounds(rightX, topY - 25, 120, 20).build();
        this.addRenderableWidget(this.toggleSkyButton);

        this.fogStartInput = new EditBox(this.font, centerX - 105, centerY + 60, 100, 20, Component.literal("Fog Start"));
        this.fogStartInput.setValue(startDef);
        this.addRenderableWidget(this.fogStartInput);

        this.fogEndInput = new EditBox(this.font, centerX + 5, centerY + 60, 100, 20, Component.literal("Fog End"));
        this.fogEndInput.setValue(endDef);
        this.addRenderableWidget(this.fogEndInput);

        this.addRenderableWidget(Button.builder(Component.literal("Save Settings"), b -> applyAndSave())
                .bounds(centerX - 100, centerY + 90, 200, 20).build());
    }

    private void updateLivePreview() {
        int fogHex = colorToHex(fogR, fogG, fogB);
        int skyHex = hasCustomSky ? colorToHex(skyR, skyG, skyB) : -1;

        try {
            float start = Float.parseFloat(this.fogStartInput.getValue());
            float end = Float.parseFloat(this.fogEndInput.getValue());
            AtmosphereConfigManager.ATMOSPHERES.put(biomeId.toString(), new AtmosphereConfigManager.AtmosphereSettings(fogHex, skyHex, start, end));
        } catch (NumberFormatException ignored) {}
    }

    private int colorToHex(float r, float g, float b) {
        return ((int)(r * 255) << 16) | ((int)(g * 255) << 8) | (int)(b * 255);
    }

    private void applyAndSave() {
        try {
            int fogHex = colorToHex(fogR, fogG, fogB);
            int skyHex = hasCustomSky ? colorToHex(skyR, skyG, skyB) : -1;
            float start = Float.parseFloat(this.fogStartInput.getValue());
            float end = Float.parseFloat(this.fogEndInput.getValue());

            AtmosphereConfigManager.ATMOSPHERES.put(biomeId.toString(), new AtmosphereConfigManager.AtmosphereSettings(fogHex, skyHex, start, end));

            AtmosphereConfigManager.save();

            assert this.minecraft != null;
            this.minecraft.setScreen(null);
        } catch (NumberFormatException ignored) {}
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        updateLivePreview();

        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int leftX = centerX - 160;
        int rightX = centerX + 40;
        int topY = centerY - 60;

        graphics.drawCenteredString(this.font, "Editing Biome: " + biomeId.toString(), centerX, centerY - 100, 0xFFFFFF);
        graphics.drawCenteredString(this.font, "Fog Start", centerX - 55, centerY + 48, 0xA0A0A0);
        graphics.drawCenteredString(this.font, "Fog End", centerX + 55, centerY + 48, 0xA0A0A0);
        graphics.drawCenteredString(this.font, "Fog Color", leftX + 60, topY - 15, 0xFFFFFF);

        drawColorBox(graphics, leftX - 16, topY + 4, 12, 12, (int)(fogR * 255) << 16);
        drawColorBox(graphics, leftX - 16, topY + 29, 12, 12, (int)(fogG * 255) << 8);
        drawColorBox(graphics, leftX - 16, topY + 54, 12, 12, (int)(fogB * 255));

        if (hasCustomSky) {
            drawColorBox(graphics, rightX - 16, topY + 4, 12, 12, (int)(skyR * 255) << 16);
            drawColorBox(graphics, rightX - 16, topY + 29, 12, 12, (int)(skyG * 255) << 8);
            drawColorBox(graphics, rightX - 16, topY + 54, 12, 12, (int)(skyB * 255));
        }

        drawColorBox(graphics, leftX, topY + 80, 120, 20, colorToHex(fogR, fogG, fogB));
        graphics.drawCenteredString(this.font, "Final Fog Output", leftX + 60, topY + 86, invertColor(colorToHex(fogR, fogG, fogB)));

        if (hasCustomSky) {
            drawColorBox(graphics, rightX, topY + 80, 120, 20, colorToHex(skyR, skyG, skyB));
            graphics.drawCenteredString(this.font, "Final Sky Output", rightX + 60, topY + 86, invertColor(colorToHex(skyR, skyG, skyB)));
        }
    }

    private void drawColorBox(GuiGraphics graphics, int x, int y, int width, int height, int hexColor) {
        int colorWithAlpha = 0xFF000000 | hexColor;
        graphics.fill(x, y, x + width, y + height, colorWithAlpha);
        graphics.renderOutline(x - 1, y - 1, width + 2, height + 2, 0xFF888888);
    }

    private int invertColor(int hex) {
        int r = (hex >> 16) & 0xFF;
        int g = (hex >> 8) & 0xFF;
        int b = hex & 0xFF;
        return (r * 0.299 + g * 0.587 + b * 0.114) > 186 ? 0xFF000000 : 0xFFFFFFFF;
    }

    private static class ColorSlider extends AbstractSliderButton {
        private final String prefix;
        private final Consumer<Double> onChange;

        public ColorSlider(int x, int y, int w, int h, String prefix, double initialVal, Consumer<Double> onChange) {
            super(x, y, w, h, Component.empty(), initialVal);
            this.prefix = prefix;
            this.onChange = onChange;
            this.updateMessage();
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Component.literal(prefix + ": " + (int)(this.value * 255)));
        }

        @Override
        protected void applyValue() {
            onChange.accept(this.value);
        }
    }
}