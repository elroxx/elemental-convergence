package com.elementalconvergence.block;

import com.elementalconvergence.ElementalConvergence;
//import com.elementalconvergence.entity.BlackSnowballEntity;
//import com.elementalconvergence.item.BlackSnowballItem;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class ModBlocks {

    public static final Block ALTAR_OF_CONVERGENCE = register("altar_of_convergence",new AltarOfConvergenceBlock(Block.Settings.create()
            .strength(3.5f)
            .requiresTool()
            .nonOpaque()
            .luminance(state -> 10) // Add light emission for the glowing core
    ));

    public static final Block BLACK_SNOW_LAYER = register("black_snow_layer", new BlackSnowLayerBlock(Block.Settings.create()
            .nonOpaque()
            .noCollision()));

    public static final Block FLOWER_GATEWAY = register("flower_gateway", new FlowerGatewayBlock(Block.Settings.create()
            .burnable()
            .strength(2.5f)
    ));

    public static final Block PRAYING_ALTAR = register("praying_altar", new PrayingAltarBlock(Block.Settings.create()
            .sounds(BlockSoundGroup.STONE)
            .dropsNothing()
            .strength(-1.0f, 3600000.0f)
    ));

    public static final Block REINFORCED_HONEYCOMB = register("reinforced_honeycomb_block", new ReinforcedHoneycombBlock(Block.Settings.create()
            .strength(-1.0f, 3600000.0f)
            .dropsNothing()
            .sounds(BlockSoundGroup.CORAL)
            .dropsNothing()
            .mapColor(MapColor.ORANGE)
            .pistonBehavior(PistonBehavior.BLOCK)
    ));


    public static <T extends Block> T register(String name, T block) {
        return Registry.register(Registries.BLOCK, ElementalConvergence.id(name), block);
    }

    //So that the class exists
    public static void initialize() {
    }
}
