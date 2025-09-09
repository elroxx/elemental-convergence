package com.elementalconvergence.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FrostedIceBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class BrittleObsidianBlock extends FrostedIceBlock {
    public BrittleObsidianBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected void melt(BlockState state, World world, BlockPos pos){
        world.setBlockState(pos, Blocks.LAVA.getDefaultState());
        world.updateNeighbor(pos, Blocks.LAVA, pos);
    }
}
