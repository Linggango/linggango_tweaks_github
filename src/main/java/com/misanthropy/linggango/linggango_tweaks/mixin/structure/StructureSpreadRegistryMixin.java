package com.misanthropy.linggango.linggango_tweaks.mixin.structure;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.sugar.Local;
import com.misanthropy.linggango.linggango_tweaks.config.TweaksConfig;
import com.mojang.serialization.Decoder;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(RegistryDataLoader.class)
public class StructureSpreadRegistryMixin {

    @Inject(
            method = "loadRegistryContents",
            at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Decoder;parse(Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;")
    )
    private static void linggango$modifyStructureSpawns(
            RegistryOps.RegistryInfoLookup lookup,
            ResourceManager manager,
            ResourceKey<?> key,
            WritableRegistry<?> registry,
            Decoder<?> decoder,
            Map<?, ?> exceptions,
            CallbackInfo ci,
            @Local String directory,
            @Local(ordinal = 1) @NonNull ResourceKey<?> elementKey,
            @Local @NonNull JsonElement jsonElement) {

        if ("worldgen/structure_set".equals(directory) && jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            if (jsonObject.has("placement")) {
                JsonObject placement = jsonObject.getAsJsonObject("placement");
                if (placement.has("type") && !"minecraft:concentric_rings".equals(placement.get("type").getAsString())) {

                    String structureId = elementKey.location().toString();
                    double factor = TweaksConfig.getFactor(structureId);

                    if (factor == 0.0) {
                        placement.addProperty("frequency", 0.0);
                    } else if (factor != 1.0) {
                        int spacing = placement.has("spacing") ? (int) (placement.get("spacing").getAsDouble() * factor) : 1;
                        int separation = placement.has("separation") ? (int) (placement.get("separation").getAsDouble() * factor) : 1;
                        if (separation >= spacing) {
                            spacing = Math.max(1, spacing);
                            separation = spacing - 1;
                        }

                        placement.addProperty("spacing", spacing);
                        placement.addProperty("separation", separation);

                        if (TweaksConfig.ID_BASED_SALT.get()) {
                            int salt = (structureId.hashCode() * 31) & 0x7FFFFFFF;
                            placement.addProperty("salt", salt);
                        }
                    }
                }
            }
        }
    }
}