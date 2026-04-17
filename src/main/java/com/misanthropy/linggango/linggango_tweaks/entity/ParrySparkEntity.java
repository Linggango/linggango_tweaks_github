package com.misanthropy.linggango.linggango_tweaks.entity;

import com.misanthropy.linggango.linggango_tweaks.registry.ModEntities;
import com.misanthropy.linggango.linggango_tweaks.registry.ModParticles;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

public class ParrySparkEntity extends Entity {

    private int lifetime = 0;
    private static final int MAX_LIFETIME = 12;
    private static final double GRAVITY = 0.12;
    private static final double BOUNCE_FACTOR = 0.3;

    public ParrySparkEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = false;
        this.noCulling = true;
    }

    public ParrySparkEntity(Level level, Vec3 pos, Vec3 velocity) {
        this(ModEntities.PARRY_SPARK.get(), level);
        this.setPos(pos.x, pos.y, pos.z);
        this.setDeltaMovement(velocity);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        super.tick();

        lifetime++;
        if (lifetime > MAX_LIFETIME) {
            this.discard();
            return;
        }

        Vec3 oldPos = this.position();
        Vec3 motion = this.getDeltaMovement();

        this.setPos(this.getX() + motion.x, this.getY() + motion.y, this.getZ() + motion.z);
        this.setDeltaMovement(motion.x, motion.y - GRAVITY, motion.z);

        HitResult hitResult = this.level().clip(new net.minecraft.world.level.ClipContext(
                oldPos,
                this.position(),
                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE,
                this
        ));

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hitResult;
            this.onHitBlock(blockHit);
        }

        if (this.level().isClientSide) {
            double distance = oldPos.distanceTo(this.position());
            int particlesToSpawn = (int) Math.ceil(distance / 0.015);
            particlesToSpawn = Math.min(particlesToSpawn, 150);

            for (int i = 0; i <= particlesToSpawn; i++) {
                double progress = particlesToSpawn == 0 ? 1.0 : (double) i / particlesToSpawn;
                double lerpX = Mth.lerp(progress, oldPos.x, this.getX());
                double lerpY = Mth.lerp(progress, oldPos.y, this.getY());
                double lerpZ = Mth.lerp(progress, oldPos.z, this.getZ());

                this.level().addParticle(
                        ModParticles.PARRY_SPARKLE.get(),
                        lerpX, lerpY, lerpZ,
                        0, 0, 0
                );
            }
        }

        if (motion.lengthSqr() < 0.01) {
            this.discard();
        }
    }

    private void onHitBlock(BlockHitResult hit) {
        Vec3 motion = this.getDeltaMovement();
        Vec3 normal = Vec3.atLowerCornerOf(hit.getDirection().getNormal());
        Vec3 reflected = motion.subtract(normal.scale(2 * motion.dot(normal)));

        this.setDeltaMovement(
                reflected.x * BOUNCE_FACTOR,
                reflected.y * BOUNCE_FACTOR,
                reflected.z * BOUNCE_FACTOR
        );

        this.setPos(
                hit.getLocation().x + normal.x * 0.1,
                hit.getLocation().y + normal.y * 0.1,
                hit.getLocation().z + normal.z * 0.1
        );
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.lifetime = tag.getInt("Lifetime");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Lifetime", this.lifetime);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 4096.0;
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return false;
    }

    @Override
    public boolean canCollideWith(Entity other) {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }
}