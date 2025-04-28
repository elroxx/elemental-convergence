package com.elementalconvergence.magic.convergencehandlers;

import com.elementalconvergence.data.IMagicDataSaver;
import com.elementalconvergence.data.MagicData;
import com.elementalconvergence.effect.ModEffects;
import com.elementalconvergence.item.ModItems;
import com.elementalconvergence.magic.IMagicHandler;
import gravity_changer.api.GravityChangerAPI;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.*;

import static com.elementalconvergence.ElementalConvergence.BASE_MAGIC_ID;

public class StarMagicHandler implements IMagicHandler {
    public static final int STAR_INDEX= (BASE_MAGIC_ID.length-1)+3;


    private static final double SHIELD_RADIUS = 5.0D; // radius to capture
    private static final double ORBIT_RADIUS = 2.0D;
    private static final int MAX_PROJECTILES = 5; // max projectiles in orbit

    private final ArrayList<OrbitingProjectile> orbit = new ArrayList<>();
    public static final int HUNGER_DEFAULT_COOLDOWN = 20;
    private int hungerCooldown = 0;

    //FOR RELEASE
    private static final float PROJECTILE_SPEED = 1.5f;
    private static final int PROJECTILE_RELEASE_DELAY = 5;

    private boolean isReleasingProjectiles = false;
    private int releaseTimer = 0;
    private int nextProjectileIndex = 0;
    private UUID releasingPlayerId = null;
    private final Set<Integer> releasedProjectiles = new HashSet<>();




    @Override
    public void handleItemRightClick(PlayerEntity player) {
    }

    @Override
    public void handleEntityRightClick(PlayerEntity player, Entity targetEntity) {

    }

    @Override
    public void handlePassive(PlayerEntity player) {

        //Debuff (hungry guy)
        if (hungerCooldown==0){
            hungerCooldown=HUNGER_DEFAULT_COOLDOWN;
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 119, 1, false, false, false));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 119, 0, false, false, false));
        }


        //Buff
        if (orbit.size() < MAX_PROJECTILES && !isReleasingProjectiles) {
            captureNearbyProjectiles(player, orbit, player.getWorld());
        }

        // Update orbiting projectiles
        updateOrbitingProjectiles(player, orbit);

        //for releasing projectiles
        if (isReleasingProjectiles && player.getUuid().equals(releasingPlayerId)) {
            handleProjectileReleaseSequence(player);
        }


        //Cooldowns
        if (hungerCooldown>0){
            hungerCooldown--;
        }

    }

    @Override
    public void handleAttack(PlayerEntity player, Entity victim) {

    }

    @Override
    public void handleKill(PlayerEntity player, Entity victim) {

    }

    @Override
    public void handleMine(PlayerEntity player) {

    }

    @Override
    public void handleBlockBreak(PlayerEntity player, BlockPos pos, BlockState state, BlockEntity entity) {

    }


    @Override
    public void handlePrimarySpell(PlayerEntity player) {

        if (!orbit.isEmpty() && !isReleasingProjectiles) {
            isReleasingProjectiles = true;
            releaseTimer = 0;
            nextProjectileIndex = 0;
            releasingPlayerId = player.getUuid();

            player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE, SoundCategory.PLAYERS,
                    0.5F, 1.5F);
        }
    }

    @Override
    public void handleSecondarySpell(PlayerEntity player) {

    }

    @Override
    public void handleTertiarySpell(PlayerEntity player) {

    }

    private void captureNearbyProjectiles(PlayerEntity player, List<OrbitingProjectile> orbit, World world) {
        // Find projectiles within shield radius
        List<ProjectileEntity> nearbyProjectiles = world.getEntitiesByClass(
                ProjectileEntity.class,
                player.getBoundingBox().expand(SHIELD_RADIUS),
                projectile -> {
                    // Only capture projectiles that aren't already in orbit and are moving toward the player
                    if (isProjectileInOrbit(projectile, orbit)) {
                        return false;
                    }

                    // Calculate if projectile is moving toward the player
                    Vec3d projectilePos = projectile.getPos();
                    Vec3d playerPos = player.getPos();
                    Vec3d velocity = projectile.getVelocity();

                    Vec3d toPlayer = playerPos.subtract(projectilePos).normalize();
                    double dotProduct = velocity.normalize().dotProduct(toPlayer);

                    // Positive dot product means projectile is moving toward player
                    return dotProduct > 0;
                }
        );

        // Capture projectiles until we reach the limit
        for (ProjectileEntity projectile : nearbyProjectiles) {
            if (orbit.size() >= MAX_PROJECTILES) {
                break;
            }

            // Calculate initial orbit angle
            double angleSpacing = 2 * Math.PI / MAX_PROJECTILES;
            double initialAngle = orbit.size() * angleSpacing;

            // Stop the projectile's original motion
            projectile.setVelocity(Vec3d.ZERO);
            projectile.setNoGravity(true);


            orbit.add(new OrbitingProjectile(projectile.getUuid(), initialAngle, (ServerWorld) world));

        }
    }


    private void updateOrbitingProjectiles(PlayerEntity player, List<OrbitingProjectile> orbit) {
        Vec3d playerPos = player.getPos().add(0, 1, 0); // Orbit around player's center
        World world = player.getWorld();

        // Update each projectile in orbit
        Iterator<OrbitingProjectile> iterator = orbit.iterator();
        while (iterator.hasNext()) {
            OrbitingProjectile orbiting = iterator.next();

            // Find the entity
            Entity entity = world.getEntityById(orbiting.entityId);
            if (!(entity instanceof ProjectileEntity projectile) || entity.isRemoved()) {
                // Remove if the projectile no longer exists
                iterator.remove();
                continue;
            }

            // Update orbit angle
            orbiting.angle += 0.1; // Adjust rotation speed as needed
            if (orbiting.angle > 2 * Math.PI) {
                orbiting.angle -= 2 * Math.PI; // Keep angle within 0-2π
            }

            // Calculate new position based on angle
            double x = playerPos.x + ORBIT_RADIUS * Math.cos(orbiting.angle);
            double y = playerPos.y;
            double z = playerPos.z + ORBIT_RADIUS * Math.sin(orbiting.angle);

            // Update projectile position
            projectile.setPos(x, y, z);
            projectile.setVelocity(Vec3d.ZERO);
        }
    }

    private boolean isProjectileInOrbit(ProjectileEntity projectile, List<OrbitingProjectile> orbit) {
        int projectileId = projectile.getId(); // Get the entity ID instead of UUID
        for (OrbitingProjectile orbiting : orbit) {
            if (orbiting.entityId == projectileId) { // Compare IDs
                return true;
            }
        }
        return false;
    }

    private void handleProjectileReleaseSequence(PlayerEntity player) {
        if (releaseTimer > 0) {
            releaseTimer--;
            return;
        }

        if (nextProjectileIndex >= orbit.size() || orbit.isEmpty()) {
            // All projectiles released or no projectiles left
            isReleasingProjectiles = false;
            nextProjectileIndex = 0;
            orbit.clear(); // Clear the orbit after releasing all projectiles
            return;
        }

        // Release the next projectile
        OrbitingProjectile orbiting = orbit.get(nextProjectileIndex);
        Entity entity = player.getWorld().getEntityById(orbiting.entityId);

        if (entity instanceof ProjectileEntity projectile) {
            // Calculate direction based on player's look vector
            Vec3d lookDirection = player.getRotationVector();

            // Get player's position (eyes)
            Vec3d playerEyePos = player.getEyePos();

            // Get projectile's current position
            Vec3d projectilePos = projectile.getPos();

            // Check if projectile is behind the player by using dot product
            Vec3d playerToProjectile = projectilePos.subtract(playerEyePos);
            double dotProduct = playerToProjectile.normalize().dotProduct(lookDirection);

            // If projectile is behind player (negative dot product), reposition it in front
            if (dotProduct < 0) {
                // Position the projectile slightly in front of the player in the direction they're looking
                Vec3d newPos = playerEyePos.add(lookDirection.multiply(1.5));
                projectile.setPos(newPos.x, newPos.y, newPos.z);
            }

            // Set velocity in the direction the player is looking
            projectile.setVelocity(lookDirection.x * PROJECTILE_SPEED,
                    lookDirection.y * PROJECTILE_SPEED,
                    lookDirection.z * PROJECTILE_SPEED);

            // Reset gravity and make it move normally again
            projectile.setNoGravity(false);

            // Play sound effect
            player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS,
                    0.5F, 1.0F);
        }

        // Increment index and reset timer for next projectile
        nextProjectileIndex++;
        releaseTimer = PROJECTILE_RELEASE_DELAY;
    }

    // class for orbiting projectiles
    private static class OrbitingProjectile {
        private final int entityId;
        private double angle;

        public OrbitingProjectile(UUID uuid, double initialAngle, ServerWorld world) {
            Entity entity = world.getEntity(uuid);
            this.entityId = (entity != null) ? entity.getId() : -1; // Entity ID : -1 if not found
            this.angle = initialAngle;
        }
    }

}
