package com.misanthropy.linggango.linggango_tweaks.mixin.chunk;

import net.minecraft.server.level.ChunkTaskPriorityQueue;
import net.minecraft.server.level.ChunkTaskPriorityQueueSorter;
import net.minecraft.util.Unit;
import net.minecraft.util.thread.ProcessorHandle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.function.Function;

@Mixin(ChunkTaskPriorityQueueSorter.class)
public abstract class FasterChunkLoadingMixin {

    @Unique private int linggango$adjusting = 0;
    @Shadow(aliases = "m_140645_") protected abstract <T> void submit(ChunkTaskPriorityQueue<Function<ProcessorHandle<Unit>, T>> queue, ProcessorHandle<T> handle);
    @Shadow public abstract boolean hasWork();
    @Inject(method = "pollTask", at = @At("RETURN")) private <T> void linggango$accelerateWorldGen(ChunkTaskPriorityQueue<Function<ProcessorHandle<Unit>, T>> queue, ProcessorHandle<T> handle, CallbackInfo ci) {
        if (this.hasWork() && this.linggango$adjusting == 0 && queue.toString().contains("worldgen")) {
            this.linggango$adjusting++;
            this.submit(queue, handle);
            this.linggango$adjusting--;

        }
    }
}