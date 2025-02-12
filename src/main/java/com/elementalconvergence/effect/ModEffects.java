package com.elementalconvergence.effect;

import com.elementalconvergence.ElementalConvergence;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;

public class ModEffects {

    public static final RegistryEntry<StatusEffect> PLAGUE = register("plague", new PlagueEffect());


    private static RegistryEntry<StatusEffect> register(String id, StatusEffect statusEffect) {
        RegistryEntry<StatusEffect> registryEntry= Registry.registerReference(
                Registries.STATUS_EFFECT,
                ElementalConvergence.id(id),
                statusEffect
        );
        return registryEntry;
    }

    public static void initialize(){
        System.out.println("Elemental convergence status effects initialized");
    }
}
