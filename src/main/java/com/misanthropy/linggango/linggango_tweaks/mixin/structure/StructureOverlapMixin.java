package com.misanthropy.linggango.linggango_tweaks.mixin.structure;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.misanthropy.linggango.linggango_tweaks.config.TweaksConfig;
import com.misanthropy.linggango.linggango_tweaks.util.StructurePlacementValidator;
import com.misanthropy.linggango.linggango_tweaks.util.StructureSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.StructureAccess;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChunkGenerator.class)
public class StructureOverlapMixin {

    @Unique
    private static final Logger linggango_tweaks$LOGGER = LogManager.getLogger();

    @WrapOperation(
            method = "tryGenerateStructure",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/StructureManager;setStartForStructure(Lnet/minecraft/core/SectionPos;Lnet/minecraft/world/level/levelgen/structure/Structure;Lnet/minecraft/world/level/levelgen/structure/StructureStart;Lnet/minecraft/world/level/chunk/StructureAccess;)V")
    )
    private void linggango$interceptStructureGeneration(@NonNull StructureManager manager, SectionPos section, @NonNull Structure structure, @NonNull StructureStart startInfo, StructureAccess access, @NonNull Operation<Void> original) {
        BoundingBox bounds = startInfo.getBoundingBox();

        ResourceLocation structureId = linggango$fetchStructureId(structure);

        if (structureId == null) {
            if (TweaksConfig.DEBUG_OVERLAP.get()) {
                linggango_tweaks$LOGGER.warn("Could not resolve ID for structure, skipping spatial checks.");
            }
            original.call(manager, section, structure, startInfo, access);
            return;
        }

        if (TweaksConfig.isStructureIgnored(structureId)) {
            original.call(manager, section, structure, startInfo, access);
            return;
        }

        if (TweaksConfig.isStructureBlacklisted(structureId)) {
            return;
        }

        ServerLevel activeLevel = linggango$extractServerLevel(manager);

        if (activeLevel == null) {
            original.call(manager, section, structure, startInfo, access);
            return;
        }

        ChunkPos chunkCoordinates = startInfo.getChunkPos();
        int verticalCenter = (bounds.minY() + bounds.maxY()) / 2;
        BlockPos centralPoint = new BlockPos(chunkCoordinates.getMinBlockX() + 8, verticalCenter, chunkCoordinates.getMinBlockZ() + 8);

        boolean placementAllowed = StructurePlacementValidator.canPlaceStructure(activeLevel, bounds, structureId.toString(), centralPoint);

        if (!placementAllowed) {
            return;
        }

        original.call(manager, section, structure, startInfo, access);

        if (startInfo.isValid()) {
            StructureSavedData persistentData = activeLevel.getDataStorage().computeIfAbsent(
                    StructureSavedData::load,
                    StructureSavedData::create,
                    "linggango_structure_data"
            );

            AABB area = new AABB(bounds.minX(), bounds.minY(), bounds.minZ(), bounds.maxX(), bounds.maxY(), bounds.maxZ());
            boolean hasWhitelistPriority = TweaksConfig.isStructureWhitelisted(structureId);

            persistentData.addPlacedObject(area, structureId.toString(), hasWhitelistPriority);
        }
    }

    @Unique
    private @Nullable ResourceLocation linggango$fetchStructureId(@NonNull Structure targetStructure) {
        MinecraftServer activeServer = ServerLifecycleHooks.getCurrentServer();
        if (activeServer != null) {
            Registry<Structure> structureRegistry = activeServer.registryAccess().registry(Registries.STRUCTURE).orElse(null);
            if (structureRegistry != null) {
                return structureRegistry.getKey(targetStructure);
            }
        }
        return null;
    }

    @Unique
    private @Nullable ServerLevel linggango$extractServerLevel(@NonNull StructureManager managerInstance) {
        LevelAccessor genericLevel = ((StructureManagerAccess) managerInstance).linggango$getLevel();
        if (genericLevel instanceof ServerLevel) {
            return (ServerLevel) genericLevel;
        }
        if (genericLevel instanceof WorldGenLevel) {
            return ((WorldGenLevel) genericLevel).getLevel();
        }
        return null;
    }
}