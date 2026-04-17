package com.misanthropy.linggango.linggango_tweaks.tweaks;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BurnTheBabyVillager {
    @SubscribeEvent
    public static void onFuelBurnTime(FurnaceFuelBurnTimeEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;
        var itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemId != null && itemId.toString().equals("easy_villagers:villager")) {
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains("villager")) {
                CompoundTag villagerData = tag.getCompound("villager");
                if (villagerData.getInt("Age") < 0) {
                    event.setBurnTime(1600);
                }
            }
        }
    }
}