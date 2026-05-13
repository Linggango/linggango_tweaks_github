package com.misanthropy.linggango.linggango_tweaks.mixin.familiarslib;

import com.misanthropy.linggango.linggango_tweaks.fixes.FamiliarsTagRelatedFixes.AllianceResolver;
import com.misanthropy.linggango.linggango_tweaks.fixes.FamiliarsTagRelatedFixes.TagReadinessService;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.entity.mobs.goals.GenericCopyOwnerTargetGoal;
import io.redspace.ironsspellbooks.entity.mobs.goals.GenericOwnerHurtByTargetGoal;
import io.redspace.ironsspellbooks.entity.mobs.goals.GenericOwnerHurtTargetGoal;
import net.alshanex.familiarslib.entity.AbstractSpellCastingPet;
import net.alshanex.familiarslib.util.HelperMethods;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.registries.IForgeRegistry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings("all")
public class FamiliarsTagRelatedFixes {

    @Mixin(AbstractSpellCastingPet.class)
    public static abstract class AllianceMixin {
        @Inject(method = "isAlliedHelper", at = @At("RETURN"), cancellable = true, remap = false)
        private void linggango$resolveAllianceDeterministically(Entity other, CallbackInfoReturnable<Boolean> cir) {
            if (!cir.getReturnValueZ() && AllianceResolver.areAllied((Entity) (Object) this, other)) {
                cir.setReturnValue(true);
            }
        }
    }

    @Mixin(AbstractSpellCastingPet.class)
    public static abstract class TagInitMixin {
        @Unique private boolean linggango$deferredInit;
        @Unique private int linggango$deferredInitArg;
        @Unique private boolean linggango$deferredSafeUpdate;
        @Unique private boolean linggango$deferredSafeUpdateArg;
        @Unique private boolean linggango$deferredUpdate;
        @Unique private boolean linggango$replayDone;
        @Unique private int linggango$seenEpoch = Integer.MIN_VALUE;

        @Invoker(value = "initializeAttackGoal", remap = false)
        protected abstract void linggango$invokeInitializeAttackGoal(int goalIndex);

        @Invoker(value = "updateAttackGoal", remap = false)
        protected abstract void linggango$invokeUpdateAttackGoal();

        @Invoker(value = "updateGoalSafely", remap = false)
        protected abstract void linggango$invokeUpdateGoalSafely(boolean trinketState);

        @Inject(method = "initializeAttackGoal", at = @At("HEAD"), cancellable = true, remap = false)
        private void linggango$deferInitializeAttackGoal(int goalIndex, CallbackInfo ci) {
            linggango$syncEpochState();
            if (linggango$shouldDefer()) {
                linggango$deferredInit = true;
                linggango$deferredInitArg = goalIndex;
                linggango$replayDone = false;
                ci.cancel();
            }
        }

        @Inject(method = "updateAttackGoal", at = @At("HEAD"), cancellable = true, remap = false)
        private void linggango$deferUpdateAttackGoal(CallbackInfo ci) {
            linggango$syncEpochState();
            if (linggango$shouldDefer()) {
                linggango$deferredUpdate = true;
                linggango$replayDone = false;
                ci.cancel();
            }
        }

        @Inject(method = "updateGoalSafely", at = @At("HEAD"), cancellable = true, remap = false)
        private void linggango$deferUpdateGoalSafely(boolean trinketState, CallbackInfo ci) {
            linggango$syncEpochState();
            if (linggango$shouldDefer()) {
                linggango$deferredSafeUpdate = true;
                linggango$deferredSafeUpdateArg = trinketState;
                linggango$replayDone = false;
                ci.cancel();
            }
        }

        @Inject(method = "onAddedToWorld", at = @At("TAIL"), remap = false)
        private void linggango$replayOnAdded(CallbackInfo ci) {
            linggango$attemptReplay();
        }

        @Inject(method = "tick", at = @At("TAIL"))
        private void linggango$replayOnTick(CallbackInfo ci) {
            linggango$attemptReplay();
        }

        @Unique
        private void linggango$attemptReplay() {
            AbstractSpellCastingPet self = (AbstractSpellCastingPet) (Object) this;
            if (self.level() != null && !self.level().isClientSide) {
                linggango$syncEpochState();
                if (TagReadinessService.areSpellTagsReady(self.level()) && !linggango$replayDone) {
                    if (linggango$deferredInit) linggango$invokeInitializeAttackGoal(linggango$deferredInitArg);
                    if (linggango$deferredSafeUpdate) linggango$invokeUpdateGoalSafely(linggango$deferredSafeUpdateArg);
                    if (linggango$deferredUpdate) linggango$invokeUpdateAttackGoal();

                    linggango$deferredInit = false;
                    linggango$deferredSafeUpdate = false;
                    linggango$deferredUpdate = false;
                    linggango$replayDone = true;
                }
            }
        }

        @Unique
        private void linggango$syncEpochState() {
            int currentEpoch = TagReadinessService.getEpoch();
            if (currentEpoch != linggango$seenEpoch) {
                linggango$seenEpoch = currentEpoch;
                linggango$replayDone = false;
            }
        }

        @Unique
        private boolean linggango$shouldDefer() {
            return !TagReadinessService.areSpellTagsReady(((AbstractSpellCastingPet) (Object) this).level());
        }
    }

    @Mixin(value = HelperMethods.class, remap = false)
    public static abstract class HelperTagGuardMixin {
        @Inject(method = "getSpellsFromTag", at = @At("HEAD"), cancellable = true)
        private static void linggango$shortCircuitWhenNotReady(TagKey<AbstractSpell> spellTag, CallbackInfoReturnable<List<AbstractSpell>> cir) {
            try {
                Supplier<?> supplier = SpellRegistry.REGISTRY;
                if (supplier != null && supplier.get() instanceof IForgeRegistry<?> forgeRegistry && forgeRegistry.tags() != null) return;
            } catch (Throwable ignored) {}
            cir.setReturnValue(new ArrayList<>());
        }
    }

    @Mixin(GenericCopyOwnerTargetGoal.class)
    public static abstract class GenericCopyOwnerTargetGoalMixin {
        @Shadow @Final private Supplier<? extends Mob> ownerGetter;

        @Inject(method = "m_8036_()Z", at = @At("RETURN"), cancellable = true, remap = false)
        private void linggango$cancelCanUseIfAllied(CallbackInfoReturnable<Boolean> cir) {
            if (cir.getReturnValueZ()) {
                Mob owner = ownerGetter.get();
                LivingEntity target = owner != null ? owner.getTarget() : null;
                if (AllianceResolver.isFamiliarOrSummon(target) && AllianceResolver.areAllied(owner, target)) cir.setReturnValue(false);
            }
        }

        @Inject(method = "m_8056_()V", at = @At("HEAD"), cancellable = true, remap = false)
        private void linggango$cancelStartIfAllied(CallbackInfo ci) {
            Mob owner = ownerGetter.get();
            LivingEntity target = owner != null ? owner.getTarget() : null;
            if (AllianceResolver.isFamiliarOrSummon(target) && AllianceResolver.areAllied(owner, target)) ci.cancel();
        }
    }

    @Mixin(GenericOwnerHurtByTargetGoal.class)
    public static abstract class GenericOwnerHurtByTargetGoalMixin {
        @Shadow @Final private Mob entity;
        @Shadow @Final private Supplier<? extends LivingEntity> owner;
        @Shadow private LivingEntity ownerLastHurtBy;

        @Inject(method = "m_8036_()Z", at = @At("RETURN"), cancellable = true, remap = false)
        private void linggango$cancelCanUseIfAllied(CallbackInfoReturnable<Boolean> cir) {
            if (cir.getReturnValueZ()) {
                LivingEntity target = ownerLastHurtBy != null ? ownerLastHurtBy : (owner.get() != null ? owner.get().getLastHurtByMob() : null);
                if (AllianceResolver.shouldBlockFriendlyInteraction(entity, target)) cir.setReturnValue(false);
            }
        }

        @Inject(method = "m_8056_()V", at = @At("HEAD"), cancellable = true, remap = false)
        private void linggango$cancelStartIfAllied(CallbackInfo ci) {
            LivingEntity target = ownerLastHurtBy != null ? ownerLastHurtBy : (owner.get() != null ? owner.get().getLastHurtByMob() : null);
            if (AllianceResolver.shouldBlockFriendlyInteraction(entity, target)) {
                entity.setTarget(null);
                ci.cancel();
            }
        }
    }

    @Mixin(GenericOwnerHurtTargetGoal.class)
    public static abstract class GenericOwnerHurtTargetGoalMixin {
        @Shadow @Final private Supplier<? extends LivingEntity> owner;
        @Shadow private LivingEntity ownerLastHurt;

        @Inject(method = "m_8036_()Z", at = @At("RETURN"), cancellable = true, remap = false)
        private void linggango$cancelCanUseIfAllied(CallbackInfoReturnable<Boolean> cir) {
            if (cir.getReturnValueZ()) {
                LivingEntity ownerEntity = owner.get();
                LivingEntity target = ownerLastHurt != null ? ownerLastHurt : (ownerEntity != null ? ownerEntity.getLastHurtMob() : null);
                if (AllianceResolver.isFamiliarOrSummon(target) && AllianceResolver.areAllied(ownerEntity, target)) cir.setReturnValue(false);
            }
        }

        @Inject(method = "m_8056_()V", at = @At("HEAD"), cancellable = true, remap = false)
        private void linggango$cancelStartIfAllied(CallbackInfo ci) {
            LivingEntity ownerEntity = owner.get();
            LivingEntity target = ownerLastHurt != null ? ownerLastHurt : (ownerEntity != null ? ownerEntity.getLastHurtMob() : null);
            if (AllianceResolver.isFamiliarOrSummon(target) && AllianceResolver.areAllied(ownerEntity, target)) ci.cancel();
        }
    }
}