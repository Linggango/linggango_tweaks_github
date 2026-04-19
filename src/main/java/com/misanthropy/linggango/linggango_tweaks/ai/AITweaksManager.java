package com.misanthropy.linggango.linggango_tweaks.ai;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.jspecify.annotations.NonNull;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID)
public class AITweaksManager {

    public static void onCommonSetup(final @NonNull FMLCommonSetupEvent event) {
        event.enqueueWork(FastTrig::init);
    }

    @SubscribeEvent
    public static void onEntityJoin(@NonNull EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;
        if (event.getLevel().isClientSide) return;
        if (mob.getLookControl().getClass() == LookControl.class) {
            FastLookControl fastControl = new FastLookControl(mob);
            try {
                Field lookControlField = Mob.class.getDeclaredField("lookControl");
                lookControlField.setAccessible(true);
                lookControlField.set(mob, fastControl);
            } catch (Exception e) {
                try {
                    Field srgField = Mob.class.getDeclaredField("f_21365_");
                    srgField.setAccessible(true);
                    srgField.set(mob, fastControl);
                } catch (Exception ignored) {}
            }
        }
        Set<Goal> goalsToRemove = new HashSet<>();

        mob.goalSelector.getAvailableGoals().forEach(wrappedGoal -> {
            Goal goal = wrappedGoal.getGoal();
            if (goal instanceof RandomLookAroundGoal) {
                goalsToRemove.add(goal);
            }
            if (mob instanceof AbstractFish) {
                if (goal instanceof RandomSwimmingGoal || goal instanceof PanicGoal) {
                    goalsToRemove.add(goal);
                }
            }
        });
        for (Goal g : goalsToRemove) {
            mob.goalSelector.removeGoal(g);
        }
    }
}