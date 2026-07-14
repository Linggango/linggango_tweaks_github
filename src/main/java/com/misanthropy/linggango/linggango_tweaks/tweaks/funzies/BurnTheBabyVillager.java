package com.misanthropy.linggango.linggango_tweaks.tweaks.funzies;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.jspecify.annotations.NonNull;

@Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BurnTheBabyVillager {
    @SubscribeEvent
    public static void onFuelBurnTime(@NonNull FurnaceFuelBurnTimeEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemId != null && "easy_villagers".equals(itemId.getNamespace()) && "villager".equals(itemId.getPath())) {
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains("villager", Tag.TAG_COMPOUND)) {
                CompoundTag villagerData = tag.getCompound("villager");
                if (villagerData.getInt("Age") < 0) {
                    event.setBurnTime(1600);
                }
            }
        }
    }
}