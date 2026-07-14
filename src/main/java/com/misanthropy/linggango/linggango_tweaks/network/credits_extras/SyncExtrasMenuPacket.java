package com.misanthropy.linggango.linggango_tweaks.network.credits_extras;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncExtrasMenuPacket {
    private final boolean unlocked;

    public SyncExtrasMenuPacket(boolean unlocked) {
        this.unlocked = unlocked;
    }

    public SyncExtrasMenuPacket(FriendlyByteBuf buf) {
        this.unlocked = buf.readBoolean();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(this.unlocked);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                com.misanthropy.linggango.linggango_tweaks.client.ClientPacketHelper.handleSyncExtras(this.unlocked)
        ));
        context.setPacketHandled(true);
    }
}