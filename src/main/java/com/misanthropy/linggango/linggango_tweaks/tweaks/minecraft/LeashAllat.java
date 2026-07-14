package com.misanthropy.linggango.linggango_tweaks.tweaks.minecraft;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jspecify.annotations.NonNull;

@Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LeashAllat {

    @SubscribeEvent
    public static void onMobTick(LivingEvent.@NonNull LivingTickEvent event) {
        if (event.getEntity() instanceof Mob mob && mob.isLeashed()) {
            Entity holder = mob.getLeashHolder();
            if (holder == null) return;

            double distanceSqr = mob.distanceToSqr(holder);
            if (distanceSqr > 36.0D) {
                double distance = Math.sqrt(distanceSqr);
                double speed = (distance - 6.0D) * 0.05D;
                double scale = Math.max(speed, 0.2D) / distance;
                double vx = (holder.getX() - mob.getX()) * scale;
                double vy = (holder.getY() - mob.getY()) * scale;
                double vz = (holder.getZ() - mob.getZ()) * scale;

                Vec3 motion = mob.getDeltaMovement();
                mob.setDeltaMovement(motion.x + vx, motion.y + vy + (speed * 0.1D), motion.z + vz);
            }
        }
    }
}