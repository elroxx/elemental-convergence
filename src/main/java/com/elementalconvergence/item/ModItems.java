package com.elementalconvergence.item;



import com.elementalconvergence.ElementalConvergence;
import com.elementalconvergence.block.ModBlocks;
import com.elementalconvergence.block.PrayingAltarBlock;
import com.elementalconvergence.effect.ModEffects;
import com.elementalconvergence.enchantment.ModEnchantments;
import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.minecraft.client.sound.Sound;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemGroups;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.item.*;
import net.minecraft.util.Rarity;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import static com.elementalconvergence.ElementalConvergence.BASE_MAGIC_ID;
import static com.elementalconvergence.ElementalConvergence.CONVERGENCE_MAGIC_ID;

public class ModItems {

    //CUSTOM MATERIALS FOR ARMOR

    //Halo material
    public static final RegistryEntry<ArmorMaterial> HALO_ARMOR_MATERIAL = Registry.registerReference(
            Registries.ARMOR_MATERIAL,
            Identifier.of(ElementalConvergence.MOD_ID, "halo"),
            new ArmorMaterial(
                    Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                        map.put(ArmorItem.Type.HELMET, 1); // noob protection
                        map.put(ArmorItem.Type.CHESTPLATE, 0);
                        map.put(ArmorItem.Type.LEGGINGS, 0);
                        map.put(ArmorItem.Type.BOOTS, 0);
                    }),
                    30, //enchantability
                    SoundEvents.ITEM_ARMOR_EQUIP_GOLD, // Equip sound
                    () -> Ingredient.ofItems(net.minecraft.item.Items.GOLD_INGOT), // Repair ingredient
                    List.of(
                            new ArmorMaterial.Layer(Identifier.of(ElementalConvergence.MOD_ID, "halo"))
                    ),
                    0.0F, // Toughness
                    0.0F  // Knockback resistance
            )
    );

    public static final RegistryEntry<ArmorMaterial> CROWN_ARMOR_MATERIAL = Registry.registerReference(
            Registries.ARMOR_MATERIAL,
            Identifier.of(ElementalConvergence.MOD_ID, "crown"),
            new ArmorMaterial(
                    Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                        map.put(ArmorItem.Type.HELMET, 2); // noob protection
                        map.put(ArmorItem.Type.CHESTPLATE, 0);
                        map.put(ArmorItem.Type.LEGGINGS, 0);
                        map.put(ArmorItem.Type.BOOTS, 0);
                    }),
                    30, //enchantability
                    SoundEvents.ITEM_ARMOR_EQUIP_GOLD, // Equip sound
                    () -> Ingredient.ofItems(net.minecraft.item.Items.GOLD_INGOT), // Repair ingredient
                    List.of(
                            new ArmorMaterial.Layer(Identifier.of(ElementalConvergence.MOD_ID, "crown"))
                    ),
                    0.0F, // Toughness
                    0.0F  // Knockback resistance
            )
    );



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

    public static final Item ROTTEN_CORPSE = register("rotten_corpse", new Item(new Item.Settings().maxCount(4)));

    public static final Item GRAVITY_SHARD = register("gravity_shard", new TooltipItem(new Item.Settings().maxCount(64).rarity(Rarity.RARE), "item.elemental-convergence.gravity_shard.tooltip"));

    //Shadowball Item for the 3rd magic spell
    public static final Item SHADOWBALL_ITEM = register("shadowball",
           new ShadowballItem(new Item.Settings().maxCount(16)));

    public static final BlockItem ALTAR_ITEM = register("altar_of_convergence", new BlockItem(ModBlocks.ALTAR_OF_CONVERGENCE, new Item.Settings().maxCount(1)));

    public static final BlockItem FLOWER_GATEWAY_ITEM = register("flower_gateway", new BlockItem(ModBlocks.FLOWER_GATEWAY, new Item.Settings().maxCount(8)));

    public static final Item TRAIN_WHISTLE = register("train_whistle", new SteamWhistleItem(new Item.Settings().maxCount(1).rarity(Rarity.UNCOMMON)));

    public static final Item WINE = register("wine", new WineItem(new Item.Settings().maxCount(1).rarity(Rarity.UNCOMMON)));

    public static final Item HALO = register("halo", new ArmorItem(HALO_ARMOR_MATERIAL, ArmorItem.Type.HELMET, new Item.Settings().maxCount(1).rarity(Rarity.UNCOMMON)));
    public static final BlockItem PRAYING_ALTAR_ITEM = register("praying_altar", new BlockItem(ModBlocks.PRAYING_ALTAR, new Item.Settings()));

    public static final Item PORTABLE_BEEHIVE = register("portable_beehive", new PortableBeehiveItem(new Item.Settings().maxCount(1).rarity(Rarity.RARE)));
    public static final Item REINFORCED_HONEYCOMB_BLOCK_ITEM = register("reinforced_honeycomb_block", new BlockItem(ModBlocks.REINFORCED_HONEYCOMB, new Item.Settings()));
    public static final Item HONEY_STICK = register("honey_stick", new Item(new Item.Settings().maxCount(1).maxDamage(10)));
    public static final Item LOCK_ITEM = register("lock", new Item(new Item.Settings().maxCount(1)));
    public static final Item CROWN = register("crown", new ArmorItem(CROWN_ARMOR_MATERIAL, ArmorItem.Type.HELMET, new Item.Settings().maxCount(1)));
    public static final Item POLLEN_WHITE = register("pollen_white", new PollenItem(StatusEffects.DOLPHINS_GRACE, 20*60*2, 2, new Item.Settings().maxCount(8)));
    public static final Item POLLEN_RED = register("pollen_red", new PollenItem(StatusEffects.DARKNESS, 20*60, 0, new Item.Settings().maxCount(8)));
    public static final Item POLLEN_BLUE = register("pollen_blue", new PollenItem(ModEffects.INSECT_WEIGHT, 20*60*2, 0, new Item.Settings().maxCount(8)));
    public static final Item POLLEN_ORANGE = register("pollen_orange", new PollenItem(StatusEffects.UNLUCK, 20*60*2, 4, new Item.Settings().maxCount(8).rarity(Rarity.UNCOMMON)));
    public static final Item POLLEN_PURPLE = register("pollen_purple", new PollenItem(StatusEffects.LUCK, 20*60*2, 4, new Item.Settings().maxCount(8).rarity(Rarity.UNCOMMON)));
    public static final Item POLLEN_GREEN = register("pollen_green", new PollenItem(ModEffects.GILLS, 20*60*2, 0, new Item.Settings().maxCount(8).rarity(Rarity.RARE)));
    public static final Item POLLEN_YELLOW = register("pollen_yellow", new PollenItem(ModEffects.PLAGUE, 20*20, 1, new Item.Settings().maxCount(8).rarity(Rarity.RARE)));
    public static final Item POLLEN_BROWN = register("pollen_brown", new PollenItem(ModEffects.LIGHT_PHASING, 20*60*2, 0, new Item.Settings().maxCount(8).rarity(Rarity.EPIC)));
    public static final Item POLLEN_PINK = register("pollen_pink", new PollenItem(ModEffects.WINGS, 20*60, 0, new Item.Settings().maxCount(8).rarity(Rarity.EPIC)));

    public static final Item COFFIN_ITEM = register("coffin", new BlockItem(ModBlocks.COFFIN_BLOCK, new Item.Settings().maxCount(1)));

    public static final Item SCHRODINGER_CAT = register("schrodinger_cat", new SchrodingerCatItem(new Item.Settings().maxCount(1).rarity(Rarity.UNCOMMON)));

    public static final Item MYSTICAL_ENERGY = register("mystical_energy", new Item(new Item.Settings().maxCount(1).rarity(Rarity.EPIC)));
    public static final Item MYSTICAL_CHAPTER_1 = register("mystical_chapter_1", new MysticalTomeItem(getEnchantList(1), getEnchantLevelList(1), 1,new Item.Settings().maxCount(1)));
    public static final Item MYSTICAL_CHAPTER_2 = register("mystical_chapter_2", new MysticalTomeItem(getEnchantList(2), getEnchantLevelList(2), 2,new Item.Settings().maxCount(1)));

    //all pollen effects:
    //Luck, bad luck, darkness, dolphin's grace, Gills, Wings, Light Phasing, Plague, insect weight
    //common: white, red, blue
    //uncommon: orange, purple
    //rare: green, yellow
    //epic: pink, brown

    //to register every item
    public static <T extends Item> T register(String name, T item) {
        return Registry.register(Registries.ITEM, ElementalConvergence.id(name), item);
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
            eyes[i]= register(CONVERGENCE_MAGIC_ID[i]+baseString, new ConvergentEyeItem(new Item.Settings().maxCount(1).rarity(Rarity.EPIC), i+BASE_MAGIC_ID.length));
        }
        return eyes;
    }

    public static List<RegistryKey<Enchantment>> getEnchantList(int tome){
        if (tome==1){
            List<RegistryKey<Enchantment>> enchantList = new ArrayList<RegistryKey<Enchantment>>();
            enchantList.add(ModEnchantments.FANGS);
            enchantList.add(ModEnchantments.BOUNCY_ARROW);
            enchantList.add(ModEnchantments.CARRIER);
            return enchantList;
        } else {
            List<RegistryKey<Enchantment>> enchantList = new ArrayList<RegistryKey<Enchantment>>();
            enchantList.add(ModEnchantments.LAVA_WALKER);
            enchantList.add(ModEnchantments.HIGH_STEPS);
            enchantList.add(ModEnchantments.VOLCANIC_CHARGE);
            return enchantList;
        }
    }

    public static List<Integer> getEnchantLevelList(int tome){
        List<Integer> lvlList = new ArrayList<Integer>();
        lvlList.add(1);
        lvlList.add(1);
        lvlList.add(1);
        return lvlList;
    }

    //So that the class exists
    public static void initialize() {
    }

}
