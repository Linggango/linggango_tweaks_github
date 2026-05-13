package com.misanthropy.linggango.linggango_tweaks.mixin.fixes;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

@SuppressWarnings("unused")
public class ExperimentalFixes {

    @Mixin(Entity.MoveFunction.class)
    public interface EntityMoveFunctionConnectorBridgeMixin {
    }

    @Mixin(EntityType.class)
    public interface EntityTypeCategoryAccessor {
        @Mutable
        @Accessor("category")
        void linggango$setCategory(MobCategory category);
    }

    @Mixin(EntityType.class)
    public static abstract class MalformedEntityLoadMixin {
        @Inject(method = "loadEntityRecursive", at = @At("HEAD"), cancellable = true)
        private static void linggango$skipBlankEntityIds(CompoundTag tag, Level level, Function<Entity, Entity> entityTransformer, CallbackInfoReturnable<Entity> cir) {
            if (tag.getString("id").isBlank()) {
                cir.setReturnValue(null);
            }
        }
    }

    @Mixin(ItemFrame.class)
    public static abstract class MalformedItemFrameLoadMixin {
        @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
        private void linggango$removeMalformedDisplayedItem(CompoundTag tag, CallbackInfo ci) {
            if (!tag.contains("Item")) return;

            if (!tag.contains("Item", 10) || linggango$isMalformedItemFrameItem(tag.getCompound("Item"))) {
                tag.remove("Item");
            }
        }

        @Unique
        private static boolean linggango$isMalformedItemFrameItem(CompoundTag itemTag) {
            return itemTag.getString("id").isBlank() ||
                    itemTag.contains("components", 10) ||
                    itemTag.contains("count") ||
                    !itemTag.contains("Count", 99) ||
                    itemTag.getInt("Count") <= 0;
        }
    }
}