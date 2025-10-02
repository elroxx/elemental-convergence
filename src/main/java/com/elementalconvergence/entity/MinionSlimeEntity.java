package com.elementalconvergence.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MinionSlimeEntity extends SlimeEntity {
    private PlayerEntity owner;
    private UUID ownerUuid;
    private LivingEntity ownerLastAttacker;
    private int attackerCheckCooldown = 0;
    private float currentScale = 1.0f;
    private int attackCooldown = 0; //prevent combo

    private static final float BASE_HEALTH = 8.0f; //base hp for size 1.0
    private static final float BASE_DAMAGE = 4.0f;  //base dmg for size 1.0

    public MinionSlimeEntity(EntityType<? extends SlimeEntity> entityType, World world) {
        super(entityType, world);
        this.moveControl = new MinionSlimeMoveControl(this);
    }

    public static DefaultAttributeContainer.Builder createMinionSlimeAttributes() {
        return SlimeEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, BASE_DAMAGE)
                .add(EntityAttributes.GENERIC_MAX_HEALTH, BASE_HEALTH);
    }

    @Override
    protected void initGoals() {
        //clear
        this.targetSelector.getGoals().clear();
        this.goalSelector.getGoals().clear();

        //goals
        this.goalSelector.add(1, new MinionSlimeSwimmingGoal(this));
        this.goalSelector.add(2, new FollowOwnerSlimeGoal(this));
        this.goalSelector.add(3, new MinionSlimeAttackGoal(this)); //custom
        this.goalSelector.add(4, new MinionSlimeFaceTowardTargetGoal(this));
        this.goalSelector.add(5, new MinionSlimeRandomLookGoal(this));
        this.goalSelector.add(6, new MinionSlimeMoveGoal(this));

        this.targetSelector.add(1, new ActiveTargetGoal<>(this, LivingEntity.class, 10, true, false, entity -> {
            //if owner or target null
            if (this.owner == null || entity == null) {
                return false;
            }
            return entity != this.owner && entity == this.owner.getAttacking();
        }));

        this.targetSelector.add(2, new ActiveTargetGoal<>(this, LivingEntity.class, 10, true, false, entity -> {
            if (this.owner == null || entity == null) {
                return false;
            }

            if (entity == this.owner) {
                return false;
            }

            //revenge on owner
            return entity == this.ownerLastAttacker && this.ownerLastAttacker != null;
        }));
    }

    @Override
    public void tick() {
        super.tick();

        if (this.attackCooldown > 0) {
            this.attackCooldown--;
        }

        if (this.owner != null && !this.getWorld().isClient) {
            this.attackerCheckCooldown--;
            if (this.attackerCheckCooldown <= 0) {
                this.attackerCheckCooldown = 10;
                this.checkOwnerAttacker();
            }
        }
    }


    @Override
    public void pushAwayFrom(Entity entity) {
        super.pushAwayFrom(entity);

        if (!this.getWorld().isClient && entity instanceof LivingEntity livingEntity &&
                livingEntity == this.getTarget() && this.attackCooldown <= 0) {

            DamageSource damageSource = this.getDamageSources().mobAttack(this);
            float damage = (float) this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);

            if (livingEntity.damage(damageSource, damage)) {
                this.attackCooldown = 20; // 1 sec
                this.playSound(SoundEvents.ENTITY_SLIME_ATTACK, 1.0F, 1.0F);

                //apply knockback
                this.applyKnockback(livingEntity);

            }
        }
    }


    private void applyKnockback(LivingEntity target) {
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);

        if (distance > 0) {
            //normalize
            double knockbackStrength = 0.5 * this.currentScale; //scaling on size
            dx = (dx / distance) * knockbackStrength;
            dz = (dz / distance) * knockbackStrength;

            target.setVelocity(
                    target.getVelocity().x + dx,
                    target.getVelocity().y + 0.2,
                    target.getVelocity().z + dz
            );
            target.velocityModified = true;

        }
    }

    private void checkOwnerAttacker() {
        if (this.owner == null) {
            return;
        }

        LivingEntity attacker = this.owner.getAttacker();
        if (attacker != null && attacker != this.owner && attacker.isAlive()) {
            // only update if different
            if (this.ownerLastAttacker != attacker) {
                this.ownerLastAttacker = attacker;

                this.setTarget(null);
            }
        }
    }

    //dont drop anything
    @Override
    protected int getXpToDrop() {
        return 0;
    }

    @Override
    public boolean shouldDropXp() {
        return false;
    }

    @Override
    protected void dropLoot(DamageSource damageSource, boolean causedByPlayer) {
    }

    @Override
    protected void dropInventory() {
    }

    //set size to 1 so no splitting, and then change its actual size via pekhui
    @Override
    public void setSize(int size, boolean heal) {
        super.setSize(1, heal);
    }

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

    //pekhui for scale and attributes for dmg/hp
    public void setMinionSize(float size) {
        ScaleData heightScale = ScaleTypes.HEIGHT.getScaleData(this);
        ScaleData widthScale = ScaleTypes.WIDTH.getScaleData(this);

        //ScaleData hitboxHeightScale = ScaleTypes.HITBOX_HEIGHT.getScaleData(this); //I NEED HITBOX BECAUSE THIS DOESNT SCALE VS PLAYERS
        //ScaleData hitboxWidthScale = ScaleTypes.HITBOX_WIDTH.getScaleData(this);

        heightScale.setScale(size);
        widthScale.setScale(size);
        //hitboxHeightScale.setScale(size);
        //hitboxWidthScale.setScale(size);

        this.currentScale = size;

        float newMaxHealth = BASE_HEALTH * size;
        this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(newMaxHealth);
        this.setHealth(newMaxHealth);

        float newDamage = BASE_DAMAGE * size;
        this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(newDamage);
    }

    public final boolean cannotFollowOwner() {
        return this.hasVehicle() || this.getOwner() != null && this.getOwner().isSpectator();
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        if (this.ownerUuid != null) {
            nbt.putUuid("Owner", this.ownerUuid);
        }
        nbt.putFloat("CurrentScale", this.currentScale);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.containsUuid("Owner")) {
            this.ownerUuid = nbt.getUuid("Owner");
        }
        if (nbt.contains("CurrentScale")) {
            this.currentScale = nbt.getFloat("CurrentScale");
            //reapply scale when loading in nbt
            ScaleData heightScale = ScaleTypes.HEIGHT.getScaleData(this);
            ScaleData widthScale = ScaleTypes.WIDTH.getScaleData(this);
            //ScaleData hitboxHeightScale = ScaleTypes.HITBOX_HEIGHT.getScaleData(this);
            //ScaleData hitboxWidthScale = ScaleTypes.HITBOX_WIDTH.getScaleData(this);

            heightScale.setScale(this.currentScale);
            widthScale.setScale(this.currentScale);
            //hitboxHeightScale.setScale(this.currentScale);
            //hitboxWidthScale.setScale(this.currentScale);
        }
    }

    public static class MinionSlimeAttackGoal extends Goal {
        private final MinionSlimeEntity slime;
        private int growTieredTimer;

        public MinionSlimeAttackGoal(MinionSlimeEntity slime) {
            this.slime = slime;
            this.setControls(EnumSet.of(Goal.Control.LOOK, Goal.Control.MOVE));
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
            this.growTieredTimer = toGoalTicks(300);
            super.start();
        }

        @Override
        public boolean shouldContinue() {
            LivingEntity target = this.slime.getTarget();
            if (target == null || !this.slime.canTarget(target)) {
                return false;
            }
            return --this.growTieredTimer > 0;
        }

        @Override
        public boolean shouldRunEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = this.slime.getTarget();
            if (target == null) {
                return;
            }

            this.slime.lookAtEntity(target, 10.0F, 10.0F);

            MoveControl moveControl = this.slime.getMoveControl();
            if (moveControl instanceof MinionSlimeMoveControl slimeMoveControl) {
                slimeMoveControl.look(this.slime.getYaw(), true);

                double distance = this.slime.squaredDistanceTo(target);
                if (distance < 9.0) { //attack range
                    if (this.slime.isOnGround()) {
                        //jump
                        double dx = target.getX() - this.slime.getX();
                        double dz = target.getZ() - this.slime.getZ();
                        double distance2d = Math.sqrt(dx * dx + dz * dz);

                        if (distance2d > 0) {
                            //jump
                            this.slime.setVelocity(
                                    dx / distance2d * 0.6,
                                    0.4,
                                    dz / distance2d * 0.6
                            );
                            this.slime.velocityModified = true;
                        }
                    }
                } else {
                    slimeMoveControl.move(1.5);
                }
            }
        }
    }

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

            // dont follow if target
            if (this.slime.getTarget() != null) {
                return false;
            }

            if (this.slime.cannotFollowOwner()) {
                return false;
            }

            double distance = this.slime.squaredDistanceTo(owner);
            if (distance < 6.0 * 6.0) { // too close
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

            if (this.slime.getTarget() != null) {
                return false;
            }

            if (this.slime.cannotFollowOwner()) {
                return false;
            }

            double distance = this.slime.squaredDistanceTo(this.owner);
            return distance > 5.0 * 5.0; // continue until too close
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
                if (distance >= 100*100*4) { //FAR we tp
                    if (owner.getWorld() instanceof ServerWorld serverWorld) {
                        Set<PositionFlag> flags = EnumSet.of(PositionFlag.X, PositionFlag.Y, PositionFlag.Z, PositionFlag.X_ROT, PositionFlag.Y_ROT);
                        this.slime.teleport(serverWorld, this.owner.getX(), this.owner.getY(), this.owner.getZ(), flags, this.slime.getYaw(), this.slime.getPitch());
                    }
                    return;
                }

                this.slime.lookAtEntity(this.owner, 10.0F, 10.0F);
                MoveControl moveControl = this.slime.getMoveControl();
                if (moveControl instanceof MinionSlimeMoveControl slimeMoveControl) {
                    slimeMoveControl.look(this.slime.getYaw(), true);
                    slimeMoveControl.move(this.speed);
                }
            }
        }
    }
}