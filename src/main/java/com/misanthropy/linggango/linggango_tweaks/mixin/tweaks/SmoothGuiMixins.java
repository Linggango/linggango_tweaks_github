package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.WeakHashMap;

@SuppressWarnings("unused")
public class SmoothGuiMixins {

    @Mixin(AbstractWidget.class)
    public static abstract class AnimatedButtonMixin {
        @Unique private float linggango_tweaks$currentScale = 1.0F;
        @Unique private long linggango_tweaks$lastTime = 0L;

        @Inject(method = "render", at = @At("HEAD"))
        private void linggango_tweaks$pushHoverScale(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
            AbstractWidget widget = (AbstractWidget) (Object) this;
            if (!widget.visible || !(widget instanceof Button)) return;

            long time = Util.getMillis();
            if (linggango_tweaks$lastTime == 0L) linggango_tweaks$lastTime = time;
            float dt = (time - linggango_tweaks$lastTime) / 1000.0F;
            linggango_tweaks$lastTime = time;
            if (dt > 0.1F) dt = 0.1F;

            float target = widget.isHoveredOrFocused() ? 1.05F : 1.0F;
            linggango_tweaks$currentScale = Mth.lerp(1.0F - (float) Math.exp(-18.0F * dt), linggango_tweaks$currentScale, target);

            float cx = widget.getX() + widget.getWidth() / 2.0F;
            float cy = widget.getY() + widget.getHeight() / 2.0F;

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(cx, cy, 0);
            guiGraphics.pose().scale(linggango_tweaks$currentScale, linggango_tweaks$currentScale, 1.0F);
            guiGraphics.pose().translate(-cx, -cy, 0);
        }

        @Inject(method = "render", at = @At("RETURN"))
        private void linggango_tweaks$popHoverScale(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
            AbstractWidget widget = (AbstractWidget) (Object) this;
            if (!widget.visible || !(widget instanceof Button)) return;
            guiGraphics.pose().popPose();
        }
    }

    @Mixin(CreativeModeInventoryScreen.class)
    public static abstract class AnimatedCreativeTabsMixin {
        @Unique private static java.lang.reflect.Field linggango_tweaks$tabField1 = null;
        @Unique private static CreativeModeTab linggango_tweaks$getTab1() {
            if (linggango_tweaks$tabField1 == null) {
                for (java.lang.reflect.Field f : CreativeModeInventoryScreen.class.getDeclaredFields()) {
                    if (f.getType() == CreativeModeTab.class && java.lang.reflect.Modifier.isStatic(f.getModifiers())) {
                        f.setAccessible(true);
                        linggango_tweaks$tabField1 = f;
                        break;
                    }
                }
            }
            try { return linggango_tweaks$tabField1 != null ? (CreativeModeTab) linggango_tweaks$tabField1.get(null) : null; }
            catch (Exception e) { return null; }
        }

        @Unique private final Map<CreativeModeTab, Float> linggango_tweaks$tabScales = new WeakHashMap<>();
        @Unique private long linggango_tweaks$lastTabTime = 0L;
        @Unique private float linggango_tweaks$dt = 0.0F;
        @Unique private int linggango_tweaks$mouseX = 0;
        @Unique private int linggango_tweaks$mouseY = 0;
        @Unique private CreativeModeTab linggango_tweaks$currentTab = null;
        @Unique private boolean linggango_tweaks$matrixPushed = false;
        @Unique private static long linggango_tweaks$lastClickSoundTime = 0L;
        @Unique private static CreativeModeTab linggango_tweaks$lastClickedTab = null;

        @Inject(method = "render", at = @At("HEAD"))
        private void linggango_tweaks$captureMouse(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
            linggango_tweaks$mouseX = mouseX;
            linggango_tweaks$mouseY = mouseY;
            long time = Util.getMillis();
            if (linggango_tweaks$lastTabTime == 0L) linggango_tweaks$lastTabTime = time;
            linggango_tweaks$dt = (time - linggango_tweaks$lastTabTime) / 1000.0F;
            linggango_tweaks$lastTabTime = time;
            if (linggango_tweaks$dt > 0.1F) linggango_tweaks$dt = 0.1F;
        }

        @Inject(method = "renderTabButton", at = @At("HEAD"))
        private void linggango_tweaks$startTabRender(GuiGraphics guiGraphics, CreativeModeTab tab, CallbackInfo ci) {
            linggango_tweaks$currentTab = tab;
            linggango_tweaks$matrixPushed = false;
        }

        @Redirect(method = "renderTabButton", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"))
        private void linggango_tweaks$wrapTabWithScale(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y, int uOffset, int vOffset, int uWidth, int vHeight) {
            if (!linggango_tweaks$matrixPushed && linggango_tweaks$currentTab != null) {
                CreativeModeTab selectedTab = linggango_tweaks$getTab1();
                boolean isSelected = linggango_tweaks$currentTab == selectedTab;
                boolean isHovered = linggango_tweaks$mouseX >= x && linggango_tweaks$mouseX <= x + uWidth &&
                        linggango_tweaks$mouseY >= y && linggango_tweaks$mouseY <= y + vHeight;
                float target = isSelected ? 1.12F : (isHovered ? 1.08F : 1.0F);

                float currentScale = linggango_tweaks$tabScales.getOrDefault(linggango_tweaks$currentTab, 1.0F);
                currentScale = Mth.lerp(1.0F - (float) Math.exp(-18.0F * linggango_tweaks$dt), currentScale, target);
                linggango_tweaks$tabScales.put(linggango_tweaks$currentTab, currentScale);

                float cx = x + (uWidth / 2.0F);
                float cy = y + (vHeight / 2.0F);

                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(cx, cy, 0);
                guiGraphics.pose().scale(currentScale, currentScale, 1.0F);
                guiGraphics.pose().translate(-cx, -cy, 0);
                linggango_tweaks$matrixPushed = true;
            }
            guiGraphics.blit(texture, x, y, uOffset, vOffset, uWidth, vHeight);
        }

        @Inject(method = "renderTabButton", at = @At("RETURN"))
        private void linggango_tweaks$endTabRender(GuiGraphics guiGraphics, CreativeModeTab tab, CallbackInfo ci) {
            if (linggango_tweaks$matrixPushed) {
                guiGraphics.pose().popPose();
                linggango_tweaks$matrixPushed = false;
            }
            linggango_tweaks$currentTab = null;
        }

        @Inject(method = "selectTab", at = @At("HEAD"))
        private void linggango_tweaks$onTabClickedSound(CreativeModeTab tab, CallbackInfo ci) {
            CreativeModeTab selectedTab = linggango_tweaks$getTab1();
            if (tab != selectedTab) {
                long time = Util.getMillis();
                if (time - linggango_tweaks$lastClickSoundTime > 250L || linggango_tweaks$lastClickedTab != tab) {
                    linggango_tweaks$lastClickSoundTime = time;
                    linggango_tweaks$lastClickedTab = tab;
                    Minecraft.getInstance().getSoundManager().play(
                            SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.3F)
                    );
                }
            }
        }
    }

    @Mixin(CreativeModeInventoryScreen.class)
    public static abstract class SmoothCreativeScrollMixin extends AbstractContainerScreen<CreativeModeInventoryScreen.ItemPickerMenu> {
        @Shadow private float scrollOffs;
        @Shadow private boolean scrolling;

        public SmoothCreativeScrollMixin(CreativeModeInventoryScreen.ItemPickerMenu menu, Inventory playerInventory, Component title) {
            super(menu, playerInventory, title);
        }

        @Unique private static java.lang.reflect.Field linggango_tweaks$tabField2 = null;
        @Unique private static CreativeModeTab linggango_tweaks$getTab2() {
            if (linggango_tweaks$tabField2 == null) {
                for (java.lang.reflect.Field f : CreativeModeInventoryScreen.class.getDeclaredFields()) {
                    if (f.getType() == CreativeModeTab.class && java.lang.reflect.Modifier.isStatic(f.getModifiers())) {
                        f.setAccessible(true);
                        linggango_tweaks$tabField2 = f;
                        break;
                    }
                }
            }
            try { return linggango_tweaks$tabField2 != null ? (CreativeModeTab) linggango_tweaks$tabField2.get(null) : null; }
            catch (Exception e) { return null; }
        }

        @Unique private static sun.misc.Unsafe linggango_tweaks$unsafe;
        @Unique private static long linggango_tweaks$slotYOffset = -1;

        @Unique private static void linggango_tweaks$setSlotY(Slot slot, int value) {
            try {
                if (linggango_tweaks$unsafe == null) {
                    java.lang.reflect.Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
                    unsafeField.setAccessible(true);
                    linggango_tweaks$unsafe = (sun.misc.Unsafe) unsafeField.get(null);

                    for (java.lang.reflect.Field f : Slot.class.getDeclaredFields()) {
                        if (f.getType() == int.class && (f.getName().equals("y") || f.getName().equals("f_40221_") || f.getName().equals("field_75221_f"))) {
                            linggango_tweaks$slotYOffset = linggango_tweaks$unsafe.objectFieldOffset(f);
                            break;
                        }
                    }
                }
                if (linggango_tweaks$slotYOffset != -1) {
                    linggango_tweaks$unsafe.putInt(slot, linggango_tweaks$slotYOffset, value);
                }
            } catch (Exception ignored) {}
        }

        @Unique private float linggango_tweaks$targetScroll = 0.0F;
        @Unique private float linggango_tweaks$currentScroll = 0.0F;
        @Unique private long linggango_tweaks$lastScrollTime = 0L;
        @Unique private double linggango_tweaks$scrollAccumulator = 0.0;
        @Unique private boolean linggango_tweaks$wasScrolling = false;

        @Unique private int linggango_tweaks$lastPixelOffset = 0;
        @Unique private int linggango_tweaks$lastBaseRow = 0;
        @Unique private int linggango_tweaks$lastItemCount = -1;
        @Unique private CreativeModeTab linggango_tweaks$lastTab = null;

        @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
        private void linggango_tweaks$onMouseScrolled(double mouseX, double mouseY, double scrollDelta, CallbackInfoReturnable<Boolean> cir) {
            CreativeModeTab tab = linggango_tweaks$getTab2();
            if (tab == null || !tab.canScroll() || tab.getType() == CreativeModeTab.Type.INVENTORY) return;

            int totalRows = (this.getMenu().items.size() + 9 - 1) / 9 - 5;
            if (totalRows <= 0) return;

            linggango_tweaks$scrollAccumulator += scrollDelta;
            if (Math.abs(linggango_tweaks$scrollAccumulator) >= 1.0) {
                int direction = (int) Math.signum(linggango_tweaks$scrollAccumulator);
                int currentRow = Math.round(linggango_tweaks$targetScroll * totalRows);
                int nextRow = Mth.clamp(currentRow - direction, 0, totalRows);
                linggango_tweaks$targetScroll = (float) nextRow / totalRows;
                linggango_tweaks$scrollAccumulator = 0.0;
            }

            cir.setReturnValue(true);
        }

        @Inject(method = "render", at = @At("HEAD"))
        private void linggango_tweaks$onRenderUpdateScroll(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
            CreativeModeTab tab = linggango_tweaks$getTab2();
            int currentItemCount = this.getMenu().items.size();
            if (tab != linggango_tweaks$lastTab || currentItemCount != linggango_tweaks$lastItemCount) {
                linggango_tweaks$lastTab = tab;
                linggango_tweaks$lastItemCount = currentItemCount;
                linggango_tweaks$targetScroll = this.scrollOffs;
                linggango_tweaks$currentScroll = this.scrollOffs;
            }

            if (tab == null || !tab.canScroll() || tab.getType() == CreativeModeTab.Type.INVENTORY) {
                linggango_tweaks$targetScroll = this.scrollOffs;
                linggango_tweaks$currentScroll = this.scrollOffs;
                this.getMenu().scrollTo(this.scrollOffs);
                return;
            }

            int totalRows = (this.getMenu().items.size() + 9 - 1) / 9 - 5;

            if (this.scrolling) {
                linggango_tweaks$targetScroll = this.scrollOffs;
                linggango_tweaks$wasScrolling = true;
            } else if (linggango_tweaks$wasScrolling) {
                if (totalRows > 0) {
                    int nearestRow = Math.round(linggango_tweaks$targetScroll * totalRows);
                    linggango_tweaks$targetScroll = (float) nearestRow / totalRows;
                }
                linggango_tweaks$wasScrolling = false;
            }

            long time = Util.getMillis();
            if (linggango_tweaks$lastScrollTime == 0L) linggango_tweaks$lastScrollTime = time;
            float dt = (time - linggango_tweaks$lastScrollTime) / 1000.0F;
            linggango_tweaks$lastScrollTime = time;
            if (dt > 0.1F) dt = 0.1F;
            float snapThreshold = totalRows > 0 ? 0.05f / totalRows : 0.001f;
            if (!this.scrolling && totalRows > 0 && Math.abs(linggango_tweaks$targetScroll - linggango_tweaks$currentScroll) < snapThreshold) {
                linggango_tweaks$currentScroll = linggango_tweaks$targetScroll;
            } else {
                linggango_tweaks$currentScroll = Mth.lerp(1.0F - (float) Math.exp(-28.0F * dt), linggango_tweaks$currentScroll, linggango_tweaks$targetScroll);
            }

            if (Math.abs(linggango_tweaks$currentScroll - this.scrollOffs) > 0.0001F) {
                this.scrollOffs = linggango_tweaks$currentScroll;
            }

            if (totalRows > 0) {
                float exactRow = linggango_tweaks$currentScroll * totalRows;
                int baseRow = (int) Math.floor(exactRow);
                if (baseRow > totalRows) baseRow = totalRows;

                float fraction = exactRow - baseRow;
                int pixelOffset = Math.round(-fraction * 18.0F);

                linggango_tweaks$lastPixelOffset = pixelOffset;
                linggango_tweaks$lastBaseRow = baseRow;

                float snappedScroll = (float) baseRow / totalRows;
                this.getMenu().scrollTo(snappedScroll);

                for (int i = 0; i < 45; i++) {
                    if (i < this.getMenu().slots.size()) {
                        Slot slot = this.getMenu().slots.get(i);
                        int rowInGrid = i / 9;
                        int idealY = 18 + rowInGrid * 18 + pixelOffset;
                        linggango_tweaks$setSlotY(slot, idealY);
                    }
                }
            } else {
                linggango_tweaks$lastPixelOffset = 0;
                this.getMenu().scrollTo(0.0f);
            }
        }

        @Inject(method = "renderBg", at = @At("TAIL"))
        private void linggango_tweaks$renderScrollingSlotBackground(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY, CallbackInfo ci) {
            CreativeModeTab tab = linggango_tweaks$getTab2();
            if (tab == null || !tab.canScroll() || tab.getType() == CreativeModeTab.Type.INVENTORY) return;

            int totalRows = (this.getMenu().items.size() + 9 - 1) / 9 - 5;
            if (totalRows <= 0) return;

            int startX = this.leftPos + 9;
            int startY = this.topPos + 18;

            guiGraphics.enableScissor(startX, startY, startX + 162, startY + 90);

            ResourceLocation texture = tab.getBackgroundLocation();
            for (int row = 0; row < 6; row++) {
                for (int col = 0; col < 9; col++) {
                    int drawX = startX + col * 18;
                    int drawY = startY + row * 18 + linggango_tweaks$lastPixelOffset;
                    guiGraphics.blit(texture, drawX, drawY, 9, 18, 18, 18);
                }
            }

            guiGraphics.disableScissor();
        }

        @Inject(
                method = "render",
                at = @At(
                        value = "INVOKE",
                        target = "Lnet/minecraft/client/gui/screens/inventory/CreativeModeInventoryScreen;renderTooltip(Lnet/minecraft/client/gui/GuiGraphics;II)V"
                )
        )
        private void linggango_tweaks$renderExtraRow(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
            if (linggango_tweaks$lastPixelOffset >= 0) return;

            CreativeModeTab tab = linggango_tweaks$getTab2();
            if (tab == null || !tab.canScroll() || tab.getType() == CreativeModeTab.Type.INVENTORY) return;
            int startIndex = linggango_tweaks$lastBaseRow * 9 + 45;
            if (startIndex >= this.getMenu().items.size()) return;

            guiGraphics.enableScissor(this.leftPos + 9, this.topPos + 18, this.leftPos + 171, this.topPos + 108);

            for (int col = 0; col < 9; col++) {
                int itemIndex = startIndex + col;
                if (itemIndex < this.getMenu().items.size()) {
                    ItemStack stack = this.getMenu().items.get(itemIndex);
                    if (!stack.isEmpty()) {
                        int x = this.leftPos + 9 + col * 18;
                        int y = this.topPos + 18 + 5 * 18 + linggango_tweaks$lastPixelOffset;
                        guiGraphics.renderItem(stack, x, y);
                        guiGraphics.renderItemDecorations(Minecraft.getInstance().font, stack, x, y);
                    }
                }
            }

            guiGraphics.disableScissor();
        }
    }

    @Mixin(AbstractContainerScreen.class)
    public static abstract class SafeScissorMixin {
        @Shadow protected int leftPos;
        @Shadow protected int topPos;

        @Unique private static java.lang.reflect.Field linggango_tweaks$tabField3 = null;
        @Unique private static CreativeModeTab linggango_tweaks$getTab3() {
            if (linggango_tweaks$tabField3 == null) {
                for (java.lang.reflect.Field f : CreativeModeInventoryScreen.class.getDeclaredFields()) {
                    if (f.getType() == CreativeModeTab.class && java.lang.reflect.Modifier.isStatic(f.getModifiers())) {
                        f.setAccessible(true);
                        linggango_tweaks$tabField3 = f;
                        break;
                    }
                }
            }
            try { return linggango_tweaks$tabField3 != null ? (CreativeModeTab) linggango_tweaks$tabField3.get(null) : null; }
            catch (Exception e) { return null; }
        }

        @Unique private boolean linggango_tweaks$scissorActive = false;

        @Inject(method = "renderSlot", at = @At("HEAD"))
        private void linggango_tweaks$preRenderSlot(GuiGraphics guiGraphics, Slot slot, CallbackInfo ci) {
            if (((AbstractContainerScreen<?>) (Object) this) instanceof CreativeModeInventoryScreen) {
                CreativeModeTab tab = linggango_tweaks$getTab3();
                if (tab != null && tab.canScroll() && tab.getType() != CreativeModeTab.Type.INVENTORY) {
                    boolean isGrid = slot.index < 45;

                    if (isGrid && !linggango_tweaks$scissorActive) {
                        guiGraphics.enableScissor(this.leftPos + 9, this.topPos + 18, this.leftPos + 171, this.topPos + 108);
                        linggango_tweaks$scissorActive = true;
                    } else if (!isGrid && linggango_tweaks$scissorActive) {
                        guiGraphics.disableScissor();
                        linggango_tweaks$scissorActive = false;
                    }
                }
            }
        }

        @Inject(method = "render", at = @At("RETURN"))
        private void linggango_tweaks$postRenderCleanup(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
            if (linggango_tweaks$scissorActive) {
                guiGraphics.disableScissor();
                linggango_tweaks$scissorActive = false;
            }
        }
    }

    @Mixin(Screen.class)
    public static abstract class SmoothScreenTransitionMixin {
        @Unique private float linggango_tweaks$screenFade = 0.0F;
        @Unique private long linggango_tweaks$lastFadeTime = 0L;
        @Unique private boolean linggango_tweaks$initialized = false;

        @Inject(method = "init()V", at = @At("RETURN"))
        private void linggango_tweaks$onScreenInit(CallbackInfo ci) {
            if (!linggango_tweaks$initialized) {
                linggango_tweaks$screenFade = 0.0F;
                linggango_tweaks$lastFadeTime = Util.getMillis();
                linggango_tweaks$initialized = true;
            }
        }

        @Inject(method = "render", at = @At("HEAD"))
        private void linggango_tweaks$fadeInScreen(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
            long time = Util.getMillis();
            if (linggango_tweaks$lastFadeTime == 0L) linggango_tweaks$lastFadeTime = time;
            float dt = (time - linggango_tweaks$lastFadeTime) / 1000.0F;
            linggango_tweaks$lastFadeTime = time;
            if (dt > 0.1F) dt = 0.1F;

            linggango_tweaks$screenFade = Mth.lerp(1.0F - (float) Math.exp(-8.0F * dt), linggango_tweaks$screenFade, 1.0F);

            if (linggango_tweaks$screenFade > 0.995F) {
                linggango_tweaks$screenFade = 1.0F;
            }

            if (linggango_tweaks$screenFade < 1.0F) {
                guiGraphics.pose().pushPose();
                float scale = 0.95F + (linggango_tweaks$screenFade * 0.05F);
                int centerX = guiGraphics.guiWidth() / 2;
                int centerY = guiGraphics.guiHeight() / 2;
                guiGraphics.pose().translate(centerX, centerY, 0);
                guiGraphics.pose().scale(scale, scale, 1.0F);
                guiGraphics.pose().translate(-centerX, -centerY, 0);
            }
        }

        @Inject(method = "render", at = @At("RETURN"))
        private void linggango_tweaks$popFadeTransform(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
            if (linggango_tweaks$screenFade < 1.0F) {
                guiGraphics.pose().popPose();
            }
        }
    }

    @Mixin(AbstractContainerScreen.class)
    public static abstract class SmoothSlotHighlightMixin {
        @Shadow protected int leftPos;
        @Shadow protected int topPos;
        @Unique private Slot linggango_tweaks$hoveredSlot = null;
        @Unique private float linggango_tweaks$highlightAlpha = 0.0F;
        @Unique private long linggango_tweaks$lastHighlightTime = 0L;

        @Unique private static java.lang.reflect.Field linggango_tweaks$tabField4 = null;
        @Unique private static CreativeModeTab linggango_tweaks$getTab4() {
            if (linggango_tweaks$tabField4 == null) {
                for (java.lang.reflect.Field f : CreativeModeInventoryScreen.class.getDeclaredFields()) {
                    if (f.getType() == CreativeModeTab.class && java.lang.reflect.Modifier.isStatic(f.getModifiers())) {
                        f.setAccessible(true);
                        linggango_tweaks$tabField4 = f;
                        break;
                    }
                }
            }
            try { return linggango_tweaks$tabField4 != null ? (CreativeModeTab) linggango_tweaks$tabField4.get(null) : null; }
            catch (Exception e) { return null; }
        }

        @Inject(method = "render", at = @At("HEAD"))
        private void linggango_tweaks$updateHighlight(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
            AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
            Slot currentSlot = null;

            for (Slot slot : screen.getMenu().slots) {
                int x = this.leftPos + slot.x;
                int y = this.topPos + slot.y;
                if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                    currentSlot = slot;
                    break;
                }
            }

            if (currentSlot != linggango_tweaks$hoveredSlot) {
                linggango_tweaks$hoveredSlot = currentSlot;
                linggango_tweaks$highlightAlpha = 0.0F;
            }

            long time = Util.getMillis();
            if (linggango_tweaks$lastHighlightTime == 0L) linggango_tweaks$lastHighlightTime = time;
            float dt = (time - linggango_tweaks$lastHighlightTime) / 1000.0F;
            linggango_tweaks$lastHighlightTime = time;
            if (dt > 0.1F) dt = 0.1F;

            float target = linggango_tweaks$hoveredSlot != null ? 1.0F : 0.0F;
            linggango_tweaks$highlightAlpha = Mth.lerp(1.0F - (float) Math.exp(-15.0F * dt), linggango_tweaks$highlightAlpha, target);
        }

        @Inject(method = "render", at = @At("TAIL"))
        private void linggango_tweaks$renderSmoothHighlight(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
            if (linggango_tweaks$hoveredSlot != null && linggango_tweaks$highlightAlpha > 0.01F) {
                int x = this.leftPos + linggango_tweaks$hoveredSlot.x;
                int y = this.topPos + linggango_tweaks$hoveredSlot.y;

                int alpha = (int) (linggango_tweaks$highlightAlpha * 80.0F);
                int color = (alpha << 24) | 0xFFFFFF;

                boolean isCreative = ((AbstractContainerScreen<?>) (Object) this) instanceof CreativeModeInventoryScreen;
                boolean isGrid = linggango_tweaks$hoveredSlot.index < 45;

                if (isCreative && isGrid) guiGraphics.enableScissor(this.leftPos + 9, this.topPos + 18, this.leftPos + 171, this.topPos + 108);
                guiGraphics.fill(x, y, x + 16, y + 16, color);
                if (isCreative && isGrid) guiGraphics.disableScissor();
            }
        }

        @Inject(method = "isHovering(Lnet/minecraft/world/inventory/Slot;DD)Z", at = @At("HEAD"), cancellable = true)
        private void linggango_tweaks$restrictHoverBounds(Slot slot, double mx, double my, CallbackInfoReturnable<Boolean> cir) {
            if (((AbstractContainerScreen<?>) (Object) this) instanceof CreativeModeInventoryScreen) {
                CreativeModeTab selectedTab = linggango_tweaks$getTab4();
                if (selectedTab != null && selectedTab.getType() != CreativeModeTab.Type.INVENTORY && slot.index < 45) {
                    int slotScreenY = this.topPos + slot.y;
                    if (slotScreenY + 16 < this.topPos + 18 || slotScreenY > this.topPos + 108) {
                        cir.setReturnValue(false);
                    }
                }
            }
        }
    }
}