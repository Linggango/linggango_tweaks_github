package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks; // Credits: https://www.curseforge.com/minecraft/mc-mods/celestweaks

import com.aizistral.enigmaticlegacy.entities.PermanentItemEntity;
import com.misanthropy.linggango.linggango_tweaks.util.addons.AdvancementHelper;
import com.misanthropy.linggango.linggango_tweaks.util.addons.HeartOfCreationPity;
import com.misanthropy.linggango.linggango_tweaks.util.legacy.EnigmaticReflectionUtil;
import com.mojang.logging.LogUtils;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;
import java.util.Set;


@Mixin(PermanentItemEntity.class)
public abstract class PrimevalCubeMixin {
    @Unique
    private static final Logger linggango_tweaks$LOGGER = LogUtils.getLogger();
    @Unique
    private static final Random linggango_tweaks$RANDOM = new Random();


    @Unique
    private static final Set<String> linggango_tweaks$SPELLSTONES = Set.of(
            "enigmaticlegacy:golem_heart", "enigmaticlegacy:eye_of_nebula", "enigmaticlegacy:blazing_core",
            "enigmaticlegacy:void_pearl", "enigmaticlegacy:ocean_stone", "enigmaticlegacy:angel_blessing",
            "enigmaticaddons:forgotten_ice", "enigmaticaddons:revival_leaf", "enigmaticaddons:lost_engine", "enigmaticaddons:illusion_lantern"
    );

    @Inject(
            method = "<init>(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/item/ItemStack;)V",
            at = @At("RETURN"),
            remap = false
    )
    private void linggango$onConstruct(Level worldIn, double x, double y, double z, ItemStack stack, CallbackInfo ci) {
        if (stack == null || stack.isEmpty()) return;
        if (!ModList.get().isLoaded("enigmaticlegacy") || !ModList.get().isLoaded("enigmaticaddons")) return;

        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemId == null || !linggango_tweaks$SPELLSTONES.contains(itemId.toString())) return;

        PermanentItemEntity pie = (PermanentItemEntity) (Object) this;
        double searchRadius = 64.0;
        Player nearest = null;
        double nearestDistSq = Double.MAX_VALUE;

        for (Player p : worldIn.getEntitiesOfClass(Player.class, new AABB(x - searchRadius, y - searchRadius, z - searchRadius, x + searchRadius, y + searchRadius, z + searchRadius))) {
            double distSq = p.distanceToSqr(x, y, z);
            if (distSq < nearestDistSq) {
                nearestDistSq = distSq;
                nearest = p;
            }
        }

        int realityRecompense = 0;
        if (nearest != null) {
            if (EnigmaticReflectionUtil.isOKOne(nearest)) realityRecompense = 1;
            if (EnigmaticReflectionUtil.isTheWorthyOne(nearest)) realityRecompense = 2;

            double chance = HeartOfCreationPity.getCurrentChance(nearest, realityRecompense);

            if (linggango_tweaks$RANDOM.nextDouble() < chance) {
                ItemStack enigmaticItem = EnigmaticReflectionUtil.getEnigmaticItem("enigmatic_item");
                if (enigmaticItem.isEmpty()) return;

                ItemStack heart = enigmaticItem.copy();
                heart.getOrCreateTag().putUUID("PrimevalDescender", nearest.getUUID());

                pie.setItem(heart);
                pie.setOwnerId(nearest.getUUID());
                pie.setThrowerId(nearest.getUUID());

                linggango_tweaks$LOGGER.info("Bound rare Enigmatic Item to player: {} (chance: {}), realityRecompense: {}", nearest.getName().getString(), chance, realityRecompense);
                HeartOfCreationPity.resetPulls(nearest);

                if (!worldIn.isClientSide() && nearest instanceof ServerPlayer serverPlayer) {
                    AdvancementHelper.grantAdvancement(serverPlayer, "celestweaks", "main/decided_by_destiny");

                    ServerLevel server = (ServerLevel) worldIn;
                    ParticleOptions purpleStarDust = EnigmaticReflectionUtil.getPurpleStarDust();
                    ParticleOptions abyssChaos = EnigmaticReflectionUtil.getAbyssChaos();

                    if (purpleStarDust != null && abyssChaos != null) {
                        server.sendParticles(purpleStarDust, x, y, z, 30, 1.5, 1.5, 1.5, 0.2);
                        server.sendParticles(ParticleTypes.REVERSE_PORTAL, x, y, z, 40, 0.0, 0.0, 0.0, 0.25);

                        int ringParticles = 30;
                        double ringRadius = 1.0;
                        for (int i = 0; i < ringParticles; ++i) {
                            double angle = (Math.PI * 2D) * (double) i / ringParticles;
                            server.sendParticles(purpleStarDust, x + Math.cos(angle) * ringRadius, y, z + Math.sin(angle) * ringRadius, 1, 0.0, 0.0, 0.0, 0.08);
                        }

                        for (int i = 0; i < 50; ++i) {
                            server.sendParticles(abyssChaos, x + (linggango_tweaks$RANDOM.nextDouble() - 0.5) * 4.0, y + linggango_tweaks$RANDOM.nextDouble() * 1.5, z + (linggango_tweaks$RANDOM.nextDouble() - 0.5) * 4.0, 1, 0.0, 0.0, 0.0, 0.12);
                        }

                        for (int i = 0; i < 20; ++i) {
                            server.sendParticles(purpleStarDust, x + (linggango_tweaks$RANDOM.nextDouble() - 0.5) * 7.0, y + linggango_tweaks$RANDOM.nextDouble() * 0.5, z + (linggango_tweaks$RANDOM.nextDouble() - 0.5) * 7.0, 1, 0.0, 0.0, 0.0, 0.05);
                        }
                    }
                    worldIn.playSound(null, x, y, z, SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 0.5F);
                }
            } else {
                HeartOfCreationPity.incrementPull(nearest);
                linggango_tweaks$LOGGER.info("Failed roll for rare Enigmatic Item for player: {} (chance: {}), pity now: {}, realityRecompense: {}", nearest.getName().getString(), chance, HeartOfCreationPity.getPullNumber(nearest), realityRecompense);
            }
        }
    }
}