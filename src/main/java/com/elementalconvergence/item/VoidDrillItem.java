package com.elementalconvergence.item;

import com.elementalconvergence.block.ModBlocks;
import com.elementalconvergence.data.IMagicDataSaver;
import com.elementalconvergence.data.MagicData;
import com.elementalconvergence.magic.MagicRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;

import static com.elementalconvergence.magic.convergencehandlers.VoidMagicHandler.DEFAULT_VOID_DRILL_COOLDOWN;
import static com.elementalconvergence.magic.convergencehandlers.VoidMagicHandler.VOID_INDEX;

public class VoidDrillItem extends Item {
    public VoidDrillItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (!context.getWorld().isClient()) {
            BlockPos positionClicked = context.getBlockPos();
            BlockState clickedBlock = context.getWorld().getBlockState(positionClicked);
            ServerPlayerEntity player = (ServerPlayerEntity) context.getPlayer();

            if (clickedBlock.getBlock().equals(Blocks.BEDROCK)){
                IMagicDataSaver dataSaver = (IMagicDataSaver) player;
                MagicData magicData = dataSaver.getMagicData();
                int selectedMagic = magicData.getSelectedMagic();
                int voidLevel = magicData.getMagicLevel(VOID_INDEX);
                //if you are currently void and lvl 1 in void
                if (voidLevel>=1 && selectedMagic==VOID_INDEX){
                    player.getServerWorld().setBlockState(positionClicked, Blocks.AIR.getDefaultState());

                    player.getItemCooldownManager().set(this, DEFAULT_VOID_DRILL_COOLDOWN);

                    //play sound
                    player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.BLOCK_ANVIL_USE,
                            SoundCategory.PLAYERS, 1.0F, 0.75F);

                    player.getServerWorld().spawnParticles(
                            ParticleTypes.SQUID_INK,
                            positionClicked.getX() + 0.5,
                            positionClicked.getY() + 0.5,
                            positionClicked.getZ() + 0.5,
                            10,
                            0.25,
                            0.25, //so that they rise a little
                            0.25,
                            0);

                    //remove item if not in creative
                    if (!player.isCreative()){
                        context.getStack().setCount(0);
                    }

                    return ActionResult.SUCCESS;
                }
            }
        }

        return ActionResult.PASS;
    }
}
