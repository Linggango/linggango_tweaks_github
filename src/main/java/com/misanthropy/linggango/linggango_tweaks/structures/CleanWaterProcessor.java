package com.misanthropy.linggango.linggango_tweaks.structures;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import javax.annotation.Nullable;

public class CleanWaterProcessor extends StructureProcessor {
    public static final CleanWaterProcessor INSTANCE = new CleanWaterProcessor();
    public static final Codec<CleanWaterProcessor> CODEC = Codec.unit(() -> INSTANCE);

    @Nullable
    @Override public StructureTemplate.StructureBlockInfo processBlock(LevelReader level, BlockPos offset, BlockPos pos, StructureTemplate.StructureBlockInfo original, StructureTemplate.StructureBlockInfo after, StructurePlaceSettings settings) {
        BlockState state = after.state();
        if (state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED)) {
            FluidState worldFluid = level.getFluidState(after.pos());
            if (!worldFluid.isSourceOfType(Fluids.WATER)) {
                BlockState dryState = state.setValue(BlockStateProperties.WATERLOGGED, false);
                return new StructureTemplate.StructureBlockInfo(after.pos(), dryState, after.nbt());
            }
        }

        return after;
    }

    @Override protected StructureProcessorType<?> getType() {
        return LinggangoTweaks.CLEAN_WATER_PROCESSOR.get();
    }
}