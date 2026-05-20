package com.misanthropy.linggango.linggango_tweaks.datagen;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class TagGenerator extends ItemTagsProvider {

    private static final Set<String> EXCLUDED_KNIGHTQUEST_ARMOR = Set.of(
            "knightquest:witch_helmet",
            "knightquest:witch_chestplate",
            "knightquest:witch_leggings",
            "knightquest:witch_boots"
    );

    public TagGenerator(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider, BlockTagsProvider blockTags, ExistingFileHelper existingFileHelper) {
        super(packOutput, lookupProvider, blockTags.contentsGetter(), LinggangoTweaks.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        var netheriteArmorTag = tag(ItemTags.create(new ResourceLocation("forge", "armors/netherite")));

        for (Item item : ForgeRegistries.ITEMS) {
            ResourceLocation registryName = ForgeRegistries.ITEMS.getKey(item);

            if (registryName != null && registryName.getNamespace().equals("knightquest")) {
                String path = registryName.getPath();

                boolean isArmor = path.endsWith("_helmet") || path.endsWith("_chestplate") ||
                        path.endsWith("_leggings") || path.endsWith("_boots");

                if (isArmor && !EXCLUDED_KNIGHTQUEST_ARMOR.contains(registryName.toString())) {
                    netheriteArmorTag.add(item);
                }
            }
        }
    }
}