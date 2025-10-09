package com.elementalconvergence.mixin;

import com.elementalconvergence.data.IMagicDataSaver;
import com.elementalconvergence.data.MagicData;
import com.elementalconvergence.item.ConductiveArrowItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ChargedProjectilesComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.elementalconvergence.magic.convergencehandlers.ElectricityMagicHandler.ELECTRICITY_INDEX;

@Mixin(CrossbowItem.class)
public class CrossbowItemMixin {

    @Inject(method = "shootAll(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/item/ItemStack;FFLnet/minecraft/entity/LivingEntity;)V",
            at = @At("HEAD"), cancellable = true)
    private void onShootCrossbow(World world, LivingEntity shooter, Hand hand, ItemStack stack,
                                 float speed, float divergence, LivingEntity target, CallbackInfo ci) {
        if (shooter instanceof ServerPlayerEntity player) {
            //charged projectile
            ChargedProjectilesComponent chargedProjectiles = stack.get(DataComponentTypes.CHARGED_PROJECTILES);

            if (chargedProjectiles != null && !chargedProjectiles.isEmpty()) {
                // if loaded
                for (ItemStack projectile : chargedProjectiles.getProjectiles()) {
                    if (projectile.getItem() instanceof ConductiveArrowItem) {
                        IMagicDataSaver dataSaver = (IMagicDataSaver) player;
                        MagicData magicData = dataSaver.getMagicData();
                        int electricityLevel = magicData.getMagicLevel(ELECTRICITY_INDEX);
                        int selectedMagic = magicData.getSelectedMagic();

                        if (electricityLevel < 2 || selectedMagic != ELECTRICITY_INDEX) {
                            player.sendMessage(Text.literal("§cYou need to be electricity level 2 to use conductive arrows!"), true);
                            ci.cancel();
                            return;
                        }
                    }
                }
            }
        }
    }
}
