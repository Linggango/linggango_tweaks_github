package com.misanthropy.linggango.linggango_tweaks.util;

import com.misanthropy.linggango.linggango_tweaks.config.TweaksConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;


@SuppressWarnings("unused")
public class StructurePlacementValidator {
    private static final Logger LOGGER = LogManager.getLogger();

    public static boolean canPlaceStructure(LevelAccessor level, @NonNull BoundingBox box, @NonNull String structureName, @NonNull BlockPos center) {
        StructureSavedData data = getData(level);
        if (data == null) return true;

        ResourceKey<Level> dimension = getDimension(level);
        AABB aabb = new AABB(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ());

        ResourceLocation id = ResourceLocation.tryParse(structureName);
        boolean isWhitelisted = id != null && TweaksConfig.isStructureWhitelisted(id);
        StructureSavedData.PlacementResult result = data.checkPlacement(
                center, aabb, structureName,
                TweaksConfig.MAX_NEARBY.get(),
                TweaksConfig.CHECK_RADIUS.get(),
                isWhitelisted
        );

        if (result.isAllowed()) {
            if (TweaksConfig.DEBUG_OVERLAP.get()) {
                LOGGER.debug("Skipped structure {} at {} (center: {}) in dimension {} due to {}",
                        structureName, box.getCenter(), center, dimension.location(), result.getDenyReason());
            }
            return false;
        } else {
            if (TweaksConfig.DEBUG_OVERLAP.get()) {
                LOGGER.debug("Allowed structure {} at {} (center: {}) in dimension {}", structureName, box.getCenter(), center, dimension.location());
            }
        }

        if (!result.getToRemove().isEmpty()) {
            data.removeObjects(result.getToRemove());
            if (TweaksConfig.DEBUG_OVERLAP.get()) {
                LOGGER.info("Whitelisted structure {} overrides {} existing object(s)",
                        structureName, result.getToRemove().size());
            }
        }

        return true;
    }

    public static boolean canPlaceFeature(LevelAccessor level, @NonNull BlockPos pos, @NonNull String featureName) {
        StructureSavedData data = getData(level);
        if (data == null) return true;

        ResourceKey<Level> dimension = getDimension(level);
        double halfSize = 5.0;
        AABB aabb = new AABB(pos.getX() - halfSize, pos.getY() - halfSize, pos.getZ() - halfSize,
                pos.getX() + halfSize, pos.getY() + halfSize, pos.getZ() + halfSize);

        ResourceLocation id = ResourceLocation.tryParse(featureName);
        boolean isWhitelisted = id != null && TweaksConfig.isFeatureWhitelisted(id);
        StructureSavedData.PlacementResult result = data.checkPlacement(
                pos, aabb, featureName,
                TweaksConfig.MAX_NEARBY.get(),
                TweaksConfig.CHECK_RADIUS.get(),
                isWhitelisted
        );

        if (result.isAllowed()) {
            if (TweaksConfig.DEBUG_OVERLAP.get()) {
                LOGGER.info("Skipped feature {} at {} in dimension {} due to {}",
                        featureName, pos.toShortString(), dimension.location(), result.getDenyReason());
            }
            return false;
        } else {
            if (TweaksConfig.DEBUG_OVERLAP.get()) {
                LOGGER.debug("Allowed feature {} at {} in dimension {}", featureName, pos.toShortString(), dimension.location());
            }
        }

        if (!result.getToRemove().isEmpty()) {
            data.removeObjects(result.getToRemove());
            if (TweaksConfig.DEBUG_OVERLAP.get()) {
                LOGGER.info("Whitelisted feature {} overrides {} existing object(s)",
                        featureName, result.getToRemove().size());
            }
        }

        return true;
    }

    private static StructureSavedData getData(LevelAccessor level) {
        if (level instanceof ServerLevel serverLevel) {
            return serverLevel.getDataStorage().computeIfAbsent(
                    StructureSavedData::load,
                    StructureSavedData::create,
                    "linggango_structure_data"
            );
        }
        return null;
    }

    private static @NonNull ResourceKey<Level> getDimension(LevelAccessor level) {
        if (level instanceof ServerLevel serverLevel) {
            return serverLevel.dimension();
        } else if (level instanceof WorldGenLevel worldGenLevel) {
            ServerLevel underlyingLevel = worldGenLevel.getLevel();
            return underlyingLevel.dimension();
        }
        return Level.OVERWORLD;
    }
}