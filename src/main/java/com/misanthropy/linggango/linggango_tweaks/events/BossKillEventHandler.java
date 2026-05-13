package com.misanthropy.linggango.linggango_tweaks.events;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import com.misanthropy.linggango.linggango_tweaks.network.NetworkHandler;
import com.misanthropy.linggango.linggango_tweaks.network.PlayCustomCreditsPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BossKillEventHandler {

    @SubscribeEvent
    public static void onBossDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Zombie && event.getSource().getEntity() instanceof ServerPlayer player) {
            CompoundTag persistentData = player.getPersistentData();
            CompoundTag modData = persistentData.getCompound(Player.PERSISTED_NBT_TAG);
            if (!modData.getBoolean("LinggangoHasSeenCredits")) {
                modData.putBoolean("LinggangoHasSeenCredits", true);
                persistentData.put(Player.PERSISTED_NBT_TAG, modData);
                NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new PlayCustomCreditsPacket());
            }
        }
    }
}