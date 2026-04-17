package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFurnaceBlockEntity.class)
public class VillagerDeathSoundMixin {
    @Inject(method = "serverTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hasCraftingRemainingItem()Z"))
    private static void onFuelBurned(Level level, BlockPos pos, BlockState state, AbstractFurnaceBlockEntity furnace, CallbackInfo ci) {
        ItemStack fuel = furnace.getItem(1);
        if (!fuel.isEmpty()) {
            var itemId = ForgeRegistries.ITEMS.getKey(fuel.getItem());
            if (itemId != null && itemId.toString().equals("easy_villagers:villager")) {
                CompoundTag tag = fuel.getTag();
                if (tag != null && tag.contains("villager")) {
                    if (tag.getCompound("villager").getInt("Age") < 0) {
                        level.playSound(null, pos, SoundEvents.VILLAGER_HURT, SoundSource.BLOCKS, 1.0F, 1.5F + level.random.nextFloat() * 0.3F);

                    }
                }
            }
        }
    }
}