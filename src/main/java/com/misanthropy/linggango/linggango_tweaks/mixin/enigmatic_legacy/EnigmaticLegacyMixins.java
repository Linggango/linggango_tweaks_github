package com.misanthropy.linggango.linggango_tweaks.mixin.enigmatic_legacy;

import com.aizistral.enigmaticlegacy.crafting.HiddenRecipe;
import com.aizistral.enigmaticlegacy.items.CursedRing;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.misanthropy.linggango.linggango_tweaks.events.ScumSynergyEvents;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.api.SlotContext;

import java.util.List;
import java.util.UUID;

public final class EnigmaticLegacyMixins {
    private EnigmaticLegacyMixins() {}
}

@Mixin(targets = "com.aizistral.enigmaticlegacy.items.CursedRing", remap = false)
class CursedRingMixin {
    @Unique
    private static final UUID SCUM_SPELL_POWER_UUID = UUID.nameUUIDFromBytes("scum_spell_power".getBytes());
    @Unique
    private static final UUID SCUM_SUMMON_POWER_UUID = UUID.nameUUIDFromBytes("scum_summon_power".getBytes());

    @Inject(method = "getAttributeModifiers", at = @At("RETURN"), cancellable = true)
    private void linggango_tweaks$modifyScumRingAttributes(@NonNull SlotContext slotContext, UUID uuid, ItemStack stack, @NonNull CallbackInfoReturnable<Multimap<Attribute, AttributeModifier>> cir) {
        if (!ScumSynergyEvents.isScumClass(slotContext.entity())) return;
        Multimap<Attribute, AttributeModifier> newMap = ArrayListMultimap.create(cir.getReturnValue());
        newMap.removeAll(Attributes.ARMOR);
        newMap.removeAll(Attributes.ARMOR_TOUGHNESS);
        double reducedDebuff = -CursedRing.armorDebuff.getValue().asModifier() * 0.3;

        newMap.put(Attributes.ARMOR, new AttributeModifier(
                UUID.fromString("457d0ac3-69e4-482f-b636-22e0802da6bd"),
                "enigmaticlegacy:armor_modifier", reducedDebuff, AttributeModifier.Operation.MULTIPLY_TOTAL));

        newMap.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(
                UUID.fromString("95e70d83-3d50-4241-a835-996e1ef039bb"),
                "enigmaticlegacy:armor_toughness_modifier", reducedDebuff, AttributeModifier.Operation.MULTIPLY_TOTAL));

        Attribute spellPower = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation("irons_spellbooks", "spell_power"));
        if (spellPower != null) {
            newMap.put(spellPower, new AttributeModifier(SCUM_SPELL_POWER_UUID, "Scum Spell Power", 0.30, AttributeModifier.Operation.MULTIPLY_BASE));
        }

        Attribute summonPower = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation("irons_spellbooks", "summon_damage"));
        if (summonPower != null) {
            newMap.put(summonPower, new AttributeModifier(SCUM_SUMMON_POWER_UUID, "Scum Summon Power", 0.50, AttributeModifier.Operation.MULTIPLY_BASE));
        }
        cir.setReturnValue(newMap);
    }
}

@Mixin(targets = "com.aizistral.enigmaticlegacy.brewing.AbstractBrewingRecipe", remap = false)
class EnigmaticBrewingCrashFixMixin {

    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;add(Ljava/lang/Object;)Z",
                    remap = false
            ),
            require = 0
    )
    private boolean bypassNullListCrash(@Nullable List<Object> list, Object recipe) {
        if (list == null) {
            return false;
        }
        return list.add(recipe);
    }
}

@Mixin(targets = "com.aizistral.enigmaticlegacy.helpers.ItemNBTHelper", remap = false)
class EnigmaticFix {

    @Inject(
            method = "verifyExistance",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void linggango$preventVerifyCrash(@Nullable ItemStack stack, String tag, @NonNull CallbackInfoReturnable<Boolean> cir) {
        if (stack == null || !stack.hasTag()) {
            cir.setReturnValue(false);
        }
    }
}

@Mixin(HiddenRecipe.class)
class HiddenRecipeMixin {

    @Inject(
            method = "assemble(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;",
            at = @At("RETURN"),
            cancellable = true,
            remap = false
    )
    private void linggango$transferPrimevalData(@NonNull CraftingContainer inv, RegistryAccess access, @NonNull CallbackInfoReturnable<ItemStack> cir) {
        ItemStack result = cir.getReturnValue();

        if (result.isEmpty()) {
            return;
        }

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack ingredient = inv.getItem(i);

            if (ingredient.is(EnigmaticItems.ENIGMATIC_ITEM) && ingredient.hasTag()) {
                CompoundTag sourceTag = ingredient.getTag();

                if (sourceTag != null && sourceTag.contains("PrimevalDescender")) {
                    Tag data = sourceTag.get("PrimevalDescender");

                    if (data != null) {
                        result.getOrCreateTag().put("PrimevalDescender", data.copy());
                        cir.setReturnValue(result);
                        return;
                    }
                }
            }
        }
    }
}