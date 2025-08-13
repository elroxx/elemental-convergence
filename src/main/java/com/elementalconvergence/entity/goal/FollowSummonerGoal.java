package com.elementalconvergence.entity.goal;

import java.util.EnumSet;

import com.elementalconvergence.entity.MinionZombieEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.Goal.Control;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.PathNodeType;
import org.jetbrains.annotations.Nullable;

public class FollowSummonerGoal extends Goal {
    private final MinionZombieEntity minion;
    @Nullable
    private LivingEntity owner;
    private final double speed;
    private final EntityNavigation navigation;
    private int updateCountdownTicks;
    private final float maxDistance;
    private final float minDistance;
    private float oldWaterPathfindingPenalty;

    public FollowSummonerGoal(MinionZombieEntity minion, double speed, float minDistance, float maxDistance) {
        this.minion = minion;
        this.speed = speed;
        this.navigation = minion.getNavigation();
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        if (!(minion.getNavigation() instanceof MobNavigation) && !(minion.getNavigation() instanceof BirdNavigation)) {
            throw new IllegalArgumentException("Unsupported mob type for FollowOwnerGoal");
        }
    }

    public boolean canStart() {
        LivingEntity livingEntity = this.minion.getSummoner();
        if (livingEntity == null) {
            return false;
        } else if (this.minion.cannotFollowSummoner()) {
            return false;
        } else if (this.minion.getTarget() != null) {
            // Don't start following if we have a target to attack
            return false;
        } else if (this.minion.squaredDistanceTo(livingEntity) < (double)(this.minDistance * this.minDistance)) {
            return false;
        } else {
            this.owner = livingEntity;
            return true;
        }
    }

    public boolean shouldContinue() {
        if (this.navigation.isIdle()) {
            return false;
        } else if (this.minion.cannotFollowSummoner()) {
            return false;
        } else if (this.minion.getTarget() != null) {
            // Stop following if target to attack
            return false;
        } else {
            return !(this.minion.squaredDistanceTo(this.owner) <= (double)(this.maxDistance * this.maxDistance));
        }
    }

    public void start() {
        this.updateCountdownTicks = 0;
        this.oldWaterPathfindingPenalty = this.minion.getPathfindingPenalty(PathNodeType.WATER);
        this.minion.setPathfindingPenalty(PathNodeType.WATER, 0.0F);
    }

    public void stop() {
        this.owner = null;
        this.navigation.stop();
        this.minion.setPathfindingPenalty(PathNodeType.WATER, this.oldWaterPathfindingPenalty);
    }

    public void tick() {
        boolean bl = false; //this.minion.shouldTryTeleportToOwner();
        if (!bl) {
            this.minion.getLookControl().lookAt(this.owner, 10.0F, (float)this.minion.getMaxLookPitchChange());
        }

        if (--this.updateCountdownTicks <= 0) {
            this.updateCountdownTicks = this.getTickCount(10);
            this.navigation.startMovingTo(this.owner, this.speed);
        }
    }
}
