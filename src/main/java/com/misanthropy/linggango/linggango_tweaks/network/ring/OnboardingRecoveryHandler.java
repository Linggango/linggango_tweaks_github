package com.misanthropy.linggango.linggango_tweaks.network.ring;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import com.misanthropy.linggango.linggango_tweaks.client.ClientPacketHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID)
public class OnboardingRecoveryHandler {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        CompoundTag persistentData = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        int setupStage = persistentData.getInt("linggango_setup_stage");

        if (setupStage == 2) {
            com.misanthropy.linggango.linggango_tweaks.network.handler.NetworkHandler.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    OpenRingScreenPacket.INSTANCE
            );
        }
    }

    public static class OpenRingScreenPacket {
        public static final OpenRingScreenPacket INSTANCE = new OpenRingScreenPacket();

        public OpenRingScreenPacket() {}
        public OpenRingScreenPacket(FriendlyByteBuf buf) {}

        public static void encode(OpenRingScreenPacket msg, FriendlyByteBuf buf) {}
        public static OpenRingScreenPacket decode(FriendlyByteBuf buf) {
            return INSTANCE;
        }

        public static void handle(OpenRingScreenPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientPacketHelper::openRingScreen
            ));
            ctx.get().setPacketHandled(true);
        }
    }
}