package com.misanthropy.linggango.linggango_tweaks.events;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import com.misanthropy.linggango.linggango_tweaks.enchant.LinggangoEnchantments;
import com.misanthropy.linggango.linggango_tweaks.registry.LinggangoAttributes;
import com.misanthropy.linggango.linggango_tweaks.skills.SkillManager;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GunEnchantmentEventHandler {

    @SubscribeEvent
    public static void onGunFire(net.minecraftforge.event.entity.player.PlayerInteractEvent.@NonNull RightClickItem event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide && player.getPersistentData().getBoolean("lt_gunner_active")) {
            ResourceLocation weaponId = ForgeRegistries.ITEMS.getKey(event.getItemStack().getItem());
            if (weaponId != null && weaponId.getNamespace().equals("terramity")) {
                long currentTimeout = player.getPersistentData().getLong("lt_gunner_timeout");
                long expectedHitTime = player.level().getGameTime() + 20;
                if (expectedHitTime < currentTimeout) {
                    player.getPersistentData().putLong("lt_gunner_timeout", expectedHitTime);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerAttack(@NonNull AttackEntityEvent event) {
        event.getEntity().getPersistentData().putLong("linggango_last_melee", event.getEntity().level().getGameTime());
    }

    @SubscribeEvent
    public static void onLivingAttack(@NonNull LivingAttackEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            ItemStack weapon = player.getMainHandItem();
            ResourceLocation weaponId = ForgeRegistries.ITEMS.getKey(weapon.getItem());
            if (weaponId != null && weaponId.getNamespace().equals("terramity")) {
                event.getEntity().invulnerableTime = 0;
            }
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(@NonNull LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            ItemStack weapon = player.getMainHandItem();
            ResourceLocation weaponId = ForgeRegistries.ITEMS.getKey(weapon.getItem());

            if (weaponId == null || !weaponId.getNamespace().equals("terramity")) {
                return;
            }

            LivingEntity target = event.getEntity();
            target.invulnerableTime = 0;

            boolean isProjectile = event.getSource().getDirectEntity() instanceof net.minecraft.world.entity.projectile.Projectile;

            if (!isProjectile) {
                long lastMeleeTick = player.getPersistentData().getLong("linggango_last_melee");
                if (player.level().getGameTime() - lastMeleeTick <= 2) {
                    return;
                }
            }

            float originalDamage = event.getAmount();
            originalDamage *= 2.5f;

            if (player.getAttributes().hasAttribute(LinggangoAttributes.GUN_DAMAGE.get())) {
                double damagePoints = player.getAttributeValue(LinggangoAttributes.GUN_DAMAGE.get());
                if (damagePoints > 0) {
                    originalDamage = originalDamage * (float) (1.0 + (damagePoints * 0.10));
                }
            }

            if (player.getPersistentData().getBoolean("lt_gunner_active")) {
                float aoeDamage = originalDamage * 0.60f;
                java.util.List<LivingEntity> nearbyEntities = player.level().getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(3.5D));
                for (LivingEntity nearbyTarget : nearbyEntities) {
                    if (nearbyTarget != player && nearbyTarget != target) {
                        nearbyTarget.invulnerableTime = 0;
                        nearbyTarget.hurt(player.level().damageSources().explosion(player, player), aoeDamage);
                    }
                }
                if (player.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.EXPLOSION, target.getX(), target.getY(0.5D), target.getZ(), 2, 0.0D, 0.0D, 0.0D, 0.0D);
                }

                player.getPersistentData().putBoolean("lt_gunner_active", false);
                if (player instanceof ServerPlayer serverPlayer) {
                    SkillManager.setCooldown(serverPlayer, "gunner", 600);
                    SkillManager.syncToClient(serverPlayer, "gunner");
                }
            }

            float bypassPercentage = 0.30f;
            int vestShredderLevel = EnchantmentHelper.getItemEnchantmentLevel(LinggangoEnchantments.VEST_SHREDDER.get(), weapon);

            if (vestShredderLevel > 0) {
                bypassPercentage += (0.10f * vestShredderLevel);
            }

            if (bypassPercentage > 1.0f) {
                bypassPercentage = 1.0f;
            }

            float armorBypassDamage = originalDamage * bypassPercentage;
            originalDamage -= armorBypassDamage;

            event.setAmount(originalDamage);

            target.invulnerableTime = 0;
            target.hurt(player.level().damageSources().magic(), armorBypassDamage);

            int railChargeLevel = EnchantmentHelper.getItemEnchantmentLevel(LinggangoEnchantments.RAIL_CHARGE.get(), weapon);
            if (railChargeLevel > 0) {
                if (Math.random() < 0.10) {
                    originalDamage = originalDamage * 3.0f;
                    event.setAmount(originalDamage);
                }
            }

            int resourceMagazinesLevel = EnchantmentHelper.getItemEnchantmentLevel(LinggangoEnchantments.RESOURCE_MAGAZINES.get(), weapon);
            if (resourceMagazinesLevel > 0) {
                double refundChance = 0.10 * resourceMagazinesLevel;
                if (Math.random() < refundChance) {
                    Item ammoItem = getAmmoForGun(weapon.getItem());
                    if (ammoItem != null) {
                        player.getInventory().add(new ItemStack(ammoItem, 1));
                    }
                }
            }

            int bulletEchoLevel = EnchantmentHelper.getItemEnchantmentLevel(LinggangoEnchantments.BULLET_ECHO.get(), weapon);
            if (bulletEchoLevel > 0) {
                double echoChance = 0.20 + (0.10 * (bulletEchoLevel - 1));
                if (Math.random() < echoChance) {
                    target.invulnerableTime = 0;
                    target.hurt(event.getSource(), originalDamage * 0.50f);
                }
            }

            int arcaneRoundsLevel = EnchantmentHelper.getItemEnchantmentLevel(LinggangoEnchantments.ARCANE_ROUNDS.get(), weapon);
            if (arcaneRoundsLevel > 0) {
                target.invulnerableTime = 0;
                target.hurt(player.level().damageSources().magic(), originalDamage * 0.20f);
            }

            int elementalBarrageLevel = EnchantmentHelper.getItemEnchantmentLevel(LinggangoEnchantments.ELEMENTAL_BARRAGE.get(), weapon);
            if (elementalBarrageLevel > 0) {
                double chance = 0.05 * elementalBarrageLevel;
                if (Math.random() < chance) {
                    MobEffect bleeding = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("attributeslib", "bleeding"));
                    if (bleeding != null) {
                        target.addEffect(new MobEffectInstance(bleeding, 100, 1));
                    }
                    MobEffect vulnerable = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("terramity", "vulnerable"));
                    if (vulnerable != null) {
                        target.addEffect(new MobEffectInstance(vulnerable, 100, 1));
                    }
                    MobEffect armorDecrease = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("aquamirae", "armor_decrease"));
                    if (armorDecrease != null) {
                        target.addEffect(new MobEffectInstance(armorDecrease, 100, 1));
                    }
                }
            }

            int shrapnelLevel = EnchantmentHelper.getItemEnchantmentLevel(LinggangoEnchantments.SHRAPNEL_PIERCER.get(), weapon);
            if (shrapnelLevel > 0) {
                int currentStacks = target.getPersistentData().getInt("ShrapnelStacks");
                int maxStacks = shrapnelLevel == 1 ? 7 : 12;
                if (currentStacks < maxStacks) {
                    currentStacks++;
                    target.getPersistentData().putInt("ShrapnelStacks", currentStacks);
                }
                float defenseReduction = currentStacks * 0.02f;
                float bypassDamage = (originalDamage + armorBypassDamage) * defenseReduction;

                target.invulnerableTime = 0;
                target.hurt(player.level().damageSources().magic(), bypassDamage);
            }

            int overclockLevel = EnchantmentHelper.getItemEnchantmentLevel(LinggangoEnchantments.OVERCLOCKED_ROUNDS.get(), weapon);
            if (overclockLevel > 0) {
                long lastHitTime = player.getPersistentData().getLong("OverclockLastHit");
                long currentTime = player.level().getGameTime();
                int hitCombo = player.getPersistentData().getInt("OverclockCombo");

                if (currentTime - lastHitTime > 40) {
                    hitCombo = 0;
                }
                hitCombo++;

                player.getPersistentData().putLong("OverclockLastHit", currentTime);
                player.getPersistentData().putInt("OverclockCombo", hitCombo);

                float damageBoost = originalDamage * (0.03f * hitCombo);
                event.setAmount(originalDamage + damageBoost);
            }

            int explosiveLevel = EnchantmentHelper.getItemEnchantmentLevel(LinggangoEnchantments.EXPLOSIVE_ROUNDS.get(), weapon);
            if (explosiveLevel > 0) {
                float aoeDamage = originalDamage * 0.30f;
                java.util.List<LivingEntity> nearbyEntities = player.level().getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(3.0D));
                for (LivingEntity nearbyTarget : nearbyEntities) {
                    if (nearbyTarget != player && nearbyTarget != target) {
                        nearbyTarget.invulnerableTime = 0;
                        nearbyTarget.hurt(player.level().damageSources().explosion(player, player), aoeDamage);
                    }
                }
                if (player.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.EXPLOSION, target.getX(), target.getY(0.5D), target.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
                }
            }

            target.invulnerableTime = 0;
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.@NonNull PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide()) {
            ItemStack weapon = event.player.getMainHandItem();
            if (EnchantmentHelper.getItemEnchantmentLevel(LinggangoEnchantments.RAIL_CHARGE.get(), weapon) > 0) {
                MobEffect slowness = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("minecraft", "slowness"));
                if (slowness != null) {
                    event.player.addEffect(new MobEffectInstance(slowness, 20, 0, false, false, true));
                }
            }
        }
    }

    private static @Nullable Item getAmmoForGun(Item gunItem) {
        ResourceLocation gunId = ForgeRegistries.ITEMS.getKey(gunItem);
        if (gunId == null) return Items.IRON_NUGGET;

        String path = gunId.getPath();

        if (path.contains("iridium") || path.equals("stairway_to_heaven")) {
            return ForgeRegistries.ITEMS.getValue(new ResourceLocation("terramity", "iridium_round"));
        } else if (path.contains("gold") || path.equals("advanced_automatic_rifle") || path.equals("requiem")) {
            return ForgeRegistries.ITEMS.getValue(new ResourceLocation("terramity", "gold_round"));
        } else if (path.contains("copper")) {
            return ForgeRegistries.ITEMS.getValue(new ResourceLocation("terramity", "copper_round"));
        } else if (path.contains("antimatter") || path.equals("blasphemic_rapture")) {
            return ForgeRegistries.ITEMS.getValue(new ResourceLocation("terramity", "antimatter_round"));
        } else if (path.contains("shotgun") || path.contains("blunderbuss")) {
            return ForgeRegistries.ITEMS.getValue(new ResourceLocation("terramity", "daemonium_shotshell"));
        } else if (path.contains("echo") || path.equals("anti_material_rifle")) {
            return ForgeRegistries.ITEMS.getValue(new ResourceLocation("terramity", "echo_round"));
        }

        return Items.IRON_NUGGET;
    }
}