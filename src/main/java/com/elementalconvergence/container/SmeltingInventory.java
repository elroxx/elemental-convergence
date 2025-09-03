package com.elementalconvergence.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;

public class SmeltingInventory extends SimpleInventory implements RecipeInputInventory {

    public SmeltingInventory() {
        super(1); // One slot for smelting input
    }

    public SmeltingInventory(ItemStack stack) {
        super(1); // One slot for smelting input
        this.setStack(0, stack);
    }

    @Override
    public int getWidth() {
        return 1;
    }

    @Override
    public int getHeight() {
        return 1;
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return false; // This is just a temporary inventory for recipe matching
    }
}