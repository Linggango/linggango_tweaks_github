package com.misanthropy.linggango.linggango_tweaks.qol;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Field;

@Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RainfallSerenityBuffHandler {

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());

        if (id == null || !id.getNamespace().equals("celestisynth")) return;

        String path = id.getPath();

        try {

            if (path.equals("rainfall_rain")) {
                Field baseDamageField = entity.getClass().getField("baseDamage");
                baseDamageField.setDouble(entity, 15.0);
            }

            else if (path.equals("rainfall_arrow") && entity instanceof AbstractArrow arrow) {
                Field turretSourceField = entity.getClass().getField("turretSource");
                Object turretSource = turretSourceField.get(entity);

                if (turretSource != null) {
                    arrow.setBaseDamage(30.0);
                } else {
                    if (arrow.getBaseDamage() < 15.0) {
                        arrow.setBaseDamage(20.0);
                    }
                }
            }
        } catch (Exception ignored) {

        }
    }

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();

        if (player.isCrouching() && !player.level().isClientSide()) {
            if (stack.getItem().getDescriptionId().contains("rainfall_serenity")) {
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 100, 1, false, false, true));
            }
        }
    }
}