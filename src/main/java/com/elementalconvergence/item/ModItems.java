package com.elementalconvergence.item;



import com.elementalconvergence.ElementalConvergence;
import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.item.*;

import static com.elementalconvergence.ElementalConvergence.BASE_MAGIC_ID;

public class ModItems {

    //Test item with nothing on
    //public static final Item EXAMPLE_ITEM = register("test_item", new Item(new Item.Settings()));

    public static final Item TEST_ITEM = register("test_item", new TestItem(
            new Item.Settings().maxCount(1)));
    //Blank information scroll
    public static final Item BLANK_SCROLL = register("blank_scroll", new Item(new Item.Settings()));

    //all scrolls
    public static final Item[] INFO_SCROLLS = registerScrolls("_info_scroll");
    //Individual scroll
    //public static final Item EARTH_INFO_SCROLL = register(BASE_MAGIC_ID[0]+"_info_scroll", new Item(new Item.Settings().maxCount(1)));


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

    //So that the class exists
    public static void initialize() {
    }

}
