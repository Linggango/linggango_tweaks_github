package com.misanthropy.linggango.linggango_tweaks.events;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import com.misanthropy.linggango.linggango_tweaks.network.credits_extras.PlayCustomCreditsPacket;
import com.misanthropy.linggango.linggango_tweaks.network.credits_extras.SyncExtrasMenuPacket;
import com.misanthropy.linggango.linggango_tweaks.network.handler.NetworkHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BossKillEventHandler {

    @SubscribeEvent
    public static void onBossDeath(LivingDeathEvent event) {
        ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(event.getEntity().getType());

        if (entityId != null && entityId.toString().equals("goety:apostle")) {

            if (event.getEntity().level().dimension() != Level.NETHER) {
                return;
            }

            CompoundTag entityData = new CompoundTag();
            event.getEntity().saveWithoutId(entityData);

            boolean isApollyon = entityData.getBoolean("isApollyon") || entityData.getByte("isApollyon") == 1;

            if (!isApollyon && entityData.contains("ForgeData")) {
                CompoundTag forgeData = entityData.getCompound("ForgeData");
                isApollyon = forgeData.getBoolean("isApollyon") || forgeData.getByte("isApollyon") == 1;
            }

            if (isApollyon) {
                List<ServerPlayer> targets = new ArrayList<>();

                if (event.getSource().getEntity() instanceof ServerPlayer sp) {
                    targets.add(sp);
                }

                net.minecraft.world.phys.AABB arena = event.getEntity().getBoundingBox().inflate(128.0);
                for (ServerPlayer sp : event.getEntity().level().getEntitiesOfClass(ServerPlayer.class, arena)) {
                    if (!targets.contains(sp)) {
                        targets.add(sp);
                    }
                }

                if (targets.isEmpty() && event.getEntity().level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                    Player nearest = serverLevel.getNearestPlayer(event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), 10000.0, false);
                    if (nearest instanceof ServerPlayer sp) {
                        targets.add(sp);
                    }
                }

                for (ServerPlayer player : targets) {
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
    }

    @SubscribeEvent
    public static void onPlayerLogin(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CompoundTag persistentData = player.getPersistentData();
            CompoundTag modData = persistentData.getCompound(Player.PERSISTED_NBT_TAG);
            if (modData.getBoolean("LinggangoHasSeenCredits")) {
                NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SyncExtrasMenuPacket(true));
            }
        }
    }
}