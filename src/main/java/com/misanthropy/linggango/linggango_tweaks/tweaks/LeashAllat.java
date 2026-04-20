package com.misanthropy.linggango.linggango_tweaks.tweaks;

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

            double distance = mob.distanceTo(holder);
            if (distance > 6.0D) {
                double speed = (distance - 6.0D) * 0.05D;
                Vec3 vec3 = (new Vec3(holder.getX() - mob.getX(), holder.getY() - mob.getY(), holder.getZ() - mob.getZ()))
                        .normalize()
                        .scale(Math.max(speed, 0.2D));
                mob.setDeltaMovement(mob.getDeltaMovement().add(vec3.x, vec3.y + (speed * 0.1D), vec3.z));
            }
        }
    }
}