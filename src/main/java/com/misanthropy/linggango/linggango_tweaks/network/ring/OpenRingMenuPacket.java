package com.misanthropy.linggango.linggango_tweaks.network.ring;

import com.misanthropy.linggango.linggango_tweaks.client.ClientPacketHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class OpenRingMenuPacket {

    public static final OpenRingMenuPacket INSTANCE = new OpenRingMenuPacket();

    public OpenRingMenuPacket() {}
    public OpenRingMenuPacket(FriendlyByteBuf buf) {}

    public static void encode(OpenRingMenuPacket msg, FriendlyByteBuf buf) {}

    public static void handle(OpenRingMenuPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientPacketHelper::openRingScreen
        ));
        ctx.get().setPacketHandled(true);
    }
}