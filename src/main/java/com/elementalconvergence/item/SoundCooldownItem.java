package com.elementalconvergence.item;
import com.elementalconvergence.entity.ModEntities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

public class SoundCooldownItem extends Item{
    public SoundCooldownItem(Settings settings) {
        super(settings);
    }


    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient) {
            if (!user.getItemCooldownManager().isCoolingDown(this)) {
                world.playSound(
                        null,
                        user.getX(),
                        user.getY(),
                        user.getZ(),
                        ModEntities.TRAINWHISTLE_SOUND_EVENT,
                        SoundCategory.PLAYERS,
                        1.0f,
                        1.0f
                );
                user.getItemCooldownManager().set(this, 5*20); //5 seconds
            }
        }
        return TypedActionResult.success(stack, world.isClient());
    }

}
