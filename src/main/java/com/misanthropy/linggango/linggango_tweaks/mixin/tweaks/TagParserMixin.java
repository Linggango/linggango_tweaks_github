package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TagParser.class)
public abstract class TagParserMixin { // Based of CrashExploitFixer by drexhd.
    @Shadow(aliases="reader")
    @Final private StringReader f_129347_;
    @Unique private static final SimpleCommandExceptionType ERROR_COMPLEX_NBT = new SimpleCommandExceptionType(new LiteralMessage("NBT tag is too complex, depth > 512"));

    @Unique private int linggango$depth;

    @Inject(
            method = "readStruct",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/TagParser;expect(C)V", ordinal = 0, shift = Shift.AFTER)
    )
    private void increaseDepthStruct(CallbackInfoReturnable<CompoundTag> cir) throws CommandSyntaxException {
        this.linggango_tweaks$increaseDepth();
    }

    @Inject(
            method = "readStruct",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/TagParser;expect(C)V", ordinal = 2, shift = Shift.AFTER)
    )
    private void decreaseDepthStruct(CallbackInfoReturnable<CompoundTag> cir) {
        --this.linggango$depth;
    }

    @Inject(
            method = "readListTag",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/TagParser;expect(C)V", ordinal = 0, shift = Shift.AFTER)
    )
    private void increaseDepthList(CallbackInfoReturnable<CompoundTag> cir) throws CommandSyntaxException {
        this.linggango_tweaks$increaseDepth();
    }

    @Inject(
            method = "readListTag",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/TagParser;expect(C)V", ordinal = 1, shift = Shift.AFTER)
    )
    private void decreaseDepthList(CallbackInfoReturnable<CompoundTag> cir) {
        --this.linggango$depth;
    }

    @Unique
    private void linggango_tweaks$increaseDepth() throws CommandSyntaxException {
        ++this.linggango$depth;
        if (this.linggango$depth > 512) {
            throw ERROR_COMPLEX_NBT.createWithContext(this.f_129347_);
        }
    }
}