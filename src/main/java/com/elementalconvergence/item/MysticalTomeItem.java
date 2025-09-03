package com.elementalconvergence.item;

import com.elementalconvergence.container.MysticalTomeScreenHandler;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

public class MysticalTomeItem extends Item {
    private final List<RegistryKey<Enchantment>> enchantments;
    private final List<Integer> enchantmentLevels;

    public MysticalTomeItem(List<RegistryKey<Enchantment>> enchantments, List<Integer> enchantmentLevels, Settings settings) {
        super(settings);
        if (enchantments.size() != 3 || enchantmentLevels.size() != 3) {
            throw new IllegalArgumentException("MysticalTome must have exactly 3 enchantments with their levels");
        }
        this.enchantments = enchantments;
        this.enchantmentLevels = enchantmentLevels;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient && user instanceof ServerPlayerEntity serverPlayer) {
            // Open the custom screen
            serverPlayer.openHandledScreen(new MysticalTomeScreenHandler.Factory(
                    this.enchantments,
                    this.enchantmentLevels
            ));
        }
        return TypedActionResult.success(user.getStackInHand(hand));
    }

    public List<RegistryEntry<Enchantment>> getEnchantments() {
        return this.enchantments;
    }

    public List<Integer> getEnchantmentLevels() {
        return this.enchantmentLevels;
    }
}
