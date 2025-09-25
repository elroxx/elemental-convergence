package com.elementalconvergence.entity;

import com.elementalconvergence.entity.ModEntities;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpiderGrapplingHookEntity extends ProjectileEntity {
    private static final TrackedData<Boolean> ATTACHED = DataTracker.registerData(SpiderGrapplingHookEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final double LAUNCH_SPEED = 3.0;
    private static final double PULL_FORCE = 0.15;
    private static final double MAX_RANGE = 64.0;

    // Track active hooks per player
    private static final Map<UUID, SpiderGrapplingHookEntity> playerHooks = new HashMap<>();

    private boolean isAttached = false;
    private BlockPos attachedPos;
    private PlayerEntity owner;
    private int ticksExisted = 0;

    public SpiderGrapplingHookEntity(EntityType<? extends SpiderGrapplingHookEntity> entityType, World world) {
        super(entityType, world);
    }

    public SpiderGrapplingHookEntity(PlayerEntity owner, World world) {
        super(ModEntities.SPIDER_GRAPPLING_HOOK, world);
        this.owner = owner;
        this.setOwner(owner);

        // Set starting position slightly in front of player
        Vec3d eyePos = owner.getEyePos();
        Vec3d lookDir = owner.getRotationVec(1.0F);
        this.setPosition(eyePos.add(lookDir.multiply(0.3)));

        // Launch directly forward at high speed (no gravity like lashing potato)
        this.setVelocity(lookDir.multiply(LAUNCH_SPEED));

        // Register this hook for the player
        playerHooks.put(owner.getUuid(), this);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(ATTACHED, false);
    }

    public static SpiderGrapplingHookEntity getPlayerHook(PlayerEntity player) {
        SpiderGrapplingHookEntity hook = playerHooks.get(player.getUuid());
        if (hook != null && hook.isRemoved()) {
            playerHooks.remove(player.getUuid());
            return null;
        }
        return hook;
    }

    @Override
    public void tick() {
        super.tick();
        this.ticksExisted++;

        if (this.owner == null || this.owner.isRemoved() || !this.owner.isAlive()) {
            this.detachHook();
            this.discard();
            return;
        }

        // Check max range
        double distance = this.distanceTo(this.owner);
        if (distance > MAX_RANGE) {
            this.detachHook();
            this.discard();
            return;
        }

        if (!this.isAttached) {
            // Projectile phase - travel in straight line with no gravity
            HitResult hitResult = ProjectileUtil.getCollision(this, this::canHit);
            this.onCollision(hitResult);

            if (!this.getWorld().isClient && hitResult.getType() == HitResult.Type.MISS) {
                // Continue straight flight - NO GRAVITY (like lashing potato)
                Vec3d velocity = this.getVelocity();
                this.setPosition(this.getX() + velocity.x, this.getY() + velocity.y, this.getZ() + velocity.z);

                // Add spider web particles during flight
                if (this.ticksExisted % 3 == 0) {
                    this.getWorld().addParticle(ParticleTypes.WHITE_ASH,
                            this.getX(), this.getY(), this.getZ(),
                            0, 0, 0);
                }
            }
        } else {
            // Attached phase - pull player toward attachment point
            this.pullOwner();

            // Add hanging particles
            if (this.ticksExisted % 5 == 0) {
                this.getWorld().addParticle(ParticleTypes.WHITE_ASH,
                        this.getX(), this.getY(), this.getZ(),
                        0, -0.1, 0);
            }
        }
    }

    private void pullOwner() {
        if (this.owner != null && this.attachedPos != null) {
            Vec3d attachPoint = new Vec3d(this.attachedPos.getX() + 0.5, this.attachedPos.getY() + 0.5, this.attachedPos.getZ() + 0.5);
            Vec3d playerPos = this.owner.getPos().add(0, this.owner.getHeight() * 0.5, 0);
            Vec3d direction = attachPoint.subtract(playerPos);
            double distance = direction.length();

            if (distance > 1.0) {
                // Pull player toward attachment point
                direction = direction.normalize();
                Vec3d pullVelocity = direction.multiply(PULL_FORCE * Math.min(distance * 0.1, 1.0));

                Vec3d currentVelocity = this.owner.getVelocity();
                Vec3d newVelocity = currentVelocity.add(pullVelocity);

                // Limit maximum velocity
                double maxSpeed = 2.0;
                if (newVelocity.length() > maxSpeed) {
                    newVelocity = newVelocity.normalize().multiply(maxSpeed);
                }

                this.owner.setVelocity(newVelocity);
                this.owner.velocityModified = true;
            }

            // Make player immune to fall damage while attached (like lashing potato)
            this.owner.fallDistance = 0;
        }
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);

        if (!this.isAttached && !this.getWorld().isClient) {
            BlockPos pos = blockHitResult.getBlockPos();
            BlockState blockState = this.getWorld().getBlockState(pos);

            if (!blockState.isAir()) {
                // Attach to block
                this.isAttached = true;
                this.attachedPos = pos;
                this.setVelocity(Vec3d.ZERO);
                this.dataTracker.set(ATTACHED, true);

                // Position the hook at the hit point
                Vec3d hitPos = blockHitResult.getPos();
                this.setPosition(hitPos.x, hitPos.y, hitPos.z);

                // Play attachment sound
                this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.BLOCK_COBWEB_PLACE, SoundCategory.NEUTRAL, 0.5F, 1.2F);
            }
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        // Don't attach to entities, just pass through
        super.onEntityHit(entityHitResult);
    }

    @Override
    protected boolean canHit(Entity entity) {
        return super.canHit(entity) && entity != this.owner;
    }

    public void detachHook() {
        this.isAttached = false;
        this.attachedPos = null;
        if (this.owner != null) {
            playerHooks.remove(this.owner.getUuid());
        }
    }


    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);
        Entity entity = this.getWorld().getEntityById(packet.getEntityId());
        if (entity instanceof PlayerEntity) {
            this.owner = (PlayerEntity) entity;
        }
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putBoolean("Attached", this.isAttached);
        if (this.attachedPos != null) {
            nbt.putLong("AttachedPos", this.attachedPos.asLong());
        }
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        this.isAttached = nbt.getBoolean("Attached");
        if (nbt.contains("AttachedPos")) {
            this.attachedPos = BlockPos.fromLong(nbt.getLong("AttachedPos"));
        }
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        super.remove(reason);
        this.detachHook();
    }

    public boolean isAttached() {
        return this.dataTracker.get(ATTACHED);
    }
}
