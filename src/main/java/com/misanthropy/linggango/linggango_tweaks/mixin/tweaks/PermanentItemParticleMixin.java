package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks;

import com.misanthropy.linggango.linggango_tweaks.config.TweaksConfig;
import com.misanthropy.linggango.linggango_tweaks.util.legacy.EnigmaticReflectionUtil;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(ItemEntity.class)
public abstract class PermanentItemParticleMixin {
    @Unique
    private static final Random linggango_tweaks$RANDOM = new Random();

    @Unique
    private int linggango$particleTickCounter = 0;

    @Inject(
            method = "tick",
            at = @At("TAIL")
    )
    private void linggango$tickParticles(CallbackInfo ci) {
        ItemEntity pie = (ItemEntity) (Object) this;
        ItemStack stack = pie.getItem();
        Level world = pie.level();

        if (world.isClientSide() || stack.isEmpty()) return;

        linggango$particleTickCounter++;
        if (linggango$particleTickCounter > 100000) linggango$particleTickCounter = 0;

        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemId == null) return;

        ServerLevel server = (ServerLevel) world;
        double cx = pie.getX();
        double cy = pie.getY() + 0.25;
        double cz = pie.getZ();
        String idStr = itemId.toString();

        if (idStr.equals("enigmaticlegacy:enigmatic_item")) {
            ParticleOptions purpleStarDust = EnigmaticReflectionUtil.getPurpleStarDust();
            ParticleOptions abyssChaos = EnigmaticReflectionUtil.getAbyssChaos();

            double angle = pie.tickCount * Math.PI / 50.0;
            double radius = 2.0;

            if (linggango$particleTickCounter % 3 == 0 && purpleStarDust != null) {
                server.sendParticles(purpleStarDust, cx + Math.cos(angle) * radius, cy, cz + Math.sin(angle) * radius, 1, 0.0, 0.0, 0.0, 0.0);
                server.sendParticles(purpleStarDust, cx + Math.cos(Math.PI + angle) * radius, cy, cz + Math.sin(Math.PI + angle) * radius, 1, 0.0, 0.0, 0.0, 0.0);
            }

            if (abyssChaos != null && linggango_tweaks$RANDOM.nextInt(3) == 0) {
                double r = Math.cbrt(linggango_tweaks$RANDOM.nextDouble() * (Math.pow(2.2, 3.0) - Math.pow(1.2, 3.0)) + Math.pow(1.2, 3.0));
                double theta = linggango_tweaks$RANDOM.nextDouble() * 2.0 * Math.PI;
                double phi = Math.acos(2.0 * linggango_tweaks$RANDOM.nextDouble() - 1.0);

                server.sendParticles(abyssChaos, cx + r * Math.sin(phi) * Math.cos(theta), cy + r * Math.cos(phi), cz + r * Math.sin(phi) * Math.sin(theta), 1, 0.0, 0.0, 0.0, 0.0);
            }
        }

        else if (TweaksConfig.ORBITAL_PARTICLE_ITEMS.get().contains(idStr)) {
            if (pie.tickCount % 2 != 0) return;

            double baseAngle = pie.tickCount * Math.PI / 60.0;
            ParticleOptions[] particles = linggango$getCustomParticles(idStr);

            for (int i = 0; i < particles.length; ++i) {
                ParticleOptions particle = particles[i];
                if (particle != null) {
                    double px = 0.0, py = 0.0, pz = 0.0;
                    switch (i) {
                        case 0 -> {
                            px = cx + Math.cos(baseAngle * 2.0) * 2.0;
                            pz = cz + Math.sin(baseAngle * 2.0) * 2.0;
                            py = cy;
                        }
                        case 1 -> {
                            px = cx;
                            py = cy + Math.cos(baseAngle) * 2.4;
                            pz = cz + Math.sin(baseAngle) * 2.4;
                        }
                        case 2 -> {
                            px = cx + Math.cos(baseAngle / 2.0) * 2.88;
                            py = cy + Math.sin(baseAngle / 2.0) * 2.88;
                            pz = cz;
                        }
                    }
                    server.sendParticles(particle, px, py, pz, 1, 0.0, 0.0, 0.0, 0.0);
                }
            }
        }
    }

    @Unique
    private ParticleOptions[] linggango$getCustomParticles(String itemId) {
        if (itemId.equals("lethality:nightmare_sword")) {
            return new ParticleOptions[]{ParticleTypes.SQUID_INK, ParticleTypes.SMOKE, ParticleTypes.DRAGON_BREATH};
        } else if (itemId.equals("brutality:royal_guardian_sword")) {
            return new ParticleOptions[]{ParticleTypes.END_ROD, ParticleTypes.GLOW, ParticleTypes.WAX_ON};
        }

        return new ParticleOptions[]{
                EnigmaticReflectionUtil.getPurpleStarDust(),
                EnigmaticReflectionUtil.getBlueStarDust(),
                EnigmaticReflectionUtil.getRedStarDust()
        };
    }
}