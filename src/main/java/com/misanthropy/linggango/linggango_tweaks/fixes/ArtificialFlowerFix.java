package com.misanthropy.linggango.linggango_tweaks.fixes; // Credits: https://www.curseforge.com/minecraft/mc-mods/celestweaks

import auviotre.enigmatic.addon.contents.items.ArtificialFlower;
import com.google.common.collect.ImmutableMultimap;
import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

@Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID)
public class ArtificialFlowerFix {

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!ModList.get().isLoaded("enigmaticaddons")) return;

        if (event.getOriginal() instanceof ServerPlayer oldPlayer && event.getEntity() instanceof ServerPlayer newPlayer) {
            Map<ImmutableMultimap<Attribute, AttributeModifier>, Integer> oldMap = ArtificialFlower.PLAYER_ATTRIBUTE_MAP.remove(oldPlayer);

            if (oldMap != null && !oldMap.isEmpty()) {
                ArtificialFlower.PLAYER_ATTRIBUTE_MAP.put(newPlayer, oldMap);
            }
        }
    }
}