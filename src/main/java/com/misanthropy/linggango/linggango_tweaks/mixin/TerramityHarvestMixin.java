package com.misanthropy.linggango.linggango_tweaks.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(ServerPlayerGameMode.class)
public abstract class TerramityHarvestMixin {

    @Shadow protected ServerLevel level;
    @Final
    @Shadow protected ServerPlayer player;

    @Inject(method = "destroyBlock", at = @At("HEAD"))
    private void manualTerramityDropFix(@NonNull BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BlockState state = this.level.getBlockState(pos);
        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(state.getBlock());

        if (blockId != null && blockId.getNamespace().equals("terramity")) {
            String path = blockId.getPath();
            if (!path.endsWith("_ore")) return;

            ItemStack stack = this.player.getMainHandItem();
            Item item = stack.getItem();
            ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(item);
            boolean isPaxel = itemId != null && itemId.getPath().contains("paxel");

            if (!(item instanceof PickaxeItem) && !isPaxel) return;

            int toolLevel = 0;
            if (isPaxel) {
                toolLevel = 4;
            } else {
                TieredItem tiered = (TieredItem) item;
                Tier tier = tiered.getTier();
                if (tier == Tiers.WOOD || tier == Tiers.GOLD) {
                }
                else if (tier == Tiers.STONE) toolLevel = 1;
                else if (tier == Tiers.IRON) toolLevel = 2;
                else if (tier == Tiers.DIAMOND) toolLevel = 3;
                else if (tier == Tiers.NETHERITE) toolLevel = 4;
                else toolLevel = tier.getSpeed() >= 9.0F ? 4 : (tier.getSpeed() >= 8.0F ? 3 : 2);
            }

            int requiredLevel = switch (path) {
                case "decayed_black_matter_ore", "bedrock_black_matter_ore" -> 4;
                case "daemonium_ore", "deepslate_dimlite_ore", "gaianite_cluster_ore", "deepslate_iridium_ore",
                     "nether_iridium_ore", "end_iridium_ore", "profaned_ore", "cosmic_ore" -> 3;
                case "deepslate_iridescent_ore" -> 2;
                case "igneo_ruby_ore", "nether_ruby_ore", "sapphire_ore", "deepslate_sapphire_ore", "topaz_ore",
                     "deepslate_topaz_ore", "end_onyx_ore" -> 1;
                default -> 3;
            };

            if (toolLevel >= requiredLevel) {
                boolean mcreatorAllows = state.canHarvestBlock(this.level, pos, this.player);

                if (!mcreatorAllows) {
                    BlockEntity blockEntity = this.level.getBlockEntity(pos);

                    LootParams.Builder builder = new LootParams.Builder(this.level)
                            .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                            .withParameter(LootContextParams.TOOL, stack)
                            .withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity)
                            .withOptionalParameter(LootContextParams.THIS_ENTITY, this.player);

                    List<ItemStack> drops = new ArrayList<>(state.getDrops(builder));

                    if (drops.isEmpty()) {
                        Item fallbackItem = switch (path) {
                            case "deepslate_iridium_ore", "nether_iridium_ore", "end_iridium_ore" ->
                                    ForgeRegistries.ITEMS.getValue(new ResourceLocation("terramity:iridium_chunk"));
                            case "daemonium_ore" ->
                                    ForgeRegistries.ITEMS.getValue(new ResourceLocation("terramity:daemonium_chunk"));
                            case "deepslate_dimlite_ore" ->
                                    ForgeRegistries.ITEMS.getValue(new ResourceLocation("terramity:untapped_dimlite"));
                            case "gaianite_cluster_ore" ->
                                    ForgeRegistries.ITEMS.getValue(new ResourceLocation("terramity:gaianite_cluster"));
                            case "cosmic_ore" ->
                                    ForgeRegistries.ITEMS.getValue(new ResourceLocation("terramity:raw_cosmilite"));
                            default -> state.getBlock().asItem();
                        };

                        if (fallbackItem != null && fallbackItem != net.minecraft.world.item.Items.AIR) {
                            int dropCount;
                            double roll = Math.random();
                            if (roll < 0.2) {
                                dropCount = 1;
                            } else if (roll < 0.6) {
                                dropCount = 2;
                            } else {
                                dropCount = 3;
                            }
                            drops.add(new ItemStack(fallbackItem, dropCount));
                        }
                    }

                    for (ItemStack drop : drops) {
                        Block.popResource(this.level, pos, drop);
                    }
                }
            }
        }
    }
}