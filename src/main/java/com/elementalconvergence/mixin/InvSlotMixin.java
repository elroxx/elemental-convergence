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
            //verify if item has lock curse
            if (EnchantmentHelper.getLevel(playerEntity.getWorld().getRegistryManager().getWrapperOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(ModEnchantments.LOCKING_CURSE), stack) > 0) {
                //stop taking item
                cir.setReturnValue(false);
            }
        }
    }
}
