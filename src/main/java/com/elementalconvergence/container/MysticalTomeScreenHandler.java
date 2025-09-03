package com.elementalconvergence.container;

import com.elementalconvergence.ElementalConvergence;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;

import java.util.List;
import java.util.Map;

public class MysticalTomeScreenHandler extends ScreenHandler {
    private final SimpleInventory inventory;
    private final List<RegistryKey<Enchantment>> enchantments;
    private final List<Integer> enchantmentLevels;
    private final PlayerEntity player;

    public MysticalTomeScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(3), List.of(), List.of());
    }

    public MysticalTomeScreenHandler(int syncId, PlayerInventory playerInventory, SimpleInventory inventory,
                                     List<RegistryKey<Enchantment>> enchantments, List<Integer> enchantmentLevels) {
        super(ElementalConvergence.MYSTICAL_TOME_SCREEN_HANDLER, syncId);
        this.inventory = inventory;
        this.enchantments = enchantments;
        this.enchantmentLevels = enchantmentLevels;
        this.player = playerInventory.player;

        // Initialize the inventory with enchanted books
        initializeBooks();

        // Add tome inventory slots (3 slots in a row)
        for (int i = 0; i < 3; i++) {
            this.addSlot(new EnchantedBookSlot(inventory, i, 62 + i * 18, 35));
        }

        // Add player inventory slots
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        // Add player hotbar slots
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    private void initializeBooks() {
        for (int i = 0; i < 3 && i < enchantments.size(); i++) {
            ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
            book.addEnchantment(player.getRegistryManager().getWrapperOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(enchantments.get(i)), enchantmentLevels.get(i));
            inventory.setStack(i, book);
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
                // Moving from tome inventory to player inventory
                if (!this.insertItem(stackInSlot, 3, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
                // Trigger the refill logic
                if (slot instanceof EnchantedBookSlot bookSlot) {
                    bookSlot.onTakeItem(player, stackInSlot);
                }
            } else {
                // Moving from player inventory to tome inventory - not allowed
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
            return false; // Players cannot insert items into these slots
        }

        @Override
        public void onTakeItem(PlayerEntity player, ItemStack stack) {
            super.onTakeItem(player, stack);

            // Check if player has enough XP
            if (player.experienceLevel >= 3) {
                // Deduct 3 levels
                player.addExperienceLevels(-3);

                // Refill the slot with the same enchanted book
                if (this.getIndex() < enchantments.size()) {
                    ItemStack newBook = new ItemStack(Items.ENCHANTED_BOOK);
                    newBook.addEnchantment(player.getRegistryManager().getWrapperOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(enchantments.get(this.getIndex())), enchantmentLevels.get(this.getIndex()));
                    this.inventory.setStack(this.getIndex(), newBook);
                }

                // Play sound
                player.getWorld().playSound(null, player.getBlockPos(),
                        SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS,
                        1.0F, 1.0F);
            } else {
                // Not enough XP - return the item to the slot
                if (this.getIndex() < enchantments.size()) {
                    ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
                    book.addEnchantment(player.getRegistryManager().getWrapperOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(enchantments.get(this.getIndex())), enchantmentLevels.get(this.getIndex()));
                    this.inventory.setStack(this.getIndex(), book);
                }

                // Optionally send a message to the player
                if (player instanceof ServerPlayerEntity serverPlayer) {
                    serverPlayer.sendMessage(Text.translatable("message.yourmod.insufficient_xp"), true);
                }
            }
        }
    }

    public static class Factory implements NamedScreenHandlerFactory {
        private final List<RegistryKey<Enchantment>> enchantments;
        private final List<Integer> enchantmentLevels;

        public Factory(List<RegistryKey<Enchantment>> enchantments, List<Integer> enchantmentLevels) {
            this.enchantments = enchantments;
            this.enchantmentLevels = enchantmentLevels;
        }

        @Override
        public Text getDisplayName() {
            return Text.translatable("container.yourmod.mystical_tome");
        }

        @Override
        public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
            return new MysticalTomeScreenHandler(syncId, inv, new SimpleInventory(3), enchantments, enchantmentLevels);
        }
    }
}
