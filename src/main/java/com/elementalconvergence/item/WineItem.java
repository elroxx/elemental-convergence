package com.elementalconvergence.item;

import com.elementalconvergence.data.IMagicDataSaver;
import com.elementalconvergence.data.MagicData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

import static com.elementalconvergence.magic.convergencehandlers.HolyMagicHandler.HOLY_INDEX;

public class WineItem extends Item {
    public WineItem(Settings settings) {
        super(settings.maxCount(1));
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (user instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) user;

            if (!world.isClient) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 20*60, 1)); // Strength 2 60 sec
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 20*60, 0)); // Resistance 1 60 seconds

                IMagicDataSaver dataSaver = (IMagicDataSaver) player;
                MagicData magicData = dataSaver.getMagicData();
                if (magicData.getSelectedMagic()!=HOLY_INDEX){
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 20*60, 1)); // Nausea 1 60 seconds
                }

            }

            player.getInventory().insertStack(new ItemStack(Items.GLASS_BOTTLE)); //leaving glass bottles
        }

        // removed
        stack.decrement(1);
        return stack;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.DRINK;
    }

    /*@Override
    public int getMaxUseTime(ItemStack stack) {
        return 32; // Same as a regular potion
    }*/

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        return ItemStack.EMPTY.equals(user.getStackInHand(hand)) ?
                TypedActionResult.pass(user.getStackInHand(hand)) :
                TypedActionResult.consume(user.getStackInHand(hand));
    }
}
