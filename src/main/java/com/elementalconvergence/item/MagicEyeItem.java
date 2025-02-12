package com.elementalconvergence.item;

import com.elementalconvergence.ElementalConvergence;
import com.elementalconvergence.block.ModBlocks;
import com.elementalconvergence.data.IMagicDataSaver;
import com.elementalconvergence.data.IPlayerMiningMixin;
import com.elementalconvergence.data.MagicData;
import com.elementalconvergence.mixin.PlayerDataMixin;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

public class MagicEyeItem extends Item {
    private final int magicIndex;
    private final String magicString;

    public static final float DEFAULT_MOVE_SPEED=0.1f;
    public static final float DEFAULT_JUMP_HEIGHT=0.42f;
    public static final float DEFAULT_KB_RES=0.0f;
    public static final float BASE_SCALE = 1.0f;

    public MagicEyeItem(Settings settings, int magicIndex) {
        super(settings);
        this.magicIndex = magicIndex;
        this.magicString = ElementalConvergence.BASE_MAGIC_DISPLAY[magicIndex];
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

            // Check if the clicked block is an ender portal frame
            if (clickedBlock.getBlock() == ModBlocks.ALTAR_OF_CONVERGENCE) {
                // Removing the item
                context.getStack().setCount(0);

                //SELECTING MAGIC HERE
                IMagicDataSaver dataSaver = (IMagicDataSaver) player;
                dataSaver.getMagicData().setSelectedMagic(magicIndex);

                //RESETTING PLAYER DATA WHEN USING AN EYE
                player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(20.0F); //Reset max health
                ((IPlayerMiningMixin) player).setMiningSpeedMultiplier(1.0f); //Reset mining speed

                player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(DEFAULT_MOVE_SPEED); //Reset move speed
                player.getAttributeInstance(EntityAttributes.GENERIC_JUMP_STRENGTH).setBaseValue(DEFAULT_JUMP_HEIGHT); //Reset jump height
                player.getAttributeInstance(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(DEFAULT_KB_RES); //Reset knockback resistance

                ScaleData playerHeight = ScaleTypes.HEIGHT.getScaleData(player);
                ScaleData playerWidth = ScaleTypes.WIDTH.getScaleData(player);
                ScaleData playerReach = ScaleTypes.BLOCK_REACH.getScaleData(player);
                ScaleData playerHeldItem = ScaleTypes.HELD_ITEM.getScaleData(player);

                playerHeight.setScale(BASE_SCALE); //Reset player scale
                playerWidth.setScale(BASE_SCALE);
                playerReach.setScale(BASE_SCALE); //Reset player Reach
                playerHeldItem.setScale(BASE_SCALE); //Reset held item size

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