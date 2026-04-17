package com.misanthropy.linggango.linggango_tweaks.mixin.armageddon;

import dev.xkmc.l2hostility.content.capability.mob.MobTraitCap;
import dev.xkmc.l2hostility.content.traits.highlevel.SplitTrait;
import net.mcreator.armageddonmod.entity.NyxarisTheVeilOfOblivionEntity;
import net.mcreator.armageddonmod.procedures.NyxarisTheVeilOfOblivionEntityDiesProcedure;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(NyxarisTheVeilOfOblivionEntity.class)
public class MixinNyxarisTheVeilOfOblivionEntity {

    @Redirect(
        method = "m_6667_", //die
        at = @At(value = "INVOKE",
            target = "Lnet/mcreator/armageddonmod/procedures/NyxarisTheVeilOfOblivionEntityDiesProcedure;execute(Lnet/minecraft/world/level/LevelAccessor;DDD)V"),
        remap = false
    )
    private void skipLootIfSplit(LevelAccessor world, double x, double y, double z) {
        LivingEntity self = (LivingEntity)(Object) this;
        if ((MobTraitCap.HOLDER.isProper(self)
                && MobTraitCap.HOLDER.get(self).traits.keySet().stream().anyMatch(t -> t instanceof SplitTrait))
            || (self.getTags().contains("l2hostility:split_done")
                && !self.getTags().contains("l2hostility:split_main"))) {
            return;
        }
        NyxarisTheVeilOfOblivionEntityDiesProcedure.execute(world, x, y, z);
    }
}
