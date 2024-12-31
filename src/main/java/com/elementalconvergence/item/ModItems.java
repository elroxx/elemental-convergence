package com.elementalconvergence.item;



import com.elementalconvergence.ElementalConvergence;
import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.item.*;

public class ModItems {

    //Test item with nothing on
    //public static final Item EXAMPLE_ITEM = register("test_item", new Item(new Item.Settings()));

    public static final Item TEST_ITEM = register("test_item", new TestItem(
            new Item.Settings().maxCount(1)));
    //Blank information scroll
    public static final Item BLANK_SCROLL = register("blank_scroll", new Item(new Item.Settings()));


    //to register every item
    public static <T extends Item> T register(String name, T item) {
        return Registry.register(Registries.ITEM, ElementalConvergence.id(name), item);
    }

    //So that the class exists
    public static void initialize() {
    }

}
