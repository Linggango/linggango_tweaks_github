package com.misanthropy.linggango.linggango_tweaks.mixin.balancing.composite_material;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;

@Mixin(targets = "io.github.rcneg.compositematerial.common.items.dungeontools.DungeonSwordReinforced", remap = false)
public abstract class DungeonSwordMathTweak {

    @Redirect(
            method = {"getAttributeModifiers", "m_7167_"},
            at = @At(value = "NEW", target = "(Ljava/util/UUID;Ljava/lang/String;DLnet/minecraft/world/entity/ai/attributes/AttributeModifier$Operation;)Lnet/minecraft/world/entity/ai/attributes/AttributeModifier;"),
            remap = false
    )
    private AttributeModifier linggango$nerfDungeonDamage(UUID p_22200_, String p_22201_, double p_22202_, AttributeModifier.Operation p_22203_) {
        return new AttributeModifier(p_22200_, p_22201_, p_22202_ * 0.5D, p_22203_);
    }
}