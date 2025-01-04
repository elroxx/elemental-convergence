package com.elementalconvergence.block;

import com.elementalconvergence.ElementalConvergence;
//import com.elementalconvergence.entity.BlackSnowballEntity;
//import com.elementalconvergence.item.BlackSnowballItem;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class ModBlocks {
    /*public static final Block BLACK_SNOW_LAYER = register("living_shadow_layer",new BlackSnowLayerBlock(FabricBlockSettings.create()
            .mapColor(MapColor.BLACK)
            .strength(0.1f)
            .sounds(BlockSoundGroup.SNOW)
            .nonOpaque()
            .noCollision()));*/
    public static final Block BLACK_SNOW_LAYER = register("black_snow_layer", new BlackSnowLayerBlock(Block.Settings.create()
            .nonOpaque()
            .noCollision()));

    public static <T extends Block> T register(String name, T block) {
        return Registry.register(Registries.BLOCK, ElementalConvergence.id(name), block);
    }

    //So that the class exists
    public static void initialize() {
    }
}
