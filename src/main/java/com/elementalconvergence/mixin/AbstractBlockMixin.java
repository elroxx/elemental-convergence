package com.elementalconvergence.mixin;

import com.elementalconvergence.data.IMagicDataSaver;
import com.elementalconvergence.data.MagicData;
import com.elementalconvergence.effect.InsectWeightEffect;
import com.elementalconvergence.effect.ModEffects;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.elementalconvergence.magic.handlers.LightMagicHandler.LIGHT_INDEX;

@Mixin(AbstractBlock.class)
public class AbstractBlockMixin {

    @Inject(method = "getCollisionShape", at = @At("HEAD"), cancellable = true)
    private void onGetCollisionShape(BlockState state, BlockView world, BlockPos pos,
                                     ShapeContext context, CallbackInfoReturnable<VoxelShape> cir) {
        //first check if opaque to remove as many checks as possible
        if (!state.isOpaque()) {
            // Only proceed if this is an entity context
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

}
