package com.elementalconvergence.magic.handlers;

import com.elementalconvergence.ElementalConvergence;
import com.elementalconvergence.entity.ModEntities;
import com.elementalconvergence.magic.IMagicHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class LifeMagicHandler implements IMagicHandler {
    //LIFE ID=6

    @Override
    public void handleRightClick(PlayerEntity player) {

    }

    @Override
    public void handlePassive(PlayerEntity player) {

    }

    @Override
    public void handleAttack(PlayerEntity player, Entity victim) {
    }

    @Override
    public void handleKill(PlayerEntity player, Entity victim) {
        if (!victim.isAlive()){
            //TagKey<EntityType<?>> UNDEAD = TagKey.of(Registries.ENTITY_TYPE.getKey(), ElementalConvergence.id("skeletons"));
            boolean isUndead = victim.getType().isIn(EntityTypeTags.UNDEAD);
            boolean isZombie = victim.getType().isIn(EntityTypeTags.ZOMBIES);
            boolean isSkeleton = victim.getType().isIn(EntityTypeTags.SKELETONS);
            System.out.println("UNDEAD:"+isUndead+", ZOMBIE:"+isZombie+", SKELETON:"+isSkeleton);
            System.out.println(victim.getType());
            boolean isInanimate = !victim.isLiving();
            if (!(isUndead || isInanimate || isZombie || isSkeleton)){
                System.out.println();
                player.kill();
                player.getWorld().playSound(
                        null, //So that everybody hears it
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        ModEntities.DEATH_SOUND_EVENT,
                        SoundCategory.MASTER,
                        1.0f,
                        1.0f
                );
            }
        }
    }

@Override
public void handleMine(PlayerEntity player) {
}

@Override
public void handleBlockBreak(PlayerEntity player, BlockPos pos, BlockState state, BlockEntity entity) {

}

@Override
public void handlePrimarySpell(PlayerEntity player) {

}

@Override
public void handleSecondarySpell(PlayerEntity player) {

}

@Override
public void handleTertiarySpell(PlayerEntity player) {

}
}
