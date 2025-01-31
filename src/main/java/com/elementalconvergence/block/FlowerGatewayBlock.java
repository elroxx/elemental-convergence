package com.elementalconvergence.block;

import com.elementalconvergence.item.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

import java.util.Collections;
import java.util.List;

public class FlowerGatewayBlock extends Block {
    public FlowerGatewayBlock(Settings settings) {
        super(settings);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        // match model
        return VoxelShapes.union(
                // Base stump
                Block.createCuboidShape(0, 0, 0, 16, 6, 16),
                // Inner rim
                Block.createCuboidShape(2, 6, 2, 14, 8, 14),
                // Bark details
                Block.createCuboidShape(1, 0, 1, 3, 7, 3),
                Block.createCuboidShape(13, 0, 13, 15, 7, 15)
        );
    }

    @Override
    public List<ItemStack> getDroppedStacks(BlockState state, LootContextParameterSet.Builder builder) {
        return Collections.singletonList(new ItemStack(ModItems.FLOWER_GATEWAY_ITEM));
    }
}