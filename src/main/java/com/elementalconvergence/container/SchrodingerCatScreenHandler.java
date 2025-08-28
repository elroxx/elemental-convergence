package com.elementalconvergence.container;

import com.elementalconvergence.ElementalConvergence;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class SchrodingerCatScreenHandler extends ScreenHandler {
    private final Inventory previewInventory;
    private final BlockPos chestPos;
    private final RegistryKey<LootTable> lootTableKey;
    private long lootTableSeed;
    private boolean hasRerolled = false;
    private final PlayerEntity player;

    // Constructor for registry (with basic parameters)
    public SchrodingerCatScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, BlockPos.ORIGIN, null, 0L);
    }

    // Full constructor for actual use
    public SchrodingerCatScreenHandler(int syncId, PlayerInventory playerInventory, BlockPos chestPos, RegistryKey<LootTable> lootTableKey, long lootTableSeed) {
        super(ElementalConvergence.SCHRODINGER_CAT_SCREEN_HANDLER, syncId);
        this.chestPos = chestPos;
        this.lootTableKey = lootTableKey;
        this.lootTableSeed = lootTableSeed;
        this.player = playerInventory.player;
        this.previewInventory = new SimpleInventory(27); // Standard chest size

        // Only generate initial loot preview if we have valid parameters
        if (lootTableKey != null && !chestPos.equals(BlockPos.ORIGIN)) {
            generateLootPreview();
        }

        // Add preview inventory slots (3x9 grid)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new PreviewSlot(previewInventory, row * 9 + col, 8 + col * 18, 18 + row * 18));
            }
        }

        // Add player inventory slots
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Add player hotbar slots
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    private void generateLootPreview() {
        if (!player.getWorld().isClient && lootTableKey != null) {
            ServerWorld world = (ServerWorld) player.getWorld();

            // Get the loot table
            LootTable lootTable = world.getServer().getReloadableRegistries().getLootTable(lootTableKey);

            // Create loot context
            LootContextParameterSet.Builder builder = new LootContextParameterSet.Builder(world)
                    .add(LootContextParameters.ORIGIN, Vec3d.ofCenter(chestPos))
                    .add(LootContextParameters.THIS_ENTITY, player);

            // Generate loot
            List<ItemStack> loot = lootTable.generateLoot(builder.build(LootContextTypes.CHEST), lootTableSeed);

            // Clear preview inventory
            previewInventory.clear();

            // Add loot to preview inventory with BINDING_CURSE
            for (int i = 0; i < Math.min(loot.size(), 27); i++) {
                ItemStack stack = loot.get(i).copy();

                // Add BINDING_CURSE enchantment to prevent pickup
                ItemEnchantmentsComponent.Builder enchantBuilder = new ItemEnchantmentsComponent.Builder(
                        stack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT)
                );
                enchantBuilder.add(world.getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(Enchantments.BINDING_CURSE).get(), 1);
                stack.set(DataComponentTypes.ENCHANTMENTS, enchantBuilder.build());

                previewInventory.setStack(i, stack);
            }
        }
    }

    public void reroll() {
        if (!hasRerolled && !player.getWorld().isClient && lootTableKey != null) {
            // Generate new seed for reroll
            this.lootTableSeed = player.getWorld().random.nextLong();
            generateLootPreview();
            hasRerolled = true;
        }
    }

    public boolean canReroll() {
        return !hasRerolled;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        // Prevent moving items from preview slots
        if (invSlot < 27) {
            return ItemStack.EMPTY;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        // Prevent any interaction with preview slots
        if (slotIndex < 27) {
            return;
        }
        super.onSlotClick(slotIndex, button, actionType, player);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        if (chestPos.equals(BlockPos.ORIGIN)) {
            return false; // Invalid position from registry constructor
        }
        return player.squaredDistanceTo(Vec3d.ofCenter(chestPos)) <= 64.0;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);

        if (!player.getWorld().isClient && lootTableKey != null && !chestPos.equals(BlockPos.ORIGIN)) {
            // Transfer items to actual chest and clear loot table
            if (player.getWorld().getBlockEntity(chestPos) instanceof ChestBlockEntity chestEntity &&
                    chestEntity instanceof LootableContainerBlockEntity lootableContainer) {

                // Clear the loot table
                lootableContainer.setLootTable(null, 0L);

                // Transfer preview items to chest (without the binding curse)
                for (int i = 0; i < previewInventory.size(); i++) {
                    ItemStack previewStack = previewInventory.getStack(i);
                    if (!previewStack.isEmpty()) {
                        ItemStack actualStack = previewStack.copy();
                        // Remove the binding curse
                        ItemEnchantmentsComponent enchantments = actualStack.get(DataComponentTypes.ENCHANTMENTS);
                        if (enchantments != null) {
                            ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(enchantments);
                            builder.remove(entry -> entry.matchesKey(Enchantments.BINDING_CURSE));
                            ItemEnchantmentsComponent newEnchantments = builder.build();
                            if (newEnchantments.isEmpty()) {
                                actualStack.remove(DataComponentTypes.ENCHANTMENTS);
                            } else {
                                actualStack.set(DataComponentTypes.ENCHANTMENTS, newEnchantments);
                            }
                        }
                        chestEntity.setStack(i, actualStack);
                    }
                }
                chestEntity.markDirty();
            }
        }
    }

    // Custom slot class to prevent interaction
    private static class PreviewSlot extends Slot {
        public PreviewSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canTakeItems(PlayerEntity playerEntity) {
            return false;
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return false;
        }
    }
}
