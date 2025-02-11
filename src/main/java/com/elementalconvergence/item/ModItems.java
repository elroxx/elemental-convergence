package com.elementalconvergence.item;



import com.elementalconvergence.ElementalConvergence;
import com.elementalconvergence.block.ModBlocks;
import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.item.*;
import net.minecraft.util.Rarity;

import static com.elementalconvergence.ElementalConvergence.BASE_MAGIC_ID;
import static com.elementalconvergence.ElementalConvergence.CONVERGENCE_MAGIC_ID;

public class ModItems {

    public static final Item TEST_ITEM = register("test_item", new TestItem(
            new Item.Settings().maxCount(1)));

    //Blank information scroll
    public static final Item BLANK_SCROLL = register("blank_scroll", new Item(new Item.Settings()));

    //all scrolls
    public static final Item[] INFO_SCROLLS = registerScrolls("_info_scroll");

    //all magic eyes
    public static final Item[] MAGIC_EYES = registerMagicEyes("_magic_eye");


    //basic magic eye to craft other magic eyes
    public static final Item MAGIC_EYE = register("bland_magic_eye", new Item(new Item.Settings().maxCount(16)));

    //Crafting components for Convergence eyes
    public static final Item EPSILON_DUST = register("epsilon_dust", new Item(new Item.Settings().maxCount(64).rarity(Rarity.UNCOMMON)));
    public static final Item LIMITING_EYE = register("limiting_eye", new Item(new Item.Settings().maxCount(16).rarity(Rarity.RARE)));
    public static final Item BOUNDING_SEQUENCE = register("bounding_sequence", new Item(new Item.Settings().maxCount(64).rarity(Rarity.UNCOMMON)));
    public static final Item CONVERGENT_EYE = register("convergent_eye", new Item(new Item.Settings().maxCount(16).rarity(Rarity.EPIC)));

    public static final Item[] CONVERGENT_EYES = registerConvergentEyes("_convergent_eye");

    //Shadowball Item for the 3rd magic spell
    public static final Item SHADOWBALL_ITEM = register("shadowball",
           new ShadowballItem(new Item.Settings().maxCount(16)));

    public static final BlockItem ALTAR_ITEM = register("altar_of_convergence", new BlockItem(ModBlocks.ALTAR_OF_CONVERGENCE, new Item.Settings().maxCount(1)));

    public static final BlockItem FLOWER_GATEWAY_ITEM = register("flower_gateway", new BlockItem(ModBlocks.FLOWER_GATEWAY, new Item.Settings().maxCount(8)));


    //to register every item
    public static <T extends Item> T register(String name, T item) {
        return Registry.register(Registries.ITEM, ElementalConvergence.id(name), item);
    }

    public static Item[] registerScrolls(String baseString){
        Item[] scrolls = new Item[BASE_MAGIC_ID.length];
        for (int i=0; i<BASE_MAGIC_ID.length; i++){
            scrolls[i]= register(BASE_MAGIC_ID[i]+baseString, new InfoScrollItem(new Item.Settings().maxCount(1), i));
        }
        return scrolls;
    }

    public static Item[] registerMagicEyes(String baseString){
        Item[] eyes = new Item[BASE_MAGIC_ID.length];
        for (int i=0; i<BASE_MAGIC_ID.length; i++){
            eyes[i]= register(BASE_MAGIC_ID[i]+baseString, new MagicEyeItem(new Item.Settings().maxCount(1), i));
        }
        return eyes;
    }

    public static Item[] registerConvergentEyes(String baseString){
        Item[] eyes = new Item[CONVERGENCE_MAGIC_ID.length];
        for (int i=0; i<CONVERGENCE_MAGIC_ID.length; i++){
            eyes[i]= register(CONVERGENCE_MAGIC_ID[i]+baseString, new MagicEyeItem(new Item.Settings().maxCount(1).rarity(Rarity.EPIC), i+BASE_MAGIC_ID.length));
        }
        return eyes;
    }

    //So that the class exists
    public static void initialize() {
    }

}
