package com.misanthropy.linggango.linggango_tweaks.mixin.fixes;

import com.yanny.ali.api.IKeyTooltipNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.server.ValueTooltipUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("deprecation")
@Mixin(targets = {"com.yanny.ali.plugin.server.RegistriesTooltipUtils"}, remap = false)
public class AliYouAreANiceGuy {
    @Inject(method = "getEnchantmentTooltip", at = @At("HEAD"), cancellable = true, remap = false)
    private static void linggango$skipUnsafeLocalizedEnchantmentName(IServerUtils utils, Enchantment enchantment, CallbackInfoReturnable<IKeyTooltipNode> cir) {
        cir.setReturnValue(ValueTooltipUtils.getBuiltInRegistryTooltip(utils, BuiltInRegistries.ENCHANTMENT, enchantment));
    }
}