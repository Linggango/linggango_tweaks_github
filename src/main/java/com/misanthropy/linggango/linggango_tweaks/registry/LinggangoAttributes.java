package com.misanthropy.linggango.linggango_tweaks.registry;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class LinggangoAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.Keys.ATTRIBUTES, LinggangoTweaks.MOD_ID);

    public static final RegistryObject<Attribute> GUN_DAMAGE = ATTRIBUTES.register("gun_damage",
            () -> new RangedAttribute("attribute.name.linggango_tweaks.gun_damage", 0.0D, 0.0D, 1024.0D).setSyncable(true));
}