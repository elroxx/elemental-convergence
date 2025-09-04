package com.elementalconvergence.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.elementalconvergence.enchantment.ModEnchantments;

@Mixin(ArrowItem.class)
public class ArrowItemMixin {
    @Inject(method = "createArrow", at = @At("RETURN"))
    private void elemental$onCreateArrow(World world, ItemStack arrowStack, LivingEntity shooter, @Nullable ItemStack shotFrom,
                                         CallbackInfoReturnable<PersistentProjectileEntity> cir) {
        PersistentProjectileEntity arrow = cir.getReturnValue();
        if (arrow == null || shooter == null) return;

        // check both hands W
        int level = Math.max(
                EnchantmentHelper.getLevel(shooter.getWorld().getRegistryManager().getWrapperOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(ModEnchantments.BOUNCY_ARROW), shooter.getMainHandStack()),
                EnchantmentHelper.getLevel(shooter.getWorld().getRegistryManager().getWrapperOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(ModEnchantments.BOUNCY_ARROW), shooter.getOffHandStack())
        );
        if (level <= 0) return;

        NbtCompound nbt = new NbtCompound();
        nbt.putBoolean("IsBouncy", true);
        nbt.putInt("BouncesRemaining", level);
        arrow.readNbt(nbt);
    }
}
