package com.misanthropy.linggango.linggango_tweaks.mixin.loot;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Set;

@Mixin(LootTable.class)
public class UnifiedLootTweakMixin {

    @Unique
    private static final Set<Item> linggango$blacklistCache = new HashSet<>();

    @Unique
    private static final Set<Item> linggango$confluenceItemsCache = new HashSet<>();

    @Unique
    private static final Set<Item> linggango$saintsDragonsItemsCache = new HashSet<>();

    @Unique
    private static boolean linggango$cachesInitialized = false;

    @Unique
    private static void linggango$initializeCaches() {
        if (linggango$cachesInitialized) return;

        String[] blacklistedIds = {
                "mythicmetals:unobtainium",
                "enigmaticdelicacy:astral_wood",
                "enigmaticdelicacy:astral_sapling",
                "enigmaticdelicacy:astral_log",
                "enigmaticdelicacy:astral_leaf",
                "artifacts:cross_necklace",
                "artifacts:panic_necklace",
                "artifacts:cloud_in_a_bottle",
                "artifacts:fire_gauntlet",
                "artifacts:flippers",
                "ars_n_spells:spell_transcription"
        };

        for (String id : blacklistedIds) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
            if (item != null && item != Items.AIR) {
                linggango$blacklistCache.add(item);
            }
        }

        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
            if (id != null) {
                String namespace = id.getNamespace();
                if ("confluence".equals(namespace)) {
                    linggango$confluenceItemsCache.add(item);
                } else if ("saintsdragons".equals(namespace)) {
                    linggango$saintsDragonsItemsCache.add(item);
                }
            }
        }

        linggango$cachesInitialized = true;
    }

    @Inject(
            method = "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;",
            at = @At("RETURN")
    )
    private void linggango$tweakLoot(LootContext context, CallbackInfoReturnable<ObjectArrayList<ItemStack>> cir) {
        ObjectArrayList<ItemStack> generatedLoot = cir.getReturnValue();
        if (generatedLoot.isEmpty()) return;

        if (!linggango$cachesInitialized) {
            linggango$initializeCaches();
        }

        generatedLoot.removeIf(stack -> {
            if (stack.isEmpty()) return false;
            Item item = stack.getItem();

            if (!linggango$blacklistCache.isEmpty() && linggango$blacklistCache.contains(item)) {
                return true;
            }

            if (!linggango$confluenceItemsCache.isEmpty() && linggango$confluenceItemsCache.contains(item)) {
                return context.getRandom().nextFloat() < 0.5f;
            }

            if (!linggango$saintsDragonsItemsCache.isEmpty() && linggango$saintsDragonsItemsCache.contains(item)) {
                return context.getRandom().nextFloat() < 0.8f;
            }

            return false;
        });
    }
}