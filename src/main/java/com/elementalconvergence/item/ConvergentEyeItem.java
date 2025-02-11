package com.elementalconvergence.item;

import com.elementalconvergence.ElementalConvergence;
import com.elementalconvergence.block.ModBlocks;
import com.elementalconvergence.data.IMagicDataSaver;
import com.elementalconvergence.magic.MagicRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

public class ConvergentEyeItem extends Item {
    private final int magicIndex;
    private final String magicString;



    public ConvergentEyeItem(Settings settings, int magicIndex) {
        super(settings);
        this.magicIndex = magicIndex;
        this.magicString = ElementalConvergence.FULL_MAGIC_DISPLAY[magicIndex];
    }

    public String getMagicType() {
        return magicString;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (!context.getWorld().isClient()) {
            BlockPos positionClicked = context.getBlockPos();
            BlockState clickedBlock = context.getWorld().getBlockState(positionClicked);
            PlayerEntity player = context.getPlayer();

            // Check if the clicked block is an altar of convergence
            if (clickedBlock.getBlock() == ModBlocks.ALTAR_OF_CONVERGENCE) {
                // Removing the item
                context.getStack().setCount(0);

                //SELECTING MAGIC HERE
                IMagicDataSaver dataSaver = (IMagicDataSaver) player;
                dataSaver.getMagicData().setSelectedMagic(magicIndex);

                //RESETTING PLAYER DATA WHEN USING AN EYE
                MagicRegistry.resetPlayerStats(player);

                player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.BLOCKS, 2.0F, 1.0F);
                if (player.getWorld() instanceof ServerWorld serverWorld) {
                    serverWorld.spawnParticles(
                            ParticleTypes.END_ROD,
                            positionClicked.getX() + 0.5,
                            positionClicked.getY() + 0.5,
                            positionClicked.getZ() + 0.5,
                            10,
                            0.25,
                            0.25, //so that they rise a little
                            0.25,
                            0);
                }


                return ActionResult.success(true);
            }
        }

        return ActionResult.PASS;
    }



}