package com.misanthropy.linggango.linggango_tweaks;

import com.misanthropy.linggango.linggango_tweaks.chaos.ChaosDifficultyAddon;
import com.misanthropy.linggango.linggango_tweaks.config.DisplayClientConfig;
import com.misanthropy.linggango.linggango_tweaks.config.SpawnerClientConfig;
import com.misanthropy.linggango.linggango_tweaks.config.TweaksConfig;
import com.misanthropy.linggango.linggango_tweaks.enchant.LinggangoEnchantments;
import com.misanthropy.linggango.linggango_tweaks.network.handler.NetworkHandler;
import com.misanthropy.linggango.linggango_tweaks.network.parry.ParryNetwork;
import com.misanthropy.linggango.linggango_tweaks.registry.attribute.LinggangoAttributes;
import com.misanthropy.linggango.linggango_tweaks.registry.particles.ModParticles;
import com.misanthropy.linggango.linggango_tweaks.registry.sounds.SoundRegistry;
import com.misanthropy.linggango.linggango_tweaks.ring_selection.ClientAccess;
import com.misanthropy.linggango.linggango_tweaks.server.parry.ParryServerHandler;
import com.misanthropy.linggango.linggango_tweaks.skills.network.TweaksSkillNetwork;
import com.misanthropy.linggango.linggango_tweaks.tweaks.minecraft.LeashAllat;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@Mod(LinggangoTweaks.MOD_ID)
public class LinggangoTweaks {
    public static final String MOD_ID = "linggango_tweaks";

    public static final DeferredRegister<Codec<? extends BiomeModifier>> BIOME_MODIFIERS = DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, MOD_ID);
    public static final DeferredRegister<StructureProcessorType<?>> PROCESSORS = DeferredRegister.create(Registries.STRUCTURE_PROCESSOR, MOD_ID);
    public static final DeferredRegister<StructurePlacementType<?>> PLACEMENTS = DeferredRegister.create(Registries.STRUCTURE_PLACEMENT, MOD_ID);
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS = DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MOD_ID);

//    public static final ResourceLocation APOSTLE_CONTEXT = new ResourceLocation(MOD_ID, "apostle_context");

    public LinggangoTweaks() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        registerModRegistries(modEventBus);

        TweaksConfig.register();
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, SpawnerClientConfig.SPEC, "linggango-client.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, DisplayClientConfig.SPEC, "linggango-display.toml");

        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(LeashAllat.class);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientAccess.initClient(modEventBus));
        ParryServerHandler.register();
        TweaksSkillNetwork.register();
        ChaosDifficultyAddon.registerChaos();
        ParryNetwork.register();
    }

    private void registerModRegistries(IEventBus modEventBus) {
        BIOME_MODIFIERS.register(modEventBus);
        LinggangoEnchantments.ENCHANTMENTS.register(modEventBus);
        LOOT_MODIFIERS.register(modEventBus);
        PROCESSORS.register(modEventBus);
        PLACEMENTS.register(modEventBus);
        LinggangoAttributes.ATTRIBUTES.register(modEventBus);
        ModParticles.register(modEventBus);
        SoundRegistry.register(modEventBus);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(NetworkHandler::register);
    }
}