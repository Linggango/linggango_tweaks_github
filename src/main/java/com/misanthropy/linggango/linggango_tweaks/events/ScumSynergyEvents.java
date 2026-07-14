package com.misanthropy.linggango.linggango_tweaks.events;

import com.misanthropy.linggango.class_enhancement.ClassEnhancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.jspecify.annotations.NonNull;
import top.theillusivec4.curios.api.CuriosApi;

@Mod.EventBusSubscriber(modid = "linggango_tweaks")
public class ScumSynergyEvents {

    private static final boolean ENIGMATIC_LEGACY_LOADED = ModList.get().isLoaded("enigmaticlegacy");
    private static final boolean CURIOS_LOADED = ModList.get().isLoaded("curios");
    private static final ResourceLocation CURSED_RING = new ResourceLocation("enigmaticlegacy", "cursed_ring");

    private static java.lang.reflect.Field cachedClientClassIdField;
    private static boolean reflectionResolved = false;

    private static Item cachedCursedRingItem;
    private static boolean cursedRingResolved = false;

    public static boolean isScumClass(LivingEntity entity) {
        if (entity == null) {
            return false;
        }

        if (entity instanceof ServerPlayer sp) {
            ClassEnhancement.ClassSavedData data = ClassEnhancement.ClassSavedData.get(sp.serverLevel());
            return "scum".equals(data.playerClasses.get(sp.getUUID()));
        } else if (entity.level().isClientSide && entity instanceof Player p) {
            return DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> {
                assert net.minecraft.client.Minecraft.getInstance().player != null;
                if (p.getUUID().equals(net.minecraft.client.Minecraft.getInstance().player.getUUID())) {
                    if (!reflectionResolved) {
                        try {
                            Class<?> clazz = Class.forName("com.misanthropy.linggango.class_enhancement.client.ClientAccess");
                            cachedClientClassIdField = clazz.getDeclaredField("clientClassId");
                        } catch (Exception ignored) {
                        }
                        reflectionResolved = true;
                    }
                    if (cachedClientClassIdField != null) {
                        try {
                            return "scum".equals(cachedClientClassIdField.get(null));
                        } catch (Exception ignored) {
                        }
                    }
                }
                return false;
            });
        }
        return false;
    }

    public static boolean hasCursedRing(LivingEntity entity) {

        if (entity == null) { // fuckyou
            return false;
        }

        if (!ENIGMATIC_LEGACY_LOADED || !CURIOS_LOADED) return false;
        if (!cursedRingResolved) {
            cachedCursedRingItem = ForgeRegistries.ITEMS.getValue(CURSED_RING);
            cursedRingResolved = true;
        }
        if (cachedCursedRingItem == null) return false;
        return CuriosApi.getCuriosHelper().findFirstCurio(entity, stack -> stack.is(cachedCursedRingItem)).isPresent();
    }

    public static boolean isScumWithRing(LivingEntity entity) {
        return isScumClass(entity) && hasCursedRing(entity);
    }

    @SubscribeEvent
    public static void onLivingDamage(@NonNull LivingDamageEvent event) {
        if (event.getEntity().level().isClientSide) return;
        LivingEntity victim = event.getEntity();
        boolean victimIsScum = isScumWithRing(victim);

        if (victimIsScum) {
            event.setAmount(event.getAmount() * 0.6f);
        }
        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            boolean attackerIsScum = (attacker == victim) ? victimIsScum : isScumWithRing(attacker);
            if (attackerIsScum) {
                event.setAmount(event.getAmount() * 1.5f);
                victim.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1));
                victim.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 1));
            }
        }
    }

    @SubscribeEvent
    public static void onTargetChange(@NonNull LivingChangeTargetEvent event) {
        if (event.getEntity().level().isClientSide) return;

        LivingEntity newTarget = event.getNewTarget();
        if (newTarget != null && isScumWithRing(newTarget)) {
            LivingEntity mob = event.getEntity();
            if (mob.getLastHurtByMob() == newTarget) return;
            if (mob.getRandom().nextFloat() < 0.5f) {
                event.setCanceled(true);
            }
        }
    }
}