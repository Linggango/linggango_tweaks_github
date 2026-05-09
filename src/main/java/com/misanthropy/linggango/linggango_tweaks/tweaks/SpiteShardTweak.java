package com.misanthropy.linggango.linggango_tweaks.tweaks;

import net.goo.brutality.network.ClientboundSyncCapabilitiesPacket;
import net.goo.brutality.network.PacketHandler;
import net.goo.brutality.registry.BrutalityCapabilities;
import net.goo.brutality.registry.ModAttributes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "linggango_tweaks", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SpiteShardTweak {

    private static final UUID SPITE_SHARD_MAX_RAGE_UUID = UUID.fromString("e2bc1e9c-5cde-4de1-8e3a-60d97d6673d8");
    private static final UUID NERF_CORRECTION_UUID = UUID.fromString("f4bc1e9c-5cde-4de1-8e3a-60d97d6673d9");

    private static final HashMap<UUID, Float> PRE_TICK_RAGE = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) return;

        Player player = event.player;

        handleAttributeNerf(player);

        if (player.tickCount % 20 == 19) {
            captureRage(player);
        } else if (player.tickCount % 20 == 0) {
            applyBalancedGain(player);
        }
    }

    private static void handleAttributeNerf(Player player) {
        AttributeInstance maxRageAttr = player.getAttribute(ModAttributes.MAX_RAGE.get());
        if (maxRageAttr != null) {
            boolean hasOriginalModifier = maxRageAttr.getModifier(SPITE_SHARD_MAX_RAGE_UUID) != null;
            AttributeModifier correction = maxRageAttr.getModifier(NERF_CORRECTION_UUID);

            if (hasOriginalModifier) {
                if (correction == null) {

                    maxRageAttr.addTransientModifier(new AttributeModifier(
                            NERF_CORRECTION_UUID,
                            "Spite Shard Nerf",
                            -40.0D,
                            AttributeModifier.Operation.ADDITION));
                }
            } else if (correction != null) {
                maxRageAttr.removeModifier(NERF_CORRECTION_UUID);
            }
        }
    }

    private static void captureRage(Player player) {
        player.getCapability(BrutalityCapabilities.PLAYER_RAGE_CAP).ifPresent(cap -> PRE_TICK_RAGE.put(player.getUUID(), cap.rageValue()));
    }

    private static void applyBalancedGain(Player player) {
        CuriosApi.getCuriosHelper().findFirstCurio(player, stack ->
                stack.getItem().getClass().getSimpleName().equals("SpiteShard")
        ).ifPresent(slotResult -> player.getCapability(BrutalityCapabilities.PLAYER_RAGE_CAP).ifPresent(cap -> {
            Float oldRage = PRE_TICK_RAGE.remove(player.getUUID());
            if (oldRage != null) {

                List<Mob> aggroEntities = player.level().getNearbyEntities(Mob.class, TargetingConditions.forNonCombat().selector((e) -> e instanceof Mob mob && mob.getTarget() == player), player, player.getBoundingBox().inflate(5.0D));

                if (!aggroEntities.isEmpty()) {
                    float newGain = (float) aggroEntities.size() / 10.0F;
                    cap.setRageValue(oldRage + newGain);

                    PacketHandler.sendToAllClients(new ClientboundSyncCapabilitiesPacket(player.getId(), player));
                }
            }
        }));
    }
}