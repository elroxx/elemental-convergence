package com.elementalconvergence.item;

import com.elementalconvergence.entity.LashingPotatoHookEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class LashingPotatoItem extends Item {
    public LashingPotatoItem(Item.Settings settings) {
        super(settings);
    }

    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        LashingPotatoHookEntity existingHook = user.lashingPotatoHook;
        if (existingHook != null) {
            retractHook(world, user, existingHook);
        } else {
            if (!world.isClient) {
                itemStack.damage(1, user, LivingEntity.getSlotForHand(hand));
            }

            this.throwHook(world, user);
        }

        return TypedActionResult.success(itemStack, world.isClient);
    }

    private void throwHook(World world, PlayerEntity playerEntity) {
        if (!world.isClient) {
            world.spawnEntity(new LashingPotatoHookEntity(world, playerEntity));
        }

        playerEntity.incrementStat(Stats.USED.getOrCreateStat(this));
        world.playSound((PlayerEntity)null, playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), SoundEvents.ENTITY_FISHING_BOBBER_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
        playerEntity.emitGameEvent(GameEvent.ITEM_INTERACT_START);
    }

    private static void retractHook(World world, PlayerEntity playerEntity, LashingPotatoHookEntity hookEntity) {
        if (!world.isClient()) {
            hookEntity.discard();
            playerEntity.lashingPotatoHook = null;
        }

        world.playSound((PlayerEntity)null, playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), SoundEvents.ENTITY_FISHING_BOBBER_RETRIEVE, SoundCategory.NEUTRAL, 1.0F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
        playerEntity.emitGameEvent(GameEvent.ITEM_INTERACT_FINISH);
    }
}
