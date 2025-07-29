package com.elementalconvergence.mixin;

import com.elementalconvergence.data.IMagicDataSaver;
import com.elementalconvergence.data.MagicData;
import com.elementalconvergence.effect.ModEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.Potions;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.elementalconvergence.magic.convergencehandlers.SteamMagicHandler.STEAM_INDEX;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Inject(method = "finishUsing", at = @At("HEAD"))
    private void onDrink(World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        if (!world.isClient && user instanceof PlayerEntity player) {
            ItemStack self = (ItemStack) (Object) this;

            // check if waterbottle
            if (self.isOf(Items.POTION)) {
                    IMagicDataSaver dataSaver = (IMagicDataSaver) player;
                    MagicData magicData = dataSaver.getMagicData();
                    if (magicData.getSelectedMagic() == STEAM_INDEX) {
                        float healAmount = 8.0f;
                        player.heal(healAmount);
                        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_DROWNED_SWIM,
                                SoundCategory.PLAYERS, 1.0f, 1.0f);
                    }
            }
        }
    }
}
