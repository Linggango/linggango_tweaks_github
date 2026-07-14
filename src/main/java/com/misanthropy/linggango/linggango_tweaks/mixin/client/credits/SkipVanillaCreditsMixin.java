package com.misanthropy.linggango.linggango_tweaks.mixin.client.credits;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class SkipVanillaCreditsMixin {

    @Inject(method = "handleGameEvent", at = @At("HEAD"), cancellable = true)
    private void linggango$killVanillaCredits(ClientboundGameEventPacket packet, CallbackInfo ci) {
        if (packet.getEvent() == ClientboundGameEventPacket.WIN_GAME) {
            ClientPacketListener listener = (ClientPacketListener) (Object) this;
            listener.send(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.PERFORM_RESPAWN));
            ci.cancel();
        }
    }
}