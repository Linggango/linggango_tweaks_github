package com.misanthropy.linggango.linggango_tweaks.mixin.balancing.brutality;

import net.goo.brutality.event.mod.client.Keybindings;
import net.goo.brutality.registry.BrutalityCapabilities;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.goo.brutality.event.forge.client.ForgeClientPlayerStateHandler", remap = false)
public abstract class AngerManagementClientTweak {

    @Redirect(
            method = "onKeyPressed",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;m_90859_()Z"),
            remap = false
    )
    private static boolean linggango$requireRageThreshold(KeyMapping instance) {
        boolean isPressed = instance.isDown();

        if (isPressed && instance == Keybindings.RAGE_ACTIVATE_KEY) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                boolean hasAngerManagement = top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player)
                        .map(handler -> handler.findFirstCurio(stack -> stack.getItem().getClass().getSimpleName().equals("AngerManagement")).isPresent())
                        .orElse(false);

                if (hasAngerManagement) {
                    float currentRage = player.getCapability(BrutalityCapabilities.PLAYER_RAGE_CAP)
                            .map(cap -> cap.rageValue())
                            .orElse(0.0F);

                    if (currentRage < 350.0F) {
                        return false;
                    }
                }
            }
        }
        return isPressed;
    }
}