package com.misanthropy.linggango.linggango_tweaks.events;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import com.misanthropy.linggango.linggango_tweaks.registry.LinggangoAttributes;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AttributeAttachmentEvent {

    @SubscribeEvent
    public static void onModifyAttributes(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, LinggangoAttributes.GUN_DAMAGE.get());
    }
}