package com.misanthropy.linggango.linggango_tweaks.tweaks.brutality;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = "linggango_tweaks", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class StopwatchTweak {

    private static final int MINIMUM_ALLOWED_TIME = 10;
    private static final ResourceLocation STOPWATCH_ID = new ResourceLocation("adamsarsplus", "enchanters_stopwatch");

    @SubscribeEvent
    public static void onStopwatchUse(PlayerInteractEvent.RightClickItem event) {
        ItemStack stack = event.getItemStack();
        ResourceLocation registryName = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (registryName != null && registryName.equals(STOPWATCH_ID)) {
            Player player = event.getEntity();

            if (!event.getLevel().isClientSide) {
                CompoundTag tag = stack.getTag();
                if (tag != null && tag.contains("adamsarsplus_stopwatch")) {
                    CompoundTag dataTag = tag.getCompound("adamsarsplus_stopwatch");
                    int currentTime = dataTag.getInt("castTime");

                    if (currentTime > 0 && currentTime < MINIMUM_ALLOWED_TIME) {
                        dataTag.putInt("castTime", MINIMUM_ALLOWED_TIME);
                        player.displayClientMessage(Component.literal("Minimum interval is 10 seconds."), true);
                    }
                }
            }
        }
    }
}