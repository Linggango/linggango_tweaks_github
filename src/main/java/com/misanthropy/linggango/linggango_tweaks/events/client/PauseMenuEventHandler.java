package com.misanthropy.linggango.linggango_tweaks.events.client;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import com.misanthropy.linggango.linggango_tweaks.client.screen.ExtrasMenuScreen;
import com.misanthropy.linggango.linggango_tweaks.util.LinggangoConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jspecify.annotations.NonNull;

@Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PauseMenuEventHandler {
    private static final ResourceLocation EXTRAS_ICON = new ResourceLocation(LinggangoTweaks.MOD_ID, "textures/gui/extras_menu_icon.png");

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();
        if (screen instanceof PauseScreen || screen instanceof TitleScreen) {
            if (LinggangoConfig.isEnabled()) {
                int buttonSize = 20;
                int x = screen.width - buttonSize - 5;

                ImageButton extrasButton = getImageButton(screen, buttonSize, x);
                extrasButton.setTooltip(Tooltip.create(Component.literal("Extras Menu")));
                event.addListener(extrasButton);
            }
        }
    }

    private static @NonNull ImageButton getImageButton(Screen screen, int buttonSize, int x) {
        int y = screen.height - buttonSize - 5;

        return new ImageButton(
                x, y, buttonSize, buttonSize,
                0, 0, 0,
                EXTRAS_ICON,
                buttonSize, buttonSize,
                button -> screen.getMinecraft().setScreen(new ExtrasMenuScreen(screen))
        ) {
            @Override
            public void renderWidget(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                int currentFrame = (int) ((System.currentTimeMillis() / 100) % 7);
                int vOffset = currentFrame * 80;

                guiGraphics.blit(
                        EXTRAS_ICON,
                        this.getX(), this.getY(),
                        this.width, this.height,
                        0, vOffset,
                        79, 80,
                        79, 560
                );
            }
        };
    }
}