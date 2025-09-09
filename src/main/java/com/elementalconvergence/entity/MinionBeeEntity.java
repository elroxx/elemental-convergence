package com.elementalconvergence.entity;

import com.elementalconvergence.entity.goal.FollowBeeOwnerGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import java.util.UUID;

public class MinionBeeEntity extends BeeEntity {
    private PlayerEntity owner;
    private UUID ownerUuid;

    public MinionBeeEntity(EntityType<? extends BeeEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initGoals() {
        //clear the  after creating them
        super.initGoals();

        this.targetSelector.getGoals().clear();
        this.goalSelector.getGoals().clear();

        //custom goals
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new MeleeAttackGoal(this, 3.4, false));
        this.goalSelector.add(2, new FollowBeeOwnerGoal(this, 6.0, 100.0f, 8.0f));
        this.goalSelector.add(3, new WanderAroundFarGoal(this, 1.0));
        this.goalSelector.add(4, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(5, new LookAroundGoal(this));

        //this.targetSelector.add(1, new TrackOwnerAttackerGoal(this));
        //this.targetSelector.add(2, new AttackWithOwnerGoal(this));
    }

    // Prevent XP drops
    @Override
    protected int getXpToDrop() {
        return 0;
    }

    @Override
    public boolean shouldDropXp() {
        return false;
    }

    // Prevent loot drops
    @Override
    protected void dropLoot(DamageSource damageSource, boolean causedByPlayer) {
        // Don't drop any loot
    }


    // Owner management
    public void setOwner(PlayerEntity player) {
        this.owner = player;
        this.ownerUuid = player.getUuid();
    }

    public PlayerEntity getOwner() {
        if (this.owner != null && !this.owner.isRemoved()) {
            return this.owner;
        } else if (this.ownerUuid != null && this.getWorld() instanceof ServerWorld serverWorld) {
            this.owner = serverWorld.getPlayerByUuid(this.ownerUuid);
            return this.owner;
        }
        return null;
    }

    @Override
    public boolean canTarget(net.minecraft.entity.LivingEntity target) {
        if (target == this.getOwner()) {
            return false;
        }
        return super.canTarget(target);
    }

    // Save/load owner data
    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        if (this.ownerUuid != null) {
            nbt.putUuid("Owner", this.ownerUuid);
        }
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.containsUuid("Owner")) {
            this.ownerUuid = nbt.getUuid("Owner");
        }
    }
}

