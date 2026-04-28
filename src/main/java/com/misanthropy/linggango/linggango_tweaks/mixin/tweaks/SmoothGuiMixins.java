package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks;

import com.misanthropy.linggango.linggango_tweaks.client.gui.SmoothGuiSupport;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.IdentityHashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class SmoothGuiMixins {
    private static final float LINGGANGO_TWEAKS_MAX_DT = 0.1F;
    private static final float LINGGANGO_TWEAKS_EPSILON = 0.0001F;
    private static final int LINGGANGO_TWEAKS_CREATIVE_COLUMNS = 9;
    private static final int LINGGANGO_TWEAKS_VISIBLE_ROWS = 5;
    private static final int LINGGANGO_TWEAKS_VISIBLE_SLOT_COUNT = LINGGANGO_TWEAKS_CREATIVE_COLUMNS * LINGGANGO_TWEAKS_VISIBLE_ROWS;
    private static final int LINGGANGO_TWEAKS_SLOT_STRIDE = 18;
    private static final int LINGGANGO_TWEAKS_SLOT_RENDER_SIZE = 16;
    private static final int LINGGANGO_TWEAKS_GRID_LEFT = 9;
    private static final int LINGGANGO_TWEAKS_GRID_TOP = 18;
    private static final int LINGGANGO_TWEAKS_GRID_WIDTH = 162;
    private static final int LINGGANGO_TWEAKS_GRID_HEIGHT = 90;
    private static final int LINGGANGO_TWEAKS_GRID_RIGHT = LINGGANGO_TWEAKS_GRID_LEFT + LINGGANGO_TWEAKS_GRID_WIDTH;
    private static final int LINGGANGO_TWEAKS_GRID_BOTTOM = LINGGANGO_TWEAKS_GRID_TOP + LINGGANGO_TWEAKS_GRID_HEIGHT;
    private static final int LINGGANGO_TWEAKS_BACKGROUND_ROWS = LINGGANGO_TWEAKS_VISIBLE_ROWS + 1;

    @Mixin(AbstractWidget.class)
    public static abstract class AnimatedButtonMixin {
        @Unique private float linggango_tweaks$currentScale = 1.0F;
        @Unique private float linggango_tweaks$currentLift = 0.0F;
        @Unique private long linggango_tweaks$lastTime = 0L;
        @Unique private boolean linggango_tweaks$transformPushed = false;

        @Inject(method = "render", at = @At("HEAD"))
        private void linggango_tweaks$pushHoverScale(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
            linggango_tweaks$transformPushed = false;

            AbstractWidget widget = (AbstractWidget) (Object) this;
            if (!widget.visible || !(widget instanceof Button)) {
                return;
            }

            long time = Util.getMillis();
            float dt = SmoothGuiSupport.linggango_tweaks$getDeltaSeconds(time, linggango_tweaks$lastTime);
            linggango_tweaks$lastTime = time;

            boolean hoveredOrFocused = widget.isHoveredOrFocused();
            float targetScale = hoveredOrFocused ? 1.05F : 1.0F;
            float targetLift = hoveredOrFocused ? -0.75F : 0.0F;
            linggango_tweaks$currentScale = SmoothGuiSupport.linggango_tweaks$expLerp(18.0F, linggango_tweaks$currentScale, targetScale, dt);
            linggango_tweaks$currentLift = SmoothGuiSupport.linggango_tweaks$expLerp(16.0F, linggango_tweaks$currentLift, targetLift, dt);

            if (!SmoothGuiSupport.linggango_tweaks$hasTransform(linggango_tweaks$currentScale, linggango_tweaks$currentLift)) {
                return;
            }

            float centerX = widget.getX() + widget.getWidth() / 2.0F;
            float centerY = widget.getY() + widget.getHeight() / 2.0F + linggango_tweaks$currentLift;

            SmoothGuiSupport.linggango_tweaks$pushCenteredScale(guiGraphics, centerX, centerY, linggango_tweaks$currentScale);
            linggango_tweaks$transformPushed = true;
        }

        @Inject(method = "render", at = @At("RETURN"))
        private void linggango_tweaks$popHoverScale(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
            if (linggango_tweaks$transformPushed) {
                guiGraphics.pose().popPose();
                linggango_tweaks$transformPushed = false;
            }
        }
    }

    @Mixin(CreativeModeInventoryScreen.class)
    public static abstract class AnimatedCreativeTabsMixin {
        @Unique private final Map<CreativeModeTab, Float> linggango_tweaks$tabScales = new IdentityHashMap<>();
        @Unique private final Map<CreativeModeTab, Float> linggango_tweaks$tabLifts = new IdentityHashMap<>();
        @Unique private long linggango_tweaks$lastTabTime = 0L;
        @Unique private float linggango_tweaks$dt = 0.0F;
        @Unique private int linggango_tweaks$mouseX = 0;
        @Unique private int linggango_tweaks$mouseY = 0;
        @Unique private @Nullable CreativeModeTab linggango_tweaks$currentTab = null;
        @Unique private boolean linggango_tweaks$matrixPushed = false;
        @Unique private static long linggango_tweaks$lastClickSoundTime = 0L;
        @Unique private static @Nullable CreativeModeTab linggango_tweaks$lastClickedTab = null;

        @Inject(method = "render", at = @At("HEAD"))
        private void linggango_tweaks$captureMouse(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
            linggango_tweaks$mouseX = mouseX;
            linggango_tweaks$mouseY = mouseY;

            long time = Util.getMillis();
            linggango_tweaks$dt = SmoothGuiSupport.linggango_tweaks$getDeltaSeconds(time, linggango_tweaks$lastTabTime);
            linggango_tweaks$lastTabTime = time;
        }

        @Inject(method = "renderTabButton", at = @At("HEAD"))
        private void linggango_tweaks$startTabRender(GuiGraphics guiGraphics, CreativeModeTab tab, CallbackInfo ci) {
            linggango_tweaks$currentTab = tab;
            linggango_tweaks$matrixPushed = false;
        }

        @Redirect(method = "renderTabButton", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"))
        private void linggango_tweaks$wrapTabWithScale(@NonNull GuiGraphics guiGraphics, @NonNull ResourceLocation texture, int x, int y, int uOffset, int vOffset, int uWidth, int vHeight) {
            if (!linggango_tweaks$matrixPushed && linggango_tweaks$currentTab != null) {
                CreativeModeTab selectedTab = SmoothGuiSupport.linggango_tweaks$getSelectedCreativeTab();
                boolean isSelected = linggango_tweaks$currentTab == selectedTab;
                boolean isHovered = linggango_tweaks$mouseX >= x
                        && linggango_tweaks$mouseX <= x + uWidth
                        && linggango_tweaks$mouseY >= y
                        && linggango_tweaks$mouseY <= y + vHeight;

                float targetScale = isSelected ? 1.11F : (isHovered ? 1.06F : 1.0F);
                float targetLift = isSelected ? -1.0F : (isHovered ? -0.5F : 0.0F);

                float currentScale = linggango_tweaks$tabScales.getOrDefault(linggango_tweaks$currentTab, 1.0F);
                float currentLift = linggango_tweaks$tabLifts.getOrDefault(linggango_tweaks$currentTab, 0.0F);
                currentScale = SmoothGuiSupport.linggango_tweaks$expLerp(18.0F, currentScale, targetScale, linggango_tweaks$dt);
                currentLift = SmoothGuiSupport.linggango_tweaks$expLerp(16.0F, currentLift, targetLift, linggango_tweaks$dt);
                linggango_tweaks$tabScales.put(linggango_tweaks$currentTab, currentScale);
                linggango_tweaks$tabLifts.put(linggango_tweaks$currentTab, currentLift);

                if (SmoothGuiSupport.linggango_tweaks$hasTransform(currentScale, currentLift)) {
                    float centerX = x + uWidth / 2.0F;
                    float centerY = y + vHeight / 2.0F + currentLift;
                    SmoothGuiSupport.linggango_tweaks$pushCenteredScale(guiGraphics, centerX, centerY, currentScale);
                    linggango_tweaks$matrixPushed = true;
                }
            }

            guiGraphics.blit(texture, x, y, uOffset, vOffset, uWidth, vHeight);
        }

        @Inject(method = "renderTabButton", at = @At("RETURN"))
        private void linggango_tweaks$endTabRender(@NonNull GuiGraphics guiGraphics, CreativeModeTab tab, CallbackInfo ci) {
            if (linggango_tweaks$matrixPushed) {
                guiGraphics.pose().popPose();
                linggango_tweaks$matrixPushed = false;
            }

            linggango_tweaks$currentTab = null;
        }

        @Inject(method = "selectTab", at = @At("HEAD"))
        private void linggango_tweaks$onTabClickedSound(CreativeModeTab tab, CallbackInfo ci) {
            CreativeModeTab selectedTab = SmoothGuiSupport.linggango_tweaks$getSelectedCreativeTab();
            if (tab == selectedTab) {
                return;
            }

            long time = Util.getMillis();
            if (time - linggango_tweaks$lastClickSoundTime > 250L || linggango_tweaks$lastClickedTab != tab) {
                linggango_tweaks$lastClickSoundTime = time;
                linggango_tweaks$lastClickedTab = tab;
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.3F));
            }
        }
    }

    @Mixin(CreativeModeInventoryScreen.class)
    public static abstract class SmoothCreativeScrollMixin extends AbstractContainerScreen<CreativeModeInventoryScreen.ItemPickerMenu> {
        @Shadow private float scrollOffs;
        @Shadow private boolean scrolling;

        public SmoothCreativeScrollMixin(CreativeModeInventoryScreen.@NonNull ItemPickerMenu menu, @NonNull Inventory playerInventory, @NonNull Component title) {
            super(menu, playerInventory, title);
        }

        @Unique private float linggango_tweaks$targetScroll = 0.0F;
        @Unique private float linggango_tweaks$currentScroll = 0.0F;
        @Unique private long linggango_tweaks$lastScrollTime = 0L;
        @Unique private double linggango_tweaks$scrollAccumulator = 0.0D;
        @Unique private boolean linggango_tweaks$wasScrolling = false;
        @Unique private int linggango_tweaks$lastPixelOffset = 0;
        @Unique private int linggango_tweaks$lastBaseRow = 0;
        @Unique private int linggango_tweaks$lastAppliedBaseRow = -1;
        @Unique private int linggango_tweaks$lastAppliedPixelOffset = Integer.MIN_VALUE;
        @Unique private int linggango_tweaks$lastItemCount = -1;
        @Unique private @Nullable CreativeModeTab linggango_tweaks$lastTab = null;

        @Unique
        private void linggango_tweaks$restoreGridSlotPositions() {
            if (linggango_tweaks$lastAppliedPixelOffset == 0 || linggango_tweaks$lastAppliedPixelOffset == Integer.MIN_VALUE) {
                return;
            }

            for (int i = 0; i < LINGGANGO_TWEAKS_VISIBLE_SLOT_COUNT && i < this.getMenu().slots.size(); i++) {
                SmoothGuiSupport.linggango_tweaks$setSlotY(this.getMenu().slots.get(i), SmoothGuiSupport.linggango_tweaks$getBaseGridSlotY(i, LINGGANGO_TWEAKS_GRID_TOP, LINGGANGO_TWEAKS_CREATIVE_COLUMNS, LINGGANGO_TWEAKS_SLOT_STRIDE));
            }

            linggango_tweaks$lastAppliedPixelOffset = 0;
        }

        @Unique
        private void linggango_tweaks$clearScrollAnimationState(float scroll) {
            linggango_tweaks$targetScroll = scroll;
            linggango_tweaks$currentScroll = scroll;
            linggango_tweaks$scrollAccumulator = 0.0D;
            linggango_tweaks$wasScrolling = false;
            linggango_tweaks$lastPixelOffset = 0;
            linggango_tweaks$lastBaseRow = 0;
            linggango_tweaks$lastAppliedBaseRow = -1;
            linggango_tweaks$lastScrollTime = 0L;
            linggango_tweaks$lastAppliedPixelOffset = 0;
        }

        @Unique
        private void linggango_tweaks$applyGridSlotPositions(int pixelOffset) {
            if (pixelOffset == linggango_tweaks$lastAppliedPixelOffset) {
                return;
            }

            for (int i = 0; i < LINGGANGO_TWEAKS_VISIBLE_SLOT_COUNT && i < this.getMenu().slots.size(); i++) {
                SmoothGuiSupport.linggango_tweaks$setSlotY(this.getMenu().slots.get(i), SmoothGuiSupport.linggango_tweaks$getBaseGridSlotY(i, LINGGANGO_TWEAKS_GRID_TOP, LINGGANGO_TWEAKS_CREATIVE_COLUMNS, LINGGANGO_TWEAKS_SLOT_STRIDE) + pixelOffset);
            }

            linggango_tweaks$lastAppliedPixelOffset = pixelOffset;
        }

        @Unique
        private void linggango_tweaks$resetScrollState(float scroll) {
            linggango_tweaks$targetScroll = scroll;
            linggango_tweaks$currentScroll = scroll;
            linggango_tweaks$scrollAccumulator = 0.0D;
            linggango_tweaks$wasScrolling = false;
            linggango_tweaks$lastPixelOffset = 0;
            linggango_tweaks$lastBaseRow = 0;
            linggango_tweaks$lastAppliedBaseRow = -1;
            linggango_tweaks$lastScrollTime = 0L;
        }

        @Inject(method = "selectTab", at = @At("HEAD"))
        private void linggango_tweaks$restoreBeforeTabChange(CreativeModeTab tab, CallbackInfo ci) {
            linggango_tweaks$restoreGridSlotPositions();
            linggango_tweaks$clearScrollAnimationState(this.scrollOffs);
        }

        @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
        private void linggango_tweaks$onMouseScrolled(double mouseX, double mouseY, double scrollDelta, @NonNull CallbackInfoReturnable<Boolean> cir) {
            CreativeModeTab selectedTab = SmoothGuiSupport.linggango_tweaks$getSelectedCreativeTab();
            if (!SmoothGuiSupport.linggango_tweaks$shouldSmoothCreativeGrid(selectedTab)) {
                return;
            }

            int totalRows = SmoothGuiSupport.linggango_tweaks$getTotalScrollableRows(this.getMenu().items.size(), LINGGANGO_TWEAKS_CREATIVE_COLUMNS, LINGGANGO_TWEAKS_VISIBLE_ROWS);
            if (totalRows <= 0) {
                return;
            }

            linggango_tweaks$scrollAccumulator += scrollDelta;
            int wholeSteps = linggango_tweaks$scrollAccumulator > 0.0D
                    ? (int) Math.floor(linggango_tweaks$scrollAccumulator)
                    : (int) Math.ceil(linggango_tweaks$scrollAccumulator);

            if (wholeSteps != 0) {
                int currentRow = Math.round(linggango_tweaks$targetScroll * totalRows);
                int nextRow = Mth.clamp(currentRow - wholeSteps, 0, totalRows);
                linggango_tweaks$targetScroll = (float) nextRow / (float) totalRows;
                linggango_tweaks$scrollAccumulator -= wholeSteps;
            }

            cir.setReturnValue(true);
        }

        @Inject(method = "render", at = @At("HEAD"))
        private void linggango_tweaks$onRenderUpdateScroll(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
            CreativeModeTab selectedTab = SmoothGuiSupport.linggango_tweaks$getSelectedCreativeTab();
            int currentItemCount = this.getMenu().items.size();

            if (selectedTab != linggango_tweaks$lastTab || currentItemCount != linggango_tweaks$lastItemCount) {
                linggango_tweaks$lastTab = selectedTab;
                linggango_tweaks$lastItemCount = currentItemCount;
                linggango_tweaks$resetScrollState(this.scrollOffs);
            }

            if (!SmoothGuiSupport.linggango_tweaks$shouldSmoothCreativeGrid(selectedTab)) {
                linggango_tweaks$clearScrollAnimationState(this.scrollOffs);
                this.getMenu().scrollTo(this.scrollOffs);
                return;
            }

            int totalRows = SmoothGuiSupport.linggango_tweaks$getTotalScrollableRows(currentItemCount, LINGGANGO_TWEAKS_CREATIVE_COLUMNS, LINGGANGO_TWEAKS_VISIBLE_ROWS);
            if (totalRows <= 0) {
                linggango_tweaks$resetScrollState(0.0F);
                this.scrollOffs = 0.0F;
                this.getMenu().scrollTo(0.0F);
                linggango_tweaks$restoreGridSlotPositions();
                return;
            }

            if (this.scrolling) {
                linggango_tweaks$targetScroll = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
                linggango_tweaks$currentScroll = linggango_tweaks$targetScroll;
                linggango_tweaks$wasScrolling = true;
            } else {
                if (linggango_tweaks$wasScrolling) {
                    int nearestRow = Math.round(linggango_tweaks$targetScroll * totalRows);
                    linggango_tweaks$targetScroll = (float) nearestRow / (float) totalRows;
                    linggango_tweaks$wasScrolling = false;
                }

                long time = Util.getMillis();
                float dt = SmoothGuiSupport.linggango_tweaks$getDeltaSeconds(time, linggango_tweaks$lastScrollTime);
                linggango_tweaks$lastScrollTime = time;

                float snapThreshold = 0.05F / (float) totalRows;
                if (Math.abs(linggango_tweaks$targetScroll - linggango_tweaks$currentScroll) < snapThreshold) {
                    linggango_tweaks$currentScroll = linggango_tweaks$targetScroll;
                } else {
                    linggango_tweaks$currentScroll = SmoothGuiSupport.linggango_tweaks$expLerp(28.0F, linggango_tweaks$currentScroll, linggango_tweaks$targetScroll, dt);
                }
            }

            linggango_tweaks$currentScroll = Mth.clamp(linggango_tweaks$currentScroll, 0.0F, 1.0F);
            if (Math.abs(linggango_tweaks$currentScroll - this.scrollOffs) > LINGGANGO_TWEAKS_EPSILON) {
                this.scrollOffs = linggango_tweaks$currentScroll;
            }

            float exactRow = linggango_tweaks$currentScroll * totalRows;
            int baseRow = Mth.clamp((int) Math.floor(exactRow), 0, totalRows);
            float fraction = exactRow - baseRow;
            int pixelOffset = Math.round(-fraction * LINGGANGO_TWEAKS_SLOT_STRIDE);

            linggango_tweaks$lastBaseRow = baseRow;
            linggango_tweaks$lastPixelOffset = pixelOffset;

            if (baseRow != linggango_tweaks$lastAppliedBaseRow) {
                this.getMenu().scrollTo((float) baseRow / (float) totalRows);
                linggango_tweaks$lastAppliedBaseRow = baseRow;
            }

            linggango_tweaks$applyGridSlotPositions(pixelOffset);
        }

        @Inject(method = "renderBg", at = @At("TAIL"))
        private void linggango_tweaks$renderScrollingSlotBackground(@NonNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY, CallbackInfo ci) {
            CreativeModeTab selectedTab = SmoothGuiSupport.linggango_tweaks$getSelectedCreativeTab();
            if (!SmoothGuiSupport.linggango_tweaks$shouldSmoothCreativeGrid(selectedTab) || linggango_tweaks$lastPixelOffset == 0) {
                return;
            }

            int totalRows = SmoothGuiSupport.linggango_tweaks$getTotalScrollableRows(this.getMenu().items.size(), LINGGANGO_TWEAKS_CREATIVE_COLUMNS, LINGGANGO_TWEAKS_VISIBLE_ROWS);
            if (totalRows <= 0) {
                return;
            }

            int startX = this.leftPos + LINGGANGO_TWEAKS_GRID_LEFT;
            int startY = this.topPos + LINGGANGO_TWEAKS_GRID_TOP;
            ResourceLocation texture = selectedTab.getBackgroundLocation();

            SmoothGuiSupport.linggango_tweaks$enableCreativeGridScissor(guiGraphics, this.leftPos, this.topPos, LINGGANGO_TWEAKS_GRID_LEFT, LINGGANGO_TWEAKS_GRID_TOP, LINGGANGO_TWEAKS_GRID_RIGHT, LINGGANGO_TWEAKS_GRID_BOTTOM);
            for (int row = 0; row < LINGGANGO_TWEAKS_BACKGROUND_ROWS; row++) {
                for (int col = 0; col < LINGGANGO_TWEAKS_CREATIVE_COLUMNS; col++) {
                    int drawX = startX + col * LINGGANGO_TWEAKS_SLOT_STRIDE;
                    int drawY = startY + row * LINGGANGO_TWEAKS_SLOT_STRIDE + linggango_tweaks$lastPixelOffset;
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
        private void linggango_tweaks$renderExtraRow(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
            if (linggango_tweaks$lastPixelOffset >= 0) {
                return;
            }

            CreativeModeTab selectedTab = SmoothGuiSupport.linggango_tweaks$getSelectedCreativeTab();
            if (!SmoothGuiSupport.linggango_tweaks$shouldSmoothCreativeGrid(selectedTab)) {
                return;
            }

            int startIndex = linggango_tweaks$lastBaseRow * LINGGANGO_TWEAKS_CREATIVE_COLUMNS + LINGGANGO_TWEAKS_VISIBLE_SLOT_COUNT;
            if (startIndex >= this.getMenu().items.size()) {
                return;
            }

            SmoothGuiSupport.linggango_tweaks$enableCreativeGridScissor(guiGraphics, this.leftPos, this.topPos, LINGGANGO_TWEAKS_GRID_LEFT, LINGGANGO_TWEAKS_GRID_TOP, LINGGANGO_TWEAKS_GRID_RIGHT, LINGGANGO_TWEAKS_GRID_BOTTOM);
            for (int col = 0; col < LINGGANGO_TWEAKS_CREATIVE_COLUMNS; col++) {
                int itemIndex = startIndex + col;
                if (itemIndex >= this.getMenu().items.size()) {
                    continue;
                }

                ItemStack stack = this.getMenu().items.get(itemIndex);
                if (stack.isEmpty()) {
                    continue;
                }

                int x = this.leftPos + LINGGANGO_TWEAKS_GRID_LEFT + col * LINGGANGO_TWEAKS_SLOT_STRIDE;
                int y = this.topPos + LINGGANGO_TWEAKS_GRID_TOP + LINGGANGO_TWEAKS_VISIBLE_ROWS * LINGGANGO_TWEAKS_SLOT_STRIDE + linggango_tweaks$lastPixelOffset;
                guiGraphics.renderItem(stack, x, y);
                guiGraphics.renderItemDecorations(Minecraft.getInstance().font, stack, x, y);
            }
            guiGraphics.disableScissor();
        }
    }

    @Mixin(AbstractContainerScreen.class)
    public static abstract class SafeScissorMixin {
        @Shadow protected int leftPos;
        @Shadow protected int topPos;

        @Unique private boolean linggango_tweaks$scissorActive = false;

        @Inject(method = "renderSlot", at = @At("HEAD"))
        private void linggango_tweaks$preRenderSlot(@NonNull GuiGraphics guiGraphics, @NonNull Slot slot, CallbackInfo ci) {
            if (!(((AbstractContainerScreen<?>) (Object) this) instanceof CreativeModeInventoryScreen)) {
                return;
            }

            CreativeModeTab selectedTab = SmoothGuiSupport.linggango_tweaks$getSelectedCreativeTab();
            if (SmoothGuiSupport.linggango_tweaks$shouldSmoothCreativeGrid(selectedTab)) {
                boolean isGridSlot = slot.index < LINGGANGO_TWEAKS_VISIBLE_SLOT_COUNT;
                if (isGridSlot && !linggango_tweaks$scissorActive) {
                    SmoothGuiSupport.linggango_tweaks$enableCreativeGridScissor(guiGraphics, this.leftPos, this.topPos, LINGGANGO_TWEAKS_GRID_LEFT, LINGGANGO_TWEAKS_GRID_TOP, LINGGANGO_TWEAKS_GRID_RIGHT, LINGGANGO_TWEAKS_GRID_BOTTOM);
                    linggango_tweaks$scissorActive = true;
                } else if (!isGridSlot && linggango_tweaks$scissorActive) {
                    guiGraphics.disableScissor();
                    linggango_tweaks$scissorActive = false;
                }
            } else {
                return;
            }
        }

        @Inject(method = "render", at = @At("RETURN"))
        private void linggango_tweaks$postRenderCleanup(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
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
        @Unique private boolean linggango_tweaks$transformPushed = false;

        @Inject(method = "init()V", at = @At("RETURN"))
        private void linggango_tweaks$onScreenInit(CallbackInfo ci) {
            if (!linggango_tweaks$initialized) {
                linggango_tweaks$screenFade = 0.0F;
                linggango_tweaks$lastFadeTime = Util.getMillis();
                linggango_tweaks$initialized = true;
            }
        }

        @Inject(method = "removed", at = @At("HEAD"))
        private void linggango_tweaks$resetTransitionState(CallbackInfo ci) {
            linggango_tweaks$screenFade = 0.0F;
            linggango_tweaks$lastFadeTime = 0L;
            linggango_tweaks$initialized = false;
            linggango_tweaks$transformPushed = false;
        }

        @Inject(method = "render", at = @At("HEAD"))
        private void linggango_tweaks$fadeInScreen(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
            linggango_tweaks$transformPushed = false;

            long time = Util.getMillis();
            float dt = SmoothGuiSupport.linggango_tweaks$getDeltaSeconds(time, linggango_tweaks$lastFadeTime);
            linggango_tweaks$lastFadeTime = time;

            linggango_tweaks$screenFade = SmoothGuiSupport.linggango_tweaks$expLerp(8.0F, linggango_tweaks$screenFade, 1.0F, dt);
            if (linggango_tweaks$screenFade > 0.995F) {
                linggango_tweaks$screenFade = 1.0F;
            }

            if (linggango_tweaks$screenFade < 1.0F) {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0.0F, (1.0F - linggango_tweaks$screenFade) * 4.0F, 0.0F);

                float scale = 0.955F + linggango_tweaks$screenFade * 0.045F;
                int centerX = guiGraphics.guiWidth() / 2;
                int centerY = guiGraphics.guiHeight() / 2;
                guiGraphics.pose().translate(centerX, centerY, 0.0F);
                guiGraphics.pose().scale(scale, scale, 1.0F);
                guiGraphics.pose().translate(-centerX, -centerY, 0.0F);
                linggango_tweaks$transformPushed = true;
            }
        }

        @Inject(method = "render", at = @At("RETURN"))
        private void linggango_tweaks$popFadeTransform(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
            if (linggango_tweaks$transformPushed) {
                guiGraphics.pose().popPose();
                linggango_tweaks$transformPushed = false;
            }
        }
    }

    @Mixin(AbstractContainerScreen.class)
    public static abstract class SmoothSlotHighlightMixin {
        @Shadow protected int leftPos;
        @Shadow protected int topPos;
        @Shadow protected @Nullable Slot hoveredSlot;

        @Unique private @Nullable Slot linggango_tweaks$highlightSlot = null;
        @Unique private float linggango_tweaks$highlightAlpha = 0.0F;
        @Unique private long linggango_tweaks$lastHighlightTime = 0L;

        @Inject(method = "render", at = @At("TAIL"))
        private void linggango_tweaks$renderSmoothHighlight(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
            Slot currentHoveredSlot = this.hoveredSlot;
            if (currentHoveredSlot != null && currentHoveredSlot != linggango_tweaks$highlightSlot) {
                linggango_tweaks$highlightSlot = currentHoveredSlot;
                linggango_tweaks$highlightAlpha = 0.0F;
            }

            long time = Util.getMillis();
            float dt = SmoothGuiSupport.linggango_tweaks$getDeltaSeconds(time, linggango_tweaks$lastHighlightTime);
            linggango_tweaks$lastHighlightTime = time;

            float target = currentHoveredSlot != null ? 1.0F : 0.0F;
            linggango_tweaks$highlightAlpha = SmoothGuiSupport.linggango_tweaks$expLerp(15.0F, linggango_tweaks$highlightAlpha, target, dt);

            if (currentHoveredSlot == null && linggango_tweaks$highlightAlpha < 0.01F) {
                linggango_tweaks$highlightSlot = null;
                return;
            }

            if (linggango_tweaks$highlightSlot == null || linggango_tweaks$highlightAlpha <= 0.01F) {
                return;
            }

            int x = this.leftPos + linggango_tweaks$highlightSlot.x;
            int y = this.topPos + linggango_tweaks$highlightSlot.y;
            float pulse = 0.92F + 0.08F * (0.5F + 0.5F * Mth.sin(time * 0.02F));
            int fillAlpha = Mth.clamp((int) (linggango_tweaks$highlightAlpha * 48.0F * pulse), 0, 255);
            int borderAlpha = Mth.clamp((int) (linggango_tweaks$highlightAlpha * 108.0F * pulse), 0, 255);
            int fillColor = fillAlpha << 24 | 0xFFFFFF;
            int borderColor = borderAlpha << 24 | 0xFFFFFF;

            boolean isCreativeGridSlot = ((AbstractContainerScreen<?>) (Object) this) instanceof CreativeModeInventoryScreen
                    && linggango_tweaks$highlightSlot.index < LINGGANGO_TWEAKS_VISIBLE_SLOT_COUNT;

            if (isCreativeGridSlot) {
                SmoothGuiSupport.linggango_tweaks$enableCreativeGridScissor(guiGraphics, this.leftPos, this.topPos, LINGGANGO_TWEAKS_GRID_LEFT, LINGGANGO_TWEAKS_GRID_TOP, LINGGANGO_TWEAKS_GRID_RIGHT, LINGGANGO_TWEAKS_GRID_BOTTOM);
            }

            guiGraphics.fill(x, y, x + LINGGANGO_TWEAKS_SLOT_RENDER_SIZE, y + LINGGANGO_TWEAKS_SLOT_RENDER_SIZE, fillColor);
            guiGraphics.fill(x, y, x + LINGGANGO_TWEAKS_SLOT_RENDER_SIZE, y + 1, borderColor);
            guiGraphics.fill(x, y + LINGGANGO_TWEAKS_SLOT_RENDER_SIZE - 1, x + LINGGANGO_TWEAKS_SLOT_RENDER_SIZE, y + LINGGANGO_TWEAKS_SLOT_RENDER_SIZE, borderColor);
            guiGraphics.fill(x, y, x + 1, y + LINGGANGO_TWEAKS_SLOT_RENDER_SIZE, borderColor);
            guiGraphics.fill(x + LINGGANGO_TWEAKS_SLOT_RENDER_SIZE - 1, y, x + LINGGANGO_TWEAKS_SLOT_RENDER_SIZE, y + LINGGANGO_TWEAKS_SLOT_RENDER_SIZE, borderColor);

            if (isCreativeGridSlot) {
                guiGraphics.disableScissor();
            }
        }

        @Inject(method = "isHovering(Lnet/minecraft/world/inventory/Slot;DD)Z", at = @At("HEAD"), cancellable = true)
        private void linggango_tweaks$restrictHoverBounds(@NonNull Slot slot, double mx, double my, @NonNull CallbackInfoReturnable<Boolean> cir) {
            if (!(((AbstractContainerScreen<?>) (Object) this) instanceof CreativeModeInventoryScreen)) {
                return;
            }

            CreativeModeTab selectedTab = SmoothGuiSupport.linggango_tweaks$getSelectedCreativeTab();
            if (!SmoothGuiSupport.linggango_tweaks$shouldSmoothCreativeGrid(selectedTab) || slot.index >= LINGGANGO_TWEAKS_VISIBLE_SLOT_COUNT) {
                return;
            }

            cir.setReturnValue(slot.isActive() && SmoothGuiSupport.linggango_tweaks$isMouseWithinVisibleSlot(this.leftPos, this.topPos, slot, mx, my, LINGGANGO_TWEAKS_GRID_LEFT, LINGGANGO_TWEAKS_GRID_TOP, LINGGANGO_TWEAKS_GRID_RIGHT, LINGGANGO_TWEAKS_GRID_BOTTOM, LINGGANGO_TWEAKS_SLOT_RENDER_SIZE));
        }
    }
}
