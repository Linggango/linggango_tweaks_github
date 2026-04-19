package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RandomizableContainerBlockEntity.class)
public abstract class ChestLootThinningMixin {

    @Shadow
    protected ResourceLocation lootTable;

    @Unique
    private @Nullable ResourceLocation linggango$cachedTable = null;

    @Inject(method = "unpackLootTable", at = @At("HEAD"))
    private void linggango$captureTable(Player player, CallbackInfo ci) {
        this.linggango$cachedTable = this.lootTable;
    }

    @Inject(method = "unpackLootTable", at = @At("RETURN"))
    private void linggango$investigateAndThinLoot(Player player, CallbackInfo ci) {
        if (this.linggango$cachedTable != null) {
            Container container = (Container) this;

            for (int i = 0; i < container.getContainerSize(); i++) {
                if (!container.getItem(i).isEmpty() && Math.random() > 0.5) {
                    container.setItem(i, ItemStack.EMPTY);
                }
            }

            this.linggango$cachedTable = null;
        }
    }
}