package com.elementalconvergence.mixin;

import com.elementalconvergence.data.BlockCollisionUtils;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BambooBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BambooBlock.class)
public class BambooBlockMixin {
    @Inject(method = "getCollisionShape", at = @At("HEAD"), cancellable = true)
    private void onGetCollisionShape(BlockState state, BlockView world, BlockPos pos,
                                     ShapeContext context, CallbackInfoReturnable<VoxelShape> cir) {
        BlockCollisionUtils.handleCollisionShape(state, world, pos, context, cir);
    }
}