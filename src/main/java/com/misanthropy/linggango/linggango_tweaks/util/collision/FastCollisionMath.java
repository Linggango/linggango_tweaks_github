package com.misanthropy.linggango.linggango_tweaks.util.collision;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.phys.AABB;

@SuppressWarnings("unused")
public final class FastCollisionMath {
    public static double calculateMaxOffsetFromBoxes(AABB entityBox, ObjectArrayList<AABB> colliders, Direction.Axis axis, double offset) {
        if (offset != 0.0D && !colliders.isEmpty()) {
            double entityMinX = entityBox.minX;
            double entityMaxX = entityBox.maxX;
            double entityMinY = entityBox.minY;
            double entityMaxY = entityBox.maxY;
            double entityMinZ = entityBox.minZ;
            double entityMaxZ = entityBox.maxZ;
            int size = colliders.size();

            if (axis == Axis.X) {
                double maxMove = offset;
                if (offset > 0.0D) {
                    for (AABB b : colliders) {
                        if (b.maxY > entityMinY && b.minY < entityMaxY && b.maxZ > entityMinZ && b.minZ < entityMaxZ) {
                            double allowed = b.minX - entityMaxX;
                            if (allowed >= 0.0D && allowed < maxMove) maxMove = allowed;
                        }
                    }
                } else {
                    for (int i = 0; i < size; i++) {
                        AABB b = colliders.get(i);
                        if (b.maxY > entityMinY && b.minY < entityMaxY && b.maxZ > entityMinZ && b.minZ < entityMaxZ) {
                            double allowed = b.maxX - entityMinX;
                            if (allowed <= 0.0D && allowed > maxMove) maxMove = allowed;
                        }
                    }
                }
                return maxMove;
            } else if (axis == Axis.Y) {
                double maxMove = offset;
                if (offset > 0.0D) {
                    for (int i = 0; i < size; i++) {
                        AABB b = colliders.get(i);
                        if (b.maxX > entityMinX && b.minX < entityMaxX && b.maxZ > entityMinZ && b.minZ < entityMaxZ) {
                            double allowed = b.minY - entityMaxY;
                            if (allowed >= 0.0D && allowed < maxMove) maxMove = allowed;
                        }
                    }
                } else {
                    for (int i = 0; i < size; i++) {
                        AABB b = colliders.get(i);
                        if (b.maxX > entityMinX && b.minX < entityMaxX && b.maxZ > entityMinZ && b.minZ < entityMaxZ) {
                            double allowed = b.maxY - entityMinY;
                            if (allowed <= 0.0D && allowed > maxMove) maxMove = allowed;
                        }
                    }
                }
                return maxMove;
            } else {
                double maxMove = offset;
                if (offset > 0.0D) {
                    for (int i = 0; i < size; i++) {
                        AABB b = colliders.get(i);
                        if (b.maxX > entityMinX && b.minX < entityMaxX && b.maxY > entityMinY && b.minY < entityMaxY) {
                            double allowed = b.minZ - entityMaxZ;
                            if (allowed >= 0.0D && allowed < maxMove) maxMove = allowed;
                        }
                    }
                } else {
                    for (int i = 0; i < size; i++) {
                        AABB b = colliders.get(i);
                        if (b.maxX > entityMinX && b.minX < entityMaxX && b.maxY > entityMinY && b.minY < entityMaxY) {
                            double allowed = b.maxZ - entityMinZ;
                            if (allowed <= 0.0D && allowed > maxMove) maxMove = allowed;
                        }
                    }
                }
                return maxMove;
            }
        }
        return offset;
    }
}