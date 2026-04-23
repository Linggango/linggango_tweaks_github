package com.misanthropy.linggango.linggango_tweaks.qol;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;

@Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID)
public class BoatTweaks {

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }

        if (event.getEntity().getVehicle() instanceof Boat boat) {
            Level level = boat.level();
            BlockPos pos = boat.blockPosition();

            BlockPos floorPos = findFloor(level, pos);
            if (floorPos == null) return;

            BlockState floorState = level.getBlockState(floorPos);
            double depth = pos.getY() - floorPos.getY();

            if (floorState.is(Blocks.SOUL_SAND)) {
                applySoulSandBoost(boat, depth);
            } else if (floorState.is(Blocks.MAGMA_BLOCK)) {
                applyMagmaPull(boat, depth);
            }
        }
    }

    private static BlockPos findFloor(Level level, BlockPos startPos) {

        for (int i = 0; i < 31; i++) {
            BlockPos checkPos = startPos.below(i);
            BlockState state = level.getBlockState(checkPos);

            if (state.is(Blocks.SOUL_SAND) || state.is(Blocks.MAGMA_BLOCK)) {
                return checkPos;
            }

            if (!state.is(Blocks.WATER) && !state.is(Blocks.BUBBLE_COLUMN)) {
                return null;
            }
        }
        return null;
    }

    private static void applySoulSandBoost(Boat boat, double depth) {

        double strength = Math.min(0.15, 0.02 + (depth * 0.005));
        Vec3 delta = boat.getDeltaMovement();
        boat.setDeltaMovement(delta.x, delta.y + strength, delta.z);
    }

    private static void applyMagmaPull(Boat boat, double depth) {

        double strength = Math.max(0, 0.1 - (depth * 0.003));
        Vec3 delta = boat.getDeltaMovement();
        boat.setDeltaMovement(delta.x, delta.y - strength, delta.z);
    }
}