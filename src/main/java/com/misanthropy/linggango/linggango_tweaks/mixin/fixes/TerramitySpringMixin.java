package com.misanthropy.linggango.linggango_tweaks.mixin.fixes;

import net.mcreator.terramity.item.SpringItem;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.api.SlotContext;

import java.util.UUID;

@Mixin(SpringItem.class)
public class TerramitySpringMixin {
    @Unique
    private static final UUID SPRING_STEP_UUID = UUID.fromString("5d012437-1234-4321-a123-123456789abc");

    @Inject(method = "onEquip", at = @At("HEAD"), remap = false, cancellable = true)
    private void onEquipMixin(SlotContext slotContext, ItemStack prevStack, ItemStack stack, CallbackInfo ci) {
        LivingEntity entity = slotContext.entity();
        if (entity != null) {
            if (!entity.level().isClientSide()) {
                entity.addEffect(new MobEffectInstance(MobEffects.JUMP, -1, 2, true, false));
            }
            AttributeInstance stepAttr = entity.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get());
            if (stepAttr != null) {
                stepAttr.removeModifier(SPRING_STEP_UUID);
                stepAttr.addPermanentModifier(new AttributeModifier(SPRING_STEP_UUID, "SpringStep", 0.4, AttributeModifier.Operation.ADDITION));
            }
        }
        ci.cancel();
    }

    @Inject(method = "onUnequip", at = @At("HEAD"), remap = false, cancellable = true)
    private void onUnequipMixin(SlotContext slotContext, ItemStack newStack, ItemStack stack, CallbackInfo ci) {
        LivingEntity entity = slotContext.entity();
        if (entity != null) {
            entity.removeEffect(MobEffects.JUMP);
            AttributeInstance stepAttr = entity.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get());
            if (stepAttr != null) {
                stepAttr.removeModifier(SPRING_STEP_UUID);
            }
        }
        ci.cancel();
    }
}