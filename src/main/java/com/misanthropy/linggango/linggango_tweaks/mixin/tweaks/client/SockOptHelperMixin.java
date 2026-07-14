package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;

@Mixin(DisconnectedScreen.class)
public class SockOptHelperMixin {

    @Unique
    private static final Component HELPFUL_REASON = Component.empty()
            .append(Component.literal("Oops! We couldn't connect to the server.\n\n").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
            .append(Component.literal("Possible reasons:\n").withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE))
            .append(Component.literal("• The server is currently offline or restarting.\n").withStyle(ChatFormatting.WHITE))
            .append(Component.literal("• You typed the IP address or port incorrectly.\n").withStyle(ChatFormatting.WHITE))
            .append(Component.literal("• Your pc needs a restart (yes, really).\n").withStyle(ChatFormatting.WHITE))
            .append(Component.literal("• Your internet, VPN, or firewall is blocking the connection.").withStyle(ChatFormatting.WHITE));

    @Shadow @Final @Mutable
    private Component reason;

    @Unique
    private boolean linggango$customReasonApplied;

    @Inject(method = "init", at = @At("HEAD"))
    private void linggango$onInit(CallbackInfo ci) {
        if (this.linggango$customReasonApplied || this.reason == null) {
            return;
        }

        String reasonText = this.reason.getString().toLowerCase(Locale.ROOT);

        if (reasonText.contains("connection refused")
                || reasonText.contains("getsockopt")
                || reasonText.contains("timed out")
                || reasonText.contains("unknown host")
                || reasonText.contains("no route to host")
                || reasonText.contains("network is unreachable")) {

            this.reason = HELPFUL_REASON;
            this.linggango$customReasonApplied = true;
        }
    }
}