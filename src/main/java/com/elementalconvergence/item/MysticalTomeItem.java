package com.elementalconvergence.item;

import com.elementalconvergence.container.MysticalTomeScreenHandler;
import com.elementalconvergence.data.IMagicDataSaver;
import com.elementalconvergence.data.MagicData;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

import static com.elementalconvergence.magic.convergencehandlers.MysticMagicHandler.MYSTIC_INDEX;

public class MysticalTomeItem extends Item {
    private final List<RegistryKey<Enchantment>> enchantments;
    private final List<Integer> enchantmentLevels;
    private final int requiredMysticLvl;

    public MysticalTomeItem(List<RegistryKey<Enchantment>> enchantments, List<Integer> enchantmentLevels, int requiredMysticLvl,Settings settings) {
        super(settings);
        if (enchantments.size() != 3 || enchantmentLevels.size() != 3) {
            throw new IllegalArgumentException("MysticalTome must have exactly 3 enchantments with their levels");
        }
        this.enchantments = enchantments;
        this.enchantmentLevels = enchantmentLevels;
        this.requiredMysticLvl = requiredMysticLvl;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient && user instanceof ServerPlayerEntity serverPlayer) {
            IMagicDataSaver dataSaver = (IMagicDataSaver) serverPlayer;
            MagicData magicData = dataSaver.getMagicData();
            int mysticLevel = magicData.getMagicLevel(MYSTIC_INDEX);

            if (mysticLevel >= this.requiredMysticLvl && magicData.getSelectedMagic() == MYSTIC_INDEX) {
                if (serverPlayer.experienceLevel < 30) {
                    serverPlayer.sendMessage(Text.of("Not level 30!"), true);
                } else {
                    // open screen, no params needed
                    serverPlayer.openHandledScreen(MysticalTomeScreenHandler.Factory.INSTANCE);

                    serverPlayer.getServerWorld().playSound(
                            null, user.getX(), user.getY(), user.getZ(),
                            SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.PLAYERS, 1.0f, 1.0f
                    );

                    return TypedActionResult.success(user.getStackInHand(hand));
                }
            }
        }
        return TypedActionResult.fail(user.getStackInHand(hand));
    }

    public List<RegistryKey<Enchantment>> getEnchantments() {
        return this.enchantments;
    }

    public List<Integer> getEnchantmentLevels() {
        return this.enchantmentLevels;
    }
}
