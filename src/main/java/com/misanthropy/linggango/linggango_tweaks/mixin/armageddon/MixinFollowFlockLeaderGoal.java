package com.misanthropy.linggango.linggango_tweaks.mixin.armageddon;

import net.minecraft.world.entity.ai.goal.FollowFlockLeaderGoal;
import net.minecraft.world.entity.animal.AbstractSchoolingFish;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Predicate;

@Mixin(FollowFlockLeaderGoal.class)
public class MixinFollowFlockLeaderGoal {
    @ModifyArg(
            method = "canUse",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/stream/Stream;filter(Ljava/util/function/Predicate;)Ljava/util/stream/Stream;",
                    ordinal = 1
            )
    )
    private @NonNull Predicate<AbstractSchoolingFish> linggango$addHasFollowersCheck(@NonNull Predicate<AbstractSchoolingFish> originalPredicate) {
        return originalPredicate.and(Predicate.not(AbstractSchoolingFish::hasFollowers));
    }
}