package com.elementalconvergence.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import com.elementalconvergence.ElementalConvergence;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import javax.swing.text.Element;

public class InfoScrollItem extends Item {
    private final int magicIndex;
    private final String magicString;

    public InfoScrollItem(Settings settings, int magicIndex) {
        super(settings);
        this.magicIndex = magicIndex;
        this.magicString = ElementalConvergence.BASE_MAGIC_DISPLAY[magicIndex];
    }

    public String getMagicType() {
        return magicString;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient()) {
            user.sendMessage(Text.of("test"));
        }
        return TypedActionResult.success(user.getStackInHand(hand));
    }

}