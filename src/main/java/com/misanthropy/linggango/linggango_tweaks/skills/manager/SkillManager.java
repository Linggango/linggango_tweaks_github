package com.misanthropy.linggango.linggango_tweaks.skills.manager;

import com.misanthropy.linggango.class_enhancement.ClassEnhancement;
import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import com.misanthropy.linggango.linggango_tweaks.skills.network.TweaksSkillNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SkillManager {

    private static final UUID VAMP_DAY_ID       = UUID.fromString("11a3b8d9-6f1c-4b3a-92e2-5c8e7e120f2a");
    private static final UUID SMAGE_BUFF_ID     = UUID.fromString("22a3b8d9-6f1c-4b3a-92e2-5c8e7e120f2b");
    private static final UUID MONK_STEP_ID      = UUID.fromString("33a3b8d9-6f1c-4b3a-92e2-5c8e7e120f2c");
    private static final UUID NMAGE_MANA_ID     = UUID.fromString("44a3b8d9-6f1c-4b3a-92e2-5c8e7e120f2d");
    private static final UUID NMAGE_SHRED_ID    = UUID.fromString("55a3b8d9-6f1c-4b3a-92e2-5c8e7e120f2e");
    private static final UUID BERSERK_HP_ID     = UUID.fromString("66a3b8d9-6f1c-4b3a-92e2-5c8e7e120f2f");
    private static final UUID BERSERK_DMG_ID    = UUID.fromString("77a3b8d9-6f1c-4b3a-92e2-5c8e7e120f3a");
    private static final UUID BERSERK_SPEED_ID  = UUID.fromString("88a3b8d9-6f1c-4b3a-92e2-5c8e7e120f3b");
    private static final UUID BERSERK_SPEED_BUFF_ID = UUID.fromString("99a3b8d9-6f1c-4b3a-92e2-5c8e7e120f3c");

    private static final Map<String, ResourceLocation> RL_CACHE = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, SoundEvent> SOUND_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Attribute> ATTR_CACHE = new ConcurrentHashMap<>();

    private record RecentBreak(UUID playerUuid, BlockPos pos, long tick) {
    }
    private static final List<RecentBreak> RECENT_BREAKS = new ArrayList<>();

    private static ResourceLocation rl(String s) {
        return RL_CACHE.computeIfAbsent(s, ResourceLocation::new);
    }

    private static SoundEvent getSound(String id) {
        return SOUND_CACHE.computeIfAbsent(rl(id), loc -> {
            SoundEvent evt = ForgeRegistries.SOUND_EVENTS.getValue(loc);
            return evt != null ? evt : SoundEvent.createVariableRangeEvent(loc);
        });
    }

    private static Attribute getAttr(String name) {
        return ATTR_CACHE.computeIfAbsent(name, n -> ForgeRegistries.ATTRIBUTES.getValue(rl(n)));
    }

    public static void playCustomSound(@NonNull Player player, @NonNull String soundId, float vol, float pitch) {
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                getSound(soundId), SoundSource.PLAYERS, vol, pitch);
    }

    public static String getPlayerClass(@NonNull Player player) {
        if (player.level().isClientSide) {
            String c = net.minecraftforge.fml.DistExecutor.unsafeCallWhenOn(
                    net.minecraftforge.api.distmarker.Dist.CLIENT,
                    () -> () -> com.misanthropy.linggango.linggango_tweaks.skills.client.ClientSkillEvents.currentClassId
            );
            return (c == null || "civilian".equals(c)) ? "none" : c;
        }
        String c = ClassEnhancement.ClassSavedData.get((ServerLevel) player.level())
                .playerClasses.get(player.getUUID());
        return (c == null || "civilian".equals(c)) ? "none" : c;
    }

    public static void useActiveSkill(@NonNull ServerPlayer player) {
        String classId = getPlayerClass(player);
        CompoundTag data = player.getPersistentData();
        long gameTime = player.level().getGameTime();

        long cdEnd = data.getLong("lt_cd_" + classId);
        if (gameTime < cdEnd) return;

        boolean synced = false;

        switch (classId) {
            case "vampire" -> {
                if (data.getBoolean("lt_vamp_active")) return;
                data.putBoolean("lt_vamp_active", true);
                data.putInt("lt_vamp_hits", 10);
                setCooldown(player, classId, 2400);
                playCustomSound(player, "brutality:blood_magic_missile", 1.0F, 1.0F);
                synced = true;
            }
            case "miner" -> {
                boolean lightActive = !data.getBoolean("lt_miner_light_active");
                data.putBoolean("lt_miner_light_active", lightActive);
                if (lightActive) {
                    playCustomSound(player, "minecraft:block.amethyst_block.chime", 1.0F, 1.2F);
                } else {
                    playCustomSound(player, "minecraft:block.fire.extinguish", 0.5F, 1.5F);
                }
                synced = true;
            }
            case "berserker" -> {
                if (data.getBoolean("lt_berserk_active")) return;
                data.putBoolean("lt_berserk_active", true);
                data.putLong("lt_berserk_end", gameTime + 400);
                setCooldown(player, classId, 6400);

                addAttribute(player, "minecraft:generic.max_health", BERSERK_HP_ID, -0.80, AttributeModifier.Operation.MULTIPLY_BASE);
                addAttribute(player, "minecraft:generic.attack_damage", BERSERK_DMG_ID, 0.15, AttributeModifier.Operation.MULTIPLY_BASE);
                addAttribute(player, "minecraft:generic.attack_speed", BERSERK_SPEED_ID, 0.30, AttributeModifier.Operation.MULTIPLY_BASE);
                addAttribute(player, "minecraft:generic.movement_speed", BERSERK_SPEED_BUFF_ID, 0.10, AttributeModifier.Operation.MULTIPLY_BASE);

                float maxHp = player.getMaxHealth();
                if (player.getHealth() > maxHp) {
                    player.setHealth(maxHp);
                }

                playCustomSound(player, "minecraft:entity.ravager.roar", 1.0F, 1.0F);
                synced = true;
            }
            case "machinist" -> {
                ItemStack hand = player.getMainHandItem();
                if (hand.isDamageableItem()) {
                    int repairAmt = (int) (hand.getMaxDamage() * 0.20f);
                    hand.setDamageValue(Math.max(0, hand.getDamageValue() - repairAmt));
                    setCooldown(player, classId, 12000);
                    data.putInt("lt_repair_ticks", 3);
                    synced = true;
                }
            }
            case "north_mage" -> {
                data.putBoolean("lt_nmage_active", true);
                addAttribute(player, "irons_spellbooks:max_mana", NMAGE_MANA_ID, 0.20, AttributeModifier.Operation.MULTIPLY_BASE);
                addAttribute(player, "ars_nouveau:ars_nouveau.perk.max_mana", NMAGE_MANA_ID, 0.20, AttributeModifier.Operation.MULTIPLY_BASE);
                data.putLong("lt_nmage_end", gameTime + 200);
                playCustomSound(player, "minecraft:block.enchantment_table.use", 1.0F, 1.0F);
                synced = true;
            }
            case "south_mage" -> {
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
            }
            case "gambler" -> {
                if (data.getInt("lt_gambler_roll_timer") > 0) return;
                data.putInt("lt_gambler_roll_timer", 40);
                synced = true;
            }
            case "gunner" -> {
                if (!data.getBoolean("lt_gunner_active")) {
                    data.putBoolean("lt_gunner_active", true);
                    data.putLong("lt_gunner_timeout", gameTime + 100);
                    playCustomSound(player, "minecraft:entity.tnt.primed", 1.0F, 1.0F);
                    synced = true;
                }
            }
            case "tank", "tanker" -> {
                data.putBoolean("lt_tank_absorb_active", true);
                data.putLong("lt_tank_absorb_end", gameTime + 60);
                data.putFloat("lt_tank_absorbed_damage", 0.0f);
                setCooldown(player, classId, 2400);
                playCustomSound(player, "minecraft:item.shield.block", 1.0F, 0.5F);
                synced = true;
            }
        }

        if (synced) syncToClient(player, classId);
    }

    public static void setCooldown(@NonNull ServerPlayer player, String classId, int ticks) {
        long end = player.level().getGameTime() + ticks;
        CompoundTag data = player.getPersistentData();
        data.putLong("lt_cd_" + classId, end);
        data.putInt("lt_maxcd_" + classId, ticks);
    }

    public static void syncToClient(@NonNull ServerPlayer player, @NonNull String classId) {
        CompoundTag data = player.getPersistentData();
        long gameTime = player.level().getGameTime();
        int remaining = (int) Math.max(0, data.getLong("lt_cd_" + classId) - gameTime);
        int max = data.getInt("lt_maxcd_" + classId);

        boolean isActive = switch (classId) {
            case "south_mage" -> data.getBoolean("lt_smage_active");
            case "north_mage" -> data.getBoolean("lt_nmage_active");
            case "berserker"  -> data.getBoolean("lt_berserk_active");
            case "vampire"    -> data.getBoolean("lt_vamp_active");
            case "miner"      -> data.getBoolean("lt_miner_light_active");
            case "gambler"    -> data.getInt("lt_gambler_roll_timer") > 0;
            case "gunner"     -> data.getBoolean("lt_gunner_active");
            case "tank", "tanker" -> data.getBoolean("lt_tank_absorb_active");
            case "ranger"     -> data.getInt("lt_ranger_stacks") > 0;
            default -> false;
        };

        if ("ranger".equals(classId)) {
            remaining = 0;
            max = data.getInt("lt_ranger_stacks");
        }

        TweaksSkillNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                new TweaksSkillNetwork.SkillSyncS2CPacket(classId, remaining, max, isActive));
    }

    @SubscribeEvent
    public static void onLivingDeath(@NonNull LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!"berserker".equals(getPlayerClass(player))) return;

        long gameTime = player.level().getGameTime();
        if (gameTime < player.getPersistentData().getLong("lt_cd_berserk_revive")) return;

        event.setCanceled(true);
        player.setHealth(1.0F);
        player.getPersistentData().putLong("lt_cd_berserk_revive", gameTime + 12000);
        playCustomSound(player, "brutality:big_explosion", 1.0F, 1.0F);
    }

    @SubscribeEvent
    public static void onLivingDamage(@NonNull LivingDamageEvent event) {
        if (event.getEntity().level().isClientSide) return;

        LivingEntity victim = event.getEntity();
        net.minecraft.world.entity.Entity attackerEnt = event.getSource().getEntity();

        boolean victimIsPlayer = victim instanceof ServerPlayer;
        boolean attackerIsPlayer = attackerEnt instanceof ServerPlayer;

        if (!victimIsPlayer && !attackerIsPlayer) return;

        long gameTime = victim.level().getGameTime();

        if (victimIsPlayer) {
            ServerPlayer damaged = (ServerPlayer) victim;
            String damagedClass = getPlayerClass(damaged);
            CompoundTag data = damaged.getPersistentData();
            data.putLong("lt_last_damage_time", gameTime);

            if ("gambler".equals(damagedClass)) {
                float rand = damaged.level().random.nextFloat();
                if (rand < 0.01f) {
                    event.setAmount(Float.MAX_VALUE);
                } else if (rand < 0.11f) {
                    event.setAmount(event.getAmount() * 3.0f);
                }
            }

            if ("ranger".equals(damagedClass)) {
                if (data.getInt("lt_ranger_stacks") > 0) {
                    data.putInt("lt_ranger_stacks", 0);
                    syncToClient(damaged, damagedClass);
                    playCustomSound(damaged, "minecraft:block.glass.break", 1.0f, 0.5f);
                }

                if (damaged.level().random.nextFloat() < 0.10f) {
                    int bone = damaged.level().random.nextInt(3);
                    playCustomSound(damaged, "minecraft:entity.skeleton.hurt", 1.0f, 0.5f);
                    if (bone == 0) {
                        damaged.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 1));
                        damaged.displayClientMessage(Component.literal("You broke your leg!").withStyle(ChatFormatting.RED), true);
                    } else if (bone == 1) {
                        damaged.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0));
                        damaged.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 0));
                        playCustomSound(damaged, "minecraft:entity.enderman.stare", 1.0f, 1.0f);
                        damaged.displayClientMessage(Component.literal("You suffered a concussion!").withStyle(ChatFormatting.RED), true);
                    } else {
                        damaged.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 200, 1));
                        damaged.displayClientMessage(Component.literal("You injured your arm! Your draw speed drops heavily.").withStyle(ChatFormatting.RED), true);
                    }
                }
            }

            if ("tank".equals(damagedClass) || "tanker".equals(damagedClass)) {
                if (data.getBoolean("lt_tank_absorb_active") && gameTime < data.getLong("lt_tank_absorb_end")) {
                    float absorbed = data.getFloat("lt_tank_absorbed_damage");
                    data.putFloat("lt_tank_absorbed_damage", absorbed + event.getAmount());
                    event.setCanceled(true);
                    playCustomSound(damaged, "minecraft:item.shield.block", 1.0f, 0.8f);
                } else {
                    float healthAfter = damaged.getHealth() - event.getAmount();
                    if (healthAfter <= 4.0f && healthAfter > 0.0f && !data.getBoolean("lt_tank_cdr_used")) {
                        long currentCd = data.getLong("lt_cd_" + damagedClass);
                        if (currentCd > gameTime) {
                            data.putLong("lt_cd_" + damagedClass, Math.max(gameTime, currentCd - 600));
                            data.putBoolean("lt_tank_cdr_used", true);
                            playCustomSound(damaged, "minecraft:block.beacon.activate", 1.0f, 2.0f);
                            syncToClient(damaged, damagedClass);
                        }
                    }
                }
            }
        }

        if (attackerIsPlayer) {
            ServerPlayer player = (ServerPlayer) attackerEnt;
            String attackerClass = getPlayerClass(player);
            CompoundTag data = player.getPersistentData();

            if ("vampire".equals(attackerClass) && data.getBoolean("lt_vamp_active")) {
                int hits = data.getInt("lt_vamp_hits");
                if (hits > 0) {
                    hits--;
                    data.putInt("lt_vamp_hits", hits);
                    player.heal(event.getAmount() * 0.60f);

                    if (hits <= 0) {
                        data.putBoolean("lt_vamp_active", false);
                        playCustomSound(player, "terra_entity:blood_crawler_hurt", 1.0F, 1.0F);
                    } else {
                        String[] bloodSounds = {
                                "simplyswords:magic_sword_attack_with_blood_01",
                                "simplyswords:magic_sword_attack_with_blood_02",
                                "simplyswords:magic_sword_attack_with_blood_03",
                                "simplyswords:magic_sword_attack_with_blood_04"
                        };
                        String chosenSound = bloodSounds[player.getRandom().nextInt(bloodSounds.length)];
                        playCustomSound(player, chosenSound, 1.0F, 1.0F);
                    }
                    syncToClient(player, attackerClass);
                }
            }

            if ("gambler".equals(attackerClass)) {
                float rand = player.level().random.nextFloat();
                if (rand < 0.10f) {
                    event.setAmount(event.getAmount() * 3.0f);
                    playCustomSound(player, "minecraft:entity.experience_orb.pickup", 1.0F, 2.0F);
                }
                if (player.level().random.nextFloat() < 0.05f) {
                    LivingEntity target = event.getEntity();
                    MobEffect[] badEffects = {
                            MobEffects.POISON, MobEffects.WITHER, MobEffects.WEAKNESS,
                            MobEffects.MOVEMENT_SLOWDOWN, MobEffects.BLINDNESS
                    };
                    MobEffect chosen = badEffects[player.level().random.nextInt(badEffects.length)];
                    target.addEffect(new MobEffectInstance(chosen, 100, 0));
                }
            }

            if ("ranger".equals(attackerClass) && event.getSource().getDirectEntity() instanceof Projectile) {
                int stacks = data.getInt("lt_ranger_stacks");
                if (stacks < 5) {
                    stacks++;
                    data.putInt("lt_ranger_stacks", stacks);
                    syncToClient(player, attackerClass);
                }
                
                float mult = 1.0f + (stacks * 0.02f);
                event.setAmount(event.getAmount() * mult);

                playCustomSound(player, "minecraft:entity.experience_orb.pickup", 0.5f, 1.0f + (stacks * 0.1f));
            }

            if ("tank".equals(attackerClass) || "tanker".equals(attackerClass)) {
                float storedDamage = data.getFloat("lt_tank_absorbed_damage");
                if (storedDamage > 0) {
                    event.setAmount(event.getAmount() + storedDamage);
                    data.putFloat("lt_tank_absorbed_damage", 0);
                    playCustomSound(player, "minecraft:entity.generic.explode", 0.5f, 1.2f);
                }
            }

            if ("monk".equals(attackerClass) && !data.getBoolean("lt_monk_is_double_hitting")) {
                if (player.level().random.nextFloat() < 0.05f) {
                    LivingEntity target = event.getEntity();
                    data.putBoolean("lt_monk_is_double_hitting", true);

                    target.invulnerableTime = 0;
                    target.hurt(player.damageSources().playerAttack(player), event.getAmount());
                    playCustomSound(player, "minecraft:entity.player.attack.sweep", 1.0f, 1.5f);

                    data.putBoolean("lt_monk_is_double_hitting", false);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player.level().isClientSide) return;
        if (!"miner".equals(getPlayerClass(player))) return;

        synchronized (RECENT_BREAKS) {
            RECENT_BREAKS.add(new RecentBreak(player.getUUID(), event.getPos().immutable(), player.level().getGameTime()));
        }
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide) return;

        if (event.getEntity() instanceof ItemEntity itemEntity) {
            long gameTime = event.getLevel().getGameTime();
            RecentBreak match = null;

            synchronized (RECENT_BREAKS) {
                RECENT_BREAKS.removeIf(b -> gameTime - b.tick > 2);

                for (RecentBreak b : RECENT_BREAKS) {
                    double dx = itemEntity.getX() - (b.pos.getX() + 0.5);
                    double dy = itemEntity.getY() - (b.pos.getY() + 0.5);
                    double dz = itemEntity.getZ() - (b.pos.getZ() + 0.5);
                    double distSq = dx * dx + dy * dy + dz * dz;

                    if (distSq < 2.25) {
                        match = b;
                        break;
                    }
                }
            }

            if (match != null && event.getLevel() instanceof ServerLevel serverLevel) {
                ServerPlayer player = (ServerPlayer) serverLevel.getPlayerByUUID(match.playerUuid);
                if (player != null) {
                    ItemStack tool = player.getMainHandItem();
                    int toolFortune = tool.getEnchantmentLevel(net.minecraft.world.item.enchantment.Enchantments.BLOCK_FORTUNE);
                    boolean hasSilk = tool.getEnchantmentLevel(net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH) > 0;

                    if (!hasSilk && toolFortune < 2) {
                        ItemStack stack = itemEntity.getItem();
                        if (isFortuneAffected(stack.getItem())) {
                            int count = stack.getCount();
                            float rand = player.getRandom().nextFloat();
                            int multiplier = 1;

                            if (rand < 0.25f) multiplier = 2;
                            else if (rand < 0.50f) multiplier = 3;

                            if (multiplier > 1) {
                                stack.setCount(count * multiplier);
                                itemEntity.setItem(stack);
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean isFortuneAffected(net.minecraft.world.item.Item item) {
        ResourceLocation rl = ForgeRegistries.ITEMS.getKey(item);
        if (rl == null) return false;
        String path = rl.getPath();

        if (path.startsWith("raw_") || path.endsWith("_gem") || path.contains("ruby") || path.contains("sapphire") || path.contains("amethyst")) {
            return true;
        }

        return item == Items.DIAMOND || item == Items.EMERALD || item == Items.COAL ||
                item == Items.LAPIS_LAZULI || item == Items.REDSTONE || item == Items.QUARTZ ||
                item == Items.AMETHYST_SHARD || item == Items.GLOWSTONE_DUST || item == Items.CLAY_BALL ||
                item == Items.FLINT || item == Items.PRISMARINE_CRYSTALS || item == Items.MELON_SLICE ||
                item == Items.NETHER_WART || item == Items.SWEET_BERRIES || item == Items.GLOW_BERRIES ||
                item == Items.CARROT || item == Items.POTATO || item == Items.WHEAT_SEEDS ||
                item == Items.BEETROOT_SEEDS || item == Items.COCOA_BEANS;
    }

    @SubscribeEvent
    public static void onHarvestCheck(PlayerEvent.HarvestCheck event) {
        Player player = event.getEntity();
        if (!"miner".equals(getPlayerClass(player))) return;

        ItemStack stack = player.getMainHandItem();
        if (isPickaxe(stack)) {
            BlockState state = event.getTargetBlock();
            if (canStonePickaxeHarvest(state)) {
                event.setCanHarvest(true);
            }
        }
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        if (!"miner".equals(getPlayerClass(player))) return;

        ItemStack stack = player.getMainHandItem();
        if (isPickaxe(stack)) {
            BlockState state = event.getState();
            if (canStonePickaxeHarvest(state)) {
                event.setNewSpeed(Math.max(event.getNewSpeed(), 4.0f));
            }
        }
    }

    private static boolean isPickaxe(ItemStack stack) {
        return stack.isEmpty() || !stack.canPerformAction(net.minecraftforge.common.ToolActions.PICKAXE_DIG);
    }

    private static boolean canStonePickaxeHarvest(BlockState state) {
        if (!state.is(net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE)) {
            return false;
        }
        return !state.is(net.minecraft.tags.BlockTags.NEEDS_IRON_TOOL) &&
                !state.is(net.minecraft.tags.BlockTags.NEEDS_DIAMOND_TOOL);
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
        Player p = event.getEntity();
        if (p.level().isClientSide) return;
        CompoundTag data = p.getPersistentData();
        if (data.contains("lt_miner_light_x")) {
            BlockPos oldPos = new BlockPos(
                    data.getInt("lt_miner_light_x"),
                    data.getInt("lt_miner_light_y"),
                    data.getInt("lt_miner_light_z")
            );
            removeLightBlock(p.level(), oldPos);
            data.remove("lt_miner_light_x");
            data.remove("lt_miner_light_y");
            data.remove("lt_miner_light_z");
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent event) {
        Player p = event.getEntity();
        if (p.level().isClientSide) return;
        CompoundTag data = p.getPersistentData();
        if (data.contains("lt_miner_light_x") && p.getServer() != null) {
            ServerLevel oldLevel = p.getServer().getLevel(event.getFrom());
            if (oldLevel != null) {
                BlockPos oldPos = new BlockPos(
                        data.getInt("lt_miner_light_x"),
                        data.getInt("lt_miner_light_y"),
                        data.getInt("lt_miner_light_z")
                );
                removeLightBlock(oldLevel, oldPos);
            }
            data.remove("lt_miner_light_x");
            data.remove("lt_miner_light_y");
            data.remove("lt_miner_light_z");
        }
    }

    private static void removeLightBlock(net.minecraft.world.level.Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(Blocks.LIGHT)) {
            if (state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED)
                    && state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED)) {
                level.setBlock(pos, Blocks.WATER.defaultBlockState(), 3);
            } else {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.@NonNull PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Player p = event.player;
        CompoundTag data = p.getPersistentData();
        String c = getPlayerClass(p);
        long gameTime = p.level().getGameTime();
        boolean isServer = !p.level().isClientSide;

        if (isServer && p.tickCount % 40 == 0) {
            syncToClient((ServerPlayer) p, c);
        }

        if (p.level().isClientSide) return;

        if (p.tickCount % 100 == 0) {
            synchronized (RECENT_BREAKS) {
                RECENT_BREAKS.removeIf(b -> gameTime - b.tick > 5);
            }
        }

        if (!"north_mage".equals(c) && data.getBoolean("lt_nmage_active")) {
            data.putBoolean("lt_nmage_active", false);
            removeAttribute(p, "irons_spellbooks:max_mana", NMAGE_MANA_ID);
            removeAttribute(p, "ars_nouveau:ars_nouveau.perk.max_mana", NMAGE_MANA_ID);
        }
        if (!"berserker".equals(c) && data.getBoolean("lt_berserk_active")) {
            data.putBoolean("lt_berserk_active", false);
            removeAttribute(p, "minecraft:generic.max_health", BERSERK_HP_ID);
            removeAttribute(p, "minecraft:generic.attack_damage", BERSERK_DMG_ID);
            removeAttribute(p, "minecraft:generic.attack_speed", BERSERK_SPEED_ID);
            removeAttribute(p, "minecraft:generic.movement_speed", BERSERK_SPEED_BUFF_ID);
        }
        if (!"vampire".equals(c) && data.getBoolean("lt_vamp_active")) {
            data.putBoolean("lt_vamp_active", false);
            data.remove("lt_vamp_hits");
        }

        if ("miner".equals(c)) {
            boolean lightActive = data.getBoolean("lt_miner_light_active");
            boolean hadLightPos = data.contains("lt_miner_light_x");
            BlockPos currentPos = p.blockPosition();

            BlockPos oldPos = null;
            if (hadLightPos) {
                oldPos = new BlockPos(
                        data.getInt("lt_miner_light_x"),
                        data.getInt("lt_miner_light_y"),
                        data.getInt("lt_miner_light_z")
                );
            }

            if (lightActive) {
                BlockPos targetPos = null;
                for (int i = 0; i <= 2; i++) {
                    BlockPos checkPos = currentPos.above(i);
                    BlockState state = p.level().getBlockState(checkPos);
                    boolean isCurrentLight = hadLightPos && checkPos.equals(oldPos) && state.is(Blocks.LIGHT);
                    if (state.isAir() || state.is(Blocks.CAVE_AIR) || isCurrentLight || (state.is(Blocks.WATER) && state.getFluidState().isSource())) {
                        targetPos = checkPos;
                        break;
                    }
                }

                if (targetPos != null) {
                    if (oldPos == null || !oldPos.equals(targetPos)) {
                        if (oldPos != null) {
                            removeLightBlock(p.level(), oldPos);
                        }

                        BlockState targetState = p.level().getBlockState(targetPos);
                        boolean isWater = targetState.is(Blocks.WATER);
                        BlockState placementState = Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, 15);
                        if (isWater) {
                            placementState = placementState.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED, true);
                        }

                        p.level().setBlock(targetPos, placementState, 3);
                        data.putInt("lt_miner_light_x", targetPos.getX());
                        data.putInt("lt_miner_light_y", targetPos.getY());
                        data.putInt("lt_miner_light_z", targetPos.getZ());
                    }
                } else {
                    if (oldPos != null) {
                        removeLightBlock(p.level(), oldPos);
                    }
                    data.remove("lt_miner_light_x");
                    data.remove("lt_miner_light_y");
                    data.remove("lt_miner_light_z");
                }
            } else if (hadLightPos) {
                if (oldPos != null) {
                    removeLightBlock(p.level(), oldPos);
                }
                data.remove("lt_miner_light_x");
                data.remove("lt_miner_light_y");
                data.remove("lt_miner_light_z");
            }
        } else {
            if (data.contains("lt_miner_light_x")) {
                BlockPos oldPos = new BlockPos(
                        data.getInt("lt_miner_light_x"),
                        data.getInt("lt_miner_light_y"),
                        data.getInt("lt_miner_light_z")
                );
                removeLightBlock(p.level(), oldPos);
                data.remove("lt_miner_light_x");
                data.remove("lt_miner_light_y");
                data.remove("lt_miner_light_z");
            }
            if (data.getBoolean("lt_miner_light_active")) {
                data.putBoolean("lt_miner_light_active", false);
            }
        }

        if ("berserker".equals(c)) {
            if (data.getBoolean("lt_berserk_active")) {
                p.addEffect(new MobEffectInstance(MobEffects.JUMP, 10, 1, false, false, true));
                if (gameTime >= data.getLong("lt_berserk_end")) {
                    data.putBoolean("lt_berserk_active", false);
                    removeAttribute(p, "minecraft:generic.max_health", BERSERK_HP_ID);
                    removeAttribute(p, "minecraft:generic.attack_damage", BERSERK_DMG_ID);
                    removeAttribute(p, "minecraft:generic.attack_speed", BERSERK_SPEED_ID);
                    removeAttribute(p, "minecraft:generic.movement_speed", BERSERK_SPEED_BUFF_ID);
                    syncToClient((ServerPlayer) p, c);
                }
            }

            float health = p.getHealth();
            if (health <= 6.0f) {
                int hbTimer = data.getInt("lt_heartbeat_timer");
                if (hbTimer <= 0) {
                    float hp = Math.max(1.0f, Math.min(health, 6.0f));
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

        if ("vampire".equals(c)) {
            boolean isDay = p.level().isDay() && p.level().canSeeSky(p.blockPosition());
            boolean wasDay = data.getBoolean("lt_was_day");

            if (isDay != wasDay) {
                data.putBoolean("lt_was_day", isDay);
                playCustomSound(p, isDay ? "goety:frozen_zombie_death" : "goety:wight_teleport_scream", 1.0F, 1.0F);
            }

            if (isDay) {
                p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0, false, false, true));
                addAttribute(p, "minecraft:generic.max_health", VAMP_DAY_ID, -4.0, AttributeModifier.Operation.ADDITION);
                data.putBoolean("lt_vamp_hp_applied", true);
            } else {
                if (data.getBoolean("lt_vamp_hp_applied")) {
                    removeAttribute(p, "minecraft:generic.max_health", VAMP_DAY_ID);
                    data.remove("lt_vamp_hp_applied");
                }
                p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, false, false, true));
                p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 0, false, false, true));
            }
        } else if (data.getBoolean("lt_vamp_hp_applied")) {
            removeAttribute(p, "minecraft:generic.max_health", VAMP_DAY_ID);
            data.remove("lt_vamp_hp_applied");
        }

        if ("gambler".equals(c)) {
            int rollTimer = data.getInt("lt_gambler_roll_timer");
            if (rollTimer > 0) {
                rollTimer--;
                data.putInt("lt_gambler_roll_timer", rollTimer);

                if (rollTimer % 4 == 0) {
                    playCustomSound(p, "minecraft:ui.button.click", 0.5F,
                            1.5F + (p.level().random.nextFloat() * 0.5F));
                }

                if (rollTimer == 0) {
                    float failChance = p.level().random.nextFloat();
                    if (failChance < 0.10f) {
                        playCustomSound(p, "alexsmobs:sculk_boomer_fart", 1.0f, 1.0f);
                        p.displayClientMessage(Component.literal("You failed to roll anything!")
                                .withStyle(ChatFormatting.RED), true);
                    } else {
                        int roll = p.level().random.nextInt(6);
                        switch (roll) {
                            case 0 -> {
                                ItemStack gHand = p.getMainHandItem();
                                if (gHand.isDamageableItem()) {
                                    gHand.setDamageValue(Math.max(0, gHand.getDamageValue()
                                            - (int) (gHand.getMaxDamage() * 0.25f)));
                                }
                                playCustomSound(p, "immersive_aircraft:repair", 1.0f, 1.0f);
                            }
                            case 1 -> {
                                p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 1));
                                playCustomSound(p, "cataclysm:leviathan_roar", 1.0f, 1.0f);
                            }
                            case 2 -> {
                                p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 1));
                                playCustomSound(p, "dungeonnowloading:fairkeeper_boros_armor_break", 1.0f, 1.0f);
                            }
                            case 3 -> {
                                p.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 600, 0));
                                playCustomSound(p, "combatroll:roll_cooldown_ready", 1.0f, 1.0f);
                            }
                            case 4 -> {
                                p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, 2));
                                p.addEffect(new MobEffectInstance(MobEffects.JUMP, 600, 1));
                                playCustomSound(p, "goety:wind_blast", 1.0f, 1.0f);
                            }
                            case 5 -> {
                                p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 400, 2));
                                playCustomSound(p, "goety:wight_teleport_scream", 1.0f, 1.0f);
                            }
                        }
                    }
                    setCooldown((ServerPlayer) p, c, 3600);
                    syncToClient((ServerPlayer) p, c);
                }
            }

            if (p.tickCount % 20 == 0) {
                boolean hasBad = false;
                for (MobEffectInstance instance : p.getActiveEffects()) {
                    if (!instance.getEffect().isBeneficial()) {
                        hasBad = true;
                        break;
                    }
                }
                if (hasBad) {
                    List<MobEffect> toRemove = new ArrayList<>();
                    for (MobEffectInstance instance : p.getActiveEffects()) {
                        if (!instance.getEffect().isBeneficial()) {
                            toRemove.add(instance.getEffect());
                        }
                    }
                    for (MobEffect effect : toRemove) {
                        p.removeEffect(effect);
                    }
                    playCustomSound(p, "minecraft:block.amethyst_block.chime", 1.0F, 1.0F);
                }

                if (p.level().random.nextFloat() < 0.05f) {
                    List<MobEffectInstance> effects = new ArrayList<>(p.getActiveEffects());
                    if (!effects.isEmpty()) {
                        MobEffectInstance inst = effects.get(p.level().random.nextInt(effects.size()));
                        if (inst.getAmplifier() < 4) {
                            p.addEffect(new MobEffectInstance(inst.getEffect(), inst.getDuration(),
                                    inst.getAmplifier() + 1, inst.isAmbient(), inst.isVisible(), inst.showIcon()));
                        }
                    }
                }
            }
        }

        if ("machinist".equals(c)) {
            int repairTicks = data.getInt("lt_repair_ticks");
            if (repairTicks > 0 && p.tickCount % 4 == 0) {
                playCustomSound(p, "immersive_aircraft:repair", 1.0F,
                        1.0F + (3 - repairTicks) * 0.15F);
                data.putInt("lt_repair_ticks", repairTicks - 1);
            }
        }

        if ("north_mage".equals(c) && data.getBoolean("lt_nmage_active")) {
            if (gameTime >= data.getLong("lt_nmage_end")) {
                data.putBoolean("lt_nmage_active", false);
                removeAttribute(p, "irons_spellbooks:max_mana", NMAGE_MANA_ID);
                removeAttribute(p, "ars_nouveau:ars_nouveau.perk.max_mana", NMAGE_MANA_ID);
                setCooldown((ServerPlayer) p, c, 800);
                syncToClient((ServerPlayer) p, c);
            }
        }

        if ("gunner".equals(c) && data.getBoolean("lt_gunner_active")) {
            if (gameTime >= data.getLong("lt_gunner_timeout")) {
                data.putBoolean("lt_gunner_active", false);
                setCooldown((ServerPlayer) p, c, 600);
                syncToClient((ServerPlayer) p, c);
            }
        }

        if ("north_mage".equals(c)) {
            addAttribute(p, "puffish_attributes:armor_shred", NMAGE_SHRED_ID, 1.0, AttributeModifier.Operation.ADDITION);
            data.putBoolean("lt_shred_applied", true);
        } else if (data.getBoolean("lt_shred_applied")) {
            removeAttribute(p, "puffish_attributes:armor_shred", NMAGE_SHRED_ID);
            data.remove("lt_shred_applied");
        }

        if ("tank".equals(c) || "tanker".equals(c)) {
            if (p.getHealth() > 4.0f) {
                data.putBoolean("lt_tank_cdr_used", false);
            }

            if (data.getBoolean("lt_tank_absorb_active") && gameTime >= data.getLong("lt_tank_absorb_end")) {
                data.putBoolean("lt_tank_absorb_active", false);
                playCustomSound(p, "minecraft:block.anvil.use", 0.5f, 1.5f);
                syncToClient((ServerPlayer) p, c);
            }
        }

        if ("monk".equals(c)) {
            int ticks = data.getInt("lt_monk_ticks");
            if (p.isSprinting()) {
                if (ticks < 60) {
                    if (++ticks == 60) {
                        playCustomSound(p, "goety:wind_blast", 0.6F, 1.2F);
                    }
                }
                data.putInt("lt_monk_ticks", ticks);
                if (ticks >= 60) {
                    p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 10, 1, false, false, true));
                    addAttribute(p, "forge:step_height_addition", MONK_STEP_ID, 2.0, AttributeModifier.Operation.ADDITION);
                    data.putBoolean("lt_monk_step_applied", true);
                }
            } else {
                ticks = Math.max(0, ticks - 2);
                data.putInt("lt_monk_ticks", ticks);
                if (ticks < 60 && data.getBoolean("lt_monk_step_applied")) {
                    removeAttribute(p, "forge:step_height_addition", MONK_STEP_ID);
                    data.remove("lt_monk_step_applied");
                }
            }

            if (p.tickCount % 40 == 0) {
                long lastDmg = data.getLong("lt_last_damage_time");
                if (gameTime - lastDmg >= 200 && p.getHealth() < p.getMaxHealth()) {
                    p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, false, false, true));
                }
            }
        } else if (data.getBoolean("lt_monk_step_applied")) {
            removeAttribute(p, "forge:step_height_addition", MONK_STEP_ID);
            data.remove("lt_monk_step_applied");
        }

        if ("south_mage".equals(c) && data.getBoolean("lt_smage_active")) {
            if (p.tickCount % 20 == 0) {
                playCustomSound(p, "celestisynth:heartbeat", 1.0F, 1.0F);
                p.hurt(p.damageSources().magic(), 2.0f);
            }
        }
    }

    @SubscribeEvent
    public static void onSetTarget(@NonNull LivingChangeTargetEvent event) {
        if (event.getNewTarget() instanceof Player p
                && "scum".equals(getPlayerClass(p))
                && p.level().random.nextFloat() < 0.5f) {
            event.setNewTarget(null);
        }
    }

    private static void addAttribute(@NonNull Player p, @NonNull String regName,
                                     @NonNull UUID id, double val,
                                     AttributeModifier.@NonNull Operation op) {
        Attribute attr = getAttr(regName);
        if (attr == null) return;
        AttributeInstance inst = p.getAttribute(attr);
        if (inst != null && inst.getModifier(id) == null) {
            inst.addTransientModifier(new AttributeModifier(id, "lt_modifier", val, op));
        }
    }

    private static void removeAttribute(@NonNull Player p, @NonNull String regName, @NonNull UUID id) {
        Attribute attr = getAttr(regName);
        if (attr == null) return;
        AttributeInstance inst = p.getAttribute(attr);
        if (inst != null && inst.getModifier(id) != null) {
            inst.removeModifier(id);
        }
    }
    }