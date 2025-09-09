package com.elementalconvergence.magic.convergencehandlers;

import com.elementalconvergence.data.IMagicDataSaver;
import com.elementalconvergence.data.MagicData;
import com.elementalconvergence.effect.ModEffects;
import com.elementalconvergence.enchantment.ModEnchantments;
import com.elementalconvergence.item.ModItems;
import com.elementalconvergence.magic.IMagicHandler;
import com.elementalconvergence.networking.TaskScheduler;
import com.terraformersmc.modmenu.util.mod.Mod;
import gravity_changer.api.GravityChangerAPI;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.EvokerFangsEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

import java.util.Set;

import static com.elementalconvergence.ElementalConvergence.BASE_MAGIC_ID;

public class SlimeMagicHandler implements IMagicHandler {
    public static final int SLIME_INDEX= (BASE_MAGIC_ID.length-1)+9;

    public static final float SLIME_GRAVITY = 0.001f; //to remove fall damage as much as possible

    public static final int LEAP_DEFAULT_COOLDOWN = 3*20; //3 seconds
    private int leapCooldown=0;

    public static final int REGAIN_SIZE_FROM_SPLIT_TIMER_MAX=2500; //2:05 minutes
    private int regainSizeTimer=0;
    public static final float BASE_SIZE = 1.0f;
    public static final float SPLIT_SIZE = 0.5f;
    private final float SIZE_INCREMENT = SPLIT_SIZE/REGAIN_SIZE_FROM_SPLIT_TIMER_MAX;


    @Override
    public void handleItemRightClick(PlayerEntity player) {

    }

    @Override
    public void handleEntityRightClick(PlayerEntity player, Entity targetEntity) {

    }

    @Override
    public void handlePassive(PlayerEntity player) {
        //buff
        Vec3d velocity = player.getVelocity();

        //ground bounce
        /*if (player.isOnGround() && velocity.y < -0.1 && !player.isSneaking()) {
            System.out.println(velocity.y);
            double bounceFactor = 1.0;
            player.setVelocity(velocity.x, -velocity.y * bounceFactor, velocity.z);
            player.velocityModified = true;
        }*/

        //wall bounce
        if (Math.abs(velocity.x) > 0.1 || Math.abs(velocity.z) > 0.1) {
            BlockPos pos = player.getBlockPos();
            World world = player.getWorld();
            if (world.getBlockState(pos.offset(getDirectionFromVelocity(velocity))).isSolidBlock(world, pos)) {
                player.setVelocity(-velocity.x * 0.8, velocity.y, -velocity.z * 0.8);
                player.velocityModified = true;
            }
        }

        //debuff
        /*if (player.isOnGround()) {
            Vec3d vel = player.getVelocity();
            player.setVelocity(vel.multiply(0.6, 1.0, 0.6)); //slower horizontal movement
        }*/

        //no fall damage
        double currentgStrength = GravityChangerAPI.getBaseGravityStrength(player);
        if (Math.abs(currentgStrength-SLIME_GRAVITY)>=0.005){
            GravityChangerAPI.setBaseGravityStrength(player, SLIME_GRAVITY);
        }


        //cooldowns
        if (leapCooldown>0){
            leapCooldown--;
        }

        if (regainSizeTimer>0){
            regainSizeTimer--;

            //scaling up logic goes here.



            if (regainSizeTimer==0){
                ScaleData playerHeight = ScaleTypes.HEIGHT.getScaleData(player);
                ScaleData playerWidth = ScaleTypes.WIDTH.getScaleData(player);
                playerHeight.setBaseScale(BASE_SIZE);
                playerWidth.setBaseScale(BASE_SIZE);

                //playsound as well (like an egg pop or something like that to say that its done and everything can be reused again)
            }
        }

    }

    @Override
    public void handleAttack(PlayerEntity player, Entity victim) {

    }

    @Override
    public void handleKill(PlayerEntity player, Entity victim) {

    }

    @Override
    public void handleMine(PlayerEntity player) {

    }

    @Override
    public void handleBlockBreak(PlayerEntity player, BlockPos pos, BlockState state, BlockEntity entity) {

    }

    @Override
    public void handlePrimarySpell(PlayerEntity player) {
        IMagicDataSaver dataSaver = (IMagicDataSaver) player;
        MagicData magicData = dataSaver.getMagicData();
        int slimeLevel = magicData.getMagicLevel(SLIME_INDEX);
        if (slimeLevel>=1 && regainSizeTimer==0){

            ServerWorld world = (ServerWorld) player.getWorld();



            //spawning minion part
            SlimeEntity slime1 = new SlimeEntity(EntityType.SLIME, world);
            world.spawnEntity(slime1);

            //playsound + particles
            regainSizeTimer=REGAIN_SIZE_FROM_SPLIT_TIMER_MAX;
        }
    }

    @Override
    public void handleSecondarySpell(PlayerEntity player) {
        IMagicDataSaver dataSaver = (IMagicDataSaver) player;
        MagicData magicData = dataSaver.getMagicData();
        int slimeLevel = magicData.getMagicLevel(SLIME_INDEX);
        if (slimeLevel>=2 && player.isOnGround() && leapCooldown==0) {
            Vec3d look = player.getRotationVec(1.0F);
            double leapStrength = 2.5;

            Vec3d velocity = new Vec3d(look.x * leapStrength, 1.5, look.z * leapStrength);
            player.setVelocity(velocity);
            player.velocityModified = true;

            ScaleData playerHeight = ScaleTypes.HEIGHT.getScaleData(player);
            ScaleData playerWidth = ScaleTypes.WIDTH.getScaleData(player);
            playerHeight.setBaseScale(SPLIT_SIZE);
            playerWidth.setBaseScale(SPLIT_SIZE);


            // playsound+particles
            player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.BLOCK_SLIME_BLOCK_STEP, SoundCategory.PLAYERS, 1.0F, 1.0F);
            ((ServerWorld) player.getWorld()).spawnParticles(ParticleTypes.ITEM_SLIME, player.getX(), player.getY(), player.getZ(), 30, 0.5, 0.5, 0.5, 0.1);

            //cooldown
            leapCooldown=LEAP_DEFAULT_COOLDOWN;

        }
    }

    @Override
    public void handleTertiarySpell(PlayerEntity player) {

    }

    public static Direction getDirectionFromVelocity(Vec3d velocity) {
        double x = velocity.x;
        double z = velocity.z;

        if (Math.abs(x) > Math.abs(z)) {
            return x > 0 ? Direction.EAST : Direction.WEST;
        } else if (Math.abs(z) > 0) {
            return z > 0 ? Direction.SOUTH : Direction.NORTH;
        }

        return Direction.UP; //fallback in error ig
    }

}

