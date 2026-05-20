package com.misanthropy.linggango.linggango_tweaks.util.legacy; // Credits: https://www.curseforge.com/minecraft/mc-mods/celestweaks

import com.mojang.logging.LogUtils;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("all")
public class EnigmaticReflectionUtil {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static ParticleOptions BLUE_STAR_DUST = null;
    private static ParticleOptions RED_STAR_DUST = null;
    private static ParticleOptions PURPLE_STAR_DUST = null;
    private static ParticleOptions ABYSS_CHAOS = null;
    private static boolean addonParticlesInitialized = false;

    private static Constructor<?> permanentConstructor = null;
    private static Method setOwnerIdMethod = null;
    private static Method setThrowerIdMethod = null;
    private static boolean permanentInitialized = false;

    private static Method addLocalizedStringMethod;
    private static Method isOKOneMethod;
    private static Method isTheWorthyOneMethod;
    private static Method addHiddenRecipeMethod;
    private static boolean hiddenRecipeInitialized;

    private static void initAddonParticles() {
        if (!addonParticlesInitialized) {
            if (!ModList.get().isLoaded("enigmaticaddons")) {
                addonParticlesInitialized = true;
            } else {
                try {
                    Class<?> particlesClass = Class.forName("auviotre.enigmatic.addon.registries.EnigmaticAddonParticles");
                    BLUE_STAR_DUST = (ParticleOptions) particlesClass.getField("BLUE_STAR_DUST").get(null);
                    RED_STAR_DUST = (ParticleOptions) particlesClass.getField("RED_STAR_DUST").get(null);
                    PURPLE_STAR_DUST = (ParticleOptions) particlesClass.getField("PURPLE_STAR_DUST").get(null);
                    ABYSS_CHAOS = (ParticleOptions) particlesClass.getField("ABYSS_CHAOS").get(null);
                } catch (Throwable e) {
                    BLUE_STAR_DUST = null;
                    RED_STAR_DUST = null;
                    PURPLE_STAR_DUST = null;
                    ABYSS_CHAOS = null;
                }
                addonParticlesInitialized = true;
            }
        }
    }

    public static ParticleOptions getPurpleStarDust() {
        if (!addonParticlesInitialized) initAddonParticles();
        return PURPLE_STAR_DUST;
    }

    public static ParticleOptions getBlueStarDust() {
        if (!addonParticlesInitialized) initAddonParticles();
        return BLUE_STAR_DUST;
    }

    public static ParticleOptions getRedStarDust() {
        if (!addonParticlesInitialized) initAddonParticles();
        return RED_STAR_DUST;
    }

    public static ParticleOptions getAbyssChaos() {
        if (!addonParticlesInitialized) initAddonParticles();
        return ABYSS_CHAOS;
    }

    private static void initPermanentEntity() {
        if (!permanentInitialized) {
            try {
                Class<?> clazz = Class.forName("com.aizistral.enigmaticlegacy.entities.PermanentItemEntity");
                permanentConstructor = clazz.getConstructor(Level.class, Double.TYPE, Double.TYPE, Double.TYPE, ItemStack.class);
                setOwnerIdMethod = clazz.getMethod("setOwnerId", UUID.class);
                setThrowerIdMethod = clazz.getMethod("setThrowerId", UUID.class);
            } catch (Throwable e) {
                permanentConstructor = null;
                setOwnerIdMethod = null;
                setThrowerIdMethod = null;
            }
            permanentInitialized = true;
        }
    }

    public static Entity createPermanentItemEntity(ServerLevel level, double x, double y, double z, ItemStack stack, UUID ownerUUID) {
        if (!isLegacyLoaded()) return null;

        initPermanentEntity();
        if (permanentConstructor == null) return null;

        try {
            Object entity = permanentConstructor.newInstance(level, x, y, z, stack);
            if (ownerUUID != null) {
                if (setOwnerIdMethod != null) setOwnerIdMethod.invoke(entity, ownerUUID);
                if (setThrowerIdMethod != null) setThrowerIdMethod.invoke(entity, ownerUUID);
            }
            return (Entity) entity;
        } catch (Throwable e) {
            LOGGER.error("Failed to create PermanentItemEntity via reflection", e);
            return null;
        }
    }

    public static void addLocalizedString(List<Component> list, String key) {
        if (addLocalizedStringMethod != null) {
            try {
                addLocalizedStringMethod.invoke(null, list, key);
            } catch (Throwable ignored) {}
        }
    }

    public static boolean isOKOne(Player player) {
        if (isOKOneMethod == null) return false;
        try {
            return (Boolean) isOKOneMethod.invoke(null, player);
        } catch (Throwable e) {
            LOGGER.error("Failed to invoke isOKOne via reflection", e);
            return false;
        }
    }

    public static boolean isTheWorthyOne(Player player) {
        if (isTheWorthyOneMethod == null) return false;
        try {
            return (Boolean) isTheWorthyOneMethod.invoke(null, player);
        } catch (Throwable e) {
            LOGGER.error("Failed to invoke isTheWorthyOne via reflection", e);
            return false;
        }
    }

    public static boolean isLegacyLoaded() {
        return ModList.get().isLoaded("enigmaticlegacy");
    }

    public static boolean isAddonsLoaded() {
        return ModList.get().isLoaded("enigmaticaddons");
    }

    public static boolean isCombinedLoaded() {
        return isAddonsLoaded() && isLegacyLoaded();
    }

    private static void initHiddenRecipe() {
        if (!hiddenRecipeInitialized) {
            try {
                Class<?> hiddenRecipeClass = Class.forName("com.aizistral.enigmaticlegacy.crafting.HiddenRecipe");
                addHiddenRecipeMethod = hiddenRecipeClass.getMethod("addRecipe", ItemStack.class, ItemStack[].class);
            } catch (Throwable e) {
                addHiddenRecipeMethod = null;
            }
            hiddenRecipeInitialized = true;
        }
    }

    public static void tryAddHiddenRecipe(ItemStack output, ItemStack... inputs) {
        if (isLegacyLoaded()) {
            initHiddenRecipe();
            if (addHiddenRecipeMethod != null) {
                try {
                    addHiddenRecipeMethod.invoke(null, output, (Object) inputs);
                } catch (Throwable e) {
                    LOGGER.error("Failed to invoke addRecipe for HiddenRecipe via reflection", e);
                }
            }
        }
    }

    public static ItemStack getEnigmaticItem(String itemName) {
        if (!isLegacyLoaded()) return ItemStack.EMPTY;
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("enigmaticlegacy", itemName));
        return item != null ? new ItemStack(item) : ItemStack.EMPTY;
    }

    public static ItemStack getAddonItem(String itemName) {
        if (!isAddonsLoaded()) return ItemStack.EMPTY;
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("enigmaticaddons", itemName));
        return item != null ? new ItemStack(item) : ItemStack.EMPTY;
    }

    static {
        try {
            Class<?> helper = Class.forName("com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper");
            addLocalizedStringMethod = helper.getMethod("addLocalizedString", List.class, String.class);
        } catch (Throwable e) {
            addLocalizedStringMethod = null;
        }

        try {
            Class<?> superAddonHandlerClass = Class.forName("auviotre.enigmatic.addon.handlers.SuperAddonHandler");
            isOKOneMethod = superAddonHandlerClass.getMethod("isOKOne", Player.class);
        } catch (Throwable e) {
            isOKOneMethod = null;
        }

        try {
            Class<?> superpositionHandlerClass = Class.forName("com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler");
            isTheWorthyOneMethod = superpositionHandlerClass.getMethod("isTheWorthyOne", Player.class);
        } catch (Throwable e) {
            isTheWorthyOneMethod = null;
        }
    }
}