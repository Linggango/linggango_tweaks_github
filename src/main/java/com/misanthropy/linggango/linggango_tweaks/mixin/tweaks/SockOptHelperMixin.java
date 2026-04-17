package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DisconnectedScreen.class)
public class SockOptHelperMixin {

    @Shadow @Final @Mutable
    private Component reason;

    @Inject(method = "init", at = @At("HEAD"))
    private void linggango$onInit(CallbackInfo ci) {
        String reasonText = this.reason.getString();
        if (reasonText.contains("Connection refused") || reasonText.contains("getsockopt")) {

            this.reason = Component.literal("Oops! We couldn't connect to the server.\n\n")
                    .append(Component.literal("Possible reasons:\n").withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal("• The server is currently offline or restarting.\n"))
                    .append(Component.literal("• You typed the IP address or port incorrectly.\n"))
                    .append(Component.literal("• Your internet or firewall is blocking the connection."));
        }
    }
}