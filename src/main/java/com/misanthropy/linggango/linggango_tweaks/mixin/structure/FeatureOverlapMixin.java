package com.misanthropy.linggango.linggango_tweaks.mixin.structure;

import com.misanthropy.linggango.linggango_tweaks.config.TweaksConfig;
import com.misanthropy.linggango.linggango_tweaks.util.StructurePlacementValidator;
import com.misanthropy.linggango.linggango_tweaks.util.StructureSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("unused")
@Mixin(Feature.class)
public class FeatureOverlapMixin<FC extends FeatureConfiguration> {

    @Inject(
            method = "place(Lnet/minecraft/world/level/levelgen/feature/configurations/FeatureConfiguration;Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void linggango$evaluateFeaturePlacement(FC config, WorldGenLevel level, ChunkGenerator generator, RandomSource random, @NonNull BlockPos pos, @NonNull CallbackInfoReturnable<Boolean> cir) {
        Feature<?> currentFeature = (Feature<?>) (Object) this;
        ResourceLocation featureId = ForgeRegistries.FEATURES.getKey(currentFeature);

        if (featureId == null || TweaksConfig.isFeatureIgnored(featureId)) {
            return;
        }

        if (TweaksConfig.isFeatureBlacklisted(featureId)) {
            cir.setReturnValue(false);
            return;
        }

        boolean validPlacement = StructurePlacementValidator.canPlaceFeature(level, pos, featureId.toString());

        if (!validPlacement) {
            cir.setReturnValue(false);
        }
    }

    @Inject(
            method = "place(Lnet/minecraft/world/level/levelgen/feature/configurations/FeatureConfiguration;Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;)Z",
            at = @At("RETURN")
    )
    private void linggango$recordSuccessfulFeature(FC config, @NonNull WorldGenLevel level, ChunkGenerator generator, RandomSource random, @NonNull BlockPos pos, @NonNull CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) {
            return;
        }

        Feature<?> currentFeature = (Feature<?>) (Object) this;
        ResourceLocation featureId = ForgeRegistries.FEATURES.getKey(currentFeature);

        if (featureId == null || TweaksConfig.isFeatureIgnored(featureId)) {
            return;
        }

        ServerLevel underlyingServerLevel = level.getLevel();
        if (underlyingServerLevel == null) {
            return;
        }

        StructureSavedData levelData = underlyingServerLevel.getDataStorage().computeIfAbsent(
                StructureSavedData::load,
                StructureSavedData::create,
                "linggango_structure_data"
        );

        double boundingRadius = 5.0;
        AABB featureFootprint = new AABB(
                pos.getX() - boundingRadius, pos.getY() - boundingRadius, pos.getZ() - boundingRadius,
                pos.getX() + boundingRadius, pos.getY() + boundingRadius, pos.getZ() + boundingRadius
        );

        boolean hasWhitelistPriority = TweaksConfig.isFeatureWhitelisted(featureId);
        levelData.addPlacedObject(featureFootprint, featureId.toString(), hasWhitelistPriority);
    }
}