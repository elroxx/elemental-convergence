package com.elementalconvergence.world.dimension;

import com.elementalconvergence.ElementalConvergence;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionType;

public class ModDimensions {
    public static final RegistryKey<World> BEEHIVE_WORLD_KEY = RegistryKey.of(RegistryKeys.WORLD,
            ElementalConvergence.id("beehive_dimension"));

    public static final RegistryKey<DimensionType> BEEHIVE_DIMENSION_TYPE = RegistryKey.of(RegistryKeys.DIMENSION_TYPE,
            ElementalConvergence.id("beehive_dimension_type"));

    public static final RegistryKey<World> VOID_WORLD_KEY = RegistryKey.of(RegistryKeys.WORLD,
            ElementalConvergence.id("void_dimension"));

    public static final RegistryKey<DimensionType> VOID_DIMENSION_TYPE = RegistryKey.of(RegistryKeys.DIMENSION_TYPE,
            ElementalConvergence.id("void_dimension_type"));

    public static final RegistryKey<Biome> VOID_BIOME = RegistryKey.of(RegistryKeys.BIOME,
            ElementalConvergence.id("void_biome"));

    public static void initialize() {

    }
}
