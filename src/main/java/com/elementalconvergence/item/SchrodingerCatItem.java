package com.elementalconvergence.item;

import com.elementalconvergence.container.SchrodingerCatScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class SchrodingerCatItem extends Item {

    public SchrodingerCatItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);

        if (world.isClient) {
            return ActionResult.SUCCESS;
        }

        // Check if it's a chest
        if (!(state.getBlock() instanceof ChestBlock)) {
            return ActionResult.PASS;
        }

        // Get the chest block entity
        if (!(world.getBlockEntity(pos) instanceof ChestBlockEntity chestEntity)) {
            return ActionResult.PASS;
        }

        // Check if it's a lootable container with an ungenerated loot table
        if (!(chestEntity instanceof LootableContainerBlockEntity lootableContainer)) {
            return ActionResult.PASS;
        }

        // Check if the loot table hasn't been generated yet
        if (lootableContainer.getLootTable() == null) {
            context.getPlayer().sendMessage(Text.literal("This chest has no loot table!").formatted(Formatting.RED), false);
            return ActionResult.FAIL;
        }

        // Check if the chest has already been opened (loot generated)
        // If the chest has items but no loot table, it means the loot has already been generated
        if (!chestEntity.isEmpty()) {
            context.getPlayer().sendMessage(Text.literal("This chest has already been opened!").formatted(Formatting.RED), false);
            return ActionResult.FAIL;
        }

        // Open the Schrödinger's Cat preview screen
        if (context.getPlayer() instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.openHandledScreen(new SchrodingerCatScreenHandlerFactory(
                    pos,
                    lootableContainer.getLootTable(),
                    lootableContainer.getLootTableSeed()
            ));
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public ItemStack getDefaultStack() {
        ItemStack stack = super.getDefaultStack();

        // Add lore to explain the item
        LoreComponent lore = new LoreComponent(List.of(
                Text.literal("Right-click an unopened chest").formatted(Formatting.GRAY),
                Text.literal("to preview its contents").formatted(Formatting.GRAY),
                Text.literal("before it collapses!").formatted(Formatting.DARK_PURPLE, Formatting.ITALIC)
        ));

        stack.set(DataComponentTypes.LORE, lore);
        return stack;
    }
}