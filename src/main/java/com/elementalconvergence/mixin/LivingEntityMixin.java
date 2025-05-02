package com.elementalconvergence.mixin;

import com.elementalconvergence.effect.ModEffects;
import com.google.common.base.Objects;
import gravity_changer.api.GravityChangerAPI;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
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
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.Random;

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
    @Shadow @Nullable
    private LivingEntity attacker;
    //@Shadow public int age;

    @Inject(method = "onStatusEffectRemoved", at = @At("HEAD"))
    private void onStatusEffectEnd(StatusEffectInstance effect, CallbackInfo ci) {
        RegistryEntry<StatusEffect> statusEffect = effect.getEffectType();

        //Just to check if its the end of gravity instability
        if (statusEffect.equals(ModEffects.GRAVITY_INSTABILITY)) {
            LivingEntity entity = (LivingEntity) (Object) this;
            //Put gravity back to normal in those case
            GravityChangerAPI.setBaseGravityDirection(entity, Direction.DOWN);
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

}
