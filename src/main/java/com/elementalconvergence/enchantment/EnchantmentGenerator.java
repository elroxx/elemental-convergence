package com.elementalconvergence.enchantment;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.enchantment.effect.EnchantmentEffectTarget;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;

import java.util.concurrent.CompletableFuture;

public class EnchantmentGenerator extends FabricDynamicRegistryProvider {
    public EnchantmentGenerator(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
        System.out.println("TEST ENCHANT");
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup registries, Entries entries) {

        register(entries, ModEnchantments.LOCKING_CURSE, Enchantment.builder(
                                Enchantment.definition(
                                        // any items
                                        registries.getWrapperOrThrow(RegistryKeys.ITEM).getOrThrow(ItemTags.VANISHING_ENCHANTABLE),
                                        //0 so no enchant table
                                        1,
                                        // max lvl 1
                                        1,
                                        // (shouldnt be obtainable)
                                        Enchantment.leveledCost(25, 0),
                                        Enchantment.leveledCost(50, 0),
                                        // anvil cost
                                        10,
                                        // any slot
                                        AttributeModifierSlot.ANY
                                )
                        )
                        // Add the curse properties
                        .addEffect(
                                // Using POST_ATTACK as a placeholder - the actual binding logic will be handled elsewhere
                                EnchantmentEffectComponentTypes.POST_ATTACK,
                                EnchantmentEffectTarget.ATTACKER,
                                EnchantmentEffectTarget.VICTIM,
                                new LockingCurseEffect(EnchantmentLevelBasedValue.constant(1.0f))
                        )
        );
    }

    private void register(Entries entries, RegistryKey<Enchantment> key, Enchantment.Builder builder, ResourceCondition... resourceConditions) {
        entries.add(key, builder.build(key.getValue()), resourceConditions);
    }

    @Override
    public String getName() {
        return "LockingCurseGenerator";
    }
}
