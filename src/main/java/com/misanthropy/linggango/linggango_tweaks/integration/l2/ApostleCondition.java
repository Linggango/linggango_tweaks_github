package com.misanthropy.linggango.linggango_tweaks.integration.l2;

import com.Polarice3.Goety.common.entities.boss.Apostle;
import dev.xkmc.l2hostility.content.config.SpecialConfigCondition;
import dev.xkmc.l2serial.serialization.SerialClass;
import org.jspecify.annotations.NonNull;
import z1gned.goetyrevelation.util.ApollyonAbilityHelper;

import javax.annotation.Nullable;

@SuppressWarnings("unused")
@SerialClass
public class ApostleCondition extends SpecialConfigCondition<ApostleEntityContext> {

    @SerialClass.SerialField
    @Nullable
    public int[] titleNumber;

    @SerialClass.SerialField
    @Nullable
    public Boolean isApollyon;

    @SerialClass.SerialField
    @Nullable
    public Boolean isSecondPhase;

    public ApostleCondition() {
        super(ApostleEntityContext.class);
    }

    @Override
    public boolean test(@NonNull ApostleEntityContext context) {
        if (!(context.le() instanceof Apostle apostle)) return false;
        ApollyonAbilityHelper helper = (ApollyonAbilityHelper) apostle;
        if (titleNumber != null) {
            int current = helper.allTitleApostle$getTitleNumber();
            boolean matched = false;
            for (int t : titleNumber) {
                if (t == current) { matched = true; break; }
            }
            if (!matched) return false;
        }
        if (isApollyon != null && helper.allTitlesApostle_1_20_1$isApollyon() != isApollyon) return false;
        return isSecondPhase == null || apostle.isSecondPhase() == isSecondPhase;
    }
}
