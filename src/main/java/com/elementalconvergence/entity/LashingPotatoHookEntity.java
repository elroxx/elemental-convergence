package com.elementalconvergence.entity;

import com.elementalconvergence.data.IGrapplingHookDataSaver;
import com.elementalconvergence.item.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Entity.MoveEffect;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class LashingPotatoHookEntity extends ProjectileEntity {
    public static final TrackedData<Boolean> IN_BLOCK;
    public static final TrackedData<Float> LENGTH;
    private static final float MAX_LENGTH = 100.0F;
    private static final double VELOCITY_MULTIPLIER = (double)5.0F;

    public LashingPotatoHookEntity(EntityType<? extends LashingPotatoHookEntity> entityType, World world) {
        super(entityType, world);
        this.ignoreCameraFrustum = true;
    }

    public LashingPotatoHookEntity(World world, PlayerEntity playerEntity) {
        this(ModEntities.LASHING_POTATO_HOOK, world);
        this.setOwner(playerEntity);
        this.setPosition(playerEntity.getX(), playerEntity.getEyeY() - 0.1, playerEntity.getZ());
        this.setVelocity(playerEntity.getRotationVec(1.0F).multiply(VELOCITY_MULTIPLIER));
    }

    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(IN_BLOCK, false);
        builder.add(LENGTH, 0.0F);
    }

    public boolean shouldRender(double distance) {
        return true;
    }

    public void updateTrackedPositionAndAngles(double x, double y, double z, float yaw, float pitch, int interpolationSteps) {
    }

    public void tick() {
        super.tick();
        PlayerEntity playerEntity = this.getPlayerOwner();
        if (playerEntity != null && (this.getWorld().isClient() || !this.shouldRemove(playerEntity))) {
            HitResult hitResult = ProjectileUtil.getCollision(this, this::canHit);
            if (hitResult.getType() != Type.MISS) {
                this.onCollision(hitResult);
            }

            this.setPosition(hitResult.getPos());
            this.checkBlockCollision();
        } else {
            this.discard();
        }
    }

    private boolean shouldRemove(PlayerEntity playerEntity) {
        if (!playerEntity.isRemoved() && playerEntity.isAlive() && playerEntity.isHolding(ModItems.LASHING_POTATO_HOOK) && !(this.squaredDistanceTo(playerEntity) > (double)(MAX_LENGTH * MAX_LENGTH))) {
            return false;
        } else {
            this.discard();
            return true;
        }
    }

    protected boolean canHit(Entity entity) {
        return false;
    }

    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        this.setVelocity(Vec3d.ZERO);
        this.setInBlock(true);
        PlayerEntity playerEntity = this.getPlayerOwner();
        if (playerEntity != null) {
            double distance = playerEntity.getEyePos().subtract(blockHitResult.getPos()).length();
            this.setLength(Math.max((float)distance * 0.5F - 3.0F, 1.5F));
        }
    }

    public void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putBoolean("in_block", this.isInBlock());
        nbt.putFloat("length", this.getLength());
    }

    public void readCustomDataFromNbt(NbtCompound nbt) {
        this.setInBlock(nbt.getBoolean("in_block"));
        this.setLength(nbt.getFloat("length"));
    }

    private void setInBlock(boolean inBlock) {
        this.getDataTracker().set(IN_BLOCK, inBlock);
    }

    private void setLength(float length) {
        this.getDataTracker().set(LENGTH, length);
    }

    public boolean isInBlock() {
        return (Boolean)this.getDataTracker().get(IN_BLOCK);
    }

    public float getLength() {
        return (Float)this.getDataTracker().get(LENGTH);
    }

    protected Entity.MoveEffect getMoveEffect() {
        return MoveEffect.NONE;
    }

    public void remove(Entity.RemovalReason reason) {
        this.updatePlayerHook(null);
        super.remove(reason);
    }

    public void onRemoved() {
        this.updatePlayerHook(null);
    }

    public void setOwner(@Nullable Entity entity) {
        super.setOwner(entity);
        this.updatePlayerHook(this);
    }

    private void updatePlayerHook(@Nullable LashingPotatoHookEntity hookEntity) {
        PlayerEntity playerEntity = this.getPlayerOwner();
        if (playerEntity != null) {
            IGrapplingHookDataSaver hookDataSaver = (IGrapplingHookDataSaver) playerEntity;
            hookDataSaver.getGrapplingHookData().setGrapplingHookEntity(hookEntity);
        }
    }

    @Nullable
    public PlayerEntity getPlayerOwner() {
        Entity entity = this.getOwner();
        return entity instanceof PlayerEntity ? (PlayerEntity)entity : null;
    }

    public boolean canUsePortals() {
        return false;
    }

    /*@Override
    public Packet<ClientPlayPacketListener> createSpawnPacket(net.minecraft.server.network.EntityTrackerEntry entry) {
        int ownerId = this.getOwner() == null ? 0 : this.getOwner().getId();
        return new EntitySpawnS2CPacket(this, ownerId); // second arg is the int entityData
    }*/

    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);
        if (this.getPlayerOwner() == null) {
            this.kill();
        }
    }

    static {
        IN_BLOCK = DataTracker.registerData(LashingPotatoHookEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
        LENGTH = DataTracker.registerData(LashingPotatoHookEntity.class, TrackedDataHandlerRegistry.FLOAT);
    }
}
