package com.misanthropy.linggango.linggango_tweaks.tweaks.relics;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;

@Mod.EventBusSubscriber(modid = "linggango_tweaks", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RageGloveTweak {
    private static final int MAX_RAGE_STACKS = 10;
    private static Item rageGloveCache = null;
    private static boolean rageGloveSearched = false;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player) {
            capRageStacks(player);
        }
    }

    private static void capRageStacks(Player player) {
        lazyInit();
        if (rageGloveCache == null) return;

        CuriosApi.getCuriosInventory(player).ifPresent(handler ->
                handler.findFirstCurio(rageGloveCache).ifPresent(result -> {
                    ItemStack stack = result.stack();
                    if (stack.hasTag()) {
                        CompoundTag tag = stack.getTag();
                        if (tag != null && tag.contains("stacks", 3)) {
                            int currentStacks = tag.getInt("stacks");
                            if (currentStacks > MAX_RAGE_STACKS) {
                                tag.putInt("stacks", MAX_RAGE_STACKS);
                            }
                        }
                    }
                })
        );
    }

    private static void lazyInit() {
        if (!rageGloveSearched) {
            for (Item item : ForgeRegistries.ITEMS.getValues()) {
                if (item.getClass().getSimpleName().equals("RageGloveItem")) {
                    rageGloveCache = item;
                    break;
                }
            }
            rageGloveSearched = true;
        }
    }
}