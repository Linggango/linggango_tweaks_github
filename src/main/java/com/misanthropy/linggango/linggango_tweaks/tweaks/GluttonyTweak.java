package com.misanthropy.linggango.linggango_tweaks.tweaks;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

@Mod.EventBusSubscriber(modid = "linggango_tweaks", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GluttonyTweak {

    private static final int MAX_SOULS = 1000;

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            capSouls(player);
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            capSouls(player);
        }
    }

    private static void capSouls(Player player) {

        CuriosApi.getCuriosHelper().findFirstCurio(player, stack ->
                stack.getItem().getClass().getSimpleName().equals("Gluttony")
        ).ifPresent(slotResult -> {
            ItemStack charm = slotResult.stack();
            CompoundTag tag = charm.getOrCreateTag();
            if (tag.contains("souls")) {
                int currentSouls = tag.getInt("souls");
                if (currentSouls > MAX_SOULS) {
                    tag.putInt("souls", MAX_SOULS);
                }
            }
        });
    }
}