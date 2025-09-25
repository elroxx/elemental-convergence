package com.elementalconvergence.item;

import com.elementalconvergence.entity.SpiderGrapplingHookEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class SpiderGrapplingHookItem extends Item {
    public SpiderGrapplingHookItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);

        if (!world.isClient) {
            // Check if player already has a grappling hook out
            SpiderGrapplingHookEntity existingHook = SpiderGrapplingHookEntity.getPlayerHook(user);
            if (existingHook != null) {
                // Detach and remove existing hook
                existingHook.detach();
                existingHook.discard();
            } else {
                // Launch new grappling hook
                SpiderGrapplingHookEntity grapplingHook = new SpiderGrapplingHookEntity(user, world);
                world.spawnEntity(grapplingHook);
            }
        }

        user.incrementStat(Stats.USED.getOrCreateStat(this));
        world.playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.ENTITY_SPIDER_STEP, SoundCategory.NEUTRAL, 0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));


        return TypedActionResult.success(itemStack, world.isClient());
    }
}