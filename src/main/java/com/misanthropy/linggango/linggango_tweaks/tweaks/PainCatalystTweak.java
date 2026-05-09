package com.misanthropy.linggango.linggango_tweaks.tweaks;

import net.goo.brutality.event.LivingEntityEventHandler;
import net.goo.brutality.network.ClientboundSyncCapabilitiesPacket;
import net.goo.brutality.network.PacketHandler;
import net.goo.brutality.registry.BrutalityCapabilities;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.HashMap;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "linggango_tweaks", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PainCatalystTweak {
    private static final float RAGE_GAIN_PER_HIT = 0.5F;
    private static final HashMap<UUID, Float> PRE_HURT_RAGE = new HashMap<>();

    @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGHEST)
    public static void onLivingHurtPre(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof Player attacker) captureRage(attacker);
        if (event.getEntity() instanceof Player victim) captureRage(victim);
    }

    @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOWEST)
    public static void onLivingHurtPost(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof Player attacker) applyBalancedRage(attacker);
        if (event.getEntity() instanceof Player victim) applyBalancedRage(victim);
    }

    private static void captureRage(Player player) {
        boolean hasCurio = CuriosApi.getCuriosInventory(player).map(handler ->
                handler.findFirstCurio(stack -> stack.getItem().getClass().getSimpleName().equals("PainCatalyst")).isPresent()
        ).orElse(false);

        if (hasCurio) {
            player.getCapability(BrutalityCapabilities.PLAYER_RAGE_CAP).ifPresent(cap -> PRE_HURT_RAGE.put(player.getUUID(), cap.rageValue()));
        }
    }

    private static void applyBalancedRage(Player player) {
        Float oldRage = PRE_HURT_RAGE.remove(player.getUUID());
        if (oldRage == null) return;

        CuriosApi.getCuriosInventory(player).ifPresent(handler -> handler.findFirstCurio(stack ->
                stack.getItem().getClass().getSimpleName().equals("PainCatalyst")
        ).ifPresent(slotResult -> player.getCapability(BrutalityCapabilities.PLAYER_RAGE_CAP).ifPresent(cap -> {

            float newRage = oldRage + RAGE_GAIN_PER_HIT;
            cap.setRageValue(newRage);

            LivingEntityEventHandler.tryTriggerRage(player, handler, cap);
            PacketHandler.sendToAllClients(new ClientboundSyncCapabilitiesPacket(player.getId(), player));
        })));
    }
}