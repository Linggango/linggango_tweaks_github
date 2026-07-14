package com.misanthropy.linggango.linggango_tweaks.events;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerManaStatsHandler {

    private static final UUID TIER_MOD_UUID = UUID.fromString("d62f4b93-5c8a-4a21-9382-7d1c5e9a4f2b");
    private static final UUID CRYSTAL_MOD_UUID = UUID.fromString("b3a9e1d8-4f2c-4c67-8910-3b5a7c2d9e1f");

    public static final String NBT_TIER = "linggango.mana_tier";
    public static final String NBT_CRYSTAL_POWER = "linggango.crystal_power";
    public static final String NBT_CRYSTAL_MANA = "linggango.crystal_mana";
    public static final String NBT_CRYSTAL_REGEN = "linggango.crystal_regen";

    private static final int MAX_POWER_CRYSTALS = 4;
    private static final int MAX_MANA_CRYSTALS = 2;
    private static final int MAX_REGEN_CRYSTALS = 2;

    private static Attribute spellPower;
    private static Attribute arsDamage;
    private static Attribute maxMana;
    private static Attribute manaRegen;
    private static boolean attributesCached = false;

    private static void cacheAttributes() {
        if (attributesCached) return;
        spellPower = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation("irons_spellbooks:spell_power"));
        arsDamage = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation("ars_nouveau:ars_nouveau.perk.spell_damage"));
        maxMana = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation("irons_spellbooks:max_mana"));
        manaRegen = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation("irons_spellbooks:mana_regen"));
        attributesCached = true;
    }

    public static void applyAllBuffs(Player player) {
        cacheAttributes();

        CompoundTag data = player.getPersistentData();
        int tier = data.getInt(NBT_TIER);
        int powerCrystals = data.getInt(NBT_CRYSTAL_POWER);
        int manaCrystals = data.getInt(NBT_CRYSTAL_MANA);
        int regenCrystals = data.getInt(NBT_CRYSTAL_REGEN);

        double ironPowerBonus = tier == 1 ? 3.0 : tier == 2 ? 5.0 : tier == 3 ? 8.0 : 0.0;
        double arsDamageBonus = tier == 1 ? 3.0 : tier == 2 ? 6.0 : tier == 3 ? 9.0 : 0.0;

        double crystalPowerBonus = powerCrystals * 0.20;
        double crystalManaBonus = manaCrystals * 50.0;
        double crystalRegenBonus = regenCrystals * 0.5;

        updateAttribute(player, spellPower, TIER_MOD_UUID, "Mana Tier Power", ironPowerBonus);
        updateAttribute(player, arsDamage, TIER_MOD_UUID, "Mana Tier Ars", arsDamageBonus);

        updateAttribute(player, spellPower, CRYSTAL_MOD_UUID, "Crystal Power", crystalPowerBonus);
        updateAttribute(player, maxMana, CRYSTAL_MOD_UUID, "Crystal Capacity", crystalManaBonus);
        updateAttribute(player, manaRegen, CRYSTAL_MOD_UUID, "Crystal Regen", crystalRegenBonus);
    }

    private static void updateAttribute(Player player, Attribute attr, UUID modId, String modName, double amount) {
        if (attr == null) return;

        AttributeInstance instance = player.getAttribute(attr);
        if (instance == null) return;

        instance.removeModifier(modId);

        if (amount > 0) {
            instance.addPermanentModifier(new AttributeModifier(modId, modName, amount, AttributeModifier.Operation.ADDITION));
        }
    }

    @SubscribeEvent
    public static void onItemEaten(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity().level().isClientSide() || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        Item eatenItem = event.getItem().getItem();
        ResourceLocation itemKey = ForgeRegistries.ITEMS.getKey(eatenItem);
        if (itemKey == null || !"kubejs".equals(itemKey.getNamespace())) return;

        CompoundTag data = player.getPersistentData();
        boolean updateNeeded = false;

        switch (itemKey.getPath()) {
            case "fading_shard_of_compressed_mana" -> {
                if (data.getInt(NBT_TIER) >= 1) {
                    player.sendSystemMessage(Component.literal("You are already too powerful for this weak shard.").withStyle(ChatFormatting.RED));
                    return;
                }
                data.putInt(NBT_TIER, 1);
                updateNeeded = true;
                player.sendSystemMessage(Component.literal("Your blood boils with raw mana..").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
                grantAdvancement(player, "linggango:fading_shard");
            }
            case "shard_of_compressed_mana" -> {
                if (data.getInt(NBT_TIER) >= 2) {
                    player.sendSystemMessage(Component.literal("You have already absorbed this level of power.").withStyle(ChatFormatting.RED));
                    return;
                }
                data.putInt(NBT_TIER, 2);
                updateNeeded = true;
                player.sendSystemMessage(Component.literal("Your blood adapts, unifies with mana.").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
                grantAdvancement(player, "linggango:pure_mana");
            }
            case "purified_shard_of_compressed_mana" -> {
                if (data.getInt(NBT_TIER) >= 3) {
                    player.sendSystemMessage(Component.literal("You have already reached the pinnacle of mana.").withStyle(ChatFormatting.LIGHT_PURPLE));
                    return;
                }
                data.putInt(NBT_TIER, 3);
                updateNeeded = true;
                player.sendSystemMessage(Component.literal("You fully unlock your potential with magic.").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
                grantAdvancement(player, "linggango:purified_shard");
            }

            case "mana_crystal" -> {
                if (data.getInt(NBT_CRYSTAL_POWER) >= MAX_POWER_CRYSTALS) {
                    player.sendSystemMessage(Component.literal("Your body cannot handle any more Spell Power crystals.").withStyle(ChatFormatting.RED));
                    return;
                }
                data.putInt(NBT_CRYSTAL_POWER, data.getInt(NBT_CRYSTAL_POWER) + 1);
                updateNeeded = true;
                player.sendSystemMessage(Component.literal("Your spell power has permanently increased!").withStyle(ChatFormatting.AQUA));
            }
            case "mana_capacity_crystal" -> {
                if (data.getInt(NBT_CRYSTAL_MANA) >= MAX_MANA_CRYSTALS) {
                    player.sendSystemMessage(Component.literal("Your body cannot handle any more Mana Capacity crystals.").withStyle(ChatFormatting.RED));
                    return;
                }
                data.putInt(NBT_CRYSTAL_MANA, data.getInt(NBT_CRYSTAL_MANA) + 1);
                updateNeeded = true;
                player.sendSystemMessage(Component.literal("Your mana capacity has expanded!").withStyle(ChatFormatting.AQUA));
            }
            case "mana_regeneration_crystal" -> {
                if (data.getInt(NBT_CRYSTAL_REGEN) >= MAX_REGEN_CRYSTALS) {
                    player.sendSystemMessage(Component.literal("Your body cannot handle any more Mana Regeneration crystals.").withStyle(ChatFormatting.RED));
                    return;
                }
                data.putInt(NBT_CRYSTAL_REGEN, data.getInt(NBT_CRYSTAL_REGEN) + 1);
                updateNeeded = true;
                player.sendSystemMessage(Component.literal("Your mana flows faster now!").withStyle(ChatFormatting.AQUA));
            }
        }

        if (updateNeeded) {
            applyAllBuffs(player);
        }
    }

    private static void grantAdvancement(ServerPlayer player, String advancementName) {
        Objects.requireNonNull(player.getServer()).getCommands().performPrefixedCommand(
                player.createCommandSourceStack().withSuppressedOutput().withPermission(4),
                "advancement grant " + player.getScoreboardName() + " only " + advancementName
        );
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        CompoundTag oldData = event.getOriginal().getPersistentData();
        CompoundTag newData = event.getEntity().getPersistentData();

        if (oldData.contains(NBT_TIER)) newData.putInt(NBT_TIER, oldData.getInt(NBT_TIER));
        if (oldData.contains(NBT_CRYSTAL_POWER)) newData.putInt(NBT_CRYSTAL_POWER, oldData.getInt(NBT_CRYSTAL_POWER));
        if (oldData.contains(NBT_CRYSTAL_MANA)) newData.putInt(NBT_CRYSTAL_MANA, oldData.getInt(NBT_CRYSTAL_MANA));
        if (oldData.contains(NBT_CRYSTAL_REGEN)) newData.putInt(NBT_CRYSTAL_REGEN, oldData.getInt(NBT_CRYSTAL_REGEN));
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        applyAllBuffs(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        applyAllBuffs(event.getEntity());
    }
}