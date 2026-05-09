package com.misanthropy.linggango.linggango_tweaks.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@SuppressWarnings("all")
public final class SmoothGuiSupport {
    private static final float LINGGANGO_TWEAKS_MAX_DT = 0.1F;

    private static @Nullable Field linggango_tweaks$selectedCreativeTabField = null;
    private static sun.misc.Unsafe linggango_tweaks$unsafe = null;
    private static long linggango_tweaks$slotYOffset = Long.MIN_VALUE;

    private SmoothGuiSupport() {
    }

    public static float linggango_tweaks$getDeltaSeconds(long now, long lastTime) {
        if (lastTime == 0L) {
            return 0.0F;
        }

        return Mth.clamp((now - lastTime) / 1000.0F, 0.0F, LINGGANGO_TWEAKS_MAX_DT);
    }

    public static float linggango_tweaks$expLerp(float speed, float current, float target, float dt) {
        if (dt <= 0.0F) {
            return current;
        }

        return Mth.lerp(1.0F - (float) Math.exp(-speed * dt), current, target);
    }

    public static int linggango_tweaks$getTotalScrollableRows(int itemCount, int columns, int visibleRows) {
        return Math.max(0, (itemCount + columns - 1) / columns - visibleRows);
    }

    public static int linggango_tweaks$getBaseGridSlotY(int slotIndex, int gridTop, int columns, int slotStride) {
        return gridTop + slotIndex / columns * slotStride;
    }

    public static boolean linggango_tweaks$shouldSmoothCreativeGrid(@Nullable CreativeModeTab tab) {
        return tab != null && tab.canScroll() && tab.getType() != CreativeModeTab.Type.INVENTORY;
    }

    public static boolean linggango_tweaks$hasTransform(float scale, float lift) {
        return Math.abs(scale - 1.0F) > 0.001F || Math.abs(lift) > 0.01F;
    }

    public static void linggango_tweaks$pushCenteredScale(@NonNull GuiGraphics guiGraphics, float centerX, float centerY, float scale) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(centerX, centerY, 0.0F);
        guiGraphics.pose().scale(scale, scale, 1.0F);
        guiGraphics.pose().translate(-centerX, -centerY, 0.0F);
    }

    public static void linggango_tweaks$enableCreativeGridScissor(@NonNull GuiGraphics guiGraphics, int leftPos, int topPos, int gridLeft, int gridTop, int gridRight, int gridBottom) {
        guiGraphics.enableScissor(leftPos + gridLeft, topPos + gridTop, leftPos + gridRight, topPos + gridBottom);
    }

    public static boolean linggango_tweaks$isMouseWithinVisibleSlot(int leftPos, int topPos, @NonNull Slot slot, double mouseX, double mouseY, int gridLeft, int gridTop, int gridRight, int gridBottom, int slotRenderSize) {
        int slotLeft = leftPos + slot.x;
        int slotTop = topPos + slot.y;
        int visibleLeft = Math.max(slotLeft, leftPos + gridLeft);
        int visibleTop = Math.max(slotTop, topPos + gridTop);
        int visibleRight = Math.min(slotLeft + slotRenderSize, leftPos + gridRight);
        int visibleBottom = Math.min(slotTop + slotRenderSize, topPos + gridBottom);

        return visibleLeft < visibleRight
                && visibleTop < visibleBottom
                && mouseX >= visibleLeft
                && mouseX < visibleRight
                && mouseY >= visibleTop
                && mouseY < visibleBottom;
    }

    public static @Nullable CreativeModeTab linggango_tweaks$getSelectedCreativeTab() {
        Field selectedTabField = linggango_tweaks$selectedCreativeTabField;
        if (selectedTabField == null) {
            selectedTabField = linggango_tweaks$findSelectedCreativeTabField();
            linggango_tweaks$selectedCreativeTabField = selectedTabField;
        }

        if (selectedTabField == null) {
            return null;
        }

        try {
            return (CreativeModeTab) selectedTabField.get(null);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    public static void linggango_tweaks$setSlotY(@NonNull Slot slot, int y) {
        try {
            if (linggango_tweaks$unsafe == null) {
                Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
                unsafeField.setAccessible(true);
                linggango_tweaks$unsafe = (sun.misc.Unsafe) unsafeField.get(null);
            }

            if (linggango_tweaks$slotYOffset == Long.MIN_VALUE) {
                for (Field field : Slot.class.getDeclaredFields()) {
                    if (field.getType() == int.class && (field.getName().equals("y") || field.getName().equals("f_40221_") || field.getName().equals("field_75221_f"))) {
                        linggango_tweaks$slotYOffset = linggango_tweaks$unsafe.objectFieldOffset(field);
                        break;
                    }
                }
            }

            if (linggango_tweaks$slotYOffset != Long.MIN_VALUE) {
                linggango_tweaks$unsafe.putInt(slot, linggango_tweaks$slotYOffset, y);
            }
        } catch (ReflectiveOperationException ignored) {
        }
    }

    private static @Nullable Field linggango_tweaks$findSelectedCreativeTabField() {
        try {
            Field selectedTab = CreativeModeInventoryScreen.class.getDeclaredField("selectedTab");
            selectedTab.setAccessible(true);
            return selectedTab;
        } catch (ReflectiveOperationException ignored) {
        }

        for (Field field : CreativeModeInventoryScreen.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) && field.getType() == CreativeModeTab.class) {
                field.setAccessible(true);
                return field;
            }
        }

        return null;
    }
}
