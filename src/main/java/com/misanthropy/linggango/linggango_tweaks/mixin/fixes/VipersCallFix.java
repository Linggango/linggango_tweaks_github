package com.misanthropy.linggango.linggango_tweaks.mixin.fixes;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.rosemarythyme.simplymore.item.uniques.VipersCallItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.Collection;

@Mixin(VipersCallItem.class)
public class VipersCallFix {

    @Redirect(
            method = "inventoryTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;getActiveEffects()Ljava/util/Collection;"
            )
    )
    private Collection<MobEffectInstance> copyActiveEffects(Player instance) {
        return new ArrayList<>(instance.getActiveEffects());
    }
}