package com.misanthropy.linggango.linggango_tweaks.tweaks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = "linggango_tweaks", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FocusScarfTweak {
    private static final String COOLDOWN_TAG = "FocusScarfCooldown";
    private static final int COOLDOWN_TICKS = 2400;
    @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOWEST, receiveCanceled = true)
    public static void onEntityDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        Level world = player.level();
        if (world.isClientSide()) return;

        CuriosApi.getCuriosHelper().findFirstCurio(player, stack ->
                stack.getItem().getClass().getSimpleName().equals("FocusScarfItem")
        ).ifPresent(slotResult -> {
            ItemStack scarf = slotResult.stack();
            long currentTime = world.getGameTime();
            CompoundTag tag = scarf.getOrCreateTag();

            boolean onCooldown = tag.contains(COOLDOWN_TAG) && (currentTime < tag.getLong(COOLDOWN_TAG) + COOLDOWN_TICKS);

            if (event.isCanceled() && player.getHealth() > 0.0F) {
                if (onCooldown) {
                    event.setCanceled(false);
                    player.setHealth(0.0F);
                } else {
                    tag.putLong(COOLDOWN_TAG, currentTime);
                    stripTerramityCooldown(player);
                }
            } else if (!event.isCanceled()) {
                if (!onCooldown) {
                    event.setCanceled(true);
                    player.setHealth(1.0F);
                    tag.putLong(COOLDOWN_TAG, currentTime);

                    BlockPos pos = player.blockPosition();
                    world.playSound(null, pos, Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("item.totem.use"))),
                            SoundSource.PLAYERS, 1.0F, 1.0F);

                    if (world instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.0, player.getZ(),
                                20, 0.3, 0.5, 0.3, 0.5);
                    }
                    stripTerramityCooldown(player);
                }
            }
        });
    }

    private static void stripTerramityCooldown(Player player) {
        player.getActiveEffects().stream()
                .filter(instance -> instance.getEffect().getDescriptionId().toLowerCase().contains("cooldown"))
                .map(net.minecraft.world.effect.MobEffectInstance::getEffect)
                .toList()
                .forEach(player::removeEffect);
    }
}