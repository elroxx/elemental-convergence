package com.elementalconvergence.entity;

import com.elementalconvergence.item.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ConductiveArrowEntity extends PersistentProjectileEntity {

    private Vec3d shooterOriginalPos;
    private boolean hasLanded = false;

    public ConductiveArrowEntity(EntityType<? extends ConductiveArrowEntity> entityType, World world) {
        super(entityType, world);
    }

    public ConductiveArrowEntity(World world, LivingEntity owner, ItemStack stack, ItemStack shotFrom) {
        super(ModEntities.CONDUCTIVE_ARROW, owner, world, stack, shotFrom);

        //keep original pos
        if (owner != null) {
            this.shooterOriginalPos = owner.getPos();
        }
    }

    @Override
    public void tick() {
        super.tick();

        //trail
        if (this.getWorld().isClient) {
            spawnParticleTrail();
        }
    }

    private void spawnParticleTrail() {
        World world = this.getWorld();
        Vec3d pos = this.getPos();

        //electric particles
        for (int i = 0; i < 3; i++) {
            double offsetX = (this.random.nextDouble() - 0.5) * 0.3;
            double offsetY = (this.random.nextDouble() - 0.5) * 0.3;
            double offsetZ = (this.random.nextDouble() - 0.5) * 0.3;

            world.addParticle(ParticleTypes.ELECTRIC_SPARK,
                    pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ,
                    0, 0, 0);

            //soul fire
            if (this.random.nextFloat() < 0.3f) {
                world.addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                        pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ,
                        0, 0, 0);
            }
        }
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        if (!this.getWorld().isClient && !hasLanded) {
            handleLanding();
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
    }

    private void handleLanding() {
        hasLanded = true;

        Entity owner = this.getOwner();
        if (owner instanceof ServerPlayerEntity player && this.getWorld() instanceof ServerWorld serverWorld) {
            Vec3d arrowPos = this.getPos();

            //spawn lightning
            if (shooterOriginalPos != null) {
                LightningEntity lightning1 = EntityType.LIGHTNING_BOLT.create(serverWorld);
                if (lightning1 != null) {
                    lightning1.refreshPositionAfterTeleport(shooterOriginalPos);
                    serverWorld.spawnEntity(lightning1);
                }
            }

            //spawn lightning part 2
            LightningEntity lightning2 = EntityType.LIGHTNING_BOLT.create(serverWorld);
            if (lightning2 != null) {
                lightning2.refreshPositionAfterTeleport(arrowPos);
                serverWorld.spawnEntity(lightning2);
            }

            //tp
            player.teleport(serverWorld, arrowPos.x, arrowPos.y, arrowPos.z,
                    player.getYaw(), player.getPitch());

            //sound
            player.playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);

            //kill
            this.discard();
        }
    }

    @Override
    protected ItemStack getDefaultItemStack() {
        return new ItemStack(ModItems.CONDUCTIVE_ARROW);
    }

    @Override
    public boolean hasNoGravity() {
        return false; // Arrow is affected by gravity
    }
}