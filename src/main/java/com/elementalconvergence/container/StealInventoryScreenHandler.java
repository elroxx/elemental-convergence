package com.elementalconvergence.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;

public class StealInventoryScreenHandler extends ScreenHandler {
    private final PlayerInventory sourceInventory;
    private final PlayerInventory targetInventory;

    public StealInventoryScreenHandler(int syncId, PlayerInventory sourceInventory, PlayerInventory targetInventory) {
        super(ScreenHandlerType.GENERIC_9X4, syncId);
        this.sourceInventory = sourceInventory;
        this.targetInventory = targetInventory;

        // Add target player's inventory slots
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(targetInventory, col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }

        // Add source player's inventory slots
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(sourceInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Add source player's hotbar slots
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(sourceInventory, col, 8 + col * 18, 142));
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasStack()) {
            ItemStack slotStack = slot.getStack();
            itemStack = slotStack.copy();

            if (index < 36) {
                if (!this.insertItem(slotStack, 36, this.slots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(slotStack, 0, 36, false)) {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return itemStack;
    }
}
