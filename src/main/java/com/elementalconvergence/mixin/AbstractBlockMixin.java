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

    @Inject(method = "getCollisionShape", at = @At("HEAD"), cancellable = true)
    private void onGetCollisionShape(BlockState state, BlockView world, BlockPos pos,
                                     ShapeContext context, CallbackInfoReturnable<VoxelShape> cir) {
        //first check if opaque to remove as many checks as possible
        if (!state.isOpaque()) {
            // Only continue if this is an entity context
            if (context instanceof EntityShapeContext entityContext) {
                Entity entity = entityContext.getEntity();
                if (entity instanceof PlayerEntity player) {
                    if (player.hasStatusEffect(ModEffects.LIGHT_PHASING)) {
                        cir.setReturnValue(VoxelShapes.empty());
                    }
                }
            }
        }
        //check for insect weight part
        if (context instanceof EntityShapeContext entityContext) {
            if (entityContext.getEntity() instanceof PlayerEntity player) {
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
