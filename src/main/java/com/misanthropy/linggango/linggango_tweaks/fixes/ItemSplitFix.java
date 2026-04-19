package com.misanthropy.linggango.linggango_tweaks.fixes;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class ItemSplitFix {
    public static void fixBug(ItemStack stack) {
        if (stack != null && !stack.isEmpty()) {
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.isEmpty()) {
                stack.setTag(null);
            }
        }
    }
}