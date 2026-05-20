package com.misanthropy.linggango.linggango_tweaks;

import com.misanthropy.linggango.linggango_tweaks.chaos.ChaosDifficultyAddon;
import com.misanthropy.linggango.linggango_tweaks.client.LinggangoRichPresence;
import com.misanthropy.linggango.linggango_tweaks.config.AtmosphereConfigManager;
import com.misanthropy.linggango.linggango_tweaks.config.SpawnerClientConfig;
import com.misanthropy.linggango.linggango_tweaks.config.TweaksConfig;
import com.misanthropy.linggango.linggango_tweaks.datagen.TagGenerator;
import com.misanthropy.linggango.linggango_tweaks.enchant.LinggangoEnchantments;
import com.misanthropy.linggango.linggango_tweaks.features.DataManagerForTrees;
import com.misanthropy.linggango.linggango_tweaks.fixes.LanguageRelatedCrashFixes;
import com.misanthropy.linggango.linggango_tweaks.loot.BalancedLootRandomizerModifier;
import com.misanthropy.linggango.linggango_tweaks.network.NetworkHandler;
import com.misanthropy.linggango.linggango_tweaks.parry.network.ParryNetwork;
import com.misanthropy.linggango.linggango_tweaks.qol.technical.LogSpamFilter;
import com.misanthropy.linggango.linggango_tweaks.registry.attribute.LinggangoAttributes;
import com.misanthropy.linggango.linggango_tweaks.registry.dimension.ApollyonDimension;
import com.misanthropy.linggango.linggango_tweaks.registry.features.ModFeatures;
import com.misanthropy.linggango.linggango_tweaks.registry.particles.ModParticles;
import com.misanthropy.linggango.linggango_tweaks.registry.sounds.SoundRegistry;
import com.misanthropy.linggango.linggango_tweaks.skills.network.TweaksSkillNetwork;
import com.misanthropy.linggango.linggango_tweaks.tweaks.jei.JeiSortStuff;
import com.misanthropy.linggango.linggango_tweaks.tweaks.minecraft.LeashAllat;
import com.misanthropy.linggango.linggango_tweaks.tweaks.minecraft.SpawnChanges;
import com.mojang.serialization.Codec;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

@Mod(LinggangoTweaks.MOD_ID)
public class LinggangoTweaks {
    public static final String MOD_ID = "linggango_tweaks";
    public static final DeferredRegister<Codec<? extends BiomeModifier>> BIOME_MODIFIERS = DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, MOD_ID);
    public static final RegistryObject<Codec<SpawnChanges>> SPAWN_CHANGES_CODEC = BIOME_MODIFIERS.register("spawn_changes", () -> Codec.unit(SpawnChanges.INSTANCE));
    public static final DeferredRegister<StructureProcessorType<?>> PROCESSORS = DeferredRegister.create(Registries.STRUCTURE_PROCESSOR, MOD_ID);
    public static final DeferredRegister<StructurePlacementType<?>> PLACEMENTS = DeferredRegister.create(Registries.STRUCTURE_PLACEMENT, MOD_ID);
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS = DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MOD_ID);
    public static final RegistryObject<Codec<BalancedLootRandomizerModifier>> BALANCED_RANDOMIZER = LOOT_MODIFIERS.register("balanced_loot_randomizer", () -> BalancedLootRandomizerModifier.CODEC);
    public static final ResourceLocation APOSTLE_CONTEXT = new ResourceLocation(MOD_ID, "apostle_context");

    public LinggangoTweaks() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        BIOME_MODIFIERS.register(modEventBus);
        LinggangoEnchantments.ENCHANTMENTS.register(modEventBus);
        LOOT_MODIFIERS.register(modEventBus);
        PROCESSORS.register(modEventBus);
        MinecraftForge.EVENT_BUS.addListener((AddReloadListenerEvent event) -> event.addListener(new DataManagerForTrees()));
        PLACEMENTS.register(modEventBus);
        LinggangoAttributes.ATTRIBUTES.register(modEventBus);
        ModParticles.register(modEventBus);
        SoundRegistry.register(modEventBus);
        ModFeatures.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(LeashAllat.class);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::gatherData);

        ApollyonDimension.register();

        TweaksConfig.register();
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, SpawnerClientConfig.SPEC, "linggango-client.toml");

        LogSpamFilter.register();
        TweaksSkillNetwork.register();
        ChaosDifficultyAddon.registerChaos();
        ParryNetwork.register();

        LanguageRelatedCrashFixes.fixLocale();
        JeiSortStuff.patchJeiSortOrder();
        LinggangoRichPresence.init();
        AtmosphereConfigManager.load();
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(NetworkHandler::register);
    }

    private void gatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        var packOutput = generator.getPackOutput();
        var existingFileHelper = event.getExistingFileHelper();
        var lookupProvider = event.getLookupProvider();
        var blockTagsProvider = new BlockTagsProvider(packOutput, lookupProvider, LinggangoTweaks.MOD_ID, existingFileHelper) {
            @Override
            protected void addTags(HolderLookup.@NotNull Provider provider) {}
        };

        generator.addProvider(event.includeServer(), blockTagsProvider);
        generator.addProvider(event.includeServer(), new TagGenerator(packOutput, lookupProvider, blockTagsProvider, existingFileHelper));
    }
}