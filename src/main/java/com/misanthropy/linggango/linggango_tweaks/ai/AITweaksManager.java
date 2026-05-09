package com.misanthropy.linggango.linggango_tweaks.ai;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID)
public class AITweaksManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(AITweaksManager.class);
    public static void onCommonSetup(final @NonNull FMLCommonSetupEvent event) {
        event.enqueueWork(FastTrig::init);
    }

    @SubscribeEvent
    public static void onEntityJoin(@NonNull EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide || !(event.getEntity() instanceof Mob mob)) return;
        if (mob.getLookControl().getClass() == LookControl.class) {
            try {
                ObfuscationReflectionHelper.setPrivateValue(Mob.class, mob, new FastLookControl(mob), "lookControl");
            } catch (Exception e) {
                LOGGER.error("Failed to inject FastLookControl for entity: {}", mob.getType(), e);
            }
        }

        boolean isFish = mob instanceof AbstractFish;
        List<Goal> goalsToRemove = mob.goalSelector.getAvailableGoals().stream()
                .map(WrappedGoal::getGoal)
                .filter(goal -> shouldRemoveGoal(goal, isFish))
                .toList();

        goalsToRemove.forEach(mob.goalSelector::removeGoal);
    }

    private static boolean shouldRemoveGoal(Goal goal, boolean isFish) {
        if (goal instanceof RandomLookAroundGoal) return true;

        if (isFish) {
            return goal instanceof RandomSwimmingGoal || goal instanceof PanicGoal;
        }

        return false;
    }
}