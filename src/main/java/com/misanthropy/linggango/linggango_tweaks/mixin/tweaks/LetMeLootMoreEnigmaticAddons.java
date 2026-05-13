package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks; // Credits: https://www.curseforge.com/minecraft/mc-mods/celestweaks

import auviotre.enigmatic.addon.contents.objects.SpecialLootModifier;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(value = SpecialLootModifier.class)
public class LetMeLootMoreEnigmaticAddons {

    @Redirect(
            method = "doApply(Lit/unimi/dsi/fastutil/objects/ObjectArrayList;Lnet/minecraft/world/level/storage/loot/LootContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/aizistral/enigmaticlegacy/handlers/SuperpositionHandler;setPersistentBoolean(Lnet/minecraft/world/entity/player/Player;Ljava/lang/String;Z)V"
            ),
            remap = false
    )
    private void linggango$redirectSetPersistentBoolean(Player player, String tag, boolean value) {
    }
}