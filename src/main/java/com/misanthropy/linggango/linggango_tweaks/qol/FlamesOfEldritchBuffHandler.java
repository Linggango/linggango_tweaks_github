package com.misanthropy.linggango.linggango_tweaks.qol;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FlamesOfEldritchBuffHandler {
    private static final UUID DAMAGE_MODIFIER_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
    private static final UUID SPEED_MODIFIER_UUID = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");
    private static final UUID REACH_MODIFIER_UUID = UUID.fromString("6a172778-5b1d-4054-ac5f-6a9c7ebed072");
    @SubscribeEvent
    public static void onAttributeModifier(@NonNull ItemAttributeModifierEvent event) {
        if (event.getSlotType() != EquipmentSlot.MAINHAND) return;

        Item item = event.getItemStack().getItem();
        int evoLevel = getEvoLevel(item);

        if (evoLevel >= 0) {
            event.removeAttribute(Attributes.ATTACK_DAMAGE);
            event.removeAttribute(Attributes.ATTACK_SPEED);

            double damage = 35.0 + (evoLevel * 10.0);
            event.addModifier(Attributes.ATTACK_DAMAGE, new AttributeModifier(
                    DAMAGE_MODIFIER_UUID, "Evo Damage", damage - 1.0, AttributeModifier.Operation.ADDITION));

            double speedModifier = (evoLevel >= 2) ? -1.6 : -2.4;
            event.addModifier(Attributes.ATTACK_SPEED, new AttributeModifier(
                    SPEED_MODIFIER_UUID, "Evo Speed", speedModifier, AttributeModifier.Operation.ADDITION));

            if (evoLevel >= 2) {
                event.addModifier(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(
                        REACH_MODIFIER_UUID, "Evo Reach", 2.0, AttributeModifier.Operation.ADDITION));
            }
        }
    }
    @SubscribeEvent
    public static void onLivingHurt(@NonNull LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        int evoLevel = getEvoLevel(player.getMainHandItem().getItem());
        if (evoLevel >= 0) {
            LivingEntity target = event.getEntity();
            if (evoLevel == 3) {
                event.setAmount(event.getAmount() * 1.35f);
            }
            CompoundTag data = target.getPersistentData();
            long currentTime = System.currentTimeMillis();
            long lastHit = data.getLong("EldritchLastHit");
            int hits = data.getInt("EldritchHits");
            if (currentTime - lastHit > 5000) {
                hits = 0;
            }

            hits++;
            data.putInt("EldritchHits", hits);
            data.putLong("EldritchLastHit", currentTime);
            if (hits >= 5) {
                int baseDuration = (evoLevel >= 1) ? 200 : 100;
                int brandDuration = (evoLevel >= 1) ? 260 : 160;
                applyProgressiveEffect(target, "attributeslib:bleeding", baseDuration, 0);
                applyProgressiveEffect(target, "lethality:hellfire", baseDuration, 0);
                int maxBrandAmp = (evoLevel >= 1) ? 4 : 0;
                applyProgressiveEffect(target, "cataclysm:blazing_brand", brandDuration, maxBrandAmp);
            }
        }
    }
    @SubscribeEvent
    public static void onTooltip(@NonNull ItemTooltipEvent event) {
        int evoLevel = getEvoLevel(event.getItemStack().getItem());

        if (evoLevel >= 0) {
            List<Component> tooltip = event.getToolTip();

            for (int i = 0; i < tooltip.size(); i++) {
                String text = tooltip.get(i).getString().toLowerCase();
                if (text.contains("evo 1") || text.contains("evo i") || text.contains("evo_one")) {
                    tooltip.set(i, Component.literal("[Evo 1] ")
                            .withStyle(ChatFormatting.GRAY)
                            .append(Component.literal("Duration +5s, Base DMG +10, Brand stacks up to 5x (After 5 Hits)")
                                    .withStyle(ChatFormatting.DARK_AQUA)));
                } else if (text.contains("evo 2") || text.contains("evo ii") || text.contains("evo_two")) {
                    tooltip.set(i, Component.literal("[Evo 2] ")
                            .withStyle(ChatFormatting.GRAY)
                            .append(Component.literal("+50% ATK Speed, +2 Reach, Base DMG +10")
                                    .withStyle(ChatFormatting.DARK_AQUA)));
                } else if (text.contains("evo 3") || text.contains("evo iii") || text.contains("evo_three")) {
                    tooltip.set(i, Component.literal("[Evo 3] ")
                            .withStyle(ChatFormatting.GRAY)
                            .append(Component.literal("+35% Final DMG, Base DMG +10")
                                    .withStyle(ChatFormatting.DARK_AQUA)));
                }
            }
        }
    }
    private static int getEvoLevel(Item item) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
        if (id == null || !id.getNamespace().equals("traveloptics")) return -1;
        String path = id.getPath();
        if (!path.startsWith("flames_of_eldritch")) return -1;
        if (path.contains("three")) return 3;
        if (path.contains("two")) return 2;
        if (path.contains("one")) return 1;
        if (path.equals("flames_of_eldritch")) return 0;

        return -1;
    }
    private static void applyProgressiveEffect(@NonNull LivingEntity target, @NonNull String effectId, int duration, int maxAmp) {
        MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(effectId));
        if (effect != null) {
            MobEffectInstance current = target.getEffect(effect);
            int newAmp = 0;
            if (current != null) {
                newAmp = Math.min(current.getAmplifier() + 1, maxAmp);
            }

            target.addEffect(new MobEffectInstance(effect, duration, newAmp));
        }
    }
}