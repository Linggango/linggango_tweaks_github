package com.misanthropy.linggango.linggango_tweaks.network.handler;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import com.misanthropy.linggango.linggango_tweaks.network.combat.PerfectHitPacket;
import com.misanthropy.linggango.linggango_tweaks.network.credits_extras.PlayCustomCreditsPacket;
import com.misanthropy.linggango.linggango_tweaks.network.credits_extras.SyncExtrasMenuPacket;
import com.misanthropy.linggango.linggango_tweaks.network.macabre.PitTeleportPacket;
import com.misanthropy.linggango.linggango_tweaks.network.ring.OnboardingRecoveryHandler;
import com.misanthropy.linggango.linggango_tweaks.network.ring.OpenRingMenuPacket;
import com.misanthropy.linggango.linggango_tweaks.network.ring.RingSelectionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;


@SuppressWarnings("all")
public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(LinggangoTweaks.MOD_ID, "main_channel"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, PerfectHitPacket.class, PerfectHitPacket::encode, PerfectHitPacket::new, PerfectHitPacket::handle);
        CHANNEL.registerMessage(id++, PlayCustomCreditsPacket.class, PlayCustomCreditsPacket::encode, PlayCustomCreditsPacket::new, PlayCustomCreditsPacket::handle);
        CHANNEL.registerMessage(id++, SyncExtrasMenuPacket.class, SyncExtrasMenuPacket::encode, SyncExtrasMenuPacket::new, SyncExtrasMenuPacket::handle);
        CHANNEL.registerMessage(id++, PitTeleportPacket.class, PitTeleportPacket::encode, PitTeleportPacket::new, PitTeleportPacket::handle);
        CHANNEL.registerMessage(id++, RingSelectionPacket.class, RingSelectionPacket::encode, RingSelectionPacket::new, RingSelectionPacket::handle);
        CHANNEL.registerMessage(id++, OpenRingMenuPacket.class, OpenRingMenuPacket::encode, OpenRingMenuPacket::new, OpenRingMenuPacket::handle);
        CHANNEL.registerMessage(id++, OnboardingRecoveryHandler.OpenRingScreenPacket.class,
                OnboardingRecoveryHandler.OpenRingScreenPacket::encode, OnboardingRecoveryHandler.OpenRingScreenPacket::decode, OnboardingRecoveryHandler.OpenRingScreenPacket::handle, Optional.empty());
    }
}