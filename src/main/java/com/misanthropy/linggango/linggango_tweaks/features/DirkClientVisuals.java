package com.misanthropy.linggango.linggango_tweaks.features;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ComputeFovModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DirkClientVisuals {

    @SubscribeEvent
    public static void onFovModifier(ComputeFovModifierEvent event) {
        Player player = event.getPlayer();
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.isEmpty()) return;

        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(mainHand.getItem());
        if (itemId != null && itemId.getNamespace().equals("macabre")) {
            String path = itemId.getPath();

            if (path.equals("sac_dir_2") || path.equals("sac_dir_3")) {
                event.setNewFovModifier(event.getFovModifier() * 0.75F);
            }
            else if (path.equals("sac_dir_4")) {
                event.setNewFovModifier(event.getFovModifier() * 0.45F);
            }
        }
    }
}