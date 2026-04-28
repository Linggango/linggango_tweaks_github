package com.misanthropy.linggango.linggango_tweaks.tweaks;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.List;

@Mod.EventBusSubscriber(modid = "linggango_tweaks", bus = Mod.EventBusSubscriber.Bus.FORGE)
public abstract class GlassRingsTweak {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onFatalDamage(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        float incomingDamage = event.getAmount();
        if (incomingDamage < player.getHealth()) return;

        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            var daemoniumSlot = handler.findFirstCurio(stack ->
                    stack.getItem().getClass().getSimpleName().equals("DaemoniumGlassRingItem"));

            if (daemoniumSlot.isPresent()) {
                event.setAmount(incomingDamage * 0.02F);
                breakRing(player, daemoniumSlot.get().stack());
                return;
            }

            var glassSlot = handler.findFirstCurio(stack ->
                    stack.getItem().getClass().getSimpleName().equals("GlassRingItem"));

            if (glassSlot.isPresent()) {
                event.setAmount(incomingDamage * 0.10F);
                breakRing(player, glassSlot.get().stack());
            }
        });
    }

    private static void breakRing(Player player, ItemStack stack) {
        player.broadcastBreakEvent(player.getUsedItemHand());
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                net.minecraft.sounds.SoundEvents.GLASS_BREAK, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
        stack.shrink(1);
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        String className = stack.getItem().getClass().getSimpleName();

        if (className.equals("GlassRingItem") || className.equals("DaemoniumGlassRingItem")) {
            List<Component> list = event.getToolTip();

            if (!list.isEmpty()) {
                Component name = list.get(0);
                list.clear();
                list.add(name);
            }

            list.add(Component.literal("§6Passive Effect:§r"));
            list.add(Component.literal("- Will break upon taking fatal damage,"));

            if (className.equals("DaemoniumGlassRingItem")) {
                list.add(Component.literal("  protecting you from §d98%§r of the strike."));
            } else {
                list.add(Component.literal("  protecting you from §e90%§r of the strike."));
            }
        }
    }
}