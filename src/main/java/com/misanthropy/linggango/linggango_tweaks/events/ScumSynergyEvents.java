package com.misanthropy.linggango.linggango_tweaks.events;

import com.misanthropy.linggango.class_enhancement.ClassEnhancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;

@Mod.EventBusSubscriber(modid = "linggango_tweaks")
public class ScumSynergyEvents {
    public static boolean isScumClass(LivingEntity entity) {
        if (entity instanceof ServerPlayer sp) {
            ClassEnhancement.ClassSavedData data = ClassEnhancement.ClassSavedData.get(sp.serverLevel());
            return "scum".equals(data.playerClasses.get(sp.getUUID()));
        } else if (entity.level().isClientSide && entity instanceof Player p) {

            return DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> {
                if (p.getUUID().equals(net.minecraft.client.Minecraft.getInstance().player.getUUID())) {
                    try {
                        Class<?> clazz = Class.forName("com.misanthropy.linggango.class_enhancement.client.ClientAccess");
                        java.lang.reflect.Field field = clazz.getDeclaredField("clientClassId");
                        return "scum".equals(field.get(null));
                    } catch (Exception e) { return false; }
                }
                return false;
            });
        }
        return false;
    }
    public static boolean hasCursedRing(LivingEntity entity) {
        if (!ModList.get().isLoaded("enigmaticlegacy") || !ModList.get().isLoaded("curios")) return false;

        return CuriosApi.getCuriosHelper().findFirstCurio(entity, stack -> {
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
            return id != null && id.toString().equals("enigmaticlegacy:cursed_ring");
        }).isPresent();
    }

    public static boolean isScumWithRing(LivingEntity entity) {
        return isScumClass(entity) && hasCursedRing(entity);
    }
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.getEntity().level().isClientSide) return;
        if (isScumWithRing(event.getEntity())) {
            event.setAmount(event.getAmount() * 0.6f);
        }
        if (event.getSource().getEntity() instanceof LivingEntity attacker && isScumWithRing(attacker)) {
            event.setAmount(event.getAmount() * 1.5f);
            LivingEntity target = event.getEntity();
            target.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1));
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 1));
        }
    }
    @SubscribeEvent
    public static void onTargetChange(LivingChangeTargetEvent event) {
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