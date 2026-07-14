package com.misanthropy.linggango.linggango_tweaks.mixin.l2;

import dev.xkmc.l2hostility.content.capability.mob.MobTraitCap;
import dev.xkmc.l2hostility.content.traits.highlevel.ReprintTrait;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class L2HostilityMixins {
    private L2HostilityMixins() {}
}

//@Mixin(value = MasterData.class, remap = false)
//class MasterDataMixin {
//    @Inject(method = "tick", at = @At("HEAD"))
//    private void injectTickHead(MobTraitCap cap, Mob mob, CallbackInfoReturnable<Boolean> cir) {
//        if (mob instanceof Apostle) {
//            ApostleL2Data.pushApostleContext(mob);
//        }
//    }
//
//    @Inject(method = "tick", at = @At("RETURN"))
//    private void injectTickReturn(MobTraitCap cap, Mob mob, CallbackInfoReturnable<Boolean> cir) {
//        if (mob instanceof Apostle) {
//            ApostleL2Data.popApostleContext();
//        }
//    }
//}
//
//@Mixin(value = MasterTrait.class, remap = false)
//class MasterTraitMixin {
//
//    @Inject(method = "allow", at = @At("HEAD"))
//    private void injectApostleContextLookup(LivingEntity le, int difficulty, int maxModLv, CallbackInfoReturnable<Boolean> cir) {
//        if (le instanceof Apostle) {
//            ApostleL2Data.pushApostleContext(le);
//        }
//    }
//
//    @Inject(method = "allow", at = @At("RETURN"))
//    private void injectApostleContextReturn(LivingEntity le, int difficulty, int maxModLv, CallbackInfoReturnable<Boolean> cir) {
//        if (le instanceof Apostle) {
//            ApostleL2Data.popApostleContext();
//        }
//    }
//
//    @Inject(method = "getConfig", at = @At("HEAD"), cancellable = true)
//    private static void injectApostleConfigLookup(EntityType<?> type, CallbackInfoReturnable<EntityConfig.MasterConfig> cir) {
//        LivingEntity current = ApostleL2Data.currentApostle();
//
//        if (current instanceof Apostle && current.getType() == type) {
//            var config = ApostleL2Data.getApostleConfig(current);
//            if (config != null) {
//                cir.setReturnValue(config.asMaster);
//                cir.cancel();
//            }
//        }
//    }
//}

@Mixin(value = MobTraitCap.class, remap = false)
interface MobTraitCapAccessor {
    @Accessor("stage")
    MobTraitCap.Stage blank_mixin_mod$getStage();
}

//@Mixin(value = MobTraitCap.class, remap = false)
//class MobTraitCapMixin {
//
//    @Inject(method = "getConfigCache", at = @At("HEAD"), cancellable = true)
//    private void injectApostleContextLookup(LivingEntity le,
//                                            @NonNull CallbackInfoReturnable<EntityConfig.Config> cir) {
//        if (le instanceof Apostle) {
//            var config = ApostleL2Data.getApostleConfig(le);
//            if (config != null) {
//                ((MobTraitCap) (Object) this).setConfigCache(config);
//                cir.setReturnValue(config);
//                cir.cancel();
//            }
//        }
//    }
//
//    @Inject(method = "tick", at = @At("HEAD"))
//    private void onTickStart(LivingEntity mob, CallbackInfo ci) {
//        if (mob instanceof Apostle) {
//            ApostleL2Data.TICKING_MOBS.add(mob.getUUID());
//        }
//    }
//
//    @Inject(method = "tick", at = @At("RETURN"))
//    private void onTickEnd(LivingEntity mob, CallbackInfo ci) {
//        if (mob instanceof Apostle) {
//            ApostleL2Data.TICKING_MOBS.remove(mob.getUUID());
//            if (ApostleL2Data.PENDING_REINIT.remove(mob.getUUID())) {
//                MobTraitCap cap = (MobTraitCap) (Object) this;
//                if (((MobTraitCapAccessor) cap).blank_mixin_mod$getStage() != MobTraitCap.Stage.POST_INIT) return;
//                ApostleL2Data.doReinit(mob, cap);
//            }
//        }
//    }
//
//    @Redirect(
//            method = "tick",
//            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setHealth(F)V"),
//            remap = true)
//    private void skipTickHealthReset(@NonNull LivingEntity mob, float p_21154_) {
//        boolean shouldSkip = mob instanceof Apostle && ApostleL2Data.SKIP_TICK_HEALTH_RESET.remove(mob.getUUID());
//        if (!shouldSkip) {
//            mob.setHealth(p_21154_);
//        }
//    }
//}

@Mixin(value = ReprintTrait.class)
class ReprintTraitMixin {

    @Redirect(
            method = "onHurtTarget",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;entrySet()Ljava/util/Set;", ordinal = 0),
            remap = false
    )
    private @NonNull Set<Map.Entry<Enchantment, Integer>> filterCursesFromReprintLoop(@NonNull Map<Enchantment, Integer> instance) {
        Set<Map.Entry<Enchantment, Integer>> filteredSet = new HashSet<>();
        for (Map.Entry<Enchantment, Integer> entry : instance.entrySet()) {
            if (!entry.getKey().isCurse()) {
                filteredSet.add(entry);
            }
        }
        return filteredSet;
    }
}

//@Mixin(value = TraitManager.class, remap = false)
//class TraitManagerMixin {
//
//    @Redirect(
//            method = "fill",
//            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setHealth(F)V"),
//            remap = true)
//    private static void skipHealthReset(@NonNull LivingEntity le, float p_21154_) {
//        boolean shouldSkip = le instanceof Apostle && ApostleL2Data.SKIP_HEALTH_RESET.contains(le.getUUID());
//        if (!shouldSkip) {
//            le.setHealth(p_21154_);
//        }