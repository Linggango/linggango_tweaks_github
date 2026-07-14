package com.misanthropy.linggango.linggango_tweaks.mixin.goety;

import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistries;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

public final class GoetyMixins {
    private GoetyMixins() {}
}

//@SuppressWarnings("unused")
//@Pseudo
//@Mixin(value = Apostle.class, remap = false)
//abstract class ApostleMixin extends LivingEntity {
//    @Unique
//    boolean linggango_tweaks$isActuallyHurting = false;
//
//    protected ApostleMixin(EntityType<? extends LivingEntity> p_20966_, Level p_20967_) {
//        super(p_20966_, p_20967_);
//    }
//
//    @Inject(
//            method = "actuallyHurt",
//            at = @org.spongepowered.asm.mixin.injection.At("HEAD"),
//            remap = true
//    )
//    private void linggango_tweaks$onActuallyHurt(DamageSource source, float amount, CallbackInfo ci) {
//        if (!source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
//            linggango_tweaks$isActuallyHurting = true;
//        }
//    }
//
//    /**
//     * @author SaloEater
//     * @reason Intercept and cap the final health modification value to fix apostle damage cap issues.
//     */
//    @ModifyVariable(
//            method = "m_21153_", // set health
//            at = @At("HEAD"),
//            argsOnly = true,
//            remap = true
//    )
//    private float linggango_tweaks$capDamageOnSetHealth(float health) {
//        if (linggango_tweaks$isActuallyHurting && TweaksConfig.APOSTLE_DAMAGE_CAP_FIX.get()) {
//            linggango_tweaks$isActuallyHurting = false;
//            var actuallyHurtAmount = this.getHealth() - health;
//            if (actuallyHurtAmount > 0) {
//                return this.getHealth() - Math.min(actuallyHurtAmount, AttributesConfig.ApostleDamageCap.get().floatValue());
//            }
//        }
//        return health;
//    }
//}

@SuppressWarnings("removal")
@Mixin(targets = "com.Polarice3.Goety.utils.BlockFinder", remap = false)
class GoetyBlockFinderPatchMixin {

    @Unique
    private static final ResourceLocation HUNTING_DENIAL_RL = new ResourceLocation("goetydelight", "hunting_denial");
    @Unique
    private static MobEffect linggango_tweaks$huntingDenialEffect;
    @Unique
    private static boolean linggango_tweaks$checkedHuntingDenial = false;

    @Inject(method = "findIllagerWard*", at = @At("HEAD"), cancellable = true, remap = false)
    private static void patchIllagerWard(@NonNull ServerLevel serverLevel, @NonNull BlockPos blockPos, int soulEnergy, @NonNull CallbackInfoReturnable<Boolean> cir) {
        Player player = serverLevel.getNearestPlayer(blockPos.getX(), blockPos.getY(), blockPos.getZ(), 32.0D, false);

        if (player != null) {
            if (!linggango_tweaks$checkedHuntingDenial) {
                linggango_tweaks$huntingDenialEffect = ForgeRegistries.MOB_EFFECTS.getValue(HUNTING_DENIAL_RL);
                linggango_tweaks$checkedHuntingDenial = true;
            }
            MobEffect huntingDenial = linggango_tweaks$huntingDenialEffect;

            if (huntingDenial != null && player.hasEffect(huntingDenial)) {
                cir.setReturnValue(true);
            }
        }
    }
}

@Mixin(targets = "net.v_black_cat.goetydelight.entities.ai.customer.CustomerAi", remap = false)
class GoetyCustomerAiPatchMixin {

    @Unique
    private static Dynamic<?> linggango_tweaks$emptyMemoriesDynamic;

    @ModifyVariable(
            method = "makeBrain(Lnet/minecraft/world/entity/PathfinderMob;Lcom/mojang/serialization/Dynamic;)Lnet/minecraft/world/entity/ai/Brain;",
            at = @At("HEAD"),
            argsOnly = true,
            remap = false
    )
    private static @NonNull Dynamic<?> interceptMakeBrainCrash(@Nullable Dynamic<?> dynamic) {
        if (dynamic == null || dynamic.getValue() == null || dynamic.getValue() instanceof net.minecraft.nbt.EndTag) {
            if (linggango_tweaks$emptyMemoriesDynamic == null) {
                NbtOps nbtops = NbtOps.INSTANCE;
                linggango_tweaks$emptyMemoriesDynamic = new Dynamic<>(nbtops, nbtops.createMap(Map.of(nbtops.createString("memories"), nbtops.emptyMap())));
            }
            return linggango_tweaks$emptyMemoriesDynamic;
        }

        return dynamic;
    }
}