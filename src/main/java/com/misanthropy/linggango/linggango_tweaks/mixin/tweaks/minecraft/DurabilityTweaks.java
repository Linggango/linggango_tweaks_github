package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks.minecraft;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class DurabilityTweaks {

    @Inject(method = "getMaxDamage", at = @At("HEAD"), cancellable = true)
    private void linggango$modifyItemDurability(CallbackInfoReturnable<Integer> cir) {
        Item item = (Object) this instanceof Item ? (Item) (Object) this : null;
        if (item == null) return;
        if (item == Items.IRON_CHESTPLATE) { cir.setReturnValue(300); return; }
        if (item == Items.IRON_LEGGINGS) { cir.setReturnValue(260); return; }
        if (item == Items.IRON_BOOTS) { cir.setReturnValue(230); return; }
        if (item == Items.IRON_HELMET) { cir.setReturnValue(200); return; }
        if (item == Items.DIAMOND_CHESTPLATE) { cir.setReturnValue(600); return; }
        if (item == Items.DIAMOND_LEGGINGS) { cir.setReturnValue(520); return; }
        if (item == Items.DIAMOND_HELMET) { cir.setReturnValue(400); return; }
        if (item == Items.DIAMOND_BOOTS) { cir.setReturnValue(400); return; }

        ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
        if (id != null) {
            String namespace = id.getNamespace();
            String path = id.getPath();

            switch (namespace) {
                case "goety" -> {
                    switch (path) {
                        case "dark_chestplate" -> cir.setReturnValue(500);
                        case "dark_leggings" -> cir.setReturnValue(400);
                        case "dark_boots" -> cir.setReturnValue(320);
                        case "dark_helmet" -> cir.setReturnValue(300);
                    }
                }
                case "terramity" -> {
                    switch (path) {
                        case "onyx_armor_chestplate", "topaz_armor_chestplate" -> cir.setReturnValue(400);
                        case "onyx_armor_leggings", "topaz_armor_leggings" -> cir.setReturnValue(320);
                        case "onyx_armor_helmet", "onyx_armor_boots", "topaz_armor_helmet", "topaz_armor_boots" -> cir.setReturnValue(300);
                        case "ruby_armor_chestplate", "sapphire_armor_chestplate" -> cir.setReturnValue(650);
                        case "ruby_armor_leggings", "sapphire_armor_leggings" -> cir.setReturnValue(600);
                        case "ruby_armor_boots", "sapphire_armor_boots" -> cir.setReturnValue(520);
                        case "ruby_armor_helmet", "sapphire_armor_helmet" -> cir.setReturnValue(500);
                    }
                }
                case "composite_material" -> {
                    switch (path) {
                        case "copper_chestplate" -> cir.setReturnValue(450);
                        case "copper_leggings" -> cir.setReturnValue(420);
                        case "copper_helmet", "copper_boots" -> cir.setReturnValue(310);

                        case "amethyst_chestplate", "amethyst_leggings" -> cir.setReturnValue(200);
                        case "amethyst_boots" -> cir.setReturnValue(160);
                        case "amethyst_helmet" -> cir.setReturnValue(130);
                    }
                }
                case "mythicmetals" -> {
                    switch (path) {
                        case "silver_chestplate" -> cir.setReturnValue(140);
                        case "silver_leggings" -> cir.setReturnValue(120);
                        case "silver_boots" -> cir.setReturnValue(110);
                        case "silver_helmet" -> cir.setReturnValue(100);

                        case "osmium_chestplate" -> cir.setReturnValue(500);
                        case "osmium_leggings" -> cir.setReturnValue(450);
                        case "osmium_boots" -> cir.setReturnValue(370);
                        case "osmium_helmet", "midas_gold_chestplate" -> cir.setReturnValue(360);

                        case "banglum_chestplate" -> cir.setReturnValue(260);
                        case "banglum_leggings", "midas_gold_boots", "midas_gold_helmet" -> cir.setReturnValue(250);
                        case "banglum_boots" -> cir.setReturnValue(220);
                        case "banglum_helmet" -> cir.setReturnValue(200);

                        case "mythril_chestplate" -> cir.setReturnValue(620);
                        case "mythril_leggings" -> cir.setReturnValue(550);
                        case "mythril_helmet" -> cir.setReturnValue(420);

                        case "midas_gold_leggings" -> cir.setReturnValue(320);
                    }
                }
                case "mekanismtools" -> {
                    switch (path) {
                        case "lapis_lazuli_chestplate" -> cir.setReturnValue(130);
                        case "lapis_lazuli_leggings" -> cir.setReturnValue(120);
                        case "lapis_lazuli_boots" -> cir.setReturnValue(100);
                        case "lapis_lazuli_helmet" -> cir.setReturnValue(90);
                    }
                }
                case "armageddon_mod" -> {
                    switch (path) {
                        case "novice_builder_armor_chestplate", "gilded_armor_chestplate", "gilded_armor_leggings" ->
                                cir.setReturnValue(200);
                        case "novice_builder_armor_leggings" -> cir.setReturnValue(180);
                        case "novice_builder_armor_helmet" -> cir.setReturnValue(170);
                        case "novice_builder_armor_boots", "gilded_armor_helmet" -> cir.setReturnValue(150);

                    }
                }
                case "mutantmonsters" -> {
                    switch (path) {
                        case "mutant_skeleton_chestplate" -> cir.setReturnValue(300);
                        case "mutant_skeleton_leggings" -> cir.setReturnValue(280);
                        case "mutant_skeleton_boots" -> cir.setReturnValue(220);
                    }
                }
            }
        }
    }
}