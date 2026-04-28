package com.misanthropy.linggango.linggango_tweaks.tweaks;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "linggango_tweaks", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RageGloveTweak {
    private static final int MAX_RAGE_STACKS = 10;

    @SubscribeEvent
    public static void onPlayerAttack(AttackEntityEvent event) {
        capRageStacks(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player) {
            capRageStacks(player);
        }
    }

    private static void capRageStacks(Player player) {
        for (ItemStack stack : player.getInventory().items) {

            if (stack.getItem().getClass().getSimpleName().equals("RageGloveItem")) {
                if (stack.hasTag()) {
                    CompoundTag tag = stack.getOrCreateTag();
                    if (tag.contains("stacks")) {
                        int currentStacks = tag.getInt("stacks");
                        if (currentStacks > MAX_RAGE_STACKS) {
                            tag.putInt("stacks", MAX_RAGE_STACKS);
                        }
                    }
                }
            }
        }
    }
}