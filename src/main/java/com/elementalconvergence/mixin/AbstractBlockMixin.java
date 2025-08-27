package com.elementalconvergence.mixin;

import com.elementalconvergence.data.IMagicDataSaver;
import com.elementalconvergence.data.MagicData;
import com.elementalconvergence.data.PollenDrop;
import com.elementalconvergence.effect.InsectWeightEffect;
import com.elementalconvergence.effect.ModEffects;
import com.elementalconvergence.item.ModItems;
import com.elementalconvergence.magic.convergencehandlers.HoneyMagicHandler;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.elementalconvergence.magic.convergencehandlers.HoneyMagicHandler.HONEY_INDEX;
import static com.elementalconvergence.magic.handlers.LightMagicHandler.LIGHT_INDEX;

@Mixin(AbstractBlock.class)
public class AbstractBlockMixin {


    private static final VoxelShape voxelShape = Block.createCuboidShape((double)0.0F, (double)14.0F, (double)0.0F, (double)16.0F, (double)16.0F, (double)16.0F);
    private static final VoxelShape voxelShape2 = Block.createCuboidShape((double)0.0F, (double)0.0F, (double)0.0F, (double)2.0F, (double)16.0F, (double)2.0F);
    private static final VoxelShape voxelShape3 = Block.createCuboidShape((double)14.0F, (double)0.0F, (double)0.0F, (double)16.0F, (double)16.0F, (double)2.0F);
    private static final VoxelShape voxelShape4 = Block.createCuboidShape((double)0.0F, (double)0.0F, (double)14.0F, (double)2.0F, (double)16.0F, (double)16.0F);
    private static final VoxelShape voxelShape5 = Block.createCuboidShape((double)14.0F, (double)0.0F, (double)14.0F, (double)16.0F, (double)16.0F, (double)16.0F);
    private static final VoxelShape NORMAL_OUTLINE_SHAPE = VoxelShapes.union(voxelShape, new VoxelShape[]{voxelShape2, voxelShape3, voxelShape4, voxelShape5});
    private static final VoxelShape voxelShape6 = Block.createCuboidShape((double)0.0F, (double)0.0F, (double)0.0F, (double)2.0F, (double)2.0F, (double)16.0F);
    private static final VoxelShape voxelShape7 = Block.createCuboidShape((double)14.0F, (double)0.0F, (double)0.0F, (double)16.0F, (double)2.0F, (double)16.0F);
    private static final VoxelShape voxelShape8 = Block.createCuboidShape((double)0.0F, (double)0.0F, (double)14.0F, (double)16.0F, (double)2.0F, (double)16.0F);
    private static final VoxelShape voxelShape9 = Block.createCuboidShape((double)0.0F, (double)0.0F, (double)0.0F, (double)16.0F, (double)2.0F, (double)2.0F);
    private static final VoxelShape COLLISION_SHAPE = Block.createCuboidShape((double)0.0F, (double)0.0F, (double)0.0F, (double)16.0F, (double)2.0F, (double)16.0F);
    private static final VoxelShape BOTTOM_OUTLINE_SHAPE = VoxelShapes.union(COLLISION_SHAPE, new VoxelShape[]{NORMAL_OUTLINE_SHAPE, voxelShape7, voxelShape6, voxelShape9, voxelShape8});
    private static final VoxelShape OUTLINE_SHAPE = VoxelShapes.fullCube().offset((double)0.0F, (double)-1.0F, (double)0.0F);

    @Inject(method = "getCollisionShape", at = @At("HEAD"), cancellable = true)
    private void onGetCollisionShape(BlockState state, BlockView world, BlockPos pos,
                                     ShapeContext context, CallbackInfoReturnable<VoxelShape> cir) {
        //first check if  the player is quantum and stop everything else from happenning then.
        if (context instanceof EntityShapeContext entityContext) {
            Entity entity = entityContext.getEntity();
            if (entity instanceof PlayerEntity player) {

                //ONLY CHECK FOR QUANTUM PHASING
                if (player.hasStatusEffect(ModEffects.QUANTUM_PHASING)) {
                    if (!state.getBlock().equals(Blocks.SCAFFOLDING) && !state.getBlock().equals(Blocks.AIR) && !state.getBlock().equals(Blocks.WATER) && !state.getBlock().equals(Blocks.LAVA)) {
                        if (context.isAbove(VoxelShapes.fullCube(), pos, true) && !context.isDescending()) {
                            cir.setReturnValue(NORMAL_OUTLINE_SHAPE);
                        }
                        else{
                            //get block below
                            Block blockBelow = world.getBlockState(pos.add(0, -1, 0)).getBlock();
                            //allow to walk through normally (also drops down if sneaking)
                            cir.setReturnValue(context.isAbove(OUTLINE_SHAPE, pos, true) || blockBelow.equals(Blocks.AIR) ? COLLISION_SHAPE : VoxelShapes.empty());
                        }
                    }
                } else {

                    //first check if opaque to remove as many checks as possible
                    if (!state.isOpaque()) {
                        // Only continue if this is an entity context
                        if (player.hasStatusEffect(ModEffects.LIGHT_PHASING)) {
                            cir.setReturnValue(VoxelShapes.empty());
                        }
                    }

                    //check for insect weight part

                    //cant also have light phasing because if not it gives a weird effect with leaves and stuff since all transparent blocks
                    if (player.hasStatusEffect(ModEffects.INSECT_WEIGHT) && !player.hasStatusEffect(ModEffects.LIGHT_PHASING)) {
                        Block block = state.getBlock();

                        //block solid, FULL BLOCK SOLID always (so not following hitboxes all the time like for flowers
                        if (InsectWeightEffect.isPlantBlock(block)) {
                            // if on top of block
                            double playerY = player.getY();
                            double blockY = pos.getY() + 1.0;

                            //if sneaking you just drop the block if not you don't
                            if (Math.abs(playerY - blockY) < 0.5 && !player.isSneaking()) {
                                //full block pos then
                                cir.setReturnValue(VoxelShapes.fullCube());
                            }
                        }
                    }
                }
            }
        }

    }


    //also pollen part here
    @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
    private void onPitcherPlantInteraction(BlockState state, World world, BlockPos pos, PlayerEntity player,
                                           BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {

        if (world.isClient) {
            return;
        }

        if (state.isOf(Blocks.PITCHER_PLANT)) {
            if (player instanceof ServerPlayerEntity serverPlayer) {
                if (canPlayerHarvestPollen(serverPlayer)) {

                    //random pollen from rarity
                    PollenDrop pollenDrop = HoneyMagicHandler.getRandomPollenWithRarity(world);
                    ItemStack pollenStack = new ItemStack(pollenDrop.item(), 1);

                    //spawn item
                    ItemEntity itemEntity = new ItemEntity(world,
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            pollenStack);
                    itemEntity.setVelocity(0, 0.2, 0);
                    world.spawnEntity(itemEntity);

                    //break plant
                    if (world.getBlockState(pos.add(0, -1, 0)).isOf(Blocks.PITCHER_PLANT)) {
                        world.breakBlock(pos.add(0, -1, 0), false);
                    }else {
                        world.breakBlock(pos, false);
                    }

                    // particles
                    if (world instanceof ServerWorld serverWorld) {
                        HoneyMagicHandler.spawnRarityEffects(serverWorld, pos, pollenDrop.rarity());
                    }

                    //rarity sound
                    HoneyMagicHandler.playRaritySound(world, pos, pollenDrop.rarity());

                    cir.setReturnValue(ActionResult.SUCCESS);
                } else {
                    cir.setReturnValue(ActionResult.FAIL);
                }
            }
        }
    }

    private boolean canPlayerHarvestPollen(ServerPlayerEntity player) {
        IMagicDataSaver dataSaver = (IMagicDataSaver) player;
        MagicData magicData = dataSaver.getMagicData();
        int honeyLevel = magicData.getMagicLevel(HONEY_INDEX);
        return (honeyLevel>=3 && magicData.getSelectedMagic()==HONEY_INDEX);
    }

}
