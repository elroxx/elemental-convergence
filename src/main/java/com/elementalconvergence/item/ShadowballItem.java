package com.elementalconvergence.item;

import com.elementalconvergence.entity.ShadowballEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SnowballItem;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.world.World;

public class ShadowballItem extends SnowballItem {
    public ShadowballItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {

        ItemStack itemStack = user.getStackInHand(hand);
        // Shadow sound when launching the item
        world.playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.NEUTRAL,
                0.3F, 1.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));

        if (!world.isClient) {
            // Creation of the entity
            ShadowballEntity shadowballEntity = new ShadowballEntity(world, user);
            shadowballEntity.setItem(itemStack);
            shadowballEntity.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, 1.5F, 1.0F);

            // Store owner-specific data in the entity's DataTracker
            shadowballEntity.setOwner(user);

            // CAN ADD PARTICLE EFFECTS BUT I HATED THEM
            /*((ServerWorld) world).spawnParticles(
                    ParticleTypes.SCULK_SOUL,
                    user.getX(), user.getY() + 1.5, user.getZ(),
                    10, 0.2, 0.2, 0.2, 0.01
            );*/

            world.spawnEntity(shadowballEntity);
        }

        user.incrementStat(Stats.USED.getOrCreateStat(this));
        if (!user.getAbilities().creativeMode) {
            itemStack.decrement(1);
        }

        return TypedActionResult.success(itemStack, world.isClient());
    }

    @Override
    public ProjectileEntity createEntity(World world, Position pos, ItemStack stack, Direction direction) {
        ShadowballEntity shadowballEntity = new ShadowballEntity(
                world,
                pos.getX(),
                pos.getY(),
                pos.getZ()
        );
        shadowballEntity.setItem(stack);
        return shadowballEntity;
    }
}
