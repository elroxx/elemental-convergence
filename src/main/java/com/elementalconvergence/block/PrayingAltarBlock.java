package com.elementalconvergence.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

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
}
