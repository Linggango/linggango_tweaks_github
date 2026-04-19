package com.misanthropy.linggango.linggango_tweaks.mixin.l2;

import dev.xkmc.l2hostility.content.traits.highlevel.ReprintTrait;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Mixin(value = ReprintTrait.class)
public class ReprintTraitMixin {

    @Redirect(
            method = "onHurtTarget",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;entrySet()Ljava/util/Set;", ordinal = 0),
            remap = false
    )
    private Set<Map.Entry<Enchantment, Integer>> filterCursesFromReprintLoop(Map<Enchantment, Integer> instance) {
        Set<Map.Entry<Enchantment, Integer>> filteredSet = new HashSet<>();
        for (Map.Entry<Enchantment, Integer> entry : instance.entrySet()) {
            if (!entry.getKey().isCurse()) {
                filteredSet.add(entry);
            }
        }
        return filteredSet;
    }
}