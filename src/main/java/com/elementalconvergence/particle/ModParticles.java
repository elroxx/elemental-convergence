package com.elementalconvergence.particle;

import com.elementalconvergence.ElementalConvergence;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModParticles {

    public static final SimpleParticleType ATOM_PARTICLE = FabricParticleTypes.simple();



    public static void initialize(){
        Registry.register(Registries.PARTICLE_TYPE, ElementalConvergence.id("atom"), ATOM_PARTICLE);
    }
}
