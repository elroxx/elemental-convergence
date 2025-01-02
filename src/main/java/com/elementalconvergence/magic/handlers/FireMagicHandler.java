package com.elementalconvergence.magic.handlers;

import com.elementalconvergence.data.IMagicDataSaver;
import com.elementalconvergence.data.MagicData;
import com.elementalconvergence.magic.IMagicHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.Set;


public class FireMagicHandler implements IMagicHandler {
    private int fireResCooldown=50;
    private int waterHurtCooldown=10;
    private int fireIndex=2; //just to keep track


    @Override
    public void handleRightClick(PlayerEntity player) {
        //player.sendMessage(Text.of("test"));
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
    }

    @Override
    public void handleAttack(PlayerEntity player, Entity victim) {
        if (victim instanceof LivingEntity){
            if (victim.isOnFire()){
                DamageSource playerdmg = player.getWorld().getDamageSources().playerAttack(player);
                victim.damage(playerdmg, 5.0f); //Deal 2.5 heart more dmg on entities on fire.
            }
        }
    }

    @Override
    public void handlePrimarySpell(PlayerEntity player) {
        IMagicDataSaver dataSaver = (IMagicDataSaver) player;
        MagicData magicData = dataSaver.getMagicData();
        int fireLevel = magicData.getMagicLevel(2);
        if (fireLevel>=2){
            String currentDimension = player.getWorld().getRegistryKey().getValue().toString();
            //System.out.println("HERE");
            //System.out.println(currentDimension);
            //System.out.println(currentDimension.equals("minecraft:the_nether"));
            if (currentDimension.equals("minecraft:overworld")){
                System.out.println("IN OVERWORLD");
                RegistryKey<World> netherkey = World.NETHER;
                MinecraftServer server = player.getWorld().getServer();
                if (server==null){
                    return;
                }
                ServerWorld nether = server.getWorld(netherkey);


                double netherPosX = player.getPos().x/8.0;
                double netherPosY = (player.getPos().y+64.0)/2.0;
                double netherPosZ = player.getPos().z/8.0;

                //Making it safe
                //Air
                BlockPos feetblock = new BlockPos((int) Math.floor(netherPosX), (int) Math.floor(netherPosY), (int) Math.floor(netherPosZ));
                destroyBlockAndDropItems(nether, feetblock);
                BlockPos headblock = new BlockPos((int) Math.floor(netherPosX), ((int) Math.floor(netherPosY))+1, (int) Math.floor(netherPosZ));
                destroyBlockAndDropItems(nether, headblock);
                BlockPos overheadblock = new BlockPos((int) Math.floor(netherPosX), ((int) Math.floor(netherPosY))+2, (int) Math.floor(netherPosZ));
                destroyBlockAndDropItems(nether, overheadblock);
                //Netherrack
                BlockPos underfeetblock = new BlockPos((int) Math.floor(netherPosX), ((int) Math.floor(netherPosY))-1, (int) Math.floor(netherPosZ));
                BlockState underfeetstate = nether.getBlockState(underfeetblock);
                if (!underfeetstate.hasSolidTopSurface(nether, underfeetblock, player)) {
                    underfeetstate.getBlock().dropStacks(underfeetstate, nether, underfeetblock);
                    nether.setBlockState(underfeetblock, Blocks.NETHERRACK.getDefaultState());
                }

                float yaw = player.getYaw();
                float pitch = player.getPitch();
                Set<PositionFlag> flags = EnumSet.of(PositionFlag.X, PositionFlag.Y, PositionFlag.Z, PositionFlag.X_ROT, PositionFlag.Y_ROT);
                player.teleport(nether, netherPosX, netherPosY, netherPosZ, flags, yaw, pitch);
            }
            else if(currentDimension.equals("minecraft:the_nether")){
                RegistryKey<World> overworldkey = World.OVERWORLD;
                MinecraftServer server = player.getWorld().getServer();
                if (server==null){
                    return;
                }
                ServerWorld overworld = server.getWorld(overworldkey);

                double owPosX = player.getPos().x*8.0;
                double owPosY = (player.getPos().y)*2.0-64;
                double owPosZ = player.getPos().z*8.0;

                //Air
                BlockPos feetblock = new BlockPos((int) Math.floor(owPosX), (int) Math.floor(owPosY), (int) Math.floor(owPosZ));
                destroyBlockAndDropItems(overworld, feetblock);
                BlockPos headblock = new BlockPos((int) Math.floor(owPosX), ((int) Math.floor(owPosY))+1, (int) Math.floor(owPosZ));
                destroyBlockAndDropItems(overworld, headblock);
                BlockPos overheadblock = new BlockPos((int) Math.floor(owPosX), ((int) Math.floor(owPosY))+2, (int) Math.floor(owPosZ));
                destroyBlockAndDropItems(overworld, overheadblock);
                //Grass
                BlockPos underfeetblock = new BlockPos((int) Math.floor(owPosX), ((int) Math.floor(owPosY))-1, (int) Math.floor(owPosZ));
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
            System.out.println();



        }

        //VERIFICATION IF THE SPELL WAS ACTUALLY CAST BY THE SERVER INSTEAD OF THE CLIENT
        /*player.sendMessage(Text.of("test"));
        System.out.println("Primary spell cast on " +
                (player.getWorld().isClient() ? "CLIENT" : "SERVER") +
                " side by player: " + player.getName().getString());*/
    }

    @Override
    public void handleSecondarySpell(PlayerEntity player) {

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
