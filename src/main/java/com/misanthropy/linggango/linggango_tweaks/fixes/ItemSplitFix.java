package com.misanthropy.linggango.linggango_tweaks.fixes;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class ItemSplitFix {
    public static void fixBug(@Nullable ItemStack stack) {
        if (stack != null && !stack.isEmpty()) {
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.isEmpty()) {
                stack.setTag(null);
            }
        }
    }
}