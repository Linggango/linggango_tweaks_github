package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.EnchantmentTableBlock;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(EnchantmentTableBlock.class)
public class EnchantmentTableBlockMixin {
    @Shadow @Final @Mutable
    public static List<BlockPos> BOOKSHELF_OFFSETS;
    static {
        BOOKSHELF_OFFSETS = BlockPos.betweenClosedStream(-3, -1, -3, 3, 1, 3)
                .filter(pos -> Math.abs(pos.getX()) >= 2 || Math.abs(pos.getZ()) >= 2)
                .map(BlockPos::immutable)
                .toList();
    }
}