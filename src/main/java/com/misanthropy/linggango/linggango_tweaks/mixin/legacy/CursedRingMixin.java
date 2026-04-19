package com.misanthropy.linggango.linggango_tweaks.mixin.legacy;

import com.aizistral.enigmaticlegacy.items.CursedRing;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.misanthropy.linggango.linggango_tweaks.events.ScumSynergyEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.api.SlotContext;

import java.util.UUID;

@Mixin(targets = "com.aizistral.enigmaticlegacy.items.CursedRing", remap = false)
public class CursedRingMixin {

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
            newMap.put(spellPower, new AttributeModifier(
                    UUID.nameUUIDFromBytes("scum_spell_power".getBytes()),
                    "Scum Spell Power", 0.30, AttributeModifier.Operation.MULTIPLY_BASE));
        }

        Attribute summonPower = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation("irons_spellbooks", "summon_damage"));
        if (summonPower != null) {
            newMap.put(summonPower, new AttributeModifier(
                    UUID.nameUUIDFromBytes("scum_summon_power".getBytes()),
                    "Scum Summon Power", 0.50, AttributeModifier.Operation.MULTIPLY_BASE));
        }
        cir.setReturnValue(newMap);
    }
}