package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import top.theillusivec4.curios.api.SlotContext;

@Mixin(targets = "net.goo.brutality.item.curios.charm.CelestialStarboard", remap = false)
public abstract class CelestialStarboardTweak {

    @Shadow private boolean wasOnGround;

    /**
     * @author Misanthropy
     * @reason Fixing a crash in brutality (the 50001st crash)
     */
    @Overwrite
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (!(entity instanceof Player player)) return;

        Level level = player.level();

        if (level instanceof ServerLevel) {
            boolean currentlyOnGround = player.onGround();
            if (!currentlyOnGround && this.wasOnGround) {
                this.wasOnGround = false;
            } else if (currentlyOnGround && !this.wasOnGround) {
                this.wasOnGround = true;
            }
            return;
        }

        if (level.isClientSide()) {
            linggango$handleClientMobility(player, stack);
        }
    }

    @Unique
    private void linggango$handleClientMobility(Player player, ItemStack stack) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.player != player) return;

        if (mc.options.keyJump.isDown() && mc.options.keyShift.isDown()) {
            if (!player.getCooldowns().isOnCooldown(stack.getItem())) {
                Vec3 lookVec = player.getLookAngle().normalize();
                Vec3 dashVec = new Vec3(lookVec.x, 0.0D, lookVec.z).scale(0.6D);
                player.setDeltaMovement(dashVec);
                player.getCooldowns().addCooldown(stack.getItem(), 100);
            }
        }

        if (mc.options.keyJump.isDown() && !player.onGround()) {
            if (player.getDeltaMovement().y < 0.3D) {
                player.setDeltaMovement(new Vec3(player.getDeltaMovement().x, 0.03D, player.getDeltaMovement().z));
            }
        }
    }
}