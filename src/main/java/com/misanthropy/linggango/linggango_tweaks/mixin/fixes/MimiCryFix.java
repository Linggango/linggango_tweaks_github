package com.misanthropy.linggango.linggango_tweaks.mixin.fixes;

import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.rosemarythyme.simplymore.item.uniques.MimicryItem;
import net.rosemarythyme.simplymore.registry.ModItemsRegistry;
import net.rosemarythyme.simplymore.registry.ModTagRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Mixin(MimicryItem.class)
public abstract class MimiCryFix {

    @Shadow(remap = false)
    public abstract String checkItem(Item item);

    @Shadow(remap = false)
    public abstract boolean isFormEnabled(MimicryItem item, Player user);

    /**
     * @author misanthropy
     * @reason Fixes crash
     */
    @Overwrite(remap = false)
    public String getWeightedRandomForm(String currentForm, Player player) {
        List<String> availableForms = new ArrayList<>();

        for(int i = 0; i < 36; ++i) {
            ItemStack itemStack = player.getInventory().getItem(i);
            if (itemStack.is(ModTagRegistry.ALL)) {
                String formString = this.checkItem(itemStack.getItem());
                if (formString != null && this.isFormEnabled((MimicryItem) ModItemsRegistry.MIMICRY_ITEMS.get(formString).get(), player) && !availableForms.contains(formString)) {
                    availableForms.add(formString);
                }
            }
        }

        int chance = Math.min(15 + availableForms.size() * 10, 50);
        if (player.getRandom().nextIntBetweenInclusive(1, 100) < chance) {
            availableForms.clear();
        }

        availableForms.remove(currentForm);
        if (availableForms.isEmpty()) {
            for(Map.Entry<String, RegistrySupplier<Item>> itemEntry : ModItemsRegistry.MIMICRY_ITEMS.entrySet()) {
                String formString = itemEntry.getKey();
                Item formItem = itemEntry.getValue().get();
                if (this.isFormEnabled((MimicryItem)formItem, player) && !availableForms.contains(formString) && !formString.equals(currentForm)) {
                    availableForms.add(formString);
                }
            }
        }

        if (availableForms.isEmpty()) {
            return currentForm;
        }

        return availableForms.get((new Random()).nextInt(availableForms.size()));
    }
}