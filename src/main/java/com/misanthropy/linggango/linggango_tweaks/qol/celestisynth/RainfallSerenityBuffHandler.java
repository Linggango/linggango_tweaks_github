package com.misanthropy.linggango.linggango_tweaks.qol.celestisynth;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.jspecify.annotations.NonNull;

@Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RainfallSerenityBuffHandler {
    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.@NonNull RightClickItem event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) {
            return;
        }

        Player player = event.getEntity();
        if (player.isCrouching() && !player.level().isClientSide()) {
            ResourceLocation loc = ForgeRegistries.ITEMS.getKey(stack.getItem());
            if (loc != null && loc.getPath().contains("rainfall_serenity")) {
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 100, 1, false, false, true));
            }
        }
    }
}