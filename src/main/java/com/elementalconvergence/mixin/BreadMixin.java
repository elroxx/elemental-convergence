package com.elementalconvergence.mixin;
import com.elementalconvergence.data.IMagicDataSaver;
import com.elementalconvergence.data.MagicData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.elementalconvergence.magic.convergencehandlers.HolyMagicHandler.HOLY_INDEX;

@Mixin(net.minecraft.item.Item.class)
public class BreadMixin {

    @Inject(method = "finishUsing", at = @At("HEAD"))
    private void onItemUseFinish(ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (user instanceof PlayerEntity player) {
            // check if held item is indeed bread
            if (stack.getItem().equals(Items.BREAD)) {

                IMagicDataSaver dataSaver = (IMagicDataSaver) player;
                MagicData magicData = dataSaver.getMagicData();

                if (magicData.getSelectedMagic() == HOLY_INDEX) {

                    if (!world.isClient) {
                        // slightly better than golden carrot (golden carrot is 14.4) saturation:15
                        // feed like steak food:8
                        //BUT SATURATION OF BREAD IS ALREADY AT 6 AND FOOD LVL AT 5
                        player.getHungerManager().add(10-5, 15.0f-6.0f);
                    }
                }
            }
        }
    }
}
