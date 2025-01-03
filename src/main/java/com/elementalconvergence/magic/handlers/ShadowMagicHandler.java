package com.elementalconvergence.magic.handlers;

import com.elementalconvergence.ElementalConvergence;
import com.elementalconvergence.data.IMagicDataSaver;
import com.elementalconvergence.data.MagicData;
import com.elementalconvergence.magic.IMagicHandler;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.elementalconvergence.ElementalConvergence.getLookingAtBlockPos;

public class ShadowMagicHandler implements IMagicHandler {
    public static final int SHADOW_INDEX=4;
    private static final int LIGHT_THRESHOLD=6;
    private static final int DEFAULT_INVIS_COOLDOWN=60;
    private int invisCooldown=0;
    private static final int DEFAULT_LIGHT_UPDATE_COOLDOWN=10;
    private int lightUpdateCooldown=0;
    private static final int INVIS_DURATION=219; //almost 11 seconds so that it stays 10 a long time
    private static final int DEFAULT_SHADOWTP_COOLDOWN=40; //2 seconds
    private int shadowTPCooldown = 0;
    private static final int SHADOWTP_MAXRANGE=50;
    private static final float CHANCE_OF_STEAL=0.01f;

    @Override
    public void handleRightClick(PlayerEntity player) {

    }

    @Override
    public void handlePassive(PlayerEntity player) {
        //Checking light level of blocks
        if (lightUpdateCooldown==0){
            lightUpdateCooldown=DEFAULT_LIGHT_UPDATE_COOLDOWN;

            BlockPos playerPosition = player.getBlockPos();
            int lightLevel = player.getWorld().getLightLevel(playerPosition);
            if (lightLevel<=LIGHT_THRESHOLD){
                if (invisCooldown==0) {
                    invisCooldown = DEFAULT_INVIS_COOLDOWN;
                    StatusEffectInstance invisibility = new StatusEffectInstance(StatusEffects.INVISIBILITY,
                            INVIS_DURATION, //DURATON
                            0, //AMPLIFIER
                            true,
                            false,
                            false
                    );
                    player.addStatusEffect(invisibility);
                    player.setInvisible(true);
                }
                if (player.getMaxHealth() != 20.0F) {
                    player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(20.0F);
                }

            }
            else if (lightLevel<=LIGHT_THRESHOLD+3){
                player.setInvisible(false);
                player.removeStatusEffect(StatusEffects.INVISIBILITY);
                invisCooldown=0; //So that the invisibility is automatically reapplied next time we enter the darkness

                if (player.getMaxHealth() != 20.0F) {
                    player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(20.0F);
                }
            }
            else{
                player.setInvisible(false);
                player.removeStatusEffect(StatusEffects.INVISIBILITY);
                invisCooldown=0; //So that the invisibility is automatically reapplied next time we enter the darkness

                if (player.getMaxHealth() != 10.0F) {
                    player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(10.0F);
                }
            }
        }

        //COOLDOWN UPDATES:
        if (lightUpdateCooldown>0){
            lightUpdateCooldown--;
        }
        if (invisCooldown>0){
            invisCooldown--;
        }
        if (shadowTPCooldown>0){
            shadowTPCooldown--;
        }
    }

    @Override
    public void handleAttack(PlayerEntity player, Entity victim) {
        if (victim instanceof LivingEntity){
            StatusEffectInstance darkness = new StatusEffectInstance(StatusEffects.DARKNESS, 15, 0, false, false, false);
            ((LivingEntity) victim).addStatusEffect(darkness);

            //Drop a random item on the ground if hit (only 25% chance of happenning)
            if (victim instanceof PlayerEntity){
                PlayerInventory inventory = ((PlayerEntity) victim).getInventory();
                List<ItemStack> nonEmptySlots = new ArrayList<>();
                for (int i=0; i< inventory.size(); i++){
                    ItemStack stack = inventory.getStack(i);
                    if (!stack.isEmpty()){
                        nonEmptySlots.add(stack);
                    }
                }

                if (!nonEmptySlots.isEmpty()){
                    Random random = new Random();

                    if (random.nextFloat()<CHANCE_OF_STEAL*nonEmptySlots.size()){
                        ItemStack selectedStack = nonEmptySlots.get(random.nextInt(nonEmptySlots.size()));

                        int slot = inventory.getSlotWithStack(selectedStack);

                        //So that we can dorp it later
                        ItemStack stackToDrop = selectedStack.copy();

                        inventory.removeStack(slot);

                        ((PlayerEntity) victim).dropItem(stackToDrop, false, false);
                    }

                }
            }
        }
    }

    @Override
    public void handlePrimarySpell(PlayerEntity player) {
        IMagicDataSaver dataSaver = (IMagicDataSaver) player;
        MagicData magicData = dataSaver.getMagicData();
        int shadowLevel = magicData.getMagicLevel(SHADOW_INDEX);
        if (shadowLevel>=2) {
            World world = player.getWorld();
            BlockPos playerPos = player.getBlockPos();
            BlockPos blockHit = getLookingAtBlockPos(player, SHADOWTP_MAXRANGE, true);
            if (blockHit == null) {
                return;
            }
            System.out.println(blockHit);
            BlockState blockHitState = world.getBlockState(blockHit);
            if (isShadowTpAble(playerPos, world) && isShadowTpAble(blockHit, world) && shadowTPCooldown == 0) {
                System.out.println("can tp??");
                //player.setPosition(blockHit.getX()+0.5,blockHit.getY(), blockHit.getZ()+0.5);
                //player.refreshPositionAndAngles(blockHit.getX()+0.5,blockHit.getY(), blockHit.getZ()+0.5, player.getYaw(), player.getPitch());
                player.teleport(blockHit.getX() + 0.5, blockHit.getY(), blockHit.getZ() + 0.5, false);
                player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BLOCK_CANDLE_EXTINGUISH, SoundCategory.PLAYERS, 2.0F, 1.0F);
                shadowTPCooldown = DEFAULT_SHADOWTP_COOLDOWN;
            }
        }

    }

    @Override
    public void handleSecondarySpell(PlayerEntity player) {

    }

    @Override
    public void handleTertiarySpell(PlayerEntity player) {

    }

    public static boolean isShadowTpAble(BlockPos pos, World world){
        int lightlvl = world.getLightLevel(pos);
        if (lightlvl<=LIGHT_THRESHOLD){
            return true;
        }
        else{
            return false;
        }
    }
}
