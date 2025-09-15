package com.elementalconvergence.entity;

import com.elementalconvergence.ElementalConvergence;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ModEntities {

    public static final Identifier DEATH_SOUND_ID = ElementalConvergence.id("life_death_sound");
    public static final SoundEvent DEATH_SOUND_EVENT = SoundEvent.of(DEATH_SOUND_ID);

    public static final Identifier TRAINWHISTLE_SOUND_ID = ElementalConvergence.id("train_whistle_sound");
    public static final SoundEvent TRAINWHISTLE_SOUND_EVENT = SoundEvent.of(TRAINWHISTLE_SOUND_ID);

    // Entity Registration (server-side)
    public static final EntityType<ShadowballEntity> SHADOWBALL = Registry.register(
            Registries.ENTITY_TYPE,
            ElementalConvergence.id("shadowball"),
            EntityType.Builder.<ShadowballEntity>create(ShadowballEntity::new, SpawnGroup.MISC)
                    .dimensions(0.25f, 0.25f)
                    .build()
    );

    public static final EntityType<MinionZombieEntity> MINION_ZOMBIE = Registry.register(
            Registries.ENTITY_TYPE,
            ElementalConvergence.id("minion_zombie"),
            EntityType.Builder.create(MinionZombieEntity::new, SpawnGroup.MONSTER)
                    .dimensions(0.6f, 1.95f)
                    .build()
    );

    public static final EntityType<PegasusEntity> PEGASUS = Registry.register(
            Registries.ENTITY_TYPE,
            ElementalConvergence.id("pegasus"),
            EntityType.Builder.create(PegasusEntity::new, SpawnGroup.AMBIENT)
                    .dimensions(1.3965F, 1.6F)
                    .build()
    );

    public static final EntityType<PouletEntity> POULET = Registry.register(
            Registries.ENTITY_TYPE,
            ElementalConvergence.id("pouletpeto"),
            EntityType.Builder.create(PouletEntity::new, SpawnGroup.AMBIENT)
                    .dimensions(0.5F, 0.5F)
                    .build()
    );

    public static final EntityType<MinionBeeEntity> MINION_BEE = Registry.register(
            Registries.ENTITY_TYPE,
            ElementalConvergence.id("minion_bee"),
            EntityType.Builder.create(MinionBeeEntity::new, SpawnGroup.AMBIENT)
                    .dimensions(0.7f, 0.6f)
                    .build()
    );

    public static <T extends Entity> EntityType<T> register(String name, EntityType<T> entityType) {
        return Registry.register(Registries.ENTITY_TYPE, ElementalConvergence.id(name), entityType);
    }

    // Server-side initialization (no renderers!)
    public static void initialize() {
        // Register entity attributes (server-side)
        FabricDefaultAttributeRegistry.register(MINION_ZOMBIE, MinionZombieEntity.createMinionZombieAttributes());
        FabricDefaultAttributeRegistry.register(PEGASUS, HorseEntity.createBaseHorseAttributes());
        FabricDefaultAttributeRegistry.register(POULET, ChickenEntity.createChickenAttributes());
        FabricDefaultAttributeRegistry.register(MINION_BEE, BeeEntity.createBeeAttributes());

        System.out.println("ModEntities initialized (server-side)");

        registerSounds();
        System.out.println("Sounds initialized");
    }

    public static void registerSounds() {
        Registry.register(Registries.SOUND_EVENT, DEATH_SOUND_ID, DEATH_SOUND_EVENT);
        Registry.register(Registries.SOUND_EVENT, TRAINWHISTLE_SOUND_ID, TRAINWHISTLE_SOUND_EVENT);
    }
}