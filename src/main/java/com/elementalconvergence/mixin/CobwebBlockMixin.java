package com.elementalconvergence.mixin;

import com.elementalconvergence.data.BlockCollisionUtils;
import com.elementalconvergence.effect.ModEffects;
import net.minecraft.block.BambooBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.CobwebBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CobwebBlock.class)
public class CobwebBlockMixin {
    @Inject(method = "onEntityCollision", at = @At("HEAD"), cancellable = true)
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (entity instanceof LivingEntity livingEntity && livingEntity.hasStatusEffect(ModEffects.ARACHNID)){
            ci.cancel();
        }
    }
}