package com.misanthropy.linggango.linggango_tweaks.enchant;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("unused")
public class LinggangoEnchantments {

    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, LinggangoTweaks.MOD_ID);

    public static final RegistryObject<Enchantment> EXTRA_PROTECTION = ENCHANTMENTS.register("extra_protection",
            () -> new ExtraProtectionEnchantment(Enchantment.Rarity.COMMON, ExtraProtectionEnchantment.ProtectionType.ALL));

    public static final RegistryObject<Enchantment> EXTRA_BLAST_PROTECTION = ENCHANTMENTS.register("extra_blast_protection",
            () -> new ExtraProtectionEnchantment(Enchantment.Rarity.RARE, ExtraProtectionEnchantment.ProtectionType.EXPLOSION));

    public static final RegistryObject<Enchantment> EXTRA_FIRE_PROTECTION = ENCHANTMENTS.register("extra_fire_protection",
            () -> new ExtraProtectionEnchantment(Enchantment.Rarity.UNCOMMON, ExtraProtectionEnchantment.ProtectionType.FIRE));

    public static final RegistryObject<Enchantment> EXTRA_PROJECTILE_PROTECTION = ENCHANTMENTS.register("extra_projectile_protection",
            () -> new ExtraProtectionEnchantment(Enchantment.Rarity.UNCOMMON, ExtraProtectionEnchantment.ProtectionType.PROJECTILE));

    public static final RegistryObject<Enchantment> VEST_SHREDDER = ENCHANTMENTS.register("vest_shredder", VestShredderEnchantment::new);
    public static final RegistryObject<Enchantment> BULLET_ECHO = ENCHANTMENTS.register("bullet_echo", BulletEchoEnchantment::new);
    public static final RegistryObject<Enchantment> ARCANE_ROUNDS = ENCHANTMENTS.register("arcane_rounds", ArcaneRoundsEnchantment::new);
    public static final RegistryObject<Enchantment> ELEMENTAL_BARRAGE = ENCHANTMENTS.register("elemental_barrage", ElementalBarrageEnchantment::new);
    public static final RegistryObject<Enchantment> SHRAPNEL_PIERCER = ENCHANTMENTS.register("shrapnel_piercer", ShrapnelPiercerEnchantment::new);
    public static final RegistryObject<Enchantment> OVERCLOCKED_ROUNDS = ENCHANTMENTS.register("overclocked_rounds", OverclockedRoundsEnchantment::new);
    public static final RegistryObject<Enchantment> RAIL_CHARGE = ENCHANTMENTS.register("rail_charge", RailChargeEnchantment::new);
    public static final RegistryObject<Enchantment> RESOURCE_MAGAZINES = ENCHANTMENTS.register("resource_magazines", ResourceMagazinesEnchantment::new);
    public static final RegistryObject<Enchantment> EXPLOSIVE_ROUNDS = ENCHANTMENTS.register("explosive_rounds", ExplosiveRoundsEnchantment::new);
}