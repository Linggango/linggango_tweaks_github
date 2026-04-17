package com.misanthropy.linggango.linggango_tweaks.events;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import com.misanthropy.linggango.linggango_tweaks.enchant.LinggangoEnchantments;
import com.misanthropy.linggango.linggango_tweaks.registry.LinggangoAttributes;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;

@Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GunEnchantmentEventHandler {

    @SubscribeEvent
    public static void onPlayerAttack(AttackEntityEvent event) {
        event.getEntity().getPersistentData().putLong("linggango_last_melee", event.getEntity().level().getGameTime());
    }

    @SuppressWarnings("resource")
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            ItemStack weapon = player.getMainHandItem();
            ResourceLocation weaponId = ForgeRegistries.ITEMS.getKey(weapon.getItem());

            if (weaponId == null || !weaponId.getNamespace().equals("terramity")) {
                return;
            }

            boolean isProjectile = event.getSource().getDirectEntity() instanceof net.minecraft.world.entity.projectile.Projectile;

            if (!isProjectile) {
                long lastMeleeTick = player.getPersistentData().getLong("linggango_last_melee");
                if (player.level().getGameTime() - lastMeleeTick <= 2) {
                    return;
                }
            }

            float originalDamage = event.getAmount();
            LivingEntity target = event.getEntity();

            originalDamage *= 5.0f;

            if (player.getAttributes().hasAttribute(LinggangoAttributes.GUN_DAMAGE.get())) {
                double damagePoints = player.getAttributeValue(LinggangoAttributes.GUN_DAMAGE.get());
                if (damagePoints > 0) {
                    originalDamage = originalDamage * (float) (1.0 + (damagePoints * 0.10));
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
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
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

    private static Item getAmmoForGun(Item gunItem) {
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