package com.elementalconvergence.item;

import com.elementalconvergence.entity.ConductiveArrowEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ConductiveArrowItem extends ArrowItem {

    public ConductiveArrowItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public PersistentProjectileEntity createArrow(World world, ItemStack stack, LivingEntity shooter, ItemStack shotFrom) {
        return new ConductiveArrowEntity(world, shooter, stack.copyWithCount(1), shotFrom);
    }
}
