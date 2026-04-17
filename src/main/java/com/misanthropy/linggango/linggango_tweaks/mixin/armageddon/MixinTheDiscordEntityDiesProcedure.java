package com.misanthropy.linggango.linggango_tweaks.mixin.armageddon;

import dev.xkmc.l2hostility.content.capability.mob.MobTraitCap;
import dev.xkmc.l2hostility.content.traits.highlevel.SplitTrait;
import net.mcreator.armageddonmod.procedures.TheDiscordEntityDiesProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TheDiscordEntityDiesProcedure.class)
public class MixinTheDiscordEntityDiesProcedure {

    @Inject(method = "execute", at = @At("HEAD"), cancellable = true, remap = false)
    private static void cancelIfSplitWithoutMain(LevelAccessor world, double x, double y, double z,
                                                  Entity entity, CallbackInfo ci) {
        if (entity instanceof LivingEntity living
                && ((MobTraitCap.HOLDER.isProper(living)
                        && MobTraitCap.HOLDER.get(living).traits.keySet().stream().anyMatch(t -> t instanceof SplitTrait))
                    || (living.getTags().contains("l2hostility:split_done")
                        && !living.getTags().contains("l2hostility:split_main")))) {
            ci.cancel();
        }
    }
}
