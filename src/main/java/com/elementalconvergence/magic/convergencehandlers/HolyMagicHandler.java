package com.elementalconvergence.magic.convergencehandlers;

import com.elementalconvergence.item.ModItems;
import com.elementalconvergence.magic.IMagicHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.component.type.PotionContentsComponent;

import java.util.*;

import static com.elementalconvergence.ElementalConvergence.BASE_MAGIC_ID;

public class HolyMagicHandler implements IMagicHandler {
    public static final int HOLY_INDEX= (BASE_MAGIC_ID.length-1)+4;


    @Override
    public void handleItemRightClick(PlayerEntity player) {
        ItemStack mainHand = player.getMainHandStack();
        ItemStack offHand = player.getOffHandStack();

        //lvl 1 ability
        if (mainHand.getItem().equals(Items.POTION) && !mainHand.get(DataComponentTypes.POTION_CONTENTS).hasEffects()){
            player.setStackInHand(Hand.MAIN_HAND, new ItemStack(ModItems.WINE, 1));

            player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                    net.minecraft.sound.SoundEvents.BLOCK_BREWING_STAND_BREW,
                    net.minecraft.sound.SoundCategory.PLAYERS, 1.0f, 1.0f);
        }
        if (offHand.getItem().equals(Items.POTION) && !offHand.get(DataComponentTypes.POTION_CONTENTS).hasEffects()){
            player.setStackInHand(Hand.OFF_HAND, new ItemStack(ModItems.WINE, 1));

            player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                    net.minecraft.sound.SoundEvents.BLOCK_BREWING_STAND_BREW,
                    net.minecraft.sound.SoundCategory.PLAYERS, 1.0f, 1.0f);
        }
    }

    @Override
    public void handleEntityRightClick(PlayerEntity player, Entity targetEntity) {

    }

    @Override
    public void handlePassive(PlayerEntity player) {

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
    }

    @Override
    public void handleSecondarySpell(PlayerEntity player) {

    }

    @Override
    public void handleTertiarySpell(PlayerEntity player) {

    }


}
