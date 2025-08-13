package com.elementalconvergence.worldgen;

import com.elementalconvergence.ElementalConvergence;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.placementmodifier.RarityFilterPlacementModifier;

import java.util.function.Predicate;

public class ModWorldGeneration {

    /*// Register the feature
    public static final Feature<DefaultFeatureConfig> PRAYING_ALTAR_FEATURE = Registry.register(
            Registries.FEATURE,
            ElementalConvergence.id("praying_altar_spawn"),
            new PrayingAltarFeature(DefaultFeatureConfig.CODEC)
    );

    // Create configured feature
    public static final RegistryKey<ConfiguredFeature<?, ?>> PRAYING_ALTAR_CONFIGURED = RegistryKey.of(
            RegistryKeys.CONFIGURED_FEATURE,
            ElementalConvergence.id("praying_altar_spawn")
    );

    // Create placed feature
    public static final RegistryKey<PlacedFeature> PRAYING_ALTAR_PLACED = RegistryKey.of(
            RegistryKeys.PLACED_FEATURE,
            ElementalConvergence.id("praying_altar_spawn")
    );

    public static void registerWorldGeneration() {
        //needs one per world
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Feature.TOP_LAYER_MODIFICATION,
                PRAYING_ALTAR_PLACED
        );


    }*/

    public static final RegistryKey<PlacedFeature> PRAYING_ALTAR_PLACED = registerWorldGeneration("praying_altar_spawn",
            GenerationStep.Feature.TOP_LAYER_MODIFICATION, BiomeSelectors.foundInOverworld());



    public static RegistryKey<PlacedFeature> registerWorldGeneration(String path, GenerationStep.Feature genStepFeature, Predicate<BiomeSelectionContext> biomeSelector){
        RegistryKey<ConfiguredFeature<?, ?>> configuredFeature = registerConfigured(path);
        System.out.println(configuredFeature);
        Feature<DefaultFeatureConfig> feature = registerFeature(path);
        System.out.println(feature);
        RegistryKey<PlacedFeature> placedFeature = registerPlaced(path);

        BiomeModifications.addFeature(
                biomeSelector,
                genStepFeature,
                placedFeature
        );

        return placedFeature;
    }

    //registering configured
    public static RegistryKey<ConfiguredFeature<?, ?>> registerConfigured(String path){
        return RegistryKey.of(
                RegistryKeys.CONFIGURED_FEATURE,
                ElementalConvergence.id(path)
        );
    }

    //registering features
    public static Feature<DefaultFeatureConfig> registerFeature(String path){
        return Registry.register(
                Registries.FEATURE,
                ElementalConvergence.id(path),
                new PrayingAltarFeature(DefaultFeatureConfig.CODEC)
        );
    }

    //registering placed
    public static RegistryKey<PlacedFeature> registerPlaced(String path){
        return RegistryKey.of(
                RegistryKeys.PLACED_FEATURE,
                ElementalConvergence.id(path)
        );
    }


    //So that the class exists
    public static void initialize() {
       ElementalConvergence.LOGGER.info("Initialized world gen");
    }




}