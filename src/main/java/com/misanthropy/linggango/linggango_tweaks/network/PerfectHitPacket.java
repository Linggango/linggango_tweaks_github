package com.misanthropy.linggango.linggango_tweaks.network;

import com.misanthropy.linggango.linggango_tweaks.client.combat.PerfectHitClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PerfectHitPacket {
    public PerfectHitPacket() {}

    public PerfectHitPacket(FriendlyByteBuf buf) {}

    public void encode(FriendlyByteBuf buf) {}

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            PerfectHitClient.triggerPerfectHit();
        });
        ctx.get().setPacketHandled(true);
    }
}