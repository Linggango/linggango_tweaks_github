package com.misanthropy.linggango.linggango_tweaks.ally;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jspecify.annotations.NonNull;

import java.util.*;

@Mod.EventBusSubscriber
public class AllyManager {

    public static final Map<UUID, Alliance> ALLIANCES = new HashMap<>();
    public static final Map<UUID, UUID> PENDING_INVITES = new HashMap<>();
    public static final Map<UUID, RequestData> PENDING_REQUESTS = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerAttack(@NonNull LivingAttackEvent event) {
        if (event.getEntity() instanceof ServerPlayer victim && event.getSource().getEntity() instanceof ServerPlayer attacker) {
            if (victim.getUUID().equals(attacker.getUUID())) return;

            Alliance victimAlliance = ALLIANCES.get(victim.getUUID());
            Alliance attackerAlliance = ALLIANCES.get(attacker.getUUID());

            if (victimAlliance != null && victimAlliance == attackerAlliance && victimAlliance.isFriendlyFire()) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.@NonNull Added event) {
        if (event.getEntity() instanceof ServerPlayer victim && event.getEffectSource() instanceof ServerPlayer attacker) {
            if (victim.getUUID().equals(attacker.getUUID())) return;

            Alliance victimAlliance = ALLIANCES.get(victim.getUUID());
            Alliance attackerAlliance = ALLIANCES.get(attacker.getUUID());

            if (victimAlliance != null && victimAlliance == attackerAlliance && victimAlliance.isFriendlyFire()) {
                if (event.getEffectInstance().getEffect().getCategory() == MobEffectCategory.HARMFUL) {
                    victim.removeEffect(event.getEffectInstance().getEffect());
                }
            }
        }
    }

    public static class Alliance {
        private final UUID id = UUID.randomUUID();
        private final Set<UUID> members = new HashSet<>();
        private boolean friendlyFire = false;

        public @NonNull UUID getId() {
            return id;
        }

        public void addMember(UUID uuid) {
            members.add(uuid);
        }

        public @NonNull Set<UUID> getMembers() {
            return members;
        }

        public boolean isFriendlyFire() {
            return !friendlyFire;
        }

        public void setFriendlyFire(boolean friendlyFire) {
            this.friendlyFire = friendlyFire;
        }
    }

    public record RequestData(UUID requester, String type, int amount) {
    }
}