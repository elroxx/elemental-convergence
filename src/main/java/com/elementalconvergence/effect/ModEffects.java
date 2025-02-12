package com.elementalconvergence.effect;

import com.elementalconvergence.ElementalConvergence;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModEffects {

    public static StatusEffect PLAGUE = register("plague", new PlagueEffect());



    public static StatusEffect register(String id, StatusEffect statusEffect) {
        Registry.register(
                Registries.STATUS_EFFECT,
                ElementalConvergence.id(id),
                statusEffect
        );
        return statusEffect;
    }

    public static void initialize(){
        System.out.println("Elemental convergence status effects initialized");
    }
}
