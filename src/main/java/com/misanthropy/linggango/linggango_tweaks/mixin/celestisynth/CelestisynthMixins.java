package com.misanthropy.linggango.linggango_tweaks.mixin.celestisynth;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.thecelestialworkshop.celestisynth.common.attack.aquaflora.AquafloraBlastOffAttack;
import org.thecelestialworkshop.celestisynth.common.attack.aquaflora.AquafloraFlowersAwayAttack;
import org.thecelestialworkshop.celestisynth.common.attack.aquaflora.AquafloraPetalPiercesAttack;
import org.thecelestialworkshop.celestisynth.common.attack.aquaflora.AquafloraSlashFrenzyAttack;
import org.thecelestialworkshop.celestisynth.common.attack.breezebreaker.*;
import org.thecelestialworkshop.celestisynth.common.attack.cresentia.CrescentiaBarrageAttack;
import org.thecelestialworkshop.celestisynth.common.attack.cresentia.CrescentiaDragonAttack;
import org.thecelestialworkshop.celestisynth.common.entity.base.CSEffectEntity;
import org.thecelestialworkshop.celestisynth.common.entity.helper.CSVisualType;
import org.thecelestialworkshop.celestisynth.common.entity.mob.misc.RainfallTurret;
import org.thecelestialworkshop.celestisynth.common.entity.projectile.RainfallArrow;
import org.thecelestialworkshop.celestisynth.common.entity.skillcast.SkillCastBreezebreakerTornado;
import org.thecelestialworkshop.celestisynth.common.item.weapons.BreezebreakerItem;
import org.thecelestialworkshop.celestisynth.common.registry.CSAttributes;
import org.thecelestialworkshop.celestisynth.common.registry.CSVisualTypes;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class CelestisynthMixins {
    private CelestisynthMixins() {}
}

@Mixin(value = {
        AquafloraBlastOffAttack.class,
        AquafloraFlowersAwayAttack.class,
        AquafloraPetalPiercesAttack.class,
        AquafloraSlashFrenzyAttack.class
}, remap = false)
abstract class AquafloraCooldownMixin {

    @Inject(method = "getCooldown", at = @At("HEAD"), cancellable = true, remap = false)
    private void tweakAquafloraCooldowns(@NonNull CallbackInfoReturnable<Integer> cir) {
        Object self = this;

        if (self instanceof AquafloraBlastOffAttack) {
            cir.setReturnValue(60);
        } else if (self instanceof AquafloraFlowersAwayAttack) {
            cir.setReturnValue(200);
        } else if (self instanceof AquafloraPetalPiercesAttack) {
            cir.setReturnValue(60);
        } else if (self instanceof AquafloraSlashFrenzyAttack) {
            cir.setReturnValue(400);
        }
    }

    @Inject(method = "tickAttack", at = @At("HEAD"), cancellable = true, remap = false)
    private void reduceFencingHits(@NonNull CallbackInfo ci) {
        if ((Object) this instanceof AquafloraPetalPiercesAttack) {
            try {
                int t = (int) this.getClass().getMethod("getTimerProgress").invoke(this);
                if (t != 0 && t != 2 && t != 5 && t != 7 && t != 10 && t != 12 && t != 15) {
                    ci.cancel();
                }
            } catch (Exception ignored) {
            }
        }
    }
}

@SuppressWarnings("all")
@Mixin(value = {
        BreezebreakerGalestormAttack.class,
        BreezebreakerDualGalestormAttack.class,
        BreezebreakerWheelAttack.class,
        BreezebreakerWhirlwindAttack.class,
        BreezebreakerWindRoarAttack.class
}, remap = false)
abstract class BreezebreakerCooldownMixin {

    @Inject(method = "getCooldown", at = @At("HEAD"), cancellable = true, remap = false)
    private void tweakBreezebreakerCooldowns(@NonNull CallbackInfoReturnable<Integer> cir) {
        Object self = this;

        if (self instanceof BreezebreakerAttack attack) {
            if (self instanceof BreezebreakerGalestormAttack || self instanceof BreezebreakerDualGalestormAttack) {
                cir.setReturnValue(attack.buffStateModified(80));
            }
            else if (self instanceof BreezebreakerWhirlwindAttack) {
                cir.setReturnValue(attack.buffStateModified(120));
            }
            else if (self instanceof BreezebreakerWindRoarAttack) {
                cir.setReturnValue(attack.buffStateModified(60));
            }
            else if (self instanceof BreezebreakerWheelAttack) {
                cir.setReturnValue(attack.buffStateModified(80));
            }
        }
    }
}

@Mixin(value = BreezebreakerItem.class, remap = false)
class BreezebreakerPassiveMixin {

    @Redirect(
            method = "onPlayerHurt",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/event/entity/living/LivingHurtEvent;setAmount(F)V",
                    remap = false
            ),
            remap = false
    )
    private void nerfPassiveVulnerability(@NonNull LivingHurtEvent instance, float amount) {
        instance.setAmount(instance.getAmount() * 1.20F);
    }
}

@Mixin(value = BreezebreakerWhirlwindAttack.class, remap = false)
class BreezebreakerWhirlwindDamageMixin {

    @Redirect(
            method = "tickAttack",
            at = @At(
                    value = "FIELD",
                    target = "Lorg/thecelestialworkshop/celestisynth/common/entity/skillcast/SkillCastBreezebreakerTornado;damage:F",
                    opcode = 181,
                    remap = false
            ),
            remap = false
    )
    private void buffTornadoDamage(@NonNull SkillCastBreezebreakerTornado tornado, float originalDamage) {
        tornado.damage = originalDamage * 5.0F;
    }
}

@Mixin(value = {
        CrescentiaBarrageAttack.class,
        CrescentiaDragonAttack.class
}, remap = false)
abstract class CrescentiaCooldownMixin {
    @Inject(method = "getCooldown", at = @At("HEAD"), cancellable = true, remap = false)
    private void tweakCrescentiaCooldowns(@NonNull CallbackInfoReturnable<Integer> cir) {
        Object self = this;
        if (self instanceof CrescentiaBarrageAttack) {
            cir.setReturnValue(100);
        }
        else if (self instanceof CrescentiaDragonAttack) {
            cir.setReturnValue(200);
        }
    }
}

@Mixin(value = RainfallTurret.class, remap = false)
abstract class TurretAccuracyMixin {
    @ModifyArg(
            method = "tickShooting",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/thecelestialworkshop/celestisynth/common/entity/projectile/RainfallArrow;m_6686_(DDDFF)V",
                    remap = false
            ),
            index = 4,
            remap = false
    )
    private float removeArrowInaccuracy(float inaccuracy) {
        return 0.0F;
    }
}

@Pseudo
@Mixin(value = CSEffectEntity.class, remap = false)
abstract class TurretFlashFix {

    @Unique
    private static Method linggango$getViewVectorMethod;
    @Unique
    private static Method linggango$setYRotMethod;
    @Unique
    private static Method linggango$setXRotMethod;
    @Unique
    private static Field linggango$yRotOField;
    @Unique
    private static Field linggango$xRotOField;

    @Inject(method = "getEffectInstance", at = @At("RETURN"), remap = false)
    private static void linggango$fixTurretMuzzleRotation(LivingEntity owner, @Nullable Entity toFollow, CSVisualType visual, double offsetX, double offsetY, double offsetZ, @NonNull CallbackInfoReturnable<?> cir) {
        Object effectObj = cir.getReturnValue();
        if (!(effectObj instanceof Entity effect) || toFollow == null) return;
        if (toFollow.getClass().getSimpleName().equals("RainfallTurret")) {
            if (visual == CSVisualTypes.RAINFALL_SHOOT.get()) {
                Vec3 look = toFollow.getLookAngle();
                double horizontalMag = Math.sqrt(look.x * look.x + look.z * look.z);
                double pitchRadians = Math.atan2(look.y, horizontalMag);
                double minScale = 2.0;
                double maxScale = 3.5;
                double forwardDistance = minScale + (maxScale - minScale) * Math.abs(Math.sin(pitchRadians));
                double maxLift = -1.0;
                double minLift = 0.0;
                double liftY = maxLift - (minLift - maxLift) * Math.abs(Math.sin(pitchRadians));
                Vec3 finalOffset = look.scale(forwardDistance).add(0, liftY, 0);
                effect.setPos(toFollow.getX() + finalOffset.x, toFollow.getY() + finalOffset.y, toFollow.getZ() + finalOffset.z);
                Vec3 shotVec = linggango$getTurretViewVector(toFollow);
                if (shotVec != null) {
                    float yaw = (float) Math.toDegrees(Math.atan2(shotVec.z, shotVec.x)) - 90.0F;
                    float pitch = (float) (-Math.toDegrees(Math.atan2(shotVec.y, Math.sqrt(shotVec.x * shotVec.x + shotVec.z * shotVec.z))));
                    linggango$setEntityRotation(effect, yaw - 17.0F, pitch);
                }
            }
        }
    }

    @Unique
    private static void linggango$setEntityRotation(Entity entity, float yaw, float pitch) {
        try {
            if (linggango$setYRotMethod == null) {
                try {
                    linggango$setYRotMethod = Entity.class.getDeclaredMethod("setYRot", float.class);
                    linggango$setXRotMethod = Entity.class.getDeclaredMethod("setXRot", float.class);
                    linggango$yRotOField = Entity.class.getDeclaredField("yRotO");
                    linggango$xRotOField = Entity.class.getDeclaredField("xRotO");
                } catch (NoSuchMethodException | NoSuchFieldException e) {
                    String mPrefix = "m_";
                    String fPrefix = "f_";

                    linggango$setYRotMethod = Entity.class.getDeclaredMethod(mPrefix + "146922_", float.class);
                    linggango$setXRotMethod = Entity.class.getDeclaredMethod(mPrefix + "146926_", float.class);
                    linggango$yRotOField = Entity.class.getDeclaredField(fPrefix + "19859_");
                    linggango$xRotOField = Entity.class.getDeclaredField(fPrefix + "19860_");
                }
                linggango$setYRotMethod.setAccessible(true);
                linggango$setXRotMethod.setAccessible(true);
                linggango$yRotOField.setAccessible(true);
                linggango$xRotOField.setAccessible(true);
            }

            linggango$setYRotMethod.invoke(entity, yaw);
            linggango$yRotOField.setFloat(entity, yaw);
            linggango$setXRotMethod.invoke(entity, pitch);
            linggango$xRotOField.setFloat(entity, pitch);
        } catch (Exception ignored) {}
    }

    @Unique
    private static Vec3 linggango$getTurretViewVector(@NonNull Entity turret) {
        try {
            if (linggango$getViewVectorMethod == null) {
                try {
                    linggango$getViewVectorMethod = turret.getClass().getMethod("getViewVector", float.class);
                } catch (NoSuchMethodException e) {
                    linggango$getViewVectorMethod = turret.getClass().getMethod("m_" + "20252_", float.class);
                }
            }
            return (Vec3) linggango$getViewVectorMethod.invoke(turret, 1.0F);
        } catch (Exception e) {
            return turret.getLookAngle();
        }
    }
}

@Mixin(value = RainfallArrow.class, remap = false)
abstract class ArrowCelesScalingMixin {

    @Inject(
            method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;)V",
            at = @At("TAIL"),
            remap = false
    )
    private void linggango$applyCelestialScaling(Level pLevel, LivingEntity pShooter, CallbackInfo ci) {
        RainfallArrow self = (RainfallArrow) (Object) this;
        LivingEntity source = null;

        if (pShooter instanceof Player player) {
            source = player;
        } else if (pShooter instanceof RainfallTurret turret) {
            source = turret.getOwner();
        }

        if (source != null) {
            AttributeInstance attr = source.getAttribute(CSAttributes.CELESTIAL_DAMAGE.get());
            if (attr != null) {
                double celestial = attr.getValue();
                double scaleFactor = 0.01;
                double newDamage = self.getBaseDamage() * (1.0 + celestial * scaleFactor);
                self.setBaseDamage(newDamage);
            }
        }
    }
}

@Mixin(value = RainfallArrow.class, remap = false)
abstract class ArrowDamageMixin {
    @ModifyArg(
            method = "hitEffect",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;m_6469_(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
                    remap = false
            ),
            index = 1,
            remap = false
    )
    private float linggango$scaleAoEDamage(float originalDamage) {
        RainfallArrow self = (RainfallArrow) (Object) this;
        return (float) (self.getBaseDamage() * 0.2F);
    }
}

@Mixin(value = RainfallArrow.class, remap = false)
abstract class ArrowFriendlyFireMixin {

    @Shadow(remap = false)
    public RainfallTurret turretSource;

    @Unique
    private double linggango$originalDamage = -1.0;

    @Inject(method = "m_5790_", at = @At("HEAD"), remap = false)
    private void linggango$preOwnerHit(EntityHitResult pResult, CallbackInfo ci) {
        RainfallArrow arrow = (RainfallArrow) (Object) this;

        if (this.linggango$originalDamage < 0.0) {
            this.linggango$originalDamage = arrow.getBaseDamage();
        }

        if (this.turretSource != null) {
            Entity target = pResult.getEntity();
            if (target instanceof LivingEntity living) {
                Entity turretOwner = this.turretSource.getOwner();
                if (turretOwner != null && turretOwner == living) {
                    arrow.setBaseDamage(0.0);
                }
            }
        }
    }

    @Inject(method = "m_5790_", at = @At("TAIL"), remap = false)
    private void linggango$postOwnerHit(EntityHitResult pResult, CallbackInfo ci) {
        RainfallArrow arrow = (RainfallArrow) (Object) this;
        if (this.turretSource != null && this.linggango$originalDamage >= 0.0) {
            arrow.setBaseDamage(this.linggango$originalDamage);
        }
    }
}