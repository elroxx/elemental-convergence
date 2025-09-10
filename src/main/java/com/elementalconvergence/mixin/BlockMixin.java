package com.elementalconvergence.mixin;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.elementalconvergence.container.SmeltingInventory;
import com.elementalconvergence.effect.ModEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;

@Mixin(Block.class)
public class BlockMixin {

    private static final SmeltingInventory INVENTORY = new SmeltingInventory();

    @Inject(at = @At("RETURN"), cancellable = true, method = "getDroppedStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;)Ljava/util/List;")
    private static void getDrops(BlockState state, ServerWorld world, BlockPos pos,
            /* @Nullable */ BlockEntity blockEntity, /* @Nullable */ Entity entity, ItemStack stack,
                                 CallbackInfoReturnable<List<ItemStack>> cir) {

        // Make sure a player broke the block
        if (entity == null || !(entity instanceof PlayerEntity)) {
            return;
        }

        final PlayerEntity player = (PlayerEntity) entity;
        final ItemStack tool = player.getMainHandStack();

        // verify if tool is good for state
        if (!tool.isSuitableFor(state)) {
            return;
        }

        if (!player.hasStatusEffect(ModEffects.FURNACE)){
            return;
        }

        // get the supposed drops
        final List<ItemStack> drops = cir.getReturnValue();

        // get new drops now
        final List<ItemStack> newDrops = drops
                .stream()
                .map(drop -> getFurnaceOutput(drop, world))
                .flatMap(Optional::stream)
                .collect(Collectors.toList());

        if (!newDrops.isEmpty()) {
            cir.setReturnValue(newDrops);
        }
    }

    private static Optional<ItemStack> getFurnaceOutput(ItemStack stack, ServerWorld world) {
        SingleStackRecipeInput recipeInput = new SingleStackRecipeInput(stack);

        return world.getRecipeManager()
                .getFirstMatch(RecipeType.SMELTING, recipeInput, world)
                .map(recipeEntry -> recipeEntry.value().getResult(world.getRegistryManager()).copy());
    }



    @Inject(method = "onEntityLand", at = @At("HEAD"), cancellable = true)
    public void onEntityLand(BlockView blockView, Entity entity, CallbackInfo callbackInfo) {
        if(entity instanceof LivingEntity livingEntity && livingEntity.getVelocity().y < -0.23 && !livingEntity.isSneaking()) {
            if( livingEntity.hasStatusEffect(ModEffects.BOUNCY)) {
                System.out.println(livingEntity.getVelocity().y);
                livingEntity.setVelocity(livingEntity.getVelocity().multiply(1F, -0.9,1F));
                livingEntity.velocityModified=true;
                livingEntity.getWorld().playSound(null, livingEntity.getBlockPos(), SoundEvents.BLOCK_SLIME_BLOCK_FALL, SoundCategory.PLAYERS, 1.0F, 0.5F);
                callbackInfo.cancel();
                return;
            }
        }
    }

}

