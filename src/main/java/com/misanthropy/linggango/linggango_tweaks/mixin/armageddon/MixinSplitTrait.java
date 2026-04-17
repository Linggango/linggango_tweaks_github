package com.misanthropy.linggango.linggango_tweaks.mixin.armageddon;

import dev.xkmc.l2hostility.content.traits.highlevel.SplitTrait;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SplitTrait.class)
public class MixinSplitTrait {

    @Unique
    private static final ThreadLocal<Boolean> linggango_tweaks$splitMainNext = ThreadLocal.withInitial(() -> false);
    @Inject(method = "onDeath", at = @At("HEAD"), remap = false)
    private void initSplitMain(int lv, LivingEntity entity, LivingDeathEvent event, CallbackInfo ci) {
        linggango_tweaks$splitMainNext.set(true);
    }
    @Redirect(
        method = "add",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;m_7967_(Lnet/minecraft/world/entity/Entity;)Z"),
        remap = false
    )
    private boolean tagAndAdd(Level level, Entity entity) {
        boolean result = level.addFreshEntity(entity);
        if (linggango_tweaks$splitMainNext.get()) {
            entity.addTag("l2hostility:split_main");
            linggango_tweaks$splitMainNext.set(false);
        } else {
            entity.addTag("l2hostility:split_done");
        }
        return result;
    }
}
