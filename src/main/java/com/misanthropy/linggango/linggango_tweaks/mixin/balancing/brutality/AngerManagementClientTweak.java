package com.misanthropy.linggango.linggango_tweaks.mixin.balancing.brutality;

import net.goo.brutality.event.mod.client.Keybindings;
import net.goo.brutality.registry.BrutalityCapabilities;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.registries.ForgeRegistries;
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
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            LocalPlayer player = mc.player;
            if (player != null) {
                var curioHandler = top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player).orElse(null);

                boolean hasAngerManagement = curioHandler.findFirstCurio(stack -> {
                    var regName = ForgeRegistries.ITEMS.getKey(stack.getItem());
                    return regName != null && regName.getPath().equals("anger_management");
                }).isPresent();

                if (hasAngerManagement) {
                    var rageCap = player.getCapability(BrutalityCapabilities.PLAYER_RAGE_CAP).orElse(null);
                    float currentRage = rageCap.rageValue();

                    if (currentRage < 350.0F) {
                        return false;
                    }
                }
            }
        }
        return isPressed;
    }
}