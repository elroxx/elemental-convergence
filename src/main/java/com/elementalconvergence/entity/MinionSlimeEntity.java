package com.elementalconvergence.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

public class MinionSlimeEntity extends SlimeEntity {
    private PlayerEntity owner;
    private UUID ownerUuid;

    public MinionSlimeEntity(EntityType<? extends SlimeEntity> entityType, World world) {
        super(entityType, world);
        // Set up custom move control
        this.moveControl = new MinionSlimeMoveControl(this);
    }

    @Override
    protected void initGoals() {
        // Clear existing goals first
        this.targetSelector.getGoals().clear();
        this.goalSelector.getGoals().clear();

        // Add custom slime-compatible goals
        this.goalSelector.add(1, new MinionSlimeSwimmingGoal(this));
        this.goalSelector.add(2, new FollowOwnerSlimeGoal(this));
        this.goalSelector.add(3, new MinionSlimeFaceTowardTargetGoal(this));
        this.goalSelector.add(4, new MinionSlimeRandomLookGoal(this));
        this.goalSelector.add(5, new MinionSlimeMoveGoal(this));

        // Custom targeting goals for minion behavior
        this.targetSelector.add(1, new DefendOwnerGoal(this));
        this.targetSelector.add(2, new AttackOwnerTargetGoal(this));
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

    // Prevent splitting when killed - ensure minion has size 1 so it won't split
    @Override
    public void setSize(int size, boolean heal) {
        // Always set minion slimes to size 1 to prevent splitting
        super.setSize(1, heal);
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
    public boolean canTarget(LivingEntity target) {
        if (target == this.getOwner()) {
            return false;
        }
        return super.canTarget(target);
    }

    // Set the minion's size using Pehkui
    public void setMinionSize(float size) {
        ScaleData heightScale = ScaleTypes.HEIGHT.getScaleData(this);
        ScaleData widthScale = ScaleTypes.WIDTH.getScaleData(this);
        heightScale.setScale(size);
        widthScale.setScale(size);
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

    // Custom slime move control
    public static class MinionSlimeMoveControl extends MoveControl {
        private float targetYaw;
        private int ticksUntilJump;
        private final MinionSlimeEntity slime;
        private boolean jumpOften;

        public MinionSlimeMoveControl(MinionSlimeEntity slime) {
            super(slime);
            this.slime = slime;
            this.targetYaw = 180.0F * slime.getYaw() / (float)Math.PI;
        }

        public void look(float targetYaw, boolean jumpOften) {
            this.targetYaw = targetYaw;
            this.jumpOften = jumpOften;
        }

        public void move(double speed) {
            this.speed = speed;
            this.state = State.MOVE_TO;
        }

        @Override
        public void tick() {
            this.entity.setYaw(this.wrapDegrees(this.entity.getYaw(), this.targetYaw, 90.0F));
            this.entity.headYaw = this.entity.getYaw();
            this.entity.bodyYaw = this.entity.getYaw();

            if (this.state != State.MOVE_TO) {
                this.entity.setForwardSpeed(0.0F);
            } else {
                this.state = State.WAIT;
                if (this.entity.isOnGround()) {
                    this.entity.setMovementSpeed((float)(this.speed * this.entity.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED)));
                    if (this.ticksUntilJump-- <= 0) {
                        this.ticksUntilJump = this.slime.getTicksUntilNextJump();
                        if (this.jumpOften) {
                            this.ticksUntilJump /= 3;
                        }
                        this.slime.getJumpControl().setActive();
                        if (this.slime.makesJumpSound()) {
                            this.slime.playSound(this.slime.getJumpSound(), this.slime.getSoundVolume(), 1.0f);
                        }
                    } else {
                        this.slime.sidewaysSpeed = 0.0F;
                        this.slime.forwardSpeed = 0.0F;
                        this.entity.setMovementSpeed(0.0F);
                    }
                } else {
                    this.entity.setMovementSpeed((float)(this.speed * this.entity.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED)));
                }
            }
        }
    }

    // Custom swimming goal for slimes
    public static class MinionSlimeSwimmingGoal extends Goal {
        private final MinionSlimeEntity slime;

        public MinionSlimeSwimmingGoal(MinionSlimeEntity slime) {
            this.slime = slime;
            this.setControls(EnumSet.of(Goal.Control.JUMP, Goal.Control.MOVE));
            slime.getNavigation().setCanSwim(true);
        }

        @Override
        public boolean canStart() {
            return (this.slime.isTouchingWater() || this.slime.isInLava()) && this.slime.getMoveControl() instanceof MinionSlimeMoveControl;
        }

        @Override
        public boolean shouldRunEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            if (this.slime.getRandom().nextFloat() < 0.8F) {
                this.slime.getJumpControl().setActive();
            }
            MoveControl moveControl = this.slime.getMoveControl();
            if (moveControl instanceof MinionSlimeMoveControl slimeMoveControl) {
                slimeMoveControl.move(1.2);
            }
        }
    }

    // Custom face toward target goal
    public static class MinionSlimeFaceTowardTargetGoal extends Goal {
        private final MinionSlimeEntity slime;
        private int ticksLeft;

        public MinionSlimeFaceTowardTargetGoal(MinionSlimeEntity slime) {
            this.slime = slime;
            this.setControls(EnumSet.of(Goal.Control.LOOK));
        }

        @Override
        public boolean canStart() {
            LivingEntity target = this.slime.getTarget();
            if (target == null) {
                return false;
            }
            return this.slime.canTarget(target) && this.slime.getMoveControl() instanceof MinionSlimeMoveControl;
        }

        @Override
        public void start() {
            this.ticksLeft = toGoalTicks(300);
            super.start();
        }

        @Override
        public boolean shouldContinue() {
            LivingEntity target = this.slime.getTarget();
            if (target == null || !this.slime.canTarget(target)) {
                return false;
            }
            return --this.ticksLeft > 0;
        }

        @Override
        public boolean shouldRunEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = this.slime.getTarget();
            if (target != null) {
                this.slime.lookAtEntity(target, 10.0F, 10.0F);
            }
            MoveControl moveControl = this.slime.getMoveControl();
            if (moveControl instanceof MinionSlimeMoveControl slimeMoveControl) {
                slimeMoveControl.look(this.slime.getYaw(), this.slime.canAttack());
            }
        }
    }

    // Custom random look goal
    public static class MinionSlimeRandomLookGoal extends Goal {
        private final MinionSlimeEntity slime;
        private float targetYaw;
        private int timer;

        public MinionSlimeRandomLookGoal(MinionSlimeEntity slime) {
            this.slime = slime;
            this.setControls(EnumSet.of(Goal.Control.LOOK));
        }

        @Override
        public boolean canStart() {
            return this.slime.getTarget() == null &&
                    (this.slime.isOnGround() || this.slime.isTouchingWater() || this.slime.isInLava() || this.slime.hasStatusEffect(StatusEffects.LEVITATION)) &&
                    this.slime.getMoveControl() instanceof MinionSlimeMoveControl;
        }

        @Override
        public void tick() {
            if (--this.timer <= 0) {
                this.timer = this.getTickCount(40 + this.slime.getRandom().nextInt(60));
                this.targetYaw = (float)this.slime.getRandom().nextInt(360);
            }
            MoveControl moveControl = this.slime.getMoveControl();
            if (moveControl instanceof MinionSlimeMoveControl slimeMoveControl) {
                slimeMoveControl.look(this.targetYaw, false);
            }
        }
    }

    // Custom move goal
    public static class MinionSlimeMoveGoal extends Goal {
        private final MinionSlimeEntity slime;

        public MinionSlimeMoveGoal(MinionSlimeEntity slime) {
            this.slime = slime;
            this.setControls(EnumSet.of(Goal.Control.JUMP, Goal.Control.MOVE));
        }

        @Override
        public boolean canStart() {
            return !this.slime.hasVehicle();
        }

        @Override
        public void tick() {
            MoveControl moveControl = this.slime.getMoveControl();
            if (moveControl instanceof MinionSlimeMoveControl slimeMoveControl) {
                slimeMoveControl.move(1.0);
            }
        }
    }

    // Custom goal to follow owner using slime movement
    public static class FollowOwnerSlimeGoal extends Goal {
        private final MinionSlimeEntity slime;
        private PlayerEntity owner;
        private int updateCountdownTicks;
        private final double speed = 1.2;

        public FollowOwnerSlimeGoal(MinionSlimeEntity slime) {
            this.slime = slime;
            this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
        }

        @Override
        public boolean canStart() {
            PlayerEntity owner = this.slime.getOwner();
            if (owner == null || owner.isSpectator()) {
                return false;
            }

            double distance = this.slime.squaredDistanceTo(owner);
            if (distance < 6.0 * 6.0) { // Too close
                return false;
            }

            this.owner = owner;
            return true;
        }

        @Override
        public boolean shouldContinue() {
            if (this.owner == null || this.owner.isSpectator()) {
                return false;
            }
            double distance = this.slime.squaredDistanceTo(this.owner);
            return distance > 4.0 * 4.0; // Continue until close enough
        }

        @Override
        public void start() {
            this.updateCountdownTicks = 0;
        }

        @Override
        public void stop() {
            this.owner = null;
            MoveControl moveControl = this.slime.getMoveControl();
            if (moveControl instanceof MinionSlimeMoveControl slimeMoveControl) {
                slimeMoveControl.move(0);
            }
        }

        @Override
        public void tick() {
            if (this.owner == null) return;

            if (--this.updateCountdownTicks <= 0) {
                this.updateCountdownTicks = 10;

                double distance = this.slime.squaredDistanceTo(this.owner);
                if (distance >= 144.0) { // 12 blocks - teleport if too far
                    if (owner.getWorld() instanceof ServerWorld serverWorld) {
                        Set<PositionFlag> flags = EnumSet.of(PositionFlag.X, PositionFlag.Y, PositionFlag.Z, PositionFlag.X_ROT, PositionFlag.Y_ROT);
                        this.slime.teleport(serverWorld, this.owner.getX(), this.owner.getY(), this.owner.getZ(), flags, this.slime.getYaw(), this.slime.getPitch());
                    }
                    return;
                }

                // Use slime's movement control
                this.slime.lookAtEntity(this.owner, 10.0F, 10.0F);
                MoveControl moveControl = this.slime.getMoveControl();
                if (moveControl instanceof MinionSlimeMoveControl slimeMoveControl) {
                    slimeMoveControl.look(this.slime.getYaw(), true);
                    slimeMoveControl.move(this.speed);
                }
            }
        }
    }

    // Goal to defend the owner
    public static class DefendOwnerGoal extends ActiveTargetGoal<LivingEntity> {
        private final MinionSlimeEntity slime;
        private LivingEntity attacker;
        private int timestamp;

        public DefendOwnerGoal(MinionSlimeEntity slime) {
            super(slime, LivingEntity.class, 5, false, false, null);
            this.slime = slime;
        }

        @Override
        public boolean canStart() {
            PlayerEntity owner = this.slime.getOwner();
            if (owner == null) {
                return false;
            }

            this.attacker = owner.getAttacker();
            int i = owner.getLastAttackedTime();
            return i != this.timestamp && this.canTrack(this.attacker, TargetPredicate.DEFAULT) && this.slime.canTarget(this.attacker);
        }

        @Override
        public void start() {
            this.mob.setTarget(this.attacker);
            PlayerEntity owner = this.slime.getOwner();
            if (owner != null) {
                this.timestamp = owner.getLastAttackedTime();
            }
            super.start();
        }
    }

    // Goal to attack owner's target
    public static class AttackOwnerTargetGoal extends ActiveTargetGoal<LivingEntity> {
        private final MinionSlimeEntity slime;
        private LivingEntity ownerTarget;
        private int timestamp;

        public AttackOwnerTargetGoal(MinionSlimeEntity slime) {
            super(slime, LivingEntity.class, 5, false, false, null);
            this.slime = slime;
        }

        @Override
        public boolean canStart() {
            PlayerEntity owner = this.slime.getOwner();
            if (owner == null) {
                return false;
            }

            this.ownerTarget = owner.getAttacking();
            int i = owner.getLastAttackTime();
            return i != this.timestamp && this.canTrack(this.ownerTarget, TargetPredicate.DEFAULT) && this.slime.canTarget(this.ownerTarget);
        }

        @Override
        public void start() {
            this.mob.setTarget(this.ownerTarget);
            PlayerEntity owner = this.slime.getOwner();
            if (owner != null) {
                this.timestamp = owner.getLastAttackTime();
            }
            super.start();
        }
    }
}