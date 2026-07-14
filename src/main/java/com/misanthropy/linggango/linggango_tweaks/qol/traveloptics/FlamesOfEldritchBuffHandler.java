package com.misanthropy.linggango.linggango_tweaks.qol.traveloptics;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FlamesOfEldritchBuffHandler {
    private static final UUID DAMAGE_MODIFIER_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
    private static final UUID SPEED_MODIFIER_UUID = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");
    private static final UUID REACH_MODIFIER_UUID = UUID.fromString("6a172778-5b1d-4054-ac5f-6a9c7ebed072");

    private static final AttributeModifier[] DAMAGE_MODIFIERS = {
            new AttributeModifier(DAMAGE_MODIFIER_UUID, "Evo Damage", 34.0, AttributeModifier.Operation.ADDITION),
            new AttributeModifier(DAMAGE_MODIFIER_UUID, "Evo Damage", 44.0, AttributeModifier.Operation.ADDITION),
            new AttributeModifier(DAMAGE_MODIFIER_UUID, "Evo Damage", 54.0, AttributeModifier.Operation.ADDITION),
            new AttributeModifier(DAMAGE_MODIFIER_UUID, "Evo Damage", 64.0, AttributeModifier.Operation.ADDITION)
    };

    private static final AttributeModifier[] SPEED_MODIFIERS = {
            new AttributeModifier(SPEED_MODIFIER_UUID, "Evo Speed", -2.4, AttributeModifier.Operation.ADDITION),
            new AttributeModifier(SPEED_MODIFIER_UUID, "Evo Speed", -1.6, AttributeModifier.Operation.ADDITION)
    };

    private static final AttributeModifier REACH_MODIFIER = new AttributeModifier(REACH_MODIFIER_UUID, "Evo Reach", 2.0, AttributeModifier.Operation.ADDITION);

    private static final ConcurrentHashMap<Item, Integer> EVO_LEVEL_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Item, Boolean> IGNITIUM_ANY_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Item, Boolean> IGNITIUM_HELMET_CACHE = new ConcurrentHashMap<>();

    private static MobEffect bleedingEffect = null;
    private static MobEffect hellfireEffect = null;
    private static MobEffect blazingBrandEffect = null;
    private static boolean effectsCached = false;

    @SubscribeEvent
    public static void onAttributeModifier(@NonNull ItemAttributeModifierEvent event) {
        Item item = event.getItemStack().getItem();

        if (event.getSlotType() == EquipmentSlot.MAINHAND) {
            int evoLevel = getCachedEvoLevel(item);
            if (evoLevel >= 0 && evoLevel < 4) {
                event.removeAttribute(Attributes.ATTACK_DAMAGE);
                event.removeAttribute(Attributes.ATTACK_SPEED);

                event.addModifier(Attributes.ATTACK_DAMAGE, DAMAGE_MODIFIERS[evoLevel]);
                event.addModifier(Attributes.ATTACK_SPEED, SPEED_MODIFIERS[evoLevel >= 2 ? 1 : 0]);

                if (evoLevel >= 2) {
                    event.addModifier(ForgeMod.ENTITY_REACH.get(), REACH_MODIFIER);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(@NonNull LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            if (!event.getSource().is(net.minecraft.world.damagesource.DamageTypes.MAGIC) &&
                    !event.getSource().is(net.minecraft.world.damagesource.DamageTypes.IN_FIRE) &&
                    !event.getSource().is(net.minecraft.world.damagesource.DamageTypes.ON_FIRE)) {

                int evoLevel = getCachedEvoLevel(player.getMainHandItem().getItem());
                if (evoLevel >= 0) {
                    LivingEntity target = event.getEntity();
                    if (evoLevel == 3) event.setAmount(event.getAmount() * 1.35f);

                    CompoundTag data = target.getPersistentData();
                    long currentTime = player.level().getGameTime();
                    long lastHit = data.getLong("EldritchLastHit");
                    int hits = data.getInt("EldritchHits");

                    if (currentTime - lastHit > 100) hits = 0;
                    hits++;
                    data.putInt("EldritchHits", hits);
                    data.putLong("EldritchLastHit", currentTime);

                    if (hits > 5) {
                        cacheEffects();
                        int baseDur = (evoLevel >= 1) ? 200 : 100;
                        int brandDur = (evoLevel >= 1) ? 260 : 160;
                        applyProgressiveEffect(target, bleedingEffect, baseDur, 0);
                        applyProgressiveEffect(target, hellfireEffect, baseDur, 0);
                        applyProgressiveEffect(target, blazingBrandEffect, brandDur, (evoLevel >= 1) ? 4 : 0);
                    }
                }
            }
        }

        if (event.getEntity() instanceof Player wearer) {
            int pieces = countIgnitiumPieces(wearer);

            if (pieces > 0) {
                event.setAmount(event.getAmount() * (1.0f - (pieces * 0.05f)));
            }

            if (pieces >= 4 && (event.getSource().is(net.minecraft.world.damagesource.DamageTypes.IN_FIRE) ||
                    event.getSource().is(net.minecraft.world.damagesource.DamageTypes.ON_FIRE) ||
                    event.getSource().is(net.minecraft.world.damagesource.DamageTypes.LAVA) ||
                    event.getSource().is(net.minecraft.world.damagesource.DamageTypes.HOT_FLOOR))) {
                event.setCanceled(true);
            }
        }

        if (event.getSource().getEntity() instanceof Player attacker) {
            ItemStack helmet = attacker.getItemBySlot(EquipmentSlot.HEAD);
            if (isCachedIgnitiumPiece(helmet.getItem(), "helmet")) {
                cacheEffects();
                applyProgressiveEffect(event.getEntity(), blazingBrandEffect, 200, 2);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof Player player && player.tickCount % 20 == 0) {
            int pieces = countIgnitiumPieces(player);
            if (pieces >= 4) {
                player.heal(0.2f);
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 220, 0, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 220, 0, false, false));
            }
        }
    }

    @SubscribeEvent
    public static void onTooltip(@NonNull ItemTooltipEvent event) {
        Item item = event.getItemStack().getItem();
        int evoLevel = getCachedEvoLevel(item);

        if (evoLevel >= 0) {
            List<Component> tooltip = event.getToolTip();
            for (int i = 0; i < tooltip.size(); i++) {
                String text = tooltip.get(i).getString().toLowerCase();
                if (text.contains("evo 1")) {
                    tooltip.set(i, Component.literal("[Evo 1] ").withStyle(ChatFormatting.GRAY)
                            .append(Component.literal("Duration +5s, Base DMG +10, Brand stacks up to 5x (After 5 Hits)").withStyle(ChatFormatting.DARK_AQUA)));
                } else if (text.contains("evo 2")) {
                    tooltip.set(i, Component.literal("[Evo 2] ").withStyle(ChatFormatting.GRAY)
                            .append(Component.literal("+50% ATK Speed, +2 Reach, Base DMG +10").withStyle(ChatFormatting.DARK_AQUA)));
                } else if (text.contains("evo 3")) {
                    tooltip.set(i, Component.literal("[Evo 3] ").withStyle(ChatFormatting.GRAY)
                            .append(Component.literal("+35% Final DMG, Base DMG +10").withStyle(ChatFormatting.DARK_AQUA)));
                }
            }
        }

        if (isCachedIgnitiumPiece(item, "")) {
            event.getToolTip().add(Component.literal("Durability Override: 1000").withStyle(ChatFormatting.GOLD));
            if (isCachedIgnitiumPiece(item, "helmet")) {
                event.getToolTip().add(Component.literal("Passive: Inflicts Blazing Brand III on Hit").withStyle(ChatFormatting.RED));
            }
        }
    }

    private static int getCachedEvoLevel(Item item) {
        return EVO_LEVEL_CACHE.computeIfAbsent(item, i -> {
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(i);
            if (id == null || !id.getNamespace().equals("traveloptics")) return -1;
            String path = id.getPath();
            if (!path.startsWith("flames_of_eldritch")) return -1;
            if (path.contains("three")) return 3;
            if (path.contains("two")) return 2;
            if (path.contains("one")) return 1;
            return path.equals("flames_of_eldritch") ? 0 : -1;
        });
    }

    private static boolean isCachedIgnitiumPiece(Item item, String type) {
        if ("helmet".equals(type)) {
            return IGNITIUM_HELMET_CACHE.computeIfAbsent(item, i -> {
                ResourceLocation id = ForgeRegistries.ITEMS.getKey(i);
                if (id == null || !id.getNamespace().equals("cataclysm")) return false;
                String path = id.getPath();
                return path.contains("ignitium") && path.contains("helmet");
            });
        } else {
            return IGNITIUM_ANY_CACHE.computeIfAbsent(item, i -> {
                ResourceLocation id = ForgeRegistries.ITEMS.getKey(i);
                if (id == null || !id.getNamespace().equals("cataclysm")) return false;
                String path = id.getPath();
                return path.contains("ignitium");
            });
        }
    }

    private static int countIgnitiumPieces(Player player) {
        int count = 0;
        for (ItemStack stack : player.getArmorSlots()) {
            if (isCachedIgnitiumPiece(stack.getItem(), "")) count++;
        }
        return count;
    }

    private static void cacheEffects() {
        if (!effectsCached) {
            bleedingEffect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("attributeslib:bleeding"));
            hellfireEffect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("lethality:hellfire"));
            blazingBrandEffect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("cataclysm:blazing_brand"));
            effectsCached = true;
        }
    }

    private static void applyProgressiveEffect(LivingEntity target, MobEffect effect, int duration, int maxAmp) {
        if (effect != null) {
            MobEffectInstance current = target.getEffect(effect);
            int newAmp = (current != null) ? Math.min(current.getAmplifier() + 1, maxAmp) : 0;
            target.addEffect(new MobEffectInstance(effect, duration, newAmp));
        }
    }
}