package com.elementalconvergence.item;


import net.minecraft.item.Item;
import com.elementalconvergence.ElementalConvergence;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import javax.swing.text.Element;
import java.util.function.Function;

public class ModItems {

    public static final Item EXAMPLE_ITEM = register("test_item", new Item(new Item.Settings()));

    public static <T extends Item> T register(String name, T item) {
        return Registry.register(Registries.ITEM, ElementalConvergence.id(name), item);
    }

    public static void initialize() {
    }

}
