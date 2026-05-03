package com.misanthropy.linggango.linggango_tweaks.util.collision;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectLinkedOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("unused")
public final class FastBlockCollisionSpliterator {
    private static final CollisionContext PARTICLE_CTX = CollisionContext.empty();
    private static final int MAX_CACHE_ENTRIES = 512;
    private static final Reference2ObjectLinkedOpenHashMap<BlockState, CollisionData> CACHE = new Reference2ObjectLinkedOpenHashMap<>(128);

    public static int getCacheSize() {
        return CACHE.size();
    }

    private static CollisionData getCached(BlockState state, BlockGetter world, BlockPos pos) {
        CollisionData data = CACHE.get(state);
        if (data == null) {
            data = buildCollisionData(state, world, pos);
            CACHE.put(state, data);
            ensureCapacity();
        } else {
            if (data.boxes().length > 1) {
                return buildCollisionData(state, world, pos);
            }
        }
        return data;
    }

    private static void ensureCapacity() {
        while (CACHE.size() > MAX_CACHE_ENTRIES) {
            CACHE.removeFirst();
        }
    }

    public static void clearCache() {
        CACHE.clear();
    }

    public static void invalidate(@Nullable BlockState state) {
        if (state != null) {
            CACHE.remove(state);
        }
    }

    static @NotNull CollisionData buildCollisionData(BlockState state, BlockGetter world, BlockPos pos) {
        boolean suffocates = state.isRedstoneConductor(world, pos);
        VoxelShape shape = state.getCollisionShape(world, pos, FastBlockCollisionSpliterator.PARTICLE_CTX);
        AABB[] boxes;

        if (shape.isEmpty()) {
            boxes = new AABB[0];
        } else if (shape == Shapes.block()) {
            boxes = new AABB[]{new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0)};
        } else {
            List<AABB> bb = shape.toAabbs();
            boxes = bb.toArray(new AABB[0]);
        }

        return new CollisionData(suffocates, boxes);
    }

    public static void collectBoxes(AABB query, BlockGetter view, BlockState state, int x, int y, int z, ObjectArrayList<AABB> out) {
        if (state != null) {
            CollisionData data = getCached(state, view, new BlockPos(x, y, z));
            for (AABB b : data.boxes()) {
                AABB ob = b.move(x, y, z);
                if (ob.intersects(query)) {
                    out.add(ob);
                }
            }
        }
    }

    public record CollisionData(boolean suffocates, AABB[] boxes) {}
}