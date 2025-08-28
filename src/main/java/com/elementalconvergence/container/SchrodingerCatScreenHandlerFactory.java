package com.elementalconvergence.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.registry.RegistryKey;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.loot.LootTable;
import net.minecraft.util.math.BlockPos;

public class SchrodingerCatScreenHandlerFactory implements NamedScreenHandlerFactory {
    private final BlockPos chestPos;
    private final RegistryKey<LootTable> lootTableKey;
    private final long lootTableSeed;

    public SchrodingerCatScreenHandlerFactory(BlockPos chestPos, RegistryKey<LootTable> lootTableKey, long lootTableSeed) {
        this.chestPos = chestPos;
        this.lootTableKey = lootTableKey;
        this.lootTableSeed = lootTableSeed;
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Schrödinger's Chest Preview");
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new SchrodingerCatScreenHandler(syncId, playerInventory, chestPos, lootTableKey, lootTableSeed);
    }
}
