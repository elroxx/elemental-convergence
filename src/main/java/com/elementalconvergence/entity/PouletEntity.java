package com.elementalconvergence.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.world.World;

public class PouletEntity extends ChickenEntity {
    public PouletEntity(EntityType<? extends ChickenEntity> entityType, World world) {
        super(entityType, world);
    }
}
