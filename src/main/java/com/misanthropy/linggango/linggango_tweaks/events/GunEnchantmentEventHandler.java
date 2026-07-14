package com.misanthropy.linggango.linggango_tweaks.events;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import com.misanthropy.linggango.linggango_tweaks.enchant.LinggangoEnchantments;
import com.misanthropy.linggango.linggango_tweaks.registry.attribute.LinggangoAttributes;
import com.misanthropy.linggango.linggango_tweaks.skills.manager.SkillManager;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
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
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GunEnchantmentEventHandler {

    private static final TagKey<Item> FORGE_GUNS = ItemTags.create(new ResourceLocation("forge", "guns"));

    private static final ResourceLocation BLEEDING_RL = new ResourceLocation("attributeslib", "bleeding");
    private static final ResourceLocation VULNERABLE_RL = new ResourceLocation("terramity", "vulnerable");
    private static final ResourceLocation ARMOR_DECREASE_RL = new ResourceLocation("aquamirae", "armor_decrease");
    private static final ResourceLocation SLOWNESS_RL = new ResourceLocation("minecraft", "slowness");

    private static final Map<Item, Boolean> IS_GUN_CACHE = new ConcurrentHashMap<>();
    private static final Map<Item, Item> AMMO_MAPPING_CACHE = new ConcurrentHashMap<>();

    private static MobEffect bleedingEffect;
    private static MobEffect vulnerableEffect;
    private static MobEffect armorDecreaseEffect;
    private static MobEffect slownessEffect;
    private static boolean effectsCached = false;

    private static Item iridiumRound;
    private static Item goldRound;
    private static Item copperRound;
    private static Item antimatterRound;
    private static Item daemoniumShotshell;
    private static Item echoRound;
    private static boolean ammoCached = false;

    private static void cacheEffects() {
        if (effectsCached) return;
        bleedingEffect = ForgeRegistries.MOB_EFFECTS.getValue(BLEEDING_RL);
        vulnerableEffect = ForgeRegistries.MOB_EFFECTS.getValue(VULNERABLE_RL);
        armorDecreaseEffect = ForgeRegistries.MOB_EFFECTS.getValue(ARMOR_DECREASE_RL);
        slownessEffect = ForgeRegistries.MOB_EFFECTS.getValue(SLOWNESS_RL);
        effectsCached = true;
    }

    private static void cacheAmmo() {
        if (ammoCached) return;
        iridiumRound = ForgeRegistries.ITEMS.getValue(new ResourceLocation("terramity", "iridium_round"));
        goldRound = ForgeRegistries.ITEMS.getValue(new ResourceLocation("terramity", "gold_round"));
        copperRound = ForgeRegistries.ITEMS.getValue(new ResourceLocation("terramity", "copper_round"));
        antimatterRound = ForgeRegistries.ITEMS.getValue(new ResourceLocation("terramity", "antimatter_round"));
        daemoniumShotshell = ForgeRegistries.ITEMS.getValue(new ResourceLocation("terramity", "daemonium_shotshell"));
        echoRound = ForgeRegistries.ITEMS.getValue(new ResourceLocation("terramity", "echo_round"));
        ammoCached = true;
    }

    public static boolean isGun(ItemStack weapon) {
        if (weapon.isEmpty()) return false;
        return IS_GUN_CACHE.computeIfAbsent(weapon.getItem(), item -> {
            ItemStack stack = new ItemStack(item);
            if (stack.is(FORGE_GUNS)) return true;

            ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
            if (id != null && id.getNamespace().equals("terramity")) {
                String path = id.getPath();
                return path.contains("gun") || path.contains("rifle") || path.contains("pistol") ||
                        path.contains("shotgun") || path.contains("cannon") || path.contains("blunderbuss") ||
                        path.contains("shooter") || path.equals("stairway_to_heaven") ||
                        path.equals("requiem") || path.equals("blasphemic_rapture") || path.equals("big_iron") ||
                        path.equals("devastation") || path.equals("vulcan") || path.equals("titanomachy") ||
                        path.equals("davy_jones") || path.equals("olympus");
            }
            return false;
        });
    }

    @SubscribeEvent
    public static void onGunFire(net.minecraftforge.event.entity.player.PlayerInteractEvent.@NonNull RightClickItem event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide && player.getPersistentData().getBoolean("lt_gunner_active")) {
            ItemStack weapon = event.getItemStack();

            if (isGun(weapon)) {
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

            if (isGun(weapon)) {
                event.getEntity().invulnerableTime = 0;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingHurt(@NonNull LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            if (player.getPersistentData().getBoolean("linggango_processing_gun")) {
                return;
            }

            ItemStack weapon = player.getMainHandItem();

            if (!isGun(weapon)) {
                return;
            }

            LivingEntity target = event.getEntity();
            boolean isProjectile = event.getSource().getDirectEntity() instanceof net.minecraft.world.entity.projectile.Projectile;

            if (!isProjectile) {
                long lastHitscanTick = target.getPersistentData().getLong("linggango_hitscan_tick");
                if (target.level().getGameTime() == lastHitscanTick) {
                    event.setCanceled(true);
                    return;
                }
                target.getPersistentData().putLong("linggango_hitscan_tick", target.level().getGameTime());

                long lastMeleeTick = player.getPersistentData().getLong("linggango_last_melee");
                if (player.level().getGameTime() - lastMeleeTick <= 2) {
                    return;
                }
            }

            target.invulnerableTime = 0;
            player.getPersistentData().putBoolean("linggango_processing_gun", true);

            float originalDamage = event.getAmount();
            originalDamage *= 1.5f;

            if (player.getAttributes().hasAttribute(LinggangoAttributes.GUN_DAMAGE.get())) {
                double damagePoints = player.getAttributeValue(LinggangoAttributes.GUN_DAMAGE.get());
                if (damagePoints > 0) {
                    originalDamage *= (float) (1.0 + (damagePoints * 0.10));
                }
            }

            if (target.getArmorValue() > 0) {
                int vestShredderLevel = EnchantmentHelper.getTagEnchantmentLevel(LinggangoEnchantments.VEST_SHREDDER.get(), weapon);
                if (vestShredderLevel > 0) {
                    float shredderBonus = Math.min(0.60f, vestShredderLevel * 0.20f);
                    originalDamage *= (1.0f + shredderBonus);
                }

                float heavyImpactBypass = Math.min(0.25f, originalDamage * 0.005f);
                originalDamage *= (1.0f + heavyImpactBypass);
            }

            float finalDamage = originalDamage;

            int railChargeLevel = EnchantmentHelper.getTagEnchantmentLevel(LinggangoEnchantments.RAIL_CHARGE.get(), weapon);
            if (railChargeLevel > 0) {
                finalDamage *= 2.0f;
            }

            int overclockLevel = EnchantmentHelper.getTagEnchantmentLevel(LinggangoEnchantments.OVERCLOCKED_ROUNDS.get(), weapon);
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

                finalDamage += originalDamage * (0.02f * overclockLevel * hitCombo);
            }

            float bonusDamage = 0f;

            int bulletEchoLevel = EnchantmentHelper.getTagEnchantmentLevel(LinggangoEnchantments.BULLET_ECHO.get(), weapon);
            if (bulletEchoLevel > 0) {
                float echoChance = 0.20f + (0.10f * (bulletEchoLevel - 1));
                if (player.getRandom().nextFloat() < echoChance) {
                    bonusDamage += finalDamage * 0.50f;
                }
            }

            int arcaneRoundsLevel = EnchantmentHelper.getTagEnchantmentLevel(LinggangoEnchantments.ARCANE_ROUNDS.get(), weapon);
            if (arcaneRoundsLevel > 0) {
                bonusDamage += finalDamage * (0.15f * arcaneRoundsLevel);
            }

            int shrapnelLevel = EnchantmentHelper.getTagEnchantmentLevel(LinggangoEnchantments.SHRAPNEL_PIERCER.get(), weapon);
            if (shrapnelLevel > 0) {
                int currentStacks = target.getPersistentData().getInt("ShrapnelStacks");
                int maxStacks = shrapnelLevel == 1 ? 7 : 12;
                if (currentStacks < maxStacks) {
                    currentStacks++;
                    target.getPersistentData().putInt("ShrapnelStacks", currentStacks);
                }
                bonusDamage += finalDamage * (currentStacks * 0.02f);
            }

            finalDamage += bonusDamage;

            event.setAmount(finalDamage);

            boolean isGunner = player.getPersistentData().getBoolean("lt_gunner_active");
            int explosiveLevel = EnchantmentHelper.getTagEnchantmentLevel(LinggangoEnchantments.EXPLOSIVE_ROUNDS.get(), weapon);

            if (isGunner || explosiveLevel > 0) {
                float aoeDamage = 0f;

                if (isGunner) {
                    aoeDamage += finalDamage * 0.50f;
                    player.getPersistentData().putBoolean("lt_gunner_active", false);
                    if (player instanceof ServerPlayer serverPlayer) {
                        SkillManager.setCooldown(serverPlayer, "gunner", 600);
                        SkillManager.syncToClient(serverPlayer, "gunner");
                    }
                }

                if (explosiveLevel > 0) {
                    aoeDamage += finalDamage * 0.25f;
                }

                List<LivingEntity> nearbyEntities = player.level().getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(3.5D));
                DamageSource explosionSource = player.level().damageSources().explosion(player, player);

                for (LivingEntity nearbyTarget : nearbyEntities) {
                    if (nearbyTarget != player && nearbyTarget != target && nearbyTarget.isAlive()) {
                        nearbyTarget.invulnerableTime = 0;
                        nearbyTarget.hurt(explosionSource, aoeDamage);
                    }
                }

                if (player.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.EXPLOSION, target.getX(), target.getY(0.5D), target.getZ(), isGunner ? 2 : 1, 0.0D, 0.0D, 0.0D, 0.0D);
                }
            }

            int resourceMagazinesLevel = EnchantmentHelper.getTagEnchantmentLevel(LinggangoEnchantments.RESOURCE_MAGAZINES.get(), weapon);
            if (resourceMagazinesLevel > 0 && player.getRandom().nextFloat() < (0.10f * resourceMagazinesLevel)) {
                Item ammoItem = getAmmoForGun(weapon.getItem());
                player.getInventory().add(new ItemStack(ammoItem, 1));
            }

            int elementalBarrageLevel = EnchantmentHelper.getTagEnchantmentLevel(LinggangoEnchantments.ELEMENTAL_BARRAGE.get(), weapon);
            if (elementalBarrageLevel > 0 && player.getRandom().nextFloat() < (0.05f * elementalBarrageLevel)) {
                cacheEffects();
                if (bleedingEffect != null) target.addEffect(new MobEffectInstance(bleedingEffect, 100, 1));
                if (vulnerableEffect != null) target.addEffect(new MobEffectInstance(vulnerableEffect, 100, 1));
                if (armorDecreaseEffect != null) target.addEffect(new MobEffectInstance(armorDecreaseEffect, 100, 1));
            }

            player.getPersistentData().putBoolean("linggango_processing_gun", false);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.@NonNull PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide()) {
            ItemStack weapon = event.player.getMainHandItem();

            if (isGun(weapon) && EnchantmentHelper.getTagEnchantmentLevel(LinggangoEnchantments.RAIL_CHARGE.get(), weapon) > 0) {
                cacheEffects();
                if (slownessEffect != null) {
                    event.player.addEffect(new MobEffectInstance(slownessEffect, 20, 0, false, false, true));
                }
            }
        }
    }

    private static @NonNull Item getAmmoForGun(Item gunItem) {
        return AMMO_MAPPING_CACHE.computeIfAbsent(gunItem, item -> {
            ResourceLocation gunId = ForgeRegistries.ITEMS.getKey(item);
            if (gunId == null) return Items.IRON_NUGGET;

            String path = gunId.getPath();
            cacheAmmo();

            if (path.contains("iridium") || path.equals("stairway_to_heaven")) {
                return iridiumRound != null ? iridiumRound : Items.IRON_NUGGET;
            } else if (path.contains("gold") || path.equals("advanced_automatic_rifle") || path.equals("requiem")) {
                return goldRound != null ? goldRound : Items.IRON_NUGGET;
            } else if (path.contains("copper")) {
                return copperRound != null ? copperRound : Items.IRON_NUGGET;
            } else if (path.contains("antimatter") || path.equals("blasphemic_rapture")) {
                return antimatterRound != null ? antimatterRound : Items.IRON_NUGGET;
            } else if (path.contains("shotgun") || path.contains("blunderbuss")) {
                return daemoniumShotshell != null ? daemoniumShotshell : Items.IRON_NUGGET;
            } else if (path.contains("echo") || path.equals("anti_material_rifle")) {
                return echoRound != null ? echoRound : Items.IRON_NUGGET;
            }

            return Items.IRON_NUGGET;
        });
    }
}