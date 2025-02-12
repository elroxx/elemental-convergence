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
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ModEntities {

    public static final Identifier DEATH_SOUND_ID = ElementalConvergence.id("life_death_sound");
    public static final SoundEvent DEATH_SOUND_EVENT = SoundEvent.of(DEATH_SOUND_ID);

    // Entity Registration
    public static final EntityType<ShadowballEntity> SHADOWBALL = Registry.register(
            Registries.ENTITY_TYPE,
            ElementalConvergence.id("shadowball"),
            EntityType.Builder.<ShadowballEntity>create(ShadowballEntity::new, SpawnGroup.MISC)
                    .dimensions(0.25f, 0.25f) // Same size as snowball
                    .build()
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
        registerSounds();
        System.out.println("Sounds initialized");
    }

    public static void registerSounds() {
        Registry.register(Registries.SOUND_EVENT, DEATH_SOUND_ID, DEATH_SOUND_EVENT);
    }
}
