package com.misanthropy.linggango.linggango_tweaks.tweaks;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = "linggango_tweaks", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MobSwabTweak {

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        ItemStack stack = event.getItemStack();

        if (stack.getItem().getClass().getSimpleName().equals("ItemMobSwab")) {
            if (!(event.getTarget() instanceof LivingEntity target)) return;

            ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(target.getType());
            if (id == null) return;

            boolean isVanilla = id.getNamespace().equals("minecraft");

            boolean isBoss = target.getType() == EntityType.WITHER ||
                    target.getType() == EntityType.ENDER_DRAGON ||
                    target.getType() == EntityType.WARDEN ||
                    target.getType() == EntityType.ELDER_GUARDIAN;

            if (!isVanilla || isBoss) {
                Player player = event.getEntity();
                if (player.level().isClientSide) {
                    player.displayClientMessage(Component.literal("§cThis entity's DNA is too complex to swab! Try vanilla mobs."), true);
                }

                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.FAIL);
            }
        }
    }
}