package com.elementalconvergence.effect;

import com.elementalconvergence.ElementalConvergence;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;

public class ModEffects {

    public static final RegistryEntry<StatusEffect> PLAGUE = register("plague", new PlagueEffect());
    public static final RegistryEntry<StatusEffect> GRAVITY_INSTABILITY = register("gravity_instability", new GravityInstabilityEffect());
    public static final RegistryEntry<StatusEffect> LIGHT_PHASING = register("light_phasing", new LightPhasingEffect());
    public static final RegistryEntry<StatusEffect> FULL_BLINDNESS = register("full_blindness", new FullBlindnessEffect());
    public static final RegistryEntry<StatusEffect> GILLS = register("gills", new GillsEffect());
    public static final RegistryEntry<StatusEffect> DROWNING = register("drowning", new DrowningEffect());
    public static final RegistryEntry<StatusEffect> EVAPORATED = register("evaporated", new EvaporatedEffect());
    public static final RegistryEntry<StatusEffect> GUARDIAN_ANGEL = register("guardian_angel", new GuardianAngelEffect());
    public static final RegistryEntry<StatusEffect> WINGS = register("wings", new WingsEffect());


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
