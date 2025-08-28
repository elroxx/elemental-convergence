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
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryKey;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
        BlockState state = world.getBlockState(pos);

        if (!(state.getBlock() instanceof ChestBlock)) return ActionResult.PASS;

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof ChestBlockEntity chestEntity)) return ActionResult.PASS;

        if (world.isClient) return ActionResult.SUCCESS;

        if (!player.isSneaking()) return ActionResult.PASS;

        if (!(chestEntity instanceof LootableContainerBlockEntity lootableChest)) {
            player.sendMessage(Text.literal("This chest has no quantum properties!").formatted(Formatting.RED), false);
            return ActionResult.SUCCESS;
        }

        ServerWorld serverWorld = (ServerWorld) world;
        SchrodingerData data = SchrodingerData.get(serverWorld);
        String posKey = pos.toShortString();

        Identifier lootId = lootableChest.getLootTable().getRegistry(); // get ID BEFORE generating loot

        // First-time lock
        if (!data.hasChest(posKey)) {
            if (lootId != null) {
                data.setLootKey(posKey, lootId.toString()); // store ID
                lootableChest.generateLoot(player);         // then generate
            }
            data.setChestHash(posKey, generateInventoryHash(lootableChest));

            player.sendMessage(Text.literal("Quantum state observed! Chest contents locked.").formatted(Formatting.GREEN), false);
            return ActionResult.SUCCESS;
        }

        // Already locked → attempt reroll
        String originalHash = data.getChestHash(posKey);
        String currentHash = generateInventoryHash(lootableChest);

        if (!originalHash.equals(currentHash)) {
            player.sendMessage(Text.literal("Cannot reroll! Quantum state has been disturbed.").formatted(Formatting.RED), false);
            return ActionResult.SUCCESS;
        }

        String savedLootId = data.getLootKey(posKey);
        if (savedLootId != null) {
            lootableChest.clear();
            lootableChest.setLootTable(
                    RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of(savedLootId)),
                    world.getRandom().nextLong()
            );
            lootableChest.generateLoot(player);

            data.setChestHash(posKey, generateInventoryHash(lootableChest));
            player.sendMessage(Text.literal("Quantum reroll successful! New reality manifested.").formatted(Formatting.GOLD), false);
        } else {
            player.sendMessage(Text.literal("Cannot reroll: Loot table lost!").formatted(Formatting.RED), false);
        }

        return ActionResult.SUCCESS;
    }

    private String generateInventoryHash(LootableContainerBlockEntity chest) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            for (int i = 0; i < chest.size(); i++) {
                ItemStack stack = chest.getStack(i);
                if (!stack.isEmpty()) {
                    String itemString = stack.getItem().toString() + ":" + stack.getCount()
                            + ":" + stack.getComponents().toString();
                    md.update(itemString.getBytes());
                    md.update((byte) i);
                }
            }

            byte[] hash = md.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
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
