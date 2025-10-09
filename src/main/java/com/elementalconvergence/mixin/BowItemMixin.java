
package com.elementalconvergence.mixin;

import com.elementalconvergence.data.IMagicDataSaver;
import com.elementalconvergence.data.MagicData;
import com.elementalconvergence.item.ConductiveArrowItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.elementalconvergence.magic.convergencehandlers.ElectricityMagicHandler.ELECTRICITY_INDEX;

@Mixin(BowItem.class)
public class BowItemMixin {

    @Inject(method = "onStoppedUsing", at = @At("HEAD"), cancellable = true)
    private void onShootArrow(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        if (user instanceof ServerPlayerEntity player) {
            //find arrow
            ItemStack arrowStack = player.getProjectileType(stack);

            //cancel if conductive arrow and not electric lvl2
            if (arrowStack.getItem() instanceof ConductiveArrowItem) {
                IMagicDataSaver dataSaver = (IMagicDataSaver) player;
                MagicData magicData = dataSaver.getMagicData();
                int electricityLevel = magicData.getMagicLevel(ELECTRICITY_INDEX);
                int selectedMagic = magicData.getSelectedMagic();

                if (electricityLevel < 2 || selectedMagic != ELECTRICITY_INDEX) {
                    player.sendMessage(Text.literal("§cYou need to be electricity level 2 to use conductive arrows!"), true);
                    ci.cancel();
                }
            }
        }
    }
}