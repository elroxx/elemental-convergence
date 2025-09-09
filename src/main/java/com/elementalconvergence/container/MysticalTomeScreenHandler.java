package com.elementalconvergence.container;

import com.elementalconvergence.ElementalConvergence;
import com.elementalconvergence.enchantment.ModEnchantments;
import com.elementalconvergence.item.ModItems;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import java.util.List;

public class MysticalTomeScreenHandler extends ScreenHandler {
    private final SimpleInventory inventory;
    private final List<RegistryKey<Enchantment>> enchantments;
    private final List<Integer> enchantmentLevels;
    private final PlayerEntity player;

    public MysticalTomeScreenHandler(int syncId, PlayerInventory playerInventory) {
        super(ElementalConvergence.MYSTICAL_TOME_SCREEN_HANDLER, syncId);
        this.inventory = new SimpleInventory(3);
        this.player = playerInventory.player;

        // Determine which tome is held
        ItemStack stack = playerInventory.player.getMainHandStack();
        if (stack.getItem() == ModItems.MYSTICAL_CHAPTER_1) {
            this.enchantments = ModItems.getEnchantList(1);
            this.enchantmentLevels = ModItems.getEnchantLevelList(1);
        } else {
            this.enchantments = ModItems.getEnchantList(2);
            this.enchantmentLevels = ModItems.getEnchantLevelList(2);
        }

        initializeBooks();
        addSlots(playerInventory);
    }

    private void initializeBooks() {
        for (int i = 0; i < 3 && i < enchantments.size(); i++) {
            ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
            book.addEnchantment(
                    player.getRegistryManager().getWrapperOrThrow(RegistryKeys.ENCHANTMENT)
                            .getOrThrow(enchantments.get(i)),
                    enchantmentLevels.get(i)
            );
            inventory.setStack(i, book);
        }
    }

    private void addSlots(PlayerInventory playerInventory) {
        // tome book slots
        for (int i = 0; i < 3; i++) {
            this.addSlot(new EnchantedBookSlot(inventory, i, 62 + i * 18, 35));
        }

        // player inventory
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        // hotbar
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);

        if (slot != null && slot.hasStack()) {
            ItemStack stackInSlot = slot.getStack();
            stack = stackInSlot.copy();

            if (slotIndex < 3) {
                if (!this.insertItem(stackInSlot, 3, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
                if (slot instanceof EnchantedBookSlot bookSlot) {
                    bookSlot.onTakeItem(player, stackInSlot);
                }
            } else {
                return ItemStack.EMPTY;
            }

            if (stackInSlot.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return stack;
    }

    private class EnchantedBookSlot extends Slot {
        public EnchantedBookSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return false;
        }

        @Override
        public void onTakeItem(PlayerEntity player, ItemStack stack) {
            super.onTakeItem(player, stack);

            if (player.experienceLevel >= 3) {
                player.addExperienceLevels(-3);

                if (this.getIndex() < enchantments.size()) {
                    ItemStack newBook = new ItemStack(Items.ENCHANTED_BOOK);
                    newBook.addEnchantment(
                            player.getRegistryManager().getWrapperOrThrow(RegistryKeys.ENCHANTMENT)
                                    .getOrThrow(enchantments.get(this.getIndex())),
                            enchantmentLevels.get(this.getIndex())
                    );
                    inventory.setStack(this.getIndex(), newBook);
                }

                player.getWorld().playSound(null, player.getBlockPos(),
                        SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS,
                        1.0F, 1.0F);
            } else {
                if (this.getIndex() < enchantments.size()) {
                    ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
                    book.addEnchantment(
                            player.getRegistryManager().getWrapperOrThrow(RegistryKeys.ENCHANTMENT)
                                    .getOrThrow(enchantments.get(this.getIndex())),
                            enchantmentLevels.get(this.getIndex())
                    );
                    inventory.setStack(this.getIndex(), book);
                }
            }
        }
    }

    public static class Factory implements NamedScreenHandlerFactory {
        public static final Factory INSTANCE = new Factory();

        private Factory() {}

        @Override
        public Text getDisplayName() {
            return Text.literal("");
        }

        @Override
        public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
            return new MysticalTomeScreenHandler(syncId, inv);
        }
    }
}
