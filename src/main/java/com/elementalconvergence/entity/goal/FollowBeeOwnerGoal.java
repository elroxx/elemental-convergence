package com.elementalconvergence.entity.goal;

import com.elementalconvergence.entity.MinionBeeEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.world.ServerWorld;

import java.util.EnumSet;
import java.util.Set;

public class FollowBeeOwnerGoal extends Goal {
    private final MinionBeeEntity bee;
    private PlayerEntity owner;
    private final double speed;
    private final float maxDistance;
    private final float minDistance;
    private int updateCountdownTicks;

    public FollowBeeOwnerGoal(MinionBeeEntity bee, double speed, float maxDistance, float minDistance) {
        this.bee = bee;
        this.speed = speed;
        this.maxDistance = maxDistance;
        this.minDistance = minDistance;
    }

    @Override
    public boolean canStart() {
        PlayerEntity owner = getOwner();
        if (owner == null || owner.isSpectator() || bee.squaredDistanceTo(owner) < (minDistance * minDistance)) {
            return false;
        }
        else if (this.bee.getTarget() != null) {
            // Don't start following if we have a target to attack
            return false;
        }
        this.owner = owner;
        return true;
    }

    @Override
    public boolean shouldContinue() {
        return !bee.getNavigation().isIdle() && bee.squaredDistanceTo(owner) > (minDistance * minDistance);
    }

    @Override
    public void start() {
        this.updateCountdownTicks = 0;
    }

    @Override
    public void stop() {
        this.owner = null;
    }

    @Override
    public void tick() {
        bee.getLookControl().lookAt(owner, 10.0f, bee.getMaxLookPitchChange());
        if (--this.updateCountdownTicks <= 0) {
            this.updateCountdownTicks = this.getTickCount(10);
            if (bee.squaredDistanceTo(owner) >= (maxDistance * maxDistance)) {

                if (owner.getWorld() instanceof ServerWorld serverWorld) {
                    Set<PositionFlag> flags = EnumSet.of(PositionFlag.X, PositionFlag.Y, PositionFlag.Z, PositionFlag.X_ROT, PositionFlag.Y_ROT);
                    bee.teleport(serverWorld, owner.getX(), owner.getY(), owner.getZ(), flags, bee.getYaw(), bee.getPitch());
                }
            } else {
                bee.getNavigation().startMovingTo(owner, speed);
            }
        }
    }

    private PlayerEntity getOwner() {
        return bee.getOwner();
    }
}
