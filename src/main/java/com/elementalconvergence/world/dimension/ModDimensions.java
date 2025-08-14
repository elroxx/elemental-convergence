package com.elementalconvergence.world.dimension;

import com.elementalconvergence.ElementalConvergence;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

public class ModDimensions {
    public static final RegistryKey<World> BEEHIVE_WORLD_KEY = RegistryKey.of(RegistryKeys.WORLD,
            ElementalConvergence.id("beehive_dimension"));

    public static final RegistryKey<DimensionType> BEEHIVE_DIMENSION_TYPE = RegistryKey.of(RegistryKeys.DIMENSION_TYPE,
            ElementalConvergence.id("beehive_dimension_type"));

    public static void initialize() {
    }
}
