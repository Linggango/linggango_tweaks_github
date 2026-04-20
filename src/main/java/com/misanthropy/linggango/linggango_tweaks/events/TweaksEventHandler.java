package com.misanthropy.linggango.linggango_tweaks.events;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jspecify.annotations.NonNull;


@Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TweaksEventHandler {

    @SubscribeEvent
    public static void onLivingHurt(@NonNull LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            ItemStack weapon = attacker.getMainHandItem();

            if (weapon.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof TorchBlock) {
                event.getEntity().setSecondsOnFire(5);
            }
        }
    }

    @SubscribeEvent
    public static void onCropBreak(BlockEvent.@NonNull BreakEvent event) {
        BlockState state = event.getState();

        if (state.getBlock() instanceof CropBlock crop) {
            if (crop.isMaxAge(state)) {
                event.setExpToDrop(1);
            }
        }
    }

            }