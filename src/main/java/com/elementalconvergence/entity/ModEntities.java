package com.elementalconvergence.entity;

import com.elementalconvergence.ElementalConvergence;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModEntities {

    // Entity Registration
    public static final EntityType<ShadowballEntity> SHADOWBALL = Registry.register(
            Registries.ENTITY_TYPE,
            ElementalConvergence.id("shadowball"),
            EntityType.Builder.<ShadowballEntity>create(ShadowballEntity::new, SpawnGroup.MISC)
                    .dimensions(0.25f, 0.25f) // Same size as snowball
                    .build("shadowball")
    );

    // Register method to register the entity type with the game
    public static <T extends Entity> EntityType<T> register(String name, EntityType<T> entityType) {
        return Registry.register(Registries.ENTITY_TYPE, ElementalConvergence.id(name), entityType);
    }

    // Initialize the class by printing a message
    public static void initialize() {
        EntityRendererRegistry.register(SHADOWBALL, (context) ->
                new FlyingItemEntityRenderer<>(context));
        System.out.println("ModEntities initialized");
    }
}
