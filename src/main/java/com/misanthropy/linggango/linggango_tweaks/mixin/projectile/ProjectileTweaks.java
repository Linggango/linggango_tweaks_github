package com.misanthropy.linggango.linggango_tweaks.mixin.projectile;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraftforge.registries.ForgeRegistries;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractArrow.class)
public class ProjectileTweaks {

    @Unique
    private static boolean linggango_tweaks$wroughtnautChecked = false;
    @Unique
    private static EntityType<?> linggango_tweaks$wroughtnautTypeCache = null;

    @Redirect(
            method = "onHitEntity",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z")
    )
    private boolean redirectProjectileDamage(Entity target, @NonNull DamageSource originalSource, float damage) {
        AbstractArrow arrow = (AbstractArrow) (Object) this;
        Entity owner = arrow.getOwner();

        if (target instanceof Player) {
            return target.hurt(originalSource, damage);
        }

        if (target instanceof LivingEntity livingTarget && livingTarget.isBlocking()) {
            return target.hurt(originalSource, damage);
        }

        if (arrow.getPierceLevel() < 3) {
            return target.hurt(originalSource, damage);
        }

        if (target instanceof LivingEntity livingTarget) {
            if (!linggango_tweaks$wroughtnautChecked) {
                for (EntityType<?> type : ForgeRegistries.ENTITY_TYPES.getValues()) {
                    ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(type);
                    if (id != null && id.getPath().contains("wroughtnaut")) {
                        linggango_tweaks$wroughtnautTypeCache = type;
                        break;
                    }
                }
                linggango_tweaks$wroughtnautChecked = true;
            }

            if (linggango_tweaks$wroughtnautTypeCache != null && target.getType() == linggango_tweaks$wroughtnautTypeCache) {
                if (owner instanceof Player playerOwner) {
                    livingTarget.setLastHurtByPlayer(playerOwner);
                }
                return target.hurt(target.damageSources().fellOutOfWorld(), damage);
            }
        }

        if (owner instanceof Player playerOwner) {
            return target.hurt(target.damageSources().playerAttack(playerOwner), damage);
        } else if (owner instanceof LivingEntity livingOwner) {
            return target.hurt(target.damageSources().mobAttack(livingOwner), damage);
        } else {
            return target.hurt(target.damageSources().generic(), damage);
        }
    }
}