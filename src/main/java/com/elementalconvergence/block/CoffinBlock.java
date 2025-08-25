package com.elementalconvergence.block;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.BedPart;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class CoffinBlock extends BedBlock {

    private static final VoxelShape COFFIN_HEAD_SHAPE = VoxelShapes.union(
            // Floor
            Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 2.0, 16.0),
            // North wall (closed end)
            Block.createCuboidShape(0.0, 2.0, 0.0, 16.0, 8.0, 2.0),
            // East wall
            Block.createCuboidShape(14.0, 2.0, 0.0, 16.0, 8.0, 16.0),
            // West wall
            Block.createCuboidShape(0.0, 2.0, 0.0, 2.0, 8.0, 16.0)
    );

    private static final VoxelShape COFFIN_FOOT_SHAPE = VoxelShapes.union(
            // Floor
            Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 2.0, 16.0),
            // South wall (closed end)
            Block.createCuboidShape(0.0, 2.0, 14.0, 16.0, 8.0, 16.0),
            // East wall
            Block.createCuboidShape(14.0, 2.0, 0.0, 16.0, 8.0, 16.0),
            // West wall
            Block.createCuboidShape(0.0, 2.0, 0.0, 2.0, 8.0, 16.0)
    );

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return state.get(PART) == BedPart.HEAD ? COFFIN_HEAD_SHAPE : COFFIN_FOOT_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return state.get(PART) == BedPart.HEAD ? COFFIN_HEAD_SHAPE : COFFIN_FOOT_SHAPE;
    }

    public CoffinBlock(Settings settings) {
        super(null, settings); // null for dye color
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        // Don't create a block entity - we don't need one for the coffin
        return null;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.CONSUME;
        }

        //check if day time
        if (!world.isDay()) {
            player.sendMessage(Text.translatable("block.minecraft.bed.no_sleep.night"), true);
            return ActionResult.CONSUME;
        }

        //check for monsters close
        if (!this.isSafeToSleep(world, pos, player)) {
            player.sendMessage(Text.translatable("block.minecraft.bed.not_safe"), true);
            return ActionResult.CONSUME;
        }

        //sleep through day
        if (this.sleepInCoffin((ServerWorld) world, pos, (ServerPlayerEntity) player)) {
            return ActionResult.SUCCESS;
        }

        return ActionResult.CONSUME;
    }

    private boolean sleepInCoffin(ServerWorld world, BlockPos pos, ServerPlayerEntity player) {
        // Set time to night (opposite of normal bed)

        world.setTimeOfDay(13000); // Evening time

        // Give the player the sleep effect briefly
        player.sendMessage(Text.literal("You sleep through the day in your coffin..."), true);

        // Spawn point is still set like a normal bed
        player.setSpawnPoint(world.getRegistryKey(), pos, 0.0f, false, true);

        return true;
    }

    private boolean isSafeToSleep(World world, BlockPos pos, PlayerEntity player) {
        // Check for monsters in a 8x8x8 area around the coffin
        return world.getEntitiesByClass(
                net.minecraft.entity.mob.HostileEntity.class,
                new net.minecraft.util.math.Box(pos).expand(8.0, 4.0, 8.0),
                entity -> true
        ).isEmpty();
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }
}
