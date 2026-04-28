package com.misanthropy.linggango.linggango_tweaks.util.collision;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.BlockGetter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class FastCollision {
    private static final Minecraft mc = Minecraft.getInstance();
    private static @Nullable BlockGetter chunk;
    private static long chunkPos;

    public FastCollision() {
    }

    @Contract(pure = true)
    public static long toLong(long x, long z) {
        return x & 4294967295L | (z & 4294967295L) << 32;
    }

    public static @Nullable BlockGetter getChunk(int chunkX, int chunkZ) {
        long pos = toLong(chunkX, chunkZ);
        if (chunk != null && chunkPos == pos) {
            return chunk;
        } else if (mc.level == null) {
            return null;
        } else {
            BlockGetter view = mc.level.getChunkSource().getChunk(chunkX, chunkZ, false);
            if (view == null) {
                return null;
            } else {
                chunk = view;
                chunkPos = pos;
                return view;
            }
        }
    }

    public static void clear() {
        chunk = null;
        chunkPos = 0;
        FastBlockCollisionSpliterator.clearCache();
    }
}