package com.elementalconvergence.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraft.util.TypedActionResult;

public class TestItem extends Item {
    public TestItem(Settings settings) {
        super(settings);
    }

    // write this if the version is below 1.21.2:
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient()) {
            user.sendMessage(Text.of("test"));
        }
        return TypedActionResult.success(user.getStackInHand(hand));
    }

}
