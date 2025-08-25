package com.elementalconvergence.block;

import com.elementalconvergence.data.IMagicDataSaver;
import com.elementalconvergence.data.MagicData;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.BedPart;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameterSet;
import java.util.ArrayList;
import java.util.List;

import static com.elementalconvergence.magic.convergencehandlers.BloodMagicHandler.BLOOD_INDEX;

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
        return null;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient || player instanceof ClientPlayerEntity) {
            return ActionResult.CONSUME;
        }

        //CHECK IF PLAYER IS ACTUALLY A VAMPIRE LVL 1
        IMagicDataSaver dataSaver = (IMagicDataSaver) player;
        MagicData magicData = dataSaver.getMagicData();
        int bloodLevel = magicData.getMagicLevel(BLOOD_INDEX);

        if (bloodLevel>=1 && magicData.getSelectedMagic()==BLOOD_INDEX) {


            //check if day time (so only works if dayTime
            if (!world.isDay()) {
                player.sendMessage(Text.translatable("You can only sleep during the day"), true);
                return ActionResult.CONSUME;
            }

            //check for monsters close
            if (!this.isSafeToSleep(world, pos, player)) {
                player.sendMessage(Text.translatable("You cannot sleep, there are monsters nearby"), true);
                return ActionResult.CONSUME;
            }

            //sleep through day
            if (this.sleepInCoffin((ServerWorld) world, pos, (ServerPlayerEntity) player)) {
                world.playSound(null, pos, SoundEvents.BLOCK_CHEST_OPEN, SoundCategory.BLOCKS, 0.8f, 0.5f);
                return ActionResult.SUCCESS;
            }
        }

        return ActionResult.CONSUME;
    }

    private boolean sleepInCoffin(ServerWorld world, BlockPos pos, ServerPlayerEntity player) {
        // set time to night

        world.setTimeOfDay(13000); //evening

        //Say they sleep
        player.sendMessage(Text.literal("You sleep through the day in your coffin..."), true);

        //set spawnpoint as if it was a bed
        player.setSpawnPoint(world.getRegistryKey(), pos, 0.0f, false, true);

        return true;
    }

    private boolean isSafeToSleep(World world, BlockPos pos, PlayerEntity player) {
        //check for monsters in a 8x8x8 area around the coffin
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

    @Override
    public List<ItemStack> getDroppedStacks(BlockState state, LootContextParameterSet.Builder builder) {
        List<ItemStack> drops = new ArrayList<>();

        //only drop from bottom
        if (state.get(PART) == BedPart.FOOT) {
            drops.add(new ItemStack(this));
        }

        return drops;
    }
}
