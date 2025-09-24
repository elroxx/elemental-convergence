package com.elementalconvergence.mixin;

import com.elementalconvergence.ElementalConvergence;
import com.elementalconvergence.effect.ModEffects;
import com.elementalconvergence.effect.VoidSicknessEffect;
import com.google.common.base.Objects;
import gravity_changer.api.GravityChangerAPI;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.elementalconvergence.enchantment.ModEnchantments;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.Random;

import static com.elementalconvergence.world.dimension.ModDimensions.VOID_WORLD_KEY;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    // Shadow methods needed for the breathing logic
    @Shadow protected int playerHitTimer;
    @Shadow @Nullable protected PlayerEntity attackingPlayer;
    @Shadow @Nullable protected LivingEntity attacking;
    @Shadow protected int lastAttackedTime;
    //@Shadow public boolean inPowderSnow;
    @Shadow protected int hurtTime;
    //@Shadow protected int timeUntilRegen;
    @Shadow protected int deathTime;
    @Shadow protected float lookDirection;
    @Shadow protected float prevLookDirection;
    @Shadow protected float bodyYaw;
    @Shadow protected float prevBodyYaw;
    @Shadow protected float headYaw;
    @Shadow protected float prevHeadYaw;
    //@Shadow protected abstract boolean isWet();
    //@Shadow protected abstract void extinguishWithSound();
    @Shadow protected abstract boolean isDead();
    //@Shadow public abstract World getWorld();
    @Shadow protected abstract void updatePostDeath();
    @Shadow protected abstract void tickStatusEffects();
    //@Shadow public abstract float getYaw();
    //@Shadow public abstract float getPitch();
    @Shadow protected abstract boolean isAlive();
    @Shadow public abstract void setAttacker(@Nullable LivingEntity attacker);
    @Shadow @Nullable private LivingEntity attacker;
    //@Shadow public int age;


    private static final Identifier STEP_HEIGHT_MODIFIER_ID = ElementalConvergence.id( "high_steps_modifier");

    @Inject(method = "onStatusEffectRemoved", at = @At("HEAD"))
    private void onStatusEffectEnd(StatusEffectInstance effect, CallbackInfo ci) {
        RegistryEntry<StatusEffect> statusEffect = effect.getEffectType();

        //Just to check if its the end of gravity instability
        if (statusEffect.equals(ModEffects.GRAVITY_INSTABILITY)) {
            LivingEntity entity = (LivingEntity) (Object) this;
            //Put gravity back to normal in those case
            GravityChangerAPI.setBaseGravityDirection(entity, Direction.DOWN);
        }

        //check if its the end of void sickness
        if (statusEffect.equals(ModEffects.VOID_SICKNESS)){
            LivingEntity entity = (LivingEntity) (Object) this;
            if (entity.getWorld().isClient()) {
                return; // Only run on server side
            }

            // When effect ends, teleport back to overworld
            if (entity.getWorld().getRegistryKey().equals(VOID_WORLD_KEY)) {
                VoidSicknessEffect.teleportToOverworld(entity);
            }
        }
    }

    private static final Random RANDOM = new Random();

    // Inject into baseTick that handles breathing (only in part with breathing related)
    @Inject(method = "baseTick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;isSubmergedIn(Lnet/minecraft/registry/tag/TagKey;)Z",
                    ordinal = 0),
            cancellable = true)
    private void modifyBreathing(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity)(Object)this;

        // Only apply to players
        if (!(entity instanceof PlayerEntity)) {
            return;
        }

        if (entity.hasStatusEffect(ModEffects.GILLS)) {
            World world = entity.getWorld();

            if (!world.isClient) {
                BlockPos blockPos = entity.getBlockPos();

                boolean inWater = entity.isSubmergedIn(FluidTags.WATER);
                boolean inRain = world.isRaining() && world.isSkyVisible(blockPos);

                if (inWater || inRain) {
                    // In water, player should gain air back (like on land) OR IN RAIN, OR IN THE NEW OTHER UNRELATED EFFECT I WILL ADD AFTER
                    entity.setAir(Math.min(entity.getAir() + 4, entity.getMaxAir()));
                } else {

                    int respirationLevel = EnchantmentHelper.getLevel(world.getRegistryManager().getWrapperOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(Enchantments.RESPIRATION),
                            entity.getEquippedStack(net.minecraft.entity.EquipmentSlot.HEAD));


                    // Respiration, there's a chance to not lose air each tick
                    if (respirationLevel > 0) {
                        if (RANDOM.nextInt(respirationLevel + 1) > 0) {
                            // Skip air loss this tick
                            ci.cancel();
                            return;
                        }
                    }

                    // In air, player should lose air (like drowning)
                    entity.setAir(entity.getAir() - 1);

                    if (entity.getAir() == -20) {
                        entity.setAir(0);
                        entity.damage(entity.getDamageSources().drown(), 2.0F);
                    }
                }

                // GOES BACK TO NORMAL BREATHING LOGIC
                ci.cancel();

                if (this.isAlive() && (((LivingEntity)(Object)this).isWet() || ((LivingEntity)(Object)this).inPowderSnow)) {
                    ((LivingEntity)(Object)this).extinguishWithSound();
                }

                if (this.hurtTime > 0) {
                    --this.hurtTime;
                }

                if (((LivingEntity)(Object)this).timeUntilRegen > 0 && !((LivingEntity) (Object)this instanceof ServerPlayerEntity)) {
                    --((LivingEntity)(Object)this).timeUntilRegen;
                }

                if (this.isDead() && ((LivingEntity)(Object)this).getWorld().shouldUpdatePostDeath((LivingEntity) (Object)this)) {
                    this.updatePostDeath();
                }

                if (playerHitTimer > 0) {
                    --this.playerHitTimer;
                } else {
                    this.attackingPlayer = null;
                }

                if (this.attacking != null && !this.attacking.isAlive()) {
                    this.attacking = null;
                }

                if (this.attacker != null) {
                    if (!this.attacker.isAlive()) {
                        this.setAttacker((LivingEntity)null);
                    } else if (((LivingEntity)(Object)this).age - this.lastAttackedTime > 100) {
                        this.setAttacker((LivingEntity)null);
                    }
                }

                this.tickStatusEffects();
                this.prevLookDirection = this.lookDirection;
                this.prevBodyYaw = this.bodyYaw;
                this.prevHeadYaw = this.headYaw;
                ((LivingEntity)(Object)this).getWorld().getProfiler().pop();


            }
        }
    }



    @Inject(method = "tickFallFlying", at = @At("HEAD"), cancellable = true)
    private void allowWingsFlightContinuation(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        EntityAccessor accessor = (EntityAccessor) entity;

        // players with wings only
        if (entity instanceof PlayerEntity player && player.hasStatusEffect(ModEffects.WINGS)) {
            boolean bl = accessor.invokeGetFlag(7);
            if (bl && !entity.isOnGround() && !entity.hasVehicle() && !entity.hasStatusEffect(StatusEffects.LEVITATION)) {
                // no elytra durability dmg
                bl = true;
                int i = entity.getFallFlyingTicks() + 1;
                if (!entity.getWorld().isClient && i % 10 == 0) {
                    entity.emitGameEvent(GameEvent.ELYTRA_GLIDE);
                }
            } else {
                bl = false;
            }

            if (!entity.getWorld().isClient) {
                accessor.invokeSetFlag(7, bl);
            }

            // cancel the rest of the method (awesome)
            ci.cancel();
        }
    }


    @Inject(method = "onEquipStack", at = @At("TAIL"))
    private void onEquipStack(EquipmentSlot slot, ItemStack oldStack, ItemStack newStack, CallbackInfo ci) {
        if (slot == EquipmentSlot.LEGS) {
            LivingEntity entity = (LivingEntity) (Object) this;
            updateStepHeight(entity);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        // check 20 ticks to update step height
        if (entity.age % 20 == 0) {
            updateStepHeight(entity);
        }
    }

    private void updateStepHeight(LivingEntity entity) {
        //change existing modifier
        if (entity.getAttributeInstance(EntityAttributes.GENERIC_STEP_HEIGHT) != null) {
            entity.getAttributeInstance(EntityAttributes.GENERIC_STEP_HEIGHT)
                    .removeModifier(STEP_HEIGHT_MODIFIER_ID);
        }

        // verify high steps lvl
        ItemStack leggings = entity.getEquippedStack(EquipmentSlot.LEGS);
        RegistryEntry<Enchantment> volcanicEntry = entity.getWorld().getRegistryManager().getWrapperOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(ModEnchantments.HIGH_STEPS);
        int highStepsLevel = EnchantmentHelper.getLevel(volcanicEntry, leggings);

        if (highStepsLevel > 0) {
            // add new modifier (doesnt change base steps, so earth can still work properly)
            float stepHeightIncrease = highStepsLevel * 0.5f;
            EntityAttributeModifier modifier = new EntityAttributeModifier(
                    STEP_HEIGHT_MODIFIER_ID,
                    stepHeightIncrease,
                    EntityAttributeModifier.Operation.ADD_VALUE
            );

            entity.getAttributeInstance(EntityAttributes.GENERIC_STEP_HEIGHT)
                    .addTemporaryModifier(modifier);
        }
    }

    private Vec3d storedMovementInput;

    @Inject(method = "travel", at = @At("HEAD"))
    private void storeMovementInput(Vec3d movementInput, CallbackInfo ci) {
        this.storedMovementInput = movementInput;
    }

    //modify x when air resistance is applied
    @ModifyArg(
            method = "travel",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;setVelocity(DDD)V"),
            index = 0 // X velocity argument
    )
    private double modifyHorizontalXVelocity(double originalX) {
        return modifyHorizontalVelocity(originalX);
    }

    //modify z when air resistance is applide
    @ModifyArg(
            method = "travel",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;setVelocity(DDD)V"),
            index = 2 // Z velocity argument
    )
    private double modifyHorizontalZVelocity(double originalZ) {
        return modifyHorizontalVelocity(originalZ);
    }

    private double modifyHorizontalVelocity(double originalVelocity) {
        LivingEntity entity = (LivingEntity) (Object) this;

        //check if bouncy
        if (entity instanceof PlayerEntity player &&
                player.hasStatusEffect(ModEffects.BOUNCY) && //so not in creative flight either
                !player.isOnGround() && !player.getAbilities().flying &&
                this.storedMovementInput != null &&
                this.storedMovementInput.lengthSquared() < 1.0E-7 && //i.e. no movement input
                !entity.hasNoDrag()) { //only when air resistance is being applied

            //less air resistance
            return originalVelocity * (0.99 / 0.91);
        }

        return originalVelocity; //otherwise we default
    }

}
