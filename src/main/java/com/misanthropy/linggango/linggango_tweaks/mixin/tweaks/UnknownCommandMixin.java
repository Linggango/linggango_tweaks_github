package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks;

import com.mojang.brigadier.exceptions.BuiltInExceptionProvider;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Commands.class)
public class UnknownCommandMixin {
    @Redirect(
            method = "getParseException",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/brigadier/exceptions/BuiltInExceptionProvider;dispatcherUnknownCommand()Lcom/mojang/brigadier/exceptions/SimpleCommandExceptionType;",
                    remap = false
            )
    )
    private static SimpleCommandExceptionType linggango$customUnknownCommand(BuiltInExceptionProvider instance) {
        return new SimpleCommandExceptionType(Component.literal("Unknown command. Please check your spelling or permissions!"));
    }
}