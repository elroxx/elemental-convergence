package com.elementalconvergence.worldgen;

import com.elementalconvergence.block.ModBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

import java.util.ArrayList;
import java.util.List;

public class PrayingAltarFeature extends Feature<DefaultFeatureConfig> {

    public PrayingAltarFeature(Codec<DefaultFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        StructureWorldAccess world = context.getWorld();
        Random random = context.getRandom();

        // origin
        BlockPos spawnPos = new BlockPos(0, 0, 0);

        int maxY = Integer.MIN_VALUE;
        List<BlockPos> highestPositions = new ArrayList<>();

        int radius = 1; //200x200 around spawn

        for (int x = spawnPos.getX() - radius; x <= spawnPos.getX() + radius; x++) {
            for (int z = spawnPos.getZ() - radius; z <= spawnPos.getZ() + radius; z++) {
                // get highest non air block
                for (int y = world.getTopY() - 1; y >= world.getBottomY(); y--) {
                    BlockPos checkPos = new BlockPos(x, y, z);
                    BlockState state = world.getBlockState(checkPos);

                    if (!state.isAir()) {
                        // Found highest block at this position (so lowering from top)
                        if (y > maxY) {
                            // max y
                            maxY = y;
                            highestPositions.clear();
                            highestPositions.add(checkPos.up()); //add to list
                        } else if (y == maxY) {
                            highestPositions.add(checkPos.up()); //in list to randomize
                        }
                        break;
                    }
                }
            }
        }

        // place at random pos
        if (!highestPositions.isEmpty()) {
            BlockPos selectedPos = highestPositions.get(random.nextInt(highestPositions.size()));

            //verify if air above block(stupid if not there tho tbh)
            if (world.getBlockState(selectedPos).isAir()) {
                world.setBlockState(selectedPos, ModBlocks.PRAYING_ALTAR.getDefaultState(), 3);
                return true;
            }
        }

        return false;
    }
}
