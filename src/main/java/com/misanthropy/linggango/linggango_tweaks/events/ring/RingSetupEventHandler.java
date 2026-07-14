package com.misanthropy.linggango.linggango_tweaks.events.ring;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "linggango_tweaks")
public class RingSetupEventHandler {

    private static final String STAGE_KEY = "linggango_setup_stage";

    private static boolean isInSetup(Player player) {
        if (player.getPersistentData().contains(Player.PERSISTED_NBT_TAG)) {
            int stage = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getInt(STAGE_KEY);
            return stage < 3;
        }
        return false;
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;
        Player original = event.getOriginal();
        original.reviveCaps();
        try {
            CompoundTag oldData = original.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
            if (oldData.contains(STAGE_KEY)) {
                CompoundTag newData = event.getEntity().getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
                newData.putInt(STAGE_KEY, oldData.getInt(STAGE_KEY));
                event.getEntity().getPersistentData().put(Player.PERSISTED_NBT_TAG, newData);
            }
        } finally {
            original.invalidateCaps();
        }
    }

    @SubscribeEvent
    public static void onPlayerAttacked(LivingAttackEvent event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide()) {
            if (isInSetup(player)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START && !event.player.level().isClientSide()) {
            Player player = event.player;
            if (player.tickCount % 4 == 0 && isInSetup(player)) {
                if (player.isOnFire()) player.clearFire();
                if (player.fallDistance > 0f) player.fallDistance = 0f;
                if (player.getAirSupply() < player.getMaxAirSupply()) player.setAirSupply(player.getMaxAirSupply());
                if (player.getFoodData().getFoodLevel() < 20) player.getFoodData().setFoodLevel(20);
                if (player.getHealth() < player.getMaxHealth()) player.setHealth(player.getMaxHealth());
            }
        }
    }
}