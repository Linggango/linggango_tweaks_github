//package com.misanthropy.linggango.linggango_tweaks.features;
//
//import com.mojang.serialization.Codec;
//import net.minecraft.core.BlockPos;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.tags.BlockTags;
//import net.minecraft.util.RandomSource;
//import net.minecraft.world.level.WorldGenLevel;
//import net.minecraft.world.level.block.Blocks;
//import net.minecraft.world.level.block.Rotation;
//import net.minecraft.world.level.block.state.BlockState;
//import net.minecraft.world.level.levelgen.feature.Feature;
//import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
//
//@SuppressWarnings("unused")
//public class CustomTreeGenerationFeature extends Feature<CustomTreeConfig> {
//
//    public CustomTreeGenerationFeature(Codec<CustomTreeConfig> codec) {
//        super(codec);
//    }
//
//    @Override
//    public boolean place(FeaturePlaceContext<CustomTreeConfig> context) {
//        WorldGenLevel level = context.level();
//        BlockPos origin = context.origin();
//        CustomTreeConfig config = context.config();
//        RandomSource random = context.random();
//
//        ResourceLocation structureId = new ResourceLocation("linggango_tweaks", config.structurePath());
//        DataManagerForTrees.TreeTemplate treeTemplate = DataManagerForTrees.getTreeTemplate(structureId);
//
//        if (treeTemplate == null) return false;
//
//        Rotation rotation = Rotation.getRandom(random);
//        BlockPos centerOffset = treeTemplate.anchors().centerOffsets().get(rotation);
//        BlockPos worldCenter = origin.offset(centerOffset);
//
//        return placeAllBlocks(level, worldCenter, treeTemplate, config, random, rotation);
//    }
//
//    private boolean placeAllBlocks(WorldGenLevel level, BlockPos center, DataManagerForTrees.TreeTemplate treeTemplate, CustomTreeConfig config, RandomSource random, Rotation rotation) {
//        boolean placedAnyBlocks = false;
//        for (DataManagerForTrees.BlockInfo blockInfo : treeTemplate.blocks()) {
//            BlockPos rotatedPos = blockInfo.pos().rotate(rotation);
//            BlockPos worldPos = center.offset(rotatedPos);
//            if (blockInfo.block() != Blocks.WHITE_WOOL && blockInfo.block() != Blocks.RED_WOOL) {
//                if (canPlaceBlock(level, worldPos, config)) {
//                    level.setBlock(worldPos, blockInfo.state(), 3);
//                    placedAnyBlocks = true;
//                }
//            }
//        }
//
//        for (BlockPos redWoolRel : treeTemplate.anchors().redWoolPositions()) {
//            BlockPos rotatedRedWool = redWoolRel.rotate(rotation);
//            BlockPos worldRedWool = center.offset(rotatedRedWool);
//            BlockPos groundPos = findGroundForTrunk(level, worldRedWool);
//
//            BlockState trunkBlock = config.trunkProvider().getState(random, worldRedWool);
//            for (int y = groundPos.getY() + 1; y <= worldRedWool.getY(); ++y) {
//                BlockPos trunkPos = new BlockPos(worldRedWool.getX(), y, worldRedWool.getZ());
//                level.setBlock(trunkPos, trunkBlock, 3);
//                placedAnyBlocks = true;
//            }
//        }
//
//        return placedAnyBlocks;
//    }
//
//    private boolean canPlaceBlock(WorldGenLevel level, BlockPos pos, CustomTreeConfig config) {
//        BlockState existing = level.getBlockState(pos);
//        if (existing.isAir() || existing.canBeReplaced()) return true;
//        return config.canReplaceLeavesInWorld() && existing.is(BlockTags.LEAVES);
//    }
//
//    private BlockPos findGroundForTrunk(WorldGenLevel level, BlockPos startPos) {
//        BlockPos current = startPos.below();
//        int maxTrunkLength = 15;
//
//        for (int blocksChecked = 0; current.getY() > level.getMinBuildHeight() && blocksChecked < maxTrunkLength; ++blocksChecked) {
//            BlockState state = level.getBlockState(current);
//            if (!state.isAir() && !state.canBeReplaced() && !state.is(BlockTags.LEAVES)) {
//                return current;
//            }
//            current = current.below();
//        }
//        return startPos;
//    }
//}