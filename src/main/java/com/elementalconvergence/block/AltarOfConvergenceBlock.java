package com.elementalconvergence.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.BlockRenderType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class AltarOfConvergenceBlock extends Block {
    // Create VoxelShapes for each component based on the model
    private static final VoxelShape BASE = Block.createCuboidShape(0, 0, 0, 16, 8, 16);

    private static final VoxelShape PILLAR_1 = Block.createCuboidShape(0, 8, 13, 3, 14, 16);
    private static final VoxelShape PILLAR_2 = Block.createCuboidShape(13, 8, 0, 16, 14, 3);
    private static final VoxelShape PILLAR_3 = Block.createCuboidShape(13, 8, 13, 16, 14, 16);
    private static final VoxelShape PILLAR_4 = Block.createCuboidShape(0, 8, 0, 3, 14, 3);

    private static final VoxelShape TIP_1 = Block.createCuboidShape(1, 14, 1, 2, 15, 2);
    private static final VoxelShape TIP_2 = Block.createCuboidShape(14, 14, 14, 15, 15, 15);
    private static final VoxelShape TIP_3 = Block.createCuboidShape(14, 14, 1, 15, 15, 2);
    private static final VoxelShape TIP_4 = Block.createCuboidShape(1, 14, 14, 2, 15, 15);

    private static final VoxelShape CORE = Block.createCuboidShape(6, 8, 6, 10, 12, 10);

    // Combine all shapes into one final shape
    private static final VoxelShape SHAPE = VoxelShapes.union(
            BASE,
            PILLAR_1, PILLAR_2, PILLAR_3, PILLAR_4,
            TIP_1, TIP_2, TIP_3, TIP_4,
            CORE
    );

    public AltarOfConvergenceBlock(Settings settings) {
        super(settings);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
        return 1.0F; // Disable AO for this block
    }

}