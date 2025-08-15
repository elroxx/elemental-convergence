package com.elementalconvergence.mixin;

import com.elementalconvergence.enchantment.ModEnchantments;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(Slot.class)
public abstract class InvSlotMixin {

    @Shadow public abstract ItemStack getStack();

    @Inject(method = "canTakeItems", at = @At("HEAD"), cancellable = true)
    private void preventTakingLockedItems(PlayerEntity playerEntity, CallbackInfoReturnable<Boolean> cir) {
        ItemStack stack = this.getStack();

        if (!stack.isEmpty()) {
            // Check if the item has the Locking Curse enchantment
            if (EnchantmentHelper.getLevel(playerEntity.getWorld().getRegistryManager().getWrapperOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(ModEnchantments.LOCKING_CURSE), stack) > 0) {
                // Prevent taking the item
                cir.setReturnValue(false);
            }
        }
    }

    /**
     * Alternative approach: Also prevent inserting locked items into slots
     * This prevents moving locked items around even with keyboard shortcuts
     */
    /*@Inject(method = "canInsert", at = @At("HEAD"), cancellable = true)
    private void preventInsertingLockedItems(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!stack.isEmpty()) {
            // Check if the item being inserted has the Locking Curse enchantment
            if (EnchantmentHelper.getLevel(playerEntity.getWorld().getRegistryManager().getWrapperOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(ModEnchantments.LOCKING_CURSE), stack) > 0) {
                // Prevent inserting the item
                cir.setReturnValue(false);
            }
        }
    }*/
}
