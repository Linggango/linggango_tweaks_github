package com.misanthropy.linggango.linggango_tweaks.mixin.fixes;

import com.mojang.serialization.Codec;
import net.mehvahdjukaar.supplementaries.common.block.TextHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextHolder.class)
public abstract class SupplementariesSignCrashFix {

    @Shadow(remap = false) @Final private int lines;

    @Shadow(remap = false)
    private static Codec<Component[]> compCodec(int size) {
        return null;
    }

    @Inject(method = "load", at = @At("HEAD"), remap = false)
    private void linggango$removeMalformedTextHolderData(CompoundTag compound, Level level, BlockPos pos, CallbackInfo ci) {
        if (!compound.contains("TextHolder")) return;

        if (!compound.contains("TextHolder", 10)) {
            compound.remove("TextHolder");
            return;
        }

        if (this.lines <= 0) return;

        CompoundTag textHolder = compound.getCompound("TextHolder");
        if (this.linggango$isMalformedMessage(textHolder.get("message"))) {
            compound.remove("TextHolder");
        } else {
            Tag filteredMessage = textHolder.get("filtered_message");
            if (filteredMessage != null && this.linggango$isMalformedMessage(filteredMessage)) {
                textHolder.remove("filtered_message");
            }
        }
    }

    @Unique
    private boolean linggango$isMalformedMessage(Tag messageTag) {
        return messageTag == null || !compCodec(this.lines).decode(NbtOps.INSTANCE, messageTag).result().isPresent();
    }
}