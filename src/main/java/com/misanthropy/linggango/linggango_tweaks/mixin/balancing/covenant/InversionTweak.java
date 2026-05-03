package com.misanthropy.linggango.linggango_tweaks.mixin.balancing.covenant;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.llenzzz.covenant_of_the_seven.events.item.curse.InversionEvents", remap = false)
public abstract class InversionTweak {

    @Redirect(
            method = "onHurt",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;m_5634_(F)V"),
            remap = false
    )
    private static void linggango$nerfInversionHealing(Player instance, float amount) {
        instance.heal(amount * 0.10f);
    }

    @Inject(
            method = "onEffectReceived",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void linggango$flipChance(MobEffectEvent.Applicable event, CallbackInfo ci) {
        if (Math.random() < 0.5) {
            ci.cancel();
        }
    }

    @ModifyConstant(
            method = "onEffectReceived",
            constant = @Constant(intValue = 255),
            remap = false
    )
    private static int linggango$limitStacking(int original) {
        return 3;
    }
}