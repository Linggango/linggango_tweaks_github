package com.misanthropy.linggango.linggango_tweaks.structures;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class GriddyStructureSystem extends StructurePlacement {
    public static final Codec<GriddyStructureSystem> CODEC = RecordCodecBuilder.<GriddyStructureSystem>mapCodec(instance ->
            placementCodec(instance).and(instance.group(
                    Codec.intRange(1, 4096).fieldOf("spacing").forGetter(GriddyStructureSystem::getSpacing),
                    Codec.intRange(0, 4096).fieldOf("x_offset").forGetter(GriddyStructureSystem::getXOffset),
                    Codec.intRange(0, 4096).fieldOf("z_offset").forGetter(GriddyStructureSystem::getZOffset)
            )).apply(instance, GriddyStructureSystem::new)
    ).flatXmap(GriddyStructureSystem::validate, GriddyStructureSystem::validate).codec();

    private final int spacing;
    private final int xOffset;
    private final int zOffset;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public GriddyStructureSystem(Vec3i locateOffset, FrequencyReductionMethod frequencyReductionMethod, float frequency, int salt, Optional<ExclusionZone> exclusionZone, int spacing, int xOffset, int zOffset) {
        super(locateOffset, frequencyReductionMethod, frequency, salt, exclusionZone);
        this.spacing = spacing;
        this.xOffset = xOffset;
        this.zOffset = zOffset;
    }

    private static DataResult<GriddyStructureSystem> validate(GriddyStructureSystem placement) {
        if (placement.spacing <= placement.xOffset || placement.spacing <= placement.zOffset) {
            return DataResult.error(() -> "Spacing must be strictly greater than offsets!");
        }
        return DataResult.success(placement);
    }

    public int getSpacing() { return this.spacing; }
    public int getXOffset() { return this.xOffset; }
    public int getZOffset() { return this.zOffset; }

    @Override protected boolean isPlacementChunk(@NotNull ChunkGeneratorStructureState state, int chunkX, int chunkZ) {
        return Math.floorMod(chunkX, this.spacing) == this.xOffset && Math.floorMod(chunkZ, this.spacing) == this.zOffset;
    }

    @Override public @NotNull StructurePlacementType<?> type() {
        return LinggangoTweaks.EXACT_GRID_PLACEMENT.get();
    }
}