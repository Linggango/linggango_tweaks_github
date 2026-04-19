package com.misanthropy.linggango.linggango_tweaks.mixin.goety;

import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.NbtOps;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Map;

@Mixin(targets = "net.v_black_cat.goetydelight.entities.ai.customer.CustomerAi", remap = false)
public class GoetyCustomerAiPatchMixin {

    @ModifyVariable(
            method = "makeBrain(Lnet/minecraft/world/entity/PathfinderMob;Lcom/mojang/serialization/Dynamic;)Lnet/minecraft/world/entity/ai/Brain;",
            at = @At("HEAD"),
            argsOnly = true,
            remap = false
    )
    private static @NonNull Dynamic<?> interceptMakeBrainCrash(@Nullable Dynamic<?> dynamic) {
        if (dynamic == null || dynamic.getValue() == null || dynamic.getValue() instanceof net.minecraft.nbt.EndTag) {
            NbtOps nbtops = NbtOps.INSTANCE;
            return new Dynamic<>(nbtops, nbtops.createMap(Map.of(nbtops.createString("memories"), nbtops.emptyMap())));
        }

        return dynamic;
    }
}