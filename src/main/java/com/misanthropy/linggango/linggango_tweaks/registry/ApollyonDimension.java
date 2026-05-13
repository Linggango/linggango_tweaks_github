package com.misanthropy.linggango.linggango_tweaks.registry;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

@SuppressWarnings("unused")
public class ApollyonDimension {
    public static final ResourceKey<DimensionType> APOLLYON_TYPE = ResourceKey.create(
            Registries.DIMENSION_TYPE,
            new ResourceLocation(LinggangoTweaks.MOD_ID, "revelation_type")
    );

    public static final ResourceKey<Level> APOLLYON_LEVEL = ResourceKey.create(
            Registries.DIMENSION,
            new ResourceLocation(LinggangoTweaks.MOD_ID, "revelation")
    );

    public static final ResourceKey<DimensionType> ASCENSION_TYPE = ResourceKey.create(
            Registries.DIMENSION_TYPE,
            new ResourceLocation(LinggangoTweaks.MOD_ID, "ascension_type")
    );

    public static final ResourceKey<Level> ASCENSION_LEVEL = ResourceKey.create(
            Registries.DIMENSION,
            new ResourceLocation(LinggangoTweaks.MOD_ID, "ascension")
    );

    public static void register() {
    }
}