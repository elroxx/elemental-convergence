package com.elementalconvergence.item;

import com.elementalconvergence.data.SchrodingerCatDataComponent;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class SchrodingerCatItem extends Item {

    public SchrodingerCatItem(Settings settings) {
        super(settings.component(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.literal("Sneak+Right-click: Lock or reroll loot chest").formatted(Formatting.GRAY),
                Text.literal("(only if contents unchanged)").formatted(Formatting.ITALIC, Formatting.GRAY)
        ))));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        PlayerEntity player = context.getPlayer();

        if (world.isClient || player == null) {
            return ActionResult.SUCCESS;
        }

        BlockState state = world.getBlockState(pos);

        // Check if the block is a chest
        if (!(state.getBlock() instanceof ChestBlock)) {
            player.sendMessage(Text.literal("Can only be used on chests!").formatted(Formatting.RED), true);
            return ActionResult.FAIL;
        }

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof ChestBlockEntity chestEntity)) {
            return ActionResult.FAIL;
        }

        // Get the chest inventory (handles double chests automatically)
        Inventory chestInventory = ChestBlock.getInventory((ChestBlock) state.getBlock(), state, world, pos, true);
        if (chestInventory == null) {
            return ActionResult.FAIL;
        }

        // Perform the swap
        swapInventories(player, chestInventory, world, pos);

        return ActionResult.SUCCESS;
    }

    private void swapInventories(PlayerEntity player, Inventory chestInventory, World world, BlockPos pos) {
        // Get player's cat inventory
        DefaultedList<ItemStack> catInventory = SchrodingerCatDataComponent.getCatInventory(player);

        // Create temporary storage for chest contents
        DefaultedList<ItemStack> tempInventory = DefaultedList.ofSize(chestInventory.size(), ItemStack.EMPTY);

        // Store chest contents in temp
        for (int i = 0; i < chestInventory.size(); i++) {
            tempInventory.set(i, chestInventory.getStack(i).copy());
        }

        // Clear chest and fill with cat inventory
        chestInventory.clear();
        int maxSlots = Math.min(catInventory.size(), chestInventory.size());
        for (int i = 0; i < maxSlots; i++) {
            if (!catInventory.get(i).isEmpty()) {
                chestInventory.setStack(i, catInventory.get(i).copy());
            }
        }

        // Clear cat inventory and fill with chest contents
        for (int i = 0; i < catInventory.size(); i++) {
            catInventory.set(i, ItemStack.EMPTY);
        }
        for (int i = 0; i < Math.min(tempInventory.size(), catInventory.size()); i++) {
            if (!tempInventory.get(i).isEmpty()) {
                catInventory.set(i, tempInventory.get(i));
            }
        }

        // Save the updated cat inventory
        SchrodingerCatDataComponent.setCatInventory(player, catInventory);

        // Mark chest as dirty to save changes
        chestInventory.markDirty();

        // Play sound and show message
        world.playSound(null, pos, SoundEvents.BLOCK_ENDER_CHEST_OPEN, SoundCategory.BLOCKS, 1.0F, 1.0F);
        player.sendMessage(Text.literal("Schrodinger's Cat swapped inventories!").formatted(Formatting.LIGHT_PURPLE), true);
    }

}
