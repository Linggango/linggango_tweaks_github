package com.misanthropy.linggango.linggango_tweaks.fixes;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;

@SuppressWarnings("unused")
public final class BornInChaosProjectileFix {

    private BornInChaosProjectileFix() {
    }

    public static Entity allowArrowLikeProjectileDamage(DamageSource source) {
        Entity directEntity = source.getDirectEntity();

        return directEntity instanceof AbstractArrow ? null : directEntity;
    }

    public static boolean allowTridentDamage(DamageSource source, ResourceKey<DamageType> damageType) {
        if (damageType == DamageTypes.TRIDENT) {
            return false;
        }

        return source.is(damageType);
    }
}