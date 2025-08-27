package com.elementalconvergence.item;

import com.elementalconvergence.ElementalConvergence;
import com.elementalconvergence.enchantment.ModEnchantments;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class SchrodingerCatItem extends Item {
    private static final String PREVIEW_MODE_KEY = "loot_preview_mode";
    private static final String REGENERATION_USED_KEY = "regeneration_used";
    private static final String CHEST_UUID_KEY = "chest_uuid";

    public SchrodingerCatItem(Settings settings) {
        super(settings);
    }

    public ActionResult handleRightClick(PlayerEntity player, ChestBlockEntity chest, BlockPos pos, ServerWorld world) {
        // Check if chest has loot table and hasn't been opened
        if (chest.getLootTable() == null) {
            player.sendMessage(Text.literal("§cThis chest doesn't have a loot table!"), true);
            return ActionResult.FAIL;
        }

        // Generate and preview loot
        return previewLoot(player, chest, pos, world);
    }

    public ActionResult handleShiftRightClick(PlayerEntity player, ChestBlockEntity chest, BlockPos pos, ServerWorld world) {
        ItemStack wand = player.getMainHandStack();

        // Check if regeneration was already used
        NbtComponent nbtComponent = wand.get(DataComponentTypes.CUSTOM_DATA);
        if (nbtComponent != null) {
            NbtCompound nbt = nbtComponent.copyNbt();
            if (nbt.getBoolean(REGENERATION_USED_KEY)) {
                player.sendMessage(Text.literal("This wand has already been used for regeneration!"), true);
                return ActionResult.FAIL;
            }
        }

        // Check if chest has loot table
        if (chest.getLootTable() == null) {
            player.sendMessage(Text.literal("This chest doesn't have a loot table to regenerate!"), true);
            return ActionResult.FAIL;
        }

        // Clear current contents and regenerate
        chest.clear();

        try {
            // Use Minecraft's built-in chest loot generation
            chest.generateLoot(player);

            // Get the generated items
            List<ItemStack> loot = new ArrayList<>();
            for (int i = 0; i < chest.size(); i++) {
                ItemStack stack = chest.getStack(i);
                if (!stack.isEmpty()) {
                    loot.add(stack.copy());
                }
            }

            // Clear and re-add to ensure proper placement
            chest.clear();
            for (ItemStack stack : loot) {
                addStackToChest(chest, stack);
            }

            // Mark regeneration as used
            NbtCompound nbt = nbtComponent != null ? nbtComponent.copyNbt() : new NbtCompound();
            nbt.putBoolean(REGENERATION_USED_KEY, true);
            wand.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

            // Clear the chest's loot table so it won't regenerate again
            chest.setLootTable(null);
            chest.setLootTableSeed(0);

            player.sendMessage(Text.literal("Loot table regenerated! This can only be done once."), true);
            return ActionResult.SUCCESS;

        } catch (Exception e) {
            ElementalConvergence.LOGGER.error("Failed to regenerate loot: " + e.getMessage());
            player.sendMessage(Text.literal("§cFailed to regenerate loot table!"), true);
            return ActionResult.FAIL;
        }
    }

    private ActionResult previewLoot(PlayerEntity player, ChestBlockEntity chest, BlockPos pos, ServerWorld world) {
        try {
            // Store original contents first
            List<ItemStack> originalContents = new ArrayList<>();
            for (int i = 0; i < chest.size(); i++) {
                originalContents.add(chest.getStack(i).copy());
            }

            // Alternative approach - let Minecraft handle the loot generation
            if (chest.getLootTable() != null) {
                // Use Minecraft's built-in chest loot generation
                chest.generateLoot(player);

                // Get the generated items and add enchantments
                List<ItemStack> loot = new ArrayList<>();
                for (int i = 0; i < chest.size(); i++) {
                    ItemStack stack = chest.getStack(i);
                    if (!stack.isEmpty()) {
                        loot.add(stack.copy());
                    }
                }

                // Clear and re-add with enchantments
                chest.clear();

                RegistryKey<Enchantment> preventPickupEnchantment = ModEnchantments.LOCKING_CURSE;

                for (ItemStack stack : loot) {
                    if (!stack.isEmpty()) {
                        try {
                            RegistryEntry<Enchantment> lockingCurse = world.getRegistryManager()
                                    .get(net.minecraft.registry.RegistryKeys.ENCHANTMENT)
                                    .getEntry(ModEnchantments.LOCKING_CURSE)
                                    .orElse(null);

                            if (lockingCurse != null) {
                                stack.addEnchantment(lockingCurse, 1);
                            }
                        } catch (Exception e) {
                            ElementalConvergence.LOGGER.warn("Failed to add preview enchantment: " + e.getMessage());
                        }
                        addStackToChest(chest, stack);
                    }
                }
            }

            // Store state in wand for cleanup
            ItemStack wand = player.getMainHandStack();
            NbtCompound nbt = new NbtCompound();
            nbt.putBoolean(PREVIEW_MODE_KEY, true);
            nbt.putString(CHEST_UUID_KEY, getChestIdentifier(pos));

            // Store original contents using modern encoding
            NbtCompound originalContentsNbt = new NbtCompound();
            for (int i = 0; i < originalContents.size(); i++) {
                if (!originalContents.get(i).isEmpty()) {
                    NbtCompound itemNbt = new NbtCompound();
                    // Use the modern encoding that preserves all data components
                    originalContents.get(i).encode(world.getRegistryManager(), itemNbt);
                    originalContentsNbt.put("slot_" + i, itemNbt);
                }
            }
            nbt.put("original_contents", originalContentsNbt);

            // Store using modern component system
            wand.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

            player.sendMessage(Text.literal("Previewing loot! Close the chest to restore original contents."), true);

            // Open chest for player
            player.openHandledScreen(chest);

            return ActionResult.SUCCESS;

        } catch (Exception e) {
            ElementalConvergence.LOGGER.error("Failed to preview loot: " + e.getMessage());
            player.sendMessage(Text.literal("§cFailed to preview loot table!"), true);
            return ActionResult.FAIL;
        }
    }

    private void addStackToChest(ChestBlockEntity chest, ItemStack stack) {
        // Find first empty slot
        for (int i = 0; i < chest.size(); i++) {
            if (chest.getStack(i).isEmpty()) {
                chest.setStack(i, stack);
                return;
            }
        }

        // If chest is full, replace the first slot (shouldn't happen with normal loot generation)
        if (!stack.isEmpty()) {
            chest.setStack(0, stack);
        }
    }

    private String getChestIdentifier(BlockPos pos) {
        return pos.getX() + "_" + pos.getY() + "_" + pos.getZ();
    }

    // This method is called when the player closes the chest inventory
    public void onChestClosed(PlayerEntity player, ChestBlockEntity chest, BlockPos pos) {
        ItemStack wand = player.getMainHandStack();
        if (!wand.isOf(this)) return;

        NbtComponent nbtComponent = wand.get(DataComponentTypes.CUSTOM_DATA);
        if (nbtComponent == null) return;

        NbtCompound nbt = nbtComponent.copyNbt();
        if (!nbt.getBoolean(PREVIEW_MODE_KEY)) return;

        String expectedChestId = nbt.getString(CHEST_UUID_KEY);
        String currentChestId = getChestIdentifier(pos);
        if (!expectedChestId.equals(currentChestId)) return;

        // Remove enchantments from all items and restore original contents
        chest.clear();

        NbtCompound originalContentsNbt = nbt.getCompound("original_contents");
        for (String key : originalContentsNbt.getKeys()) {
            if (key.startsWith("slot_")) {
                int slot = Integer.parseInt(key.substring(5));
                NbtCompound itemNbt = originalContentsNbt.getCompound(key);
                ItemStack originalStack = ItemStack.fromNbt(player.getWorld().getRegistryManager(), itemNbt).orElse(ItemStack.EMPTY);
                chest.setStack(slot, originalStack);
            }
        }

        // Clear preview mode
        nbt.remove(PREVIEW_MODE_KEY);
        nbt.remove(CHEST_UUID_KEY);
        nbt.remove("original_contents");

        if (nbt.isEmpty()) {
            wand.remove(DataComponentTypes.CUSTOM_DATA);
        } else {
            wand.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        }

        player.sendMessage(Text.literal("Original chest contents restored!"), true);
    }
}