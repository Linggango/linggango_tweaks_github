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
public class BloodHowlPendantTweak {

    private static final float BALANCED_BOOST_MULTIPLIER = 0.10F;
    private static final HashMap<UUID, Float> PRE_HIT_RAGE = new HashMap<>();

    @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGHEST)
    public static void onLivingHurtPre(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof Player attacker) trackRage(attacker);
        if (event.getEntity() instanceof Player victim) trackRage(victim);
    }

    @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOWEST)
    public static void onLivingHurtPost(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof Player attacker) applyBalancedPendantRage(attacker);
        if (event.getEntity() instanceof Player victim) applyBalancedPendantRage(victim);
    }

    private static void trackRage(Player player) {
        boolean hasCurio = CuriosApi.getCuriosInventory(player).map(handler ->
                handler.findFirstCurio(stack -> stack.getItem().getClass().getSimpleName().equals("BloodHowlPendant")).isPresent()
        ).orElse(false);

        if (hasCurio) {
            player.getCapability(BrutalityCapabilities.PLAYER_RAGE_CAP).ifPresent(cap -> PRE_HIT_RAGE.put(player.getUUID(), cap.rageValue()));
        }
    }

    private static void applyBalancedPendantRage(Player player) {
        Float oldRage = PRE_HIT_RAGE.remove(player.getUUID());
        if (oldRage == null) return;

        CuriosApi.getCuriosInventory(player).ifPresent(handler -> handler.findFirstCurio(stack ->
                stack.getItem().getClass().getSimpleName().equals("BloodHowlPendant")
        ).ifPresent(slotResult -> player.getCapability(BrutalityCapabilities.PLAYER_RAGE_CAP).ifPresent(cap -> {

            if (player.getHealth() / player.getMaxHealth() < 0.5F) {
                float currentGain = (cap.rageValue() - oldRage);

                float baseGainStripped = currentGain / 1.5F;
                float newBalancedGain = baseGainStripped * (1.0F + BALANCED_BOOST_MULTIPLIER);

                cap.setRageValue(oldRage + newBalancedGain);

                LivingEntityEventHandler.tryTriggerRage(player, handler, cap);
                PacketHandler.sendToAllClients(new ClientboundSyncCapabilitiesPacket(player.getId(), player));
            }
        })));
    }
}