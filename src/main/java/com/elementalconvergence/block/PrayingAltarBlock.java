package com.elementalconvergence.block;

import com.elementalconvergence.data.IMagicDataSaver;
import com.elementalconvergence.data.MagicData;
import com.elementalconvergence.effect.ModEffects;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import static com.elementalconvergence.magic.convergencehandlers.HolyMagicHandler.HOLY_INDEX;

public class PrayingAltarBlock extends Block {
    //basic lectern shape
    private static final VoxelShape SHAPE = VoxelShapes.union(
            Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 2.0, 16.0),
            Block.createCuboidShape(4.0, 2.0, 4.0, 12.0, 14.0, 12.0),
            Block.createCuboidShape(0.0, 15.0, 0.0, 16.0, 15.0, 16.0),
            Block.createCuboidShape(1.0, 14.0, 1.0, 15.0, 15.0, 15.0)
    );

    public PrayingAltarBlock(Settings settings) {
        super(settings);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient) {
            IMagicDataSaver dataSaver = (IMagicDataSaver) player;
            MagicData magicData = dataSaver.getMagicData();
            int selectedMagic = magicData.getSelectedMagic();

            if (selectedMagic==HOLY_INDEX){
                //an hour and a half of PRAYER
                player.addStatusEffect(new StatusEffectInstance(ModEffects.PRAYER, 20 * 60*90 , 0, false, false, false));
                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_BELL_RESONATE, SoundCategory.MASTER, 1.0f, 2.0f);
            }
        }

        return ActionResult.SUCCESS;
    }
}
