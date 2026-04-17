package com.misanthropy.linggango.linggango_tweaks.skills;

import com.misanthropy.linggango.class_enhancement.ClassEnhancement;
import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
@SuppressWarnings("resource")
public class SkillManager {

    private static final UUID VAMP_DAY_ID = UUID.fromString("11a3b8d9-6f1c-4b3a-92e2-5c8e7e120f2a");
    private static final UUID SMAGE_BUFF_ID = UUID.fromString("22a3b8d9-6f1c-4b3a-92e2-5c8e7e120f2b");
    private static final UUID MONK_STEP_ID = UUID.fromString("33a3b8d9-6f1c-4b3a-92e2-5c8e7e120f2c");
    private static final UUID NMAGE_MANA_ID = UUID.fromString("44a3b8d9-6f1c-4b3a-92e2-5c8e7e120f2d");
    private static final UUID NMAGE_SHRED_ID = UUID.fromString("55a3b8d9-6f1c-4b3a-92e2-5c8e7e120f2e");

    public static void playCustomSound(Player player, String soundId, float vol, float pitch) {
        ResourceLocation loc = new ResourceLocation(soundId);
        SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(loc);
        if (sound == null) {
            sound = SoundEvent.createVariableRangeEvent(loc);
        }
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS, vol, pitch);
    }

    public static String getPlayerClass(Player player) {
        if (player.level().isClientSide) {
            return com.misanthropy.linggango.linggango_tweaks.skills.client.ClientSkillEvents.currentClassId;
        } else {
            String c = ClassEnhancement.ClassSavedData.get((net.minecraft.server.level.ServerLevel) player.level()).playerClasses.get(player.getUUID());
            return c != null ? c : "none";
        }
    }

    public static void useActiveSkill(ServerPlayer player) {
        String classId = getPlayerClass(player);
        CompoundTag data = player.getPersistentData();

        long cdEnd = data.getLong("lt_cd_" + classId);
        if (player.level().getGameTime() < cdEnd) return;

        boolean synced = false;

        switch (classId) {
            case "machinist":
                ItemStack hand = player.getMainHandItem();
                if (hand.isDamageableItem()) {
                    int repairAmt = (int) (hand.getMaxDamage() * 0.20f);
                    hand.setDamageValue(Math.max(0, hand.getDamageValue() - repairAmt));
                    setCooldown(player, classId, 12000);

                    data.putInt("lt_repair_ticks", 3);
                    synced = true;
                }
                break;

            case "north_mage":
                data.putBoolean("lt_nmage_active", true);
                addAttribute(player, "irons_spellbooks:max_mana", NMAGE_MANA_ID, 0.20, AttributeModifier.Operation.MULTIPLY_BASE);
                addAttribute(player, "ars_nouveau:ars_nouveau.perk.max_mana", NMAGE_MANA_ID, 0.20, AttributeModifier.Operation.MULTIPLY_BASE);
                data.putLong("lt_nmage_end", player.level().getGameTime() + 200);

                setCooldown(player, classId, 800);

                playCustomSound(player, "minecraft:block.enchantment_table.use", 1.0F, 1.0F);
                synced = true;
                break;

            case "south_mage":
                boolean smageActive = data.getBoolean("lt_smage_active");
                if (smageActive) {
                    data.putBoolean("lt_smage_active", false);
                    removeAttribute(player, "irons_spellbooks:spell_power", SMAGE_BUFF_ID);
                    setCooldown(player, classId, 6000);

                } else {
                    data.putBoolean("lt_smage_active", true);
                    playCustomSound(player, "brutality:blood_magic_missile", 1.0F, 1.0F);
                    addAttribute(player, "irons_spellbooks:spell_power", SMAGE_BUFF_ID, 1.0, AttributeModifier.Operation.MULTIPLY_BASE);
                }
                synced = true;
                break;

            case "vampire":
                boolean vampActive = data.getBoolean("lt_vamp_active");
                data.putBoolean("lt_vamp_active", !vampActive);
                synced = true;
                break;

            case "miner":
                boolean crawling = data.getBoolean("lt_crawling");
                data.putBoolean("lt_crawling", !crawling);
                synced = true;
                break;

            case "gambler":
                if (data.getInt("lt_gambler_roll_timer") > 0) return;
                data.putInt("lt_gambler_roll_timer", 40);
                synced = true;
                break;
        }

        if (synced) syncToClient(player, classId);
    }

    public static void setCooldown(ServerPlayer player, String classId, int ticks) {
        player.getPersistentData().putLong("lt_cd_" + classId, player.level().getGameTime() + ticks);
        player.getPersistentData().putInt("lt_maxcd_" + classId, ticks);
    }

    public static void syncToClient(ServerPlayer player, String classId) {
        CompoundTag data = player.getPersistentData();
        long end = data.getLong("lt_cd_" + classId);
        int remaining = (int) Math.max(0, end - player.level().getGameTime());
        int max = data.getInt("lt_maxcd_" + classId);

        boolean isActive = false;
        if (classId.equals("south_mage")) isActive = data.getBoolean("lt_smage_active");
        else if (classId.equals("north_mage")) isActive = data.getBoolean("lt_nmage_active");
        else if (classId.equals("vampire")) isActive = data.getBoolean("lt_vamp_active");
        else if (classId.equals("miner")) isActive = data.getBoolean("lt_crawling");
        else if (classId.equals("gambler")) isActive = data.getInt("lt_gambler_roll_timer") > 0;

        TweaksSkillNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                new TweaksSkillNetwork.SkillSyncS2CPacket(classId, remaining, max, isActive));
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide) return;

        if (event.getEntity() instanceof ServerPlayer player && "berserker".equals(getPlayerClass(player))) {
            long cdEnd = player.getPersistentData().getLong("lt_cd_berserk_revive");
            if (player.level().getGameTime() >= cdEnd) {
                event.setCanceled(true);

                player.setHealth(1.0F);

                player.getPersistentData().putLong("lt_cd_berserk_revive", player.level().getGameTime() + 12000);

                playCustomSound(player, "brutality:big_explosion", 1.0F, 1.0F);

                AbstractSpell spell = SpellRegistry.getSpell(new ResourceLocation("irons_spellbooks:earthquake"));
                if (spell != null && !spell.getSpellId().equals("irons_spellbooks:none")) {
                    spell.onCast(player.level(), 1, player, CastSource.NONE, MagicData.getPlayerMagicData(player));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.getEntity().level().isClientSide) return;

        if (event.getEntity() instanceof ServerPlayer damagedPlayer) {
            damagedPlayer.getPersistentData().putLong("lt_last_damage_time", damagedPlayer.level().getGameTime());
        }

        if (event.getEntity() instanceof ServerPlayer player && "gambler".equals(getPlayerClass(player))) {
            float rand = player.level().random.nextFloat();
            if (rand < 0.01f) {
                event.setAmount(Float.MAX_VALUE);
            } else if (rand < 0.11f) {
                event.setAmount(event.getAmount() * 3.0f);
            }
        }

        if (event.getSource().getEntity() instanceof ServerPlayer player && "gambler".equals(getPlayerClass(player))) {
            float rand = player.level().random.nextFloat();
            if (rand < 0.10f) {
                event.setAmount(event.getAmount() * 3.0f);
                playCustomSound(player, "minecraft:entity.experience_orb.pickup", 1.0F, 2.0F);
            }
            if (player.level().random.nextFloat() < 0.05f) {
                LivingEntity target = event.getEntity();
                MobEffect[] badEffects = {MobEffects.POISON, MobEffects.WITHER, MobEffects.WEAKNESS, MobEffects.MOVEMENT_SLOWDOWN, MobEffects.BLINDNESS};
                MobEffect randomEffect = badEffects[player.level().random.nextInt(badEffects.length)];
                target.addEffect(new MobEffectInstance(randomEffect, 100, 0));
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player p = event.player;
        CompoundTag data = p.getPersistentData();
        String c = getPlayerClass(p);

        if (!p.level().isClientSide && p.tickCount % 40 == 0) {
            syncToClient((ServerPlayer) p, c);
        }

        if (c.equals("miner") && data.getBoolean("lt_crawling")) {
            p.setPose(Pose.SWIMMING);
        }

        if (p.level().isClientSide) return;

        if (c.equals("gambler")) {
            int rollTimer = data.getInt("lt_gambler_roll_timer");
            if (rollTimer > 0) {
                rollTimer--;
                data.putInt("lt_gambler_roll_timer", rollTimer);

                if (rollTimer % 4 == 0) {
                    playCustomSound(p, "minecraft:ui.button.click", 0.5F, 1.5F + (p.level().random.nextFloat() * 0.5F));
                }

                if (rollTimer == 0) {
                    float failChance = p.level().random.nextFloat();

                    if (failChance < 0.10f) {
                        playCustomSound(p, "alexsmobs:sculk_boomer_fart", 1.0f, 1.0f);
                        p.displayClientMessage(Component.literal("You failed to roll anything!").withStyle(ChatFormatting.RED), true);
                    } else {
                        int roll = p.level().random.nextInt(6);
                        if (roll == 0) {
                            ItemStack gHand = p.getMainHandItem();
                            if (gHand.isDamageableItem()) {
                                gHand.setDamageValue(Math.max(0, gHand.getDamageValue() - (int)(gHand.getMaxDamage() * 0.25f)));
                            }
                            playCustomSound(p, "immersive_aircraft:repair", 1.0f, 1.0f);
                        } else if (roll == 1) {
                            p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 1));
                            playCustomSound(p, "cataclysm:leviathan_roar", 1.0f, 1.0f);
                        } else if (roll == 2) {
                            p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 1));
                            playCustomSound(p, "dungeonnowloading:fairkeeper_boros_armor_break", 1.0f, 1.0f);
                        } else if (roll == 3) {
                            p.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 600, 0));
                            playCustomSound(p, "combatroll:roll_cooldown_ready", 1.0f, 1.0f);
                        } else if (roll == 4) {
                            p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, 2));
                            p.addEffect(new MobEffectInstance(MobEffects.JUMP, 600, 1));
                            playCustomSound(p, "goety:wind_blast", 1.0f, 1.0f);
                        } else if (roll == 5) {
                            p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 400, 2));
                            playCustomSound(p, "goety:wight_teleport_scream", 1.0f, 1.0f);
                        }
                    }
                    setCooldown((ServerPlayer) p, c, 3600);
                    syncToClient((ServerPlayer) p, c);
                }
            }

            if (p.tickCount % 20 == 0) {
                if (p.level().random.nextFloat() < 0.05f) {
                    boolean cured = false;
                    for (MobEffectInstance instance : new ArrayList<>(p.getActiveEffects())) {
                        if (!instance.getEffect().isBeneficial()) {
                            p.removeEffect(instance.getEffect());
                            cured = true;
                        }
                    }
                    if (cured) playCustomSound(p, "minecraft:block.amethyst_block.chime", 1.0F, 1.0F);
                }

                if (p.level().random.nextFloat() < 0.05f) {
                    List<MobEffectInstance> effects = new ArrayList<>(p.getActiveEffects());
                    if (!effects.isEmpty()) {
                        MobEffectInstance inst = effects.get(p.level().random.nextInt(effects.size()));
                        if (inst.getAmplifier() < 4) {
                            p.addEffect(new MobEffectInstance(inst.getEffect(), inst.getDuration(), inst.getAmplifier() + 1, inst.isAmbient(), inst.isVisible(), inst.showIcon()));
                        }
                    }
                }
            }
        }

        if (c.equals("machinist")) {
            int repairTicks = data.getInt("lt_repair_ticks");
            if (repairTicks > 0 && p.tickCount % 4 == 0) {
                playCustomSound(p, "immersive_aircraft:repair", 1.0F, 1.0F + (3 - repairTicks) * 0.15F);
                data.putInt("lt_repair_ticks", repairTicks - 1);
            }
        }

        if (c.equals("north_mage") && data.getBoolean("lt_nmage_active")) {
            long buffEnd = data.getLong("lt_nmage_end");
            if (p.level().getGameTime() >= buffEnd) {
                data.putBoolean("lt_nmage_active", false);
                removeAttribute(p, "irons_spellbooks:max_mana", NMAGE_MANA_ID);
                removeAttribute(p, "ars_nouveau:ars_nouveau.perk.max_mana", NMAGE_MANA_ID);
                syncToClient((ServerPlayer) p, c);
            }
        }

        if (c.equals("north_mage")) {
            addAttribute(p, "puffish_attributes:armor_shred", NMAGE_SHRED_ID, 1.0, AttributeModifier.Operation.ADDITION);
        } else {
            removeAttribute(p, "puffish_attributes:armor_shred", NMAGE_SHRED_ID);
        }

        if (c.equals("ranger")) {
            boolean sneaking = p.isCrouching();
            if (sneaking && !data.getBoolean("lt_path_sneak")) {
                playCustomSound(p, "combatroll:roll_cooldown_ready", 0.7F, 1.0F);
                data.putBoolean("lt_path_sneak", true);
            } else if (!sneaking) {
                data.putBoolean("lt_path_sneak", false);
            }
        }

        if (c.equals("tank") || c.equals("tanker")) {
            long cdEnd = data.getLong("lt_cd_tank");
            boolean isStill = p.isCrouching() && p.getDeltaMovement().horizontalDistanceSqr() < 0.005;

            if (isStill && p.level().getGameTime() >= cdEnd) {
                if (!data.getBoolean("lt_was_tanking")) {
                    playCustomSound(p, "dungeonnowloading:fairkeeper_boros_armor_break", 1.0F, 1.0F);
                    data.putBoolean("lt_was_tanking", true);
                }
                p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 10, 0, false, false, true));
            } else if (!isStill && data.getBoolean("lt_was_tanking")) {
                data.putBoolean("lt_was_tanking", false);
                setCooldown((ServerPlayer)p, "tank", 200);
                syncToClient((ServerPlayer)p, c);
            }
        }

        if (c.equals("monk")) {
            int ticks = data.getInt("lt_monk_ticks");
            if (p.isSprinting()) {
                if (ticks < 60) {
                    ticks++;
                    if (ticks == 60) {
                        playCustomSound(p, "goety:wind_blast", 0.6F, 1.2F);
                    }
                }
                data.putInt("lt_monk_ticks", ticks);
                if (ticks >= 60) {
                    p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 10, 1, false, false, true));
                    addAttribute(p, "forge:step_height_addition", MONK_STEP_ID, 2.0, AttributeModifier.Operation.ADDITION);
                }
            } else {
                ticks = Math.max(0, ticks - 2);
                data.putInt("lt_monk_ticks", ticks);
                if (ticks < 60) {
                    removeAttribute(p, "forge:step_height_addition", MONK_STEP_ID);
                }
            }

            if (p.tickCount % 40 == 0) {
                long lastDamageTime = data.getLong("lt_last_damage_time");
                if (p.level().getGameTime() - lastDamageTime >= 200 && p.getHealth() < p.getMaxHealth()) {
                    p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, false, false, true));
                }
            }
        } else {
            removeAttribute(p, "forge:step_height_addition", MONK_STEP_ID);
        }

        if (c.equals("vampire")) {
            boolean isDay = p.level().isDay() && p.level().canSeeSky(p.blockPosition());
            boolean wasDay = data.getBoolean("lt_was_day");

            if (isDay != wasDay) {
                data.putBoolean("lt_was_day", isDay);
                if (isDay) playCustomSound(p, "goety:frozen_zombie_death", 1.0F, 1.0F);
                else playCustomSound(p, "goety:wight_teleport_scream", 1.0F, 1.0F);
            }

            if (isDay) {
                p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0, false, false, true));
                addAttribute(p, "minecraft:generic.max_health", VAMP_DAY_ID, -4.0, AttributeModifier.Operation.ADDITION);
            } else {
                removeAttribute(p, "minecraft:generic.max_health", VAMP_DAY_ID);
                if (data.getBoolean("lt_vamp_active")) {
                    p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, false, false, true));
                    p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 0, false, false, true));
                }
            }
        } else {
            removeAttribute(p, "minecraft:generic.max_health", VAMP_DAY_ID);
        }

        if (c.equals("berserker")) {
            long currentTime = p.level().getGameTime();
            long rageCooldown = data.getLong("lt_rage_cooldown");
            long rageEnd = data.getLong("lt_rage_end");

            boolean isRaging = currentTime < rageEnd;

            if (p.getHealth() <= 6.0f && !isRaging && currentTime >= rageCooldown) {
                isRaging = true;
                data.putLong("lt_rage_end", currentTime + 1200);

                data.putLong("lt_rage_cooldown", currentTime + 4800);

                playCustomSound(p, "cataclysm:leviathan_roar", 1.0F, 1.0F);
            }

            if (isRaging && p.tickCount % 10 == 0) {
                p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 30, 1, false, false, true));
                p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 30, 0, false, false, true));
                p.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 30, 1, false, false, true));
            }

            if (p.getHealth() <= 6.0f && p.isAlive()) {
                int hbTimer = data.getInt("lt_heartbeat_timer");
                if (hbTimer <= 0) {
                    float hp = Math.max(1.0f, Math.min(p.getHealth(), 6.0f));

                    float pitch = 1.5f - ((hp - 1.0f) / 5.0f) * 0.7f;
                    int nextInterval = (int) (10.0f + ((hp - 1.0f) / 5.0f) * 50.0f);

                    playCustomSound(p, "armageddon_mod:heartbeat", 1.0F, pitch);
                    data.putInt("lt_heartbeat_timer", nextInterval);
                } else {
                    data.putInt("lt_heartbeat_timer", hbTimer - 1);
                }
            } else {
                data.putInt("lt_heartbeat_timer", 0);
            }
        }

        if (c.equals("south_mage") && data.getBoolean("lt_smage_active")) {
            if (p.tickCount % 20 == 0) {
                playCustomSound(p, "celestisynth:heartbeat", 1.0F, 1.0F);
                p.hurt(p.damageSources().magic(), 2.0f);
            }
        }
    }

    @SubscribeEvent
    public static void onVisibility(LivingEvent.LivingVisibilityEvent event) {
        if (event.getEntity() instanceof Player p && p.isCrouching()) {
            if ("ranger".equals(getPlayerClass(p))) {
                event.modifyVisibility(0.75);
            }
        }
    }

    @SubscribeEvent
    public static void onSetTarget(LivingChangeTargetEvent event) {
        if (event.getNewTarget() instanceof Player p && "scum".equals(getPlayerClass(p))) {
            if (p.level().random.nextFloat() < 0.5f) {
                event.setNewTarget(null);
            }
        }
    }

    private static void addAttribute(Player p, String regName, UUID id, double val, AttributeModifier.Operation op) {
        Attribute attr = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(regName));
        if (attr == null) return;
        AttributeInstance inst = p.getAttribute(attr);
        if (inst != null && inst.getModifier(id) == null) {
            inst.addTransientModifier(new AttributeModifier(id, "lt_modifier", val, op));
        }
    }

    private static void removeAttribute(Player p, String regName, UUID id) {
        Attribute attr = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(regName));
        if (attr == null) return;
        AttributeInstance inst = p.getAttribute(attr);
        if (inst != null) inst.removeModifier(id);
    }
}