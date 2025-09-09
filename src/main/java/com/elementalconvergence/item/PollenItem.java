package com.elementalconvergence.item;

import com.elementalconvergence.data.IMagicDataSaver;
import com.elementalconvergence.data.MagicData;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

import static com.elementalconvergence.magic.convergencehandlers.HoneyMagicHandler.HONEY_INDEX;

public class PollenItem extends Item {
    private final RegistryEntry<StatusEffect> effect;
    private final int duration;
    private final int amplifier;
    private final FoodComponent foodComponent;

    public PollenItem(RegistryEntry<StatusEffect> effect, int duration, int amplifier, Settings settings) {
        super(settings);
        this.effect = effect;
        this.duration = duration;
        this.amplifier = amplifier;

        // base food for no honey magic
        this.foodComponent = new FoodComponent.Builder()
                .nutrition(1)
                .saturationModifier(0.3f)
                .alwaysEdible()
                .build();
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        if (foodComponent != null) {
            if (user.canConsume(foodComponent.canAlwaysEat())) {
                user.setCurrentHand(hand);
                return TypedActionResult.consume(itemStack);
            } else {
                return TypedActionResult.fail(itemStack);
            }
        } else {
            return TypedActionResult.pass(user.getStackInHand(hand));
        }
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!world.isClient && user instanceof PlayerEntity player) {
            // if player just set the status effect
            player.addStatusEffect(new StatusEffectInstance(effect, duration, amplifier, true, false, true));

            //if player is also honey magic, feeds him with food value of BRead, but saturation of steak
            IMagicDataSaver dataSaver = (IMagicDataSaver) user;
            MagicData magicData = dataSaver.getMagicData();
            if (magicData.getSelectedMagic()==HONEY_INDEX) {
                player.getHungerManager().add(5-1, 12.8f-0.3f);
            }
        }
        return foodComponent != null ? user.eatFood(world, stack, foodComponent) : stack;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.EAT;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 32; //normal eating time
    }


}
