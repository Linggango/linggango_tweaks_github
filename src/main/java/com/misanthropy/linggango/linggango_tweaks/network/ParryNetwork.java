package com.misanthropy.linggango.linggango_tweaks.network;

import com.misanthropy.linggango.linggango_tweaks.server.ParryServerHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

public class ParryNetwork {
    private static final String PROTOCOL_VERSION = "2";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("linggango_tweaks", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;
        CHANNEL.messageBuilder(C2SParryPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(C2SParryPacket::new)
                .encoder(C2SParryPacket::toBytes)
                .consumerMainThread(C2SParryPacket::handle)
                .add();

        CHANNEL.messageBuilder(S2CParrySuccessPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(S2CParrySuccessPacket::new)
                .encoder(S2CParrySuccessPacket::toBytes)
                .consumerMainThread(S2CParrySuccessPacket::handle)
                .add();

        CHANNEL.messageBuilder(S2CParryStartPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(S2CParryStartPacket::new)
                .encoder(S2CParryStartPacket::toBytes)
                .consumerMainThread(S2CParryStartPacket::handle)
                .add();
    }

    public static class C2SParryPacket {
        public C2SParryPacket() {}

        public C2SParryPacket(FriendlyByteBuf buf) {}

        public void toBytes(FriendlyByteBuf buf) {}

        public void handle(Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context context = supplier.get();
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player != null) {
                    ParryServerHandler.activeParries.put(player.getUUID(), player.level().getGameTime());
                    CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> player), new S2CParryStartPacket(player.getId()));
                }
            });
            context.setPacketHandled(true);
        }
    }

    public static class S2CParryStartPacket {
        private final int entityId;

        public S2CParryStartPacket(int entityId) {
            this.entityId = entityId;
        }

        public S2CParryStartPacket(FriendlyByteBuf buf) {
            this.entityId = buf.readInt();
        }

        public void toBytes(FriendlyByteBuf buf) {
            buf.writeInt(this.entityId);
        }

        public void handle(Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context context = supplier.get();
            context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.misanthropy.linggango.linggango_tweaks.client.ParryEffects.triggerParryStartForOther(this.entityId)));
            context.setPacketHandled(true);
        }
    }

    public static class S2CParrySuccessPacket {
        private final int entityId;
        private final int tier;

        public S2CParrySuccessPacket(int entityId, int tier) {
            this.entityId = entityId;
            this.tier = tier;
        }

        public S2CParrySuccessPacket(FriendlyByteBuf buf) {
            this.entityId = buf.readInt();
            this.tier = buf.readInt();
        }

        public void toBytes(FriendlyByteBuf buf) {
            buf.writeInt(this.entityId);
            buf.writeInt(this.tier);
        }

        public void handle(Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context context = supplier.get();
            context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.misanthropy.linggango.linggango_tweaks.client.ParryEffects.triggerSuccessfulParry(this.entityId, this.tier)));
            context.setPacketHandled(true);
        }
    }
}