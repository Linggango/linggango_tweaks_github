package com.misanthropy.linggango.linggango_tweaks.features;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

@SuppressWarnings("unused")
public record CustomTreeConfiguration(
        String structurePath,
        BlockStateProvider trunkProvider,
        boolean canReplaceLeavesInWorld
) implements FeatureConfiguration {
    public static final Codec<CustomTreeConfiguration> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            Codec.STRING.fieldOf("structure_path").forGetter(CustomTreeConfiguration::structurePath),
            BlockStateProvider.CODEC.fieldOf("trunk_provider").forGetter(CustomTreeConfiguration::trunkProvider),
            Codec.BOOL.optionalFieldOf("can_replace_leaves_in_world", true).forGetter(CustomTreeConfiguration::canReplaceLeavesInWorld)
    ).apply(instance, CustomTreeConfiguration::new));
}