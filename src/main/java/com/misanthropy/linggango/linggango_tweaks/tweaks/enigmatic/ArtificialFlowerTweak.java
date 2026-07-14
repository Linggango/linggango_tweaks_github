package com.misanthropy.linggango.linggango_tweaks.tweaks.enigmatic;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = "linggango_tweaks", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ArtificialFlowerTweak {
    private static final Random RANDOM = new Random();
    private static List<MobEffect> VANILLA_BENEFICIAL;
    private static List<MobEffect> VANILLA_HARMFUL;
    private static Class<?> cachedFlowerClass;
    private static boolean flowerClassCached = false;
    private static boolean poolsReplaced = false;

    private static boolean curiosChecked = false;
    private static Object curiosHelper = null;
    private static Method getEquippedCurios = null;
    private static Method isPresent = null;
    private static Method orElse = null;
    private static Method getSlots = null;
    private static Method getStackInSlot = null;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) return;
        if (event.player.tickCount % 20 == 0) {
            scanAndSanitize(event.player, null);
        }
    }

    @SubscribeEvent
    public static void onEffectApply(MobEffectEvent.Applicable event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()) return;

        MobEffectInstance instance = event.getEffectInstance();

        MobEffect effect = instance.getEffect();
        ResourceLocation effectKey = ForgeRegistries.MOB_EFFECTS.getKey(effect);

        if (effectKey == null || effectKey.getNamespace().equals("minecraft")) return;

        if (scanAndSanitize(player, effectKey)) {
            event.setResult(Result.DENY);
        }
    }

    private static boolean scanAndSanitize(Player player, @Nullable ResourceLocation checkEffect) {
        boolean matchFound = false;

        int invSize = player.getInventory().getContainerSize();
        for (int i = 0; i < invSize; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (isArtificialFlower(stack)) {
                if (checkEffect != null && hasModdedEffect(stack, checkEffect)) {
                    matchFound = true;
                }
                sanitizeFlower(stack);
            }
        }

        if (sanitizeCuriosReflection(player, checkEffect)) {
            matchFound = true;
        }

        return matchFound;
    }

    private static boolean isArtificialFlower(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Item item = stack.getItem();

        lazyInit();
        if (cachedFlowerClass != null) {
            return cachedFlowerClass.isInstance(item);
        }

        if (item.getClass().getSimpleName().equals("ArtificialFlower")) {
            cachedFlowerClass = item.getClass();
            return true;
        }

        ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
        return key != null && key.getPath().equals("artificial_flower");
    }

    private static boolean hasModdedEffect(ItemStack stack, ResourceLocation effectKey) {
        if (stack.isEmpty() || !stack.hasTag()) return false;
        CompoundTag tag = stack.getTag();
        String target = effectKey.toString();
        assert tag != null;
        if (tag.contains("PotionEffect0", 8) && tag.getString("PotionEffect0").equals(target)) {
            return true;
        }
        return tag.contains("PotionEffect1", 8) && tag.getString("PotionEffect1").equals(target);
    }

    private static void sanitizeFlower(ItemStack stack) {
        if (stack.isEmpty()) return;
        CompoundTag tag = stack.getOrCreateTag();

        String effect0 = tag.contains("PotionEffect0", 8) ? tag.getString("PotionEffect0") : "";
        String effect1 = tag.contains("PotionEffect1", 8) ? tag.getString("PotionEffect1") : "";

        if (!effect0.isEmpty() && isVanilla(effect0)) {
            MobEffect moddedEffect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(effect0));
            boolean beneficial = moddedEffect == null || moddedEffect.isBeneficial();
            MobEffect replacement = getRandomVanillaEffectExcluding(beneficial, effect1);
            if (replacement != null) {
                ResourceLocation key = ForgeRegistries.MOB_EFFECTS.getKey(replacement);
                if (key != null) {
                    tag.putString("PotionEffect0", key.toString());
                    effect0 = key.toString();
                }
            }
        }

        if (!effect1.isEmpty() && isVanilla(effect1)) {
            MobEffect moddedEffect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(effect1));
            boolean beneficial = moddedEffect != null && moddedEffect.isBeneficial();
            MobEffect replacement = getRandomVanillaEffectExcluding(beneficial, effect0);
            if (replacement != null) {
                ResourceLocation key = ForgeRegistries.MOB_EFFECTS.getKey(replacement);
                if (key != null) {
                    tag.putString("PotionEffect1", key.toString());
                }
            }
        }
    }

    private static boolean isVanilla(String effectId) {
        if (effectId == null || effectId.isEmpty()) return true;
        ResourceLocation rl = ResourceLocation.tryParse(effectId);
        return rl == null || !rl.getNamespace().equals("minecraft");
    }

    private static MobEffect getRandomVanillaEffectExcluding(boolean beneficial, String excludeId) {
        lazyInit();
        List<MobEffect> pool = beneficial ? VANILLA_BENEFICIAL : VANILLA_HARMFUL;
        if (pool == null || pool.isEmpty()) return beneficial ? MobEffects.REGENERATION : MobEffects.POISON;

        List<MobEffect> candidates = pool.stream()
                .filter(effect -> {
                    ResourceLocation key = ForgeRegistries.MOB_EFFECTS.getKey(effect);
                    return key == null || !key.toString().equals(excludeId);
                })
                .toList();

        if (candidates.isEmpty()) {
            return pool.get(RANDOM.nextInt(pool.size()));
        }
        return candidates.get(RANDOM.nextInt(candidates.size()));
    }

    private static boolean sanitizeCuriosReflection(Player player, @Nullable ResourceLocation checkEffect) {
        try {
            if (!curiosChecked) {
                Class<?> apiClass = Class.forName("top.theillusivec4.curios.api.CuriosApi");
                curiosHelper = apiClass.getMethod("getCuriosHelper").invoke(null);
                getEquippedCurios = curiosHelper.getClass().getMethod("getEquippedCurios", net.minecraft.world.entity.LivingEntity.class);
                curiosChecked = true;
            }
            if (curiosHelper == null || getEquippedCurios == null) return false;

            Object lazyOptional = getEquippedCurios.invoke(curiosHelper, player);
            if (lazyOptional == null) return false;

            if (isPresent == null || orElse == null) {
                Class<?> lazyOptionalClass = lazyOptional.getClass();
                isPresent = lazyOptionalClass.getMethod("isPresent");
                orElse = lazyOptionalClass.getMethod("orElse", Object.class);
            }

            if ((boolean) isPresent.invoke(lazyOptional)) {
                Object handler = orElse.invoke(lazyOptional, (Object) null);
                if (handler != null) {
                    if (getSlots == null || getStackInSlot == null) {
                        Class<?> handlerClass = handler.getClass();
                        getSlots = handlerClass.getMethod("getSlots");
                        getStackInSlot = handlerClass.getMethod("getStackInSlot", int.class);
                    }

                    int slots = (int) getSlots.invoke(handler);
                    boolean matchFound = false;
                    for (int i = 0; i < slots; i++) {
                        ItemStack stack = (ItemStack) getStackInSlot.invoke(handler, i);
                        if (isArtificialFlower(stack)) {
                            if (checkEffect != null && hasModdedEffect(stack, checkEffect)) {
                                matchFound = true;
                            }
                            sanitizeFlower(stack);
                        }
                    }
                    return matchFound;
                }
            }
        } catch (Throwable ignored) {
            curiosChecked = true;
        }
        return false;
    }

    private static void lazyInit() {
        if (VANILLA_BENEFICIAL == null || !flowerClassCached) {
            initializeListsAndCache();
        }
        replaceFlowerPools();
    }

    @SuppressWarnings("unchecked")
    private static void replaceFlowerPools() {
        if (poolsReplaced) return;
        try {
            Class<?> helperClass = Class.forName("auviotre.enigmatic.addon.contents.items.ArtificialFlower$Helper");

            Field potionField = helperClass.getDeclaredField("potionEffectPool");
            Field allField = helperClass.getDeclaredField("allEffectPool");

            potionField.setAccessible(true);
            allField.setAccessible(true);

            List<MobEffect> currentPotionPool = (List<MobEffect>) potionField.get(null);
            List<MobEffect> currentAllPool = (List<MobEffect>) allField.get(null);

            if (currentPotionPool != null && !currentPotionPool.isEmpty()) {
                List<MobEffect> newPotionPool = currentPotionPool.stream()
                        .filter(e -> {
                            ResourceLocation key = ForgeRegistries.MOB_EFFECTS.getKey(e);
                            return key != null && key.getNamespace().equals("minecraft");
                        })
                        .toList();
                potionField.set(null, newPotionPool);
            }

            if (currentAllPool != null && !currentAllPool.isEmpty()) {
                List<MobEffect> newAllPool = currentAllPool.stream()
                        .filter(e -> {
                            ResourceLocation key = ForgeRegistries.MOB_EFFECTS.getKey(e);
                            return key != null && key.getNamespace().equals("minecraft");
                        })
                        .toList();
                allField.set(null, newAllPool);
            }

            poolsReplaced = true;
        } catch (Throwable ignored) {}
    }

    private static void initializeListsAndCache() {
        List<MobEffect> vanillaEffects = ForgeRegistries.MOB_EFFECTS.getValues().stream()
                .filter(e -> {
                    ResourceLocation key = ForgeRegistries.MOB_EFFECTS.getKey(e);
                    return key != null && key.getNamespace().equals("minecraft");
                })
                .toList();

        VANILLA_BENEFICIAL = vanillaEffects.stream().filter(MobEffect::isBeneficial).collect(Collectors.toList());
        VANILLA_HARMFUL = vanillaEffects.stream().filter(e -> !e.isBeneficial()).collect(Collectors.toList());

        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            if (item.getClass().getSimpleName().equals("ArtificialFlower")) {
                cachedFlowerClass = item.getClass();
                break;
            }
        }
        flowerClassCached = true;
    }
}