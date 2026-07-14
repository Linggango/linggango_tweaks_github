//package com.misanthropy.linggango.linggango_tweaks.client.gui;
//
//import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
//import com.misanthropy.linggango.linggango_tweaks.config.DisplayClientConfig;
//import net.minecraft.ChatFormatting;
//import net.minecraft.client.gui.GuiGraphics;
//import net.minecraft.client.gui.components.Button;
//import net.minecraft.client.gui.screens.Screen;
//import net.minecraft.network.chat.Component;
//import net.minecraftforge.api.distmarker.Dist;
//import net.minecraftforge.fml.common.Mod;
//import org.jspecify.annotations.NonNull;
//
//@Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
//public class OculusWarningScreen extends Screen {
//    private final Screen previousScreen;
//    private int delayTicks = 100;
//    private Button acceptButton;
//
//    public OculusWarningScreen(Screen previousScreen) {
//        super(Component.literal("Oculus Warning"));
//        this.previousScreen = previousScreen;
//    }
//
//    @Override
//    protected void init() {
//        this.acceptButton = this.addRenderableWidget(Button.builder(
//                Component.literal("I understand the risks (5)"),
//                (btn) -> {
//                    DisplayClientConfig.ACCEPTED_OCULUS_WARNING.set(true);
//                    DisplayClientConfig.SPEC.save();
//                    assert this.minecraft != null;
//                    this.minecraft.setScreen(this.previousScreen);
//                }
//        ).bounds(this.width / 2 - 100, this.height - 50, 200, 20).build());
//
//        this.acceptButton.active = false;
//    }
//
//    @Override
//    public void tick() {
//        if (this.delayTicks > 0) {
//            this.delayTicks--;
//            if (this.delayTicks % 20 == 0) {
//                this.acceptButton.setMessage(Component.literal("I understand the risks (" + (this.delayTicks / 20) + ")"));
//            }
//            if (this.delayTicks == 0) {
//                this.acceptButton.active = true;
//                this.acceptButton.setMessage(Component.literal("I understand the risks"));
//            }
//        }
//    }
//
//    @Override
//    public void render(@NonNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
//        this.renderDirtBackground(graphics);
//        graphics.drawCenteredString(this.font, Component.literal("Oculus is Installed").withStyle(ChatFormatting.RED, ChatFormatting.BOLD), this.width / 2, 40, 0xFFFFFF);
//        int startY = 80;
//        graphics.drawCenteredString(this.font, Component.literal("Linggango does not officially support shaders. By installing Oculus, you assume the following risks:"), this.width / 2, startY, 0xCCCCCC);
//        startY += 25;
//        graphics.drawString(this.font, Component.literal("• \uD83D\uDCC9 Severe performance degradation"), this.width / 2 - 120, startY, 0xFFAA00);
//        startY += 15;
//        graphics.drawString(this.font, Component.literal("• \uD83D\uDCA5 Increased likelihood of crashes"), this.width / 2 - 120, startY, 0xFFAA00);
//        startY += 15;
//        graphics.drawString(this.font, Component.literal("• \uD83D\uDC1B Unintended visual bugs and rendering glitches"), this.width / 2 - 120, startY, 0xFFAA00);
//        startY += 15;
//        graphics.drawString(this.font, Component.literal("• ⚙ General instability"), this.width / 2 - 120, startY, 0xFFAA00);
//        startY += 30;
//        graphics.drawCenteredString(this.font, Component.literal("While basic support remains active as a courtesy, we strongly advise caution."), this.width / 2, startY, 0xCCCCCC);
//
//        super.render(graphics, mouseX, mouseY, partialTick);
//    }
//}