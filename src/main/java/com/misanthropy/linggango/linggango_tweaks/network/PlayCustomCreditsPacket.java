package com.misanthropy.linggango.linggango_tweaks.network;

import com.misanthropy.linggango.linggango_tweaks.client.screen.ModernCreditsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PlayCustomCreditsPacket {
    public PlayCustomCreditsPacket() {
    }

    public PlayCustomCreditsPacket(FriendlyByteBuf buf) {
    }

    public void encode(FriendlyByteBuf buf) {
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                Minecraft.getInstance().setScreen(new ModernCreditsScreen());
            });
        });
        context.setPacketHandled(true);
    }
}