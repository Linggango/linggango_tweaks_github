package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("all")
@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class VegetationPlacementMixin {
    @Unique
    private static final TagKey<Block> FORGE_PLANT = TagKey.create(Registries.BLOCK, new ResourceLocation("forge", "plant"));
    @Unique
    private static final TagKey<Block> FORGE_WATER_PLANT = TagKey.create(Registries.BLOCK, new ResourceLocation("forge", "plant/water"));

    @Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true)
    private void linggango_allowPlacementEverywhere(LevelReader levelReader, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (levelReader instanceof Level) {
            BlockBehaviour.BlockStateBase stateBase = (BlockBehaviour.BlockStateBase) (Object) this;
            Block block = stateBase.getBlock();
            boolean isVegetation = block instanceof BushBlock ||
                    block instanceof DoublePlantBlock ||
                    block instanceof SeagrassBlock ||
                    block instanceof TallSeagrassBlock ||
                    block instanceof KelpBlock ||
                    block instanceof KelpPlantBlock ||
                    block instanceof SugarCaneBlock ||
                    block instanceof CactusBlock ||
                    block instanceof BambooStalkBlock ||
                    stateBase.is(BlockTags.FLOWERS) ||
                    stateBase.is(BlockTags.TALL_FLOWERS) ||
                    stateBase.is(BlockTags.SMALL_FLOWERS) ||
                    stateBase.is(FORGE_PLANT) ||
                    stateBase.is(FORGE_WATER_PLANT);

            if (isVegetation) {
                BlockState stateBelow = levelReader.getBlockState(pos.below());
                if (!stateBelow.isAir()) {
                    cir.setReturnValue(true);
                }
            }
        }
    }
}