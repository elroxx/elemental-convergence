package com.elementalconvergence.magic.handlers;

import com.elementalconvergence.data.IMagicDataSaver;
import com.elementalconvergence.data.MagicData;
import com.elementalconvergence.effect.ModEffects;
import com.elementalconvergence.magic.IMagicHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;


public class FireMagicHandler implements IMagicHandler {
    private int fireResCooldown=50;
    private int waterHurtCooldown=10;
    private int fireIndex=2; //just to keep track
    private static final int DEFAULT_DIMSWAP_COOLDOWN=40; //2 seconds
    private int dimensionSwapCooldown=0;
    private static final int DEFAULT_FIREBALL_COOLDOWN=5; //0.25 seconds
    private int fireballCooldown=0;
    private static final int DEFAULT_NAPALM_COOLDOWN=20*60; //1*60 seconds (so we have 60 seconds aka 1 minute)
    private int napalmCooldown=0;

    private static final int DEFAULT_FURNACE_COOLDOWN=10;
    private int furnaceCooldown=0;


    @Override //Spell lvl 1 here
    public void handleItemRightClick(PlayerEntity player) {
        ItemStack mainHand = player.getMainHandStack();
        ItemStack offHand = player.getOffHandStack();

        IMagicDataSaver dataSaver = (IMagicDataSaver) player;
        MagicData magicData = dataSaver.getMagicData();
        int fireLevel = magicData.getMagicLevel(2);
        if (fireLevel>=1) {

            if (fireballCooldown == 0) {
                if (mainHand.isOf(Items.COAL) || mainHand.isOf(Items.CHARCOAL) || offHand.isOf(Items.COAL) || offHand.isOf(Items.CHARCOAL)) {


                    double speed = 1.0;
                    int explosionPower = 3;
                    Vec3d rotation = player.getRotationVec(1.0F);
                    Vec3d velocity = rotation.multiply(speed);

                    FireballEntity fireball = new FireballEntity(player.getWorld(), player, velocity, explosionPower);

                    fireball.setPosition(player.getX() + rotation.x * 1.5, player.getY() + 1.0, player.getZ() + rotation.z * 1.5);

                    player.getWorld().spawnEntity(fireball);

                    if (!player.getAbilities().creativeMode) {
                        if (mainHand.isOf(Items.COAL) || mainHand.isOf(Items.CHARCOAL)) {
                            mainHand.decrement(1);
                        }else {
                            offHand.decrement(1);
                        }
                    }

                    player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.PLAYERS, 1.0F, 1.0F);
                    fireballCooldown=DEFAULT_FIREBALL_COOLDOWN;
                }
            }
        }
    }

    @Override
    public void handleEntityRightClick(PlayerEntity player, Entity targetEntity) {

    }

    @Override
    public void handlePassive(PlayerEntity player) {
        //Fire Res passive
        if (this.fireResCooldown<=0) {
            fireResCooldown=100;
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 219, 0, false, false, false));
        }
        else{
            fireResCooldown--;
        }

        //Getting hurt with rain or water
        if (player.isTouchingWaterOrRain()){
            if (this.waterHurtCooldown<=0) {
                DamageSource drowning = player.getWorld().getDamageSources().drown();
                player.damage(drowning, 2);
                waterHurtCooldown=10;
            }
            else{
                waterHurtCooldown--;
            }
        }
        else{
            waterHurtCooldown=10;
        }

        //UPDATE COOLDOWNS:
        if (dimensionSwapCooldown>0){
            dimensionSwapCooldown--;
        }
        if (fireballCooldown>0){
            fireballCooldown--;
        }
        if (napalmCooldown>0){
            napalmCooldown--;
        }
        if (furnaceCooldown>0){
            furnaceCooldown--;
        }
    }

    @Override //BUFF HERE
    public void handleAttack(PlayerEntity player, Entity victim) {
        if (victim instanceof LivingEntity){
            if (victim.isOnFire()){
                DamageSource playerdmg = player.getWorld().getDamageSources().playerAttack(player);
                victim.damage(playerdmg, 5.0f); //Deal 2.5 heart more dmg on entities on fire.
            }
        }
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

    @Override //SPELL LVL 2 HERE
    public void handlePrimarySpell(PlayerEntity player) {
        IMagicDataSaver dataSaver = (IMagicDataSaver) player;
        MagicData magicData = dataSaver.getMagicData();
        int fireLevel = magicData.getMagicLevel(2);
        if (fireLevel>=2){

            if (dimensionSwapCooldown==0) {
                dimensionSwapCooldown=DEFAULT_DIMSWAP_COOLDOWN;

                String currentDimension = player.getWorld().getRegistryKey().getValue().toString();
                if (currentDimension.equals("minecraft:overworld")) {
                    System.out.println("IN OVERWORLD");
                    RegistryKey<World> netherkey = World.NETHER;
                    MinecraftServer server = player.getWorld().getServer();
                    if (server == null) {
                        return;
                    }
                    ServerWorld nether = server.getWorld(netherkey);


                    double netherPosX = player.getPos().x / 8.0;
                    double netherPosY = (player.getPos().y + 64.0) / 2.0;
                    double netherPosZ = player.getPos().z / 8.0;

                    //Making it safe
                    //Air
                    BlockPos feetblock = new BlockPos((int) Math.floor(netherPosX), (int) Math.floor(netherPosY), (int) Math.floor(netherPosZ));
                    destroyBlockAndDropItems(nether, feetblock);
                    BlockPos headblock = new BlockPos((int) Math.floor(netherPosX), ((int) Math.floor(netherPosY)) + 1, (int) Math.floor(netherPosZ));
                    destroyBlockAndDropItems(nether, headblock);
                    BlockPos overheadblock = new BlockPos((int) Math.floor(netherPosX), ((int) Math.floor(netherPosY)) + 2, (int) Math.floor(netherPosZ));
                    destroyBlockAndDropItems(nether, overheadblock);
                    //Netherrack
                    BlockPos underfeetblock = new BlockPos((int) Math.floor(netherPosX), ((int) Math.floor(netherPosY)) - 1, (int) Math.floor(netherPosZ));
                    BlockState underfeetstate = nether.getBlockState(underfeetblock);
                    if (!underfeetstate.hasSolidTopSurface(nether, underfeetblock, player)) {
                        underfeetstate.getBlock().dropStacks(underfeetstate, nether, underfeetblock);
                        nether.setBlockState(underfeetblock, Blocks.NETHERRACK.getDefaultState());
                    }

                    float yaw = player.getYaw();
                    float pitch = player.getPitch();
                    Set<PositionFlag> flags = EnumSet.of(PositionFlag.X, PositionFlag.Y, PositionFlag.Z, PositionFlag.X_ROT, PositionFlag.Y_ROT);
                    player.teleport(nether, netherPosX, netherPosY, netherPosZ, flags, yaw, pitch);
                } else if (currentDimension.equals("minecraft:the_nether")) {
                    RegistryKey<World> overworldkey = World.OVERWORLD;
                    MinecraftServer server = player.getWorld().getServer();
                    if (server == null) {
                        return;
                    }
                    ServerWorld overworld = server.getWorld(overworldkey);

                    double owPosX = player.getPos().x * 8.0;
                    double owPosY = (player.getPos().y) * 2.0 - 64;
                    double owPosZ = player.getPos().z * 8.0;

                    //Air
                    BlockPos feetblock = new BlockPos((int) Math.floor(owPosX), (int) Math.floor(owPosY), (int) Math.floor(owPosZ));
                    destroyBlockAndDropItems(overworld, feetblock);
                    BlockPos headblock = new BlockPos((int) Math.floor(owPosX), ((int) Math.floor(owPosY)) + 1, (int) Math.floor(owPosZ));
                    destroyBlockAndDropItems(overworld, headblock);
                    BlockPos overheadblock = new BlockPos((int) Math.floor(owPosX), ((int) Math.floor(owPosY)) + 2, (int) Math.floor(owPosZ));
                    destroyBlockAndDropItems(overworld, overheadblock);
                    //Grass
                    BlockPos underfeetblock = new BlockPos((int) Math.floor(owPosX), ((int) Math.floor(owPosY)) - 1, (int) Math.floor(owPosZ));
                    BlockState underfeetstate = overworld.getBlockState(underfeetblock);
                    if (!underfeetstate.hasSolidTopSurface(overworld, underfeetblock, player)) {
                        underfeetstate.getBlock().dropStacks(underfeetstate, overworld, underfeetblock);
                        overworld.setBlockState(underfeetblock, Blocks.GRASS_BLOCK.getDefaultState());
                    }

                    float yaw = player.getYaw();
                    float pitch = player.getPitch();
                    Set<PositionFlag> flags = EnumSet.of(PositionFlag.X, PositionFlag.Y, PositionFlag.Z, PositionFlag.X_ROT, PositionFlag.Y_ROT);
                    player.teleport(overworld, owPosX, owPosY, owPosZ, flags, yaw, pitch);
                }
                player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
                fireballCooldown=DEFAULT_FIREBALL_COOLDOWN;
            }

        }

        //VERIFICATION IF THE SPELL WAS ACTUALLY CAST BY THE SERVER INSTEAD OF THE CLIENT
        /*player.sendMessage(Text.of("test"));
        System.out.println("Primary spell cast on " +
                (player.getWorld().isClient() ? "CLIENT" : "SERVER") +
                " side by player: " + player.getName().getString());*/
    }

    @Override
    public void handleSecondarySpell(PlayerEntity player) {
        IMagicDataSaver dataSaver = (IMagicDataSaver) player;
        MagicData magicData = dataSaver.getMagicData();
        int fireLevel = magicData.getMagicLevel(2);
        if (fireLevel>=3 && furnaceCooldown==0) {

            if (player.hasStatusEffect(ModEffects.FURNACE)) {
                player.removeStatusEffect(ModEffects.FURNACE);

                player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS,1.0f, 1.0f);
            }
            else{
                player.addStatusEffect(new StatusEffectInstance(ModEffects.FURNACE, -1, 0, true, false ,true));
                player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ENTITY_BLAZE_BURN, SoundCategory.PLAYERS,1.0f, 1.0f);
            }


            //OLD LEVEL 3
            /*if (napalmCooldown==0){
                napalmCooldown = DEFAULT_NAPALM_COOLDOWN;
                int radius=15;
                int innerRadius=5;
                int damage=10;
                Vec3d center = player.getPos();
                World world = player.getWorld();


                int baseHeight=0;
                if (world.getBlockState(new BlockPos((int)center.x, (int)center.y-1, (int)center.z)).isAir()){
                    baseHeight=-radius;
                }

                for (int i=-radius; i<=radius; i++){ //X pos (ALL RADIUS)
                    for (int j=baseHeight; j<=radius; j++){ //Y pos (ONLY higher than player)
                        for (int k=-radius; k<=radius;k++) { //Z pos
                            BlockPos currentBlock = new BlockPos(
                                    (int) center.x+i,
                                    (int) center.y+j,
                                    (int) center.z+k
                            );

                            Random random = new Random();
                            float chanceOfBlockInFire=0.3f;
                            float chanceOfBlockInFireInner=0.5f;
                            float rngf = random.nextFloat();
                            if (currentBlock.isWithinDistance(center, innerRadius) && world.getBlockState(currentBlock).isAir()){
                                if (rngf<chanceOfBlockInFireInner){
                                    world.setBlockState(currentBlock, Blocks.FIRE.getDefaultState());
                                }
                            }
                            else if (currentBlock.isWithinDistance(center, radius) && world.getBlockState(currentBlock).isAir()  && rngf<chanceOfBlockInFire){
                                world.setBlockState(currentBlock, Blocks.FIRE.getDefaultState());

                            }
                        }
                    }
                }

                // to dmg all entities in the radius
                List<Entity> entities = world.getEntitiesByClass(
                        Entity.class,
                        new Box(center.add(-radius, -2, -radius),
                                center.add(radius, 2, radius)),
                        entity -> entity != player  // TO AVOID SELF-DAMAGE
                );

                for(Entity entity : entities) {
                    if(entity.squaredDistanceTo(center) <= radius * radius) {
                        entity.damage(world.getDamageSources().inFire(), damage);
                        entity.setOnFireFor(200);
                    }
                }

                // SOUND EFFECT
                world.playSound(null, center.x, center.y, center.z,
                        SoundEvents.ENTITY_DRAGON_FIREBALL_EXPLODE, SoundCategory.PLAYERS, 2.0F, 1.0F);
                
                //Particle effects
                ServerWorld serverWorld = (ServerWorld) world;
                serverWorld.spawnParticles(ParticleTypes.EXPLOSION,
                        center.x, center.y, center.z,
                        300,
                        radius/2.0,
                        2,
                        radius/2.0,
                        0.1
                );
                serverWorld.spawnParticles(ParticleTypes.FLAME,
                        center.x, center.y, center.z,
                        300,
                        radius/2.0,
                        2,
                        radius/2.0,
                        0.1
                );
                
            }*/
        }

    }

    @Override
    public void handleTertiarySpell(PlayerEntity player) {

    }

    public void destroyBlockAndDropItems(ServerWorld world, BlockPos pos){
        BlockState blockState = world.getBlockState(pos);

        if (!blockState.isAir()){
            blockState.getBlock().dropStacks(blockState, world, pos);

            world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
        }
    }
}
