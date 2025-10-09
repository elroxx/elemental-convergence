package com.elementalconvergence.magic.convergencehandlers;

import com.elementalconvergence.magic.IMagicHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.elementalconvergence.ElementalConvergence.*;

public class ElectricityMagicHandler implements IMagicHandler {
    public static final int ELECTRICITY_INDEX= (BASE_MAGIC_ID.length-1)+12;
    //passive: sine wave motion. I want it drastic, like 1/3 speed up to *3 speed. I need to write it in NBT to save the attributes to be able to restart my sine wave where it supposed to be

    //lvl 1: looking at spyglass activates blocks. MAYBE add a
    //lvl 2: Thunderbow. add a trail of damage that disappears once the arrow hits the ground. Can right click on a redstone dust to teleport to the other side of the redstone trail. DOES NOT CONSUME IT
    //lvl 3: toggle lightning on hits

    //advancements:
    //1: conductive arrow
    //2: bolt armor trim
    //3: zombie head

    // Electric eye: top line: repeater, comparator, repeater, bottom line: copper block, redstone block, copper block
    //2 electric arrow== 4 arrow, 4 redstone, 1 lightning rod. Gives 4 arrows.





    @Override
    public void handleItemRightClick(PlayerEntity player) {

    }

    @Override
    public void handleEntityRightClick(PlayerEntity player, Entity targetEntity) {

    }

    @Override
    public void handlePassive(PlayerEntity player) {

        //Lvl 1
        if (player.isUsingSpyglass()) {
            //raycast to get block
            HitResult hitResult = player.raycast(256.0, 0.0f, false);

            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHit = (BlockHitResult) hitResult;
                BlockPos targetPos = blockHit.getBlockPos();
                World world = player.getWorld();

                //power the block
                powerBlock(world, targetPos);
            }
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

    }

    @Override
    public void handleSecondarySpell(PlayerEntity player) {

    }

    @Override
    public void handleTertiarySpell(PlayerEntity player) {

    }

    private void powerBlock(World world, BlockPos pos) {
        //create map if doesnt exist yet
        Map<BlockPos, Integer> worldPowers = poweredBlocks.computeIfAbsent(world, k -> new HashMap<>());

        //add power duration
        worldPowers.put(pos.toImmutable(), MIN_POWER_TICKS);

        //update neighbors
        world.updateNeighborsAlways(pos, world.getBlockState(pos).getBlock());
    }

    public static void decayPoweredBlocks(MinecraftServer server) {
        for (World world : server.getWorlds()) {
            Map<BlockPos, Integer> worldPowers = poweredBlocks.get(world);
            if (worldPowers == null) {
                continue;
            }
            Iterator<Map.Entry<BlockPos, Integer>> iterator = worldPowers.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<BlockPos, Integer> entry = iterator.next();
                int ticksLeft = entry.getValue() - 1;

                if (ticksLeft <= 0) {
                    //remove power
                    BlockPos pos = entry.getKey();
                    iterator.remove();

                    //update neighbors again
                    world.updateNeighborsAlways(pos, world.getBlockState(pos).getBlock());
                } else {
                    //decrement counter
                    entry.setValue(ticksLeft);
                }
            }
        }
    }

    public static boolean isBlockPoweredBySpyglass(World world, BlockPos pos) {
        Map<BlockPos, Integer> worldPowers = poweredBlocks.get(world);
        return worldPowers != null && worldPowers.containsKey(pos);
    }

    public static int getSpyglassPowerLevel(World world, BlockPos pos) {
        return isBlockPoweredBySpyglass(world, pos) ? 15 : 0;
    }
}

