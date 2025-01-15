package com.elementalconvergence.item;

import com.elementalconvergence.ElementalConvergence;
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
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MagicEyeItem extends Item {
    private final int magicIndex;
    private final String magicString;

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
            if (clickedBlock.getBlock() == Blocks.END_PORTAL_FRAME) {
                // Removing the item
                context.getStack().setCount(0);

                //SELECTING MAGIC HERE
                IMagicDataSaver dataSaver = (IMagicDataSaver) player;
                dataSaver.getMagicData().setSelectedMagic(magicIndex);

                //RESETTING PLAYER DATA WHEN USING AN EYE
                player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(20.0F); //Reset max health
                ((IPlayerMiningMixin) player).setMiningSpeedMultiplier(1.0f); //Reset mining speed
                //Reset movement speed
                //Reset player scale

                return ActionResult.success(true);
            }
        }

        return ActionResult.PASS;
    }

}