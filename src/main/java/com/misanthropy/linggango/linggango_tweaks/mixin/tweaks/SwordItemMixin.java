package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks; // Mozilla Public License

import com.misanthropy.linggango.linggango_tweaks.skills.SkillManager;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class SwordItemMixin {

    @Inject(method = "getUseDuration", at = @At("HEAD"), cancellable = true)
    public void getUseDuration(ItemStack pStack, CallbackInfoReturnable<Integer> cir) {
        if ((Object) this instanceof SwordItem) {
            cir.setReturnValue(72000);

        }
    }

    @Inject(method = "getUseAnimation", at = @At("HEAD"), cancellable = true)
    public void getUseAnimation(ItemStack pStack, CallbackInfoReturnable<UseAnim> cir) {
        if ((Object) this instanceof SwordItem) {
            cir.setReturnValue(UseAnim.BLOCK);
        }
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void use(Level pLevel, Player pPlayer, InteractionHand pHand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        if ((Object) this instanceof SwordItem) {
            ItemStack itemstack = pPlayer.getItemInHand(pHand);
            String pClass = SkillManager.getPlayerClass(pPlayer);

            if ("warrior".equals(pClass) || "warrior_".equals(pClass)) {
                pPlayer.startUsingItem(pHand);
                cir.setReturnValue(InteractionResultHolder.consume(itemstack));
            }
        }
    }
}