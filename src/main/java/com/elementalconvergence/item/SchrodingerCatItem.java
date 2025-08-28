package com.elementalconvergence.item;

import com.elementalconvergence.data.SchrodingerData;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchrodingerCatItem extends Item {

    public SchrodingerCatItem(Settings settings) {
        super(settings.component(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.literal("Right-click: Check loot chest").formatted(Formatting.GRAY),
                Text.literal("Shift+Right-click: Reroll chest").formatted(Formatting.GRAY),
                Text.literal("(if contents are unchanged)").formatted(Formatting.ITALIC, Formatting.GRAY)
        ))));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        PlayerEntity player = context.getPlayer();
        BlockState state = world.getBlockState(pos);

        if (!(state.getBlock() instanceof ChestBlock)) {
            return ActionResult.PASS;
        }

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof ChestBlockEntity chestEntity)) {
            return ActionResult.PASS;
        }

        if (world.isClient) {
            return ActionResult.SUCCESS;
        }

        // Must be sneaking for any effect
        if (!player.isSneaking()) {
            return ActionResult.PASS;
        }

        // Check if it's a lootable container with a loot table
        if (!(chestEntity instanceof LootableContainerBlockEntity lootableChest)) {
            player.sendMessage(Text.literal("This chest has no quantum properties!").formatted(Formatting.RED), false);
            return ActionResult.SUCCESS;
        }

        if (lootableChest.getLootTable() == null) {
            player.sendMessage(Text.literal("This chest has already collapsed its quantum state!").formatted(Formatting.YELLOW), false);
            return ActionResult.SUCCESS;
        }

        ServerWorld serverWorld = (ServerWorld) world;

        // If chest is already tracked → reroll, otherwise → initialize/lock
        SchrodingerData data = SchrodingerData.get(serverWorld);
        String posKey = pos.toShortString();
        if (data.hasChest(posKey)) {
            return attemptReroll(serverWorld, pos, lootableChest, player);
        } else {
            return checkChestState(serverWorld, pos, lootableChest, player);
        }
    }

    private ActionResult checkChestState(ServerWorld world, BlockPos pos, LootableContainerBlockEntity chest, PlayerEntity player) {
        SchrodingerData data = SchrodingerData.get(world);
        String posKey = pos.toShortString();

        if (!data.hasChest(posKey)) {
            // Force loot generation immediately if a loot table exists
            if (chest.getLootTable() != null) {
                chest.generateLoot(player);
            }

            String hash = generateInventoryHash(chest);
            data.setChestHash(posKey, hash);
            data.markDirty();

            player.sendMessage(Text.literal("Quantum state observed! Chest contents locked.").formatted(Formatting.GREEN), false);
        } else {
            // Compare against saved hash
            String originalHash = data.getChestHash(posKey);
            String currentHash = generateInventoryHash(chest);

            if (originalHash.equals(currentHash)) {
                player.sendMessage(Text.literal("Quantum state intact! Sneak right-click again to reroll.").formatted(Formatting.AQUA), false);
            } else {
                player.sendMessage(Text.literal("Quantum state collapsed! Contents have been altered.").formatted(Formatting.RED), false);
            }
        }

        return ActionResult.SUCCESS;
    }

    private ActionResult attemptReroll(ServerWorld world, BlockPos pos, LootableContainerBlockEntity chest, PlayerEntity player) {
        SchrodingerData data = SchrodingerData.get(world);
        String posKey = pos.toShortString();

        if (!data.hasChest(posKey)) {
            player.sendMessage(Text.literal("Chest hasn't been quantum-locked yet! Right-click first.").formatted(Formatting.RED), false);
            return ActionResult.SUCCESS;
        }

        String originalHash = data.getChestHash(posKey);
        String currentHash = generateInventoryHash(chest);

        if (!originalHash.equals(currentHash)) {
            player.sendMessage(Text.literal("Cannot reroll! Quantum state has been disturbed.").formatted(Formatting.RED), false);
            return ActionResult.SUCCESS;
        }

        // Clear the chest and regenerate loot
        chest.clear();

        // Reset the loot table so it can be rolled again
        if (chest.getLootTable() != null) {
            // Set a new random seed and force regeneration
            chest.setLootTableSeed(world.random.nextLong());
            chest.getStack(0); // This triggers regeneration

            // Generate new hash and store it
            String newHash = generateInventoryHash(chest);
            data.setChestHash(posKey, newHash);
            data.markDirty();

            player.sendMessage(Text.literal("Quantum reroll successful! New reality manifested.").formatted(Formatting.GOLD), false);
        }

        return ActionResult.SUCCESS;
    }

    private String generateInventoryHash(LootableContainerBlockEntity chest) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            for (int i = 0; i < chest.size(); i++) {
                ItemStack stack = chest.getStack(i);
                if (!stack.isEmpty()) {
                    // Base info
                    String itemString = stack.getItem().toString() + ":" + stack.getCount();

                    // Add all components (names, enchants, lore, custom model data, etc.)
                    itemString += ":" + stack.getComponents().toString();

                    md.update(itemString.getBytes());
                    md.update((byte) i); // include slot index
                }
            }

            byte[] hash = md.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            // fallback: very simple hash
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < chest.size(); i++) {
                ItemStack stack = chest.getStack(i);
                sb.append(i).append(":").append(stack.getItem().toString())
                        .append(":").append(stack.getCount())
                        .append(":").append(stack.getComponents().toString());
            }
            return String.valueOf(sb.toString().hashCode());
        }
    }
}