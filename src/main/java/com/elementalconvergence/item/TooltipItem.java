package com.elementalconvergence.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;

import java.util.List;

public class TooltipItem extends Item {

    private String tooltipKey;

    public TooltipItem(Settings settings, String tooltipKey) {
        super(settings);
        this.tooltipKey=tooltipKey;
    }

    @Override
    public void appendTooltip(ItemStack itemStack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable(tooltipKey)); //"item.tutorial.gravity_shard.tooltip"
    }
}
