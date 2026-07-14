//package com.misanthropy.linggango.linggango_tweaks.features;
//
//import net.minecraft.core.BlockPos;
//import net.minecraft.core.Vec3i;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.nbt.ListTag;
//import net.minecraft.nbt.NbtIo;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.server.packs.resources.PreparableReloadListener;
//import net.minecraft.server.packs.resources.Resource;
//import net.minecraft.server.packs.resources.ResourceManager;
//import net.minecraft.util.profiling.ProfilerFiller;
//import net.minecraft.world.level.block.Block;
//import net.minecraft.world.level.block.Blocks;
//import net.minecraft.world.level.block.Rotation;
//import net.minecraft.world.level.block.state.BlockState;
//import net.minecraft.world.level.block.state.properties.Property;
//import net.minecraftforge.registries.ForgeRegistries;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.jspecify.annotations.NonNull;
//
//import java.io.InputStream;
//import java.util.*;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.Executor;
//
//@SuppressWarnings("unused")
//public class DataManagerForTrees implements PreparableReloadListener {
//    private static final Logger LOGGER = LogManager.getLogger();
//    private static final Map<ResourceLocation, TreeTemplate> TREE_CACHE = new ConcurrentHashMap<>();
//    private static boolean isPreloaded = false;
//    public record BlockInfo(BlockPos pos, BlockState state, Block block) {}
//    public record AnchorData(List<BlockPos> redWoolPositions, BlockPos whiteWoolPos, Map<Rotation, BlockPos> centerOffsets) {}
//    public record TreeTemplate(List<BlockInfo> blocks, AnchorData anchors, Vec3i size) {}
//
//    @Override
//    public @NonNull CompletableFuture<Void> reload(PreparationBarrier stage, @NonNull ResourceManager resourceManager, @NonNull ProfilerFiller preparationsProfiler, @NonNull ProfilerFiller reloadProfiler, @NonNull Executor backgroundExecutor, @NonNull Executor gameExecutor) {
//        return CompletableFuture.runAsync(() -> {
//            if (!isPreloaded) TREE_CACHE.clear();
//        }, backgroundExecutor).thenCompose(stage::wait).thenRunAsync(() -> {
//            if (!isPreloaded) preloadAllTrees(resourceManager);
//        }, gameExecutor);
//    }
//
//    public static void preloadAllTrees(ResourceManager resourceManager) {
//        if (!isPreloaded) {
//            try {
//                Map<ResourceLocation, Resource> structureResources = resourceManager.listResources("structures",
//                        (location) -> location.getNamespace().equals("linggango_tweaks"));
//
//                for (Map.Entry<ResourceLocation, Resource> entry : structureResources.entrySet()) {
//                    ResourceLocation resourceId = entry.getKey();
//                    String path = resourceId.getPath();
//                    if (path.startsWith("structures/")) {
//                        String structurePath = path.substring("structures/".length());
//                        if (structurePath.endsWith(".nbt")) {
//                            structurePath = structurePath.substring(0, structurePath.length() - 4);
//                        }
//
//                        ResourceLocation structureId = new ResourceLocation("linggango_tweaks", structurePath);
//                        TreeTemplate template = loadAndProcessStructure(resourceManager, resourceId);
//                        if (template != null) {
//                            TREE_CACHE.put(structureId, template);
//                        }
//                    }
//                }
//                isPreloaded = true;
//            } catch (Exception e) {
//                LOGGER.error("Failed to preload tree structures", e);
//            }
//        }
//    }
//
//    private static TreeTemplate loadAndProcessStructure(ResourceManager resourceManager, ResourceLocation resourceId) {
//        try {
//            Optional<Resource> resourceOpt = resourceManager.getResource(resourceId);
//            if (resourceOpt.isEmpty()) return null;
//
//            try (InputStream inputStream = resourceOpt.get().open()) {
//                CompoundTag nbt = NbtIo.readCompressed(inputStream);
//                return processNbtStructure(resourceId, nbt);
//            }
//        } catch (Exception e) {
//            LOGGER.debug("Failed to load structure {}", resourceId, e);
//        }
//        return null;
//    }
//
//    private static TreeTemplate processNbtStructure(ResourceLocation structureId, CompoundTag nbt) {
//        try {
//            int sizeX = nbt.contains("sizeX") ? nbt.getInt("sizeX") : 0;
//            int sizeY = nbt.contains("sizeY") ? nbt.getInt("sizeY") : 0;
//            int sizeZ = nbt.contains("sizeZ") ? nbt.getInt("sizeZ") : 0;
//            Vec3i size = new Vec3i(sizeX, sizeY, sizeZ);
//
//            ListTag paletteList = nbt.getList("palette", 10);
//            List<BlockState> palette = new ArrayList<>();
//
//            for (int i = 0; i < paletteList.size(); ++i) {
//                CompoundTag paletteEntry = paletteList.getCompound(i);
//                palette.add(parseBlockStateFromPalette(paletteEntry));
//            }
//
//            ListTag blocksList = nbt.getList("blocks", 10);
//            List<BlockInfo> nonAirBlocks = new ArrayList<>();
//            List<BlockPos> redWoolPositions = new ArrayList<>();
//            BlockPos whiteWoolPos = null;
//
//            for (int i = 0; i < blocksList.size(); ++i) {
//                CompoundTag blockTag = blocksList.getCompound(i);
//                int stateIndex = blockTag.getInt("state");
//                if (stateIndex >= 0 && stateIndex < palette.size()) {
//                    BlockState state = palette.get(stateIndex);
//                    if (!state.isAir()) {
//                        ListTag posList = blockTag.getList("pos", 3);
//                        BlockPos pos = new BlockPos(posList.getInt(0), posList.getInt(1), posList.getInt(2));
//                        Block block = state.getBlock();
//                        nonAirBlocks.add(new BlockInfo(pos, state, block));
//
//                        if (block == Blocks.RED_WOOL) {
//                            redWoolPositions.add(pos);
//                        } else if (block == Blocks.WHITE_WOOL && whiteWoolPos == null) {
//                            whiteWoolPos = pos;
//                        }
//                    }
//                }
//            }
//
//            if (whiteWoolPos != null && !redWoolPositions.isEmpty()) {
//                Map<Rotation, BlockPos> centerOffsets = new EnumMap<>(Rotation.class);
//                for (Rotation rotation : Rotation.values()) {
//                    BlockPos rotatedWhiteWool = whiteWoolPos.rotate(rotation);
//                    centerOffsets.put(rotation, new BlockPos(-rotatedWhiteWool.getX(), -rotatedWhiteWool.getY(), -rotatedWhiteWool.getZ()));
//                }
//                AnchorData anchors = new AnchorData(redWoolPositions, whiteWoolPos, centerOffsets);
//                return new TreeTemplate(nonAirBlocks, anchors, size);
//            }
//            return null;
//        } catch (Exception e) {
//            return null;
//        }
//    }
//
//    private static BlockState parseBlockStateFromPalette(CompoundTag paletteEntry) {
//        String blockName = paletteEntry.getString("Name");
//        Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockName));
//        BlockState state = (block == null) ? Blocks.AIR.defaultBlockState() : block.defaultBlockState();
//
//        CompoundTag propertiesTag = paletteEntry.getCompound("Properties");
//        if (!propertiesTag.isEmpty()) {
//            for (String propertyName : propertiesTag.getAllKeys()) {
//                String propertyValue = propertiesTag.getString(propertyName);
//                for (Property<?> prop : state.getProperties()) {
//                    if (prop.getName().equals(propertyName)) {
//                        state = setPropertyValue(state, prop, propertyValue);
//                        break;
//                    }
//                }
//            }
//        }
//        return state;
//    }
//
//    private static <T extends Comparable<T>> BlockState setPropertyValue(BlockState state, Property<T> property, String value) {
//        Optional<T> propertyValue = property.getValue(value);
//        return propertyValue.map(t -> state.setValue(property, t)).orElse(state);
//    }
//
//    public static TreeTemplate getTreeTemplate(ResourceLocation id) {
//        return TREE_CACHE.get(id);
//    }
//}