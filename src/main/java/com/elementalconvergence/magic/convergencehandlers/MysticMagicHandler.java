package com.elementalconvergence.magic.convergencehandlers;

import com.elementalconvergence.data.IMagicDataSaver;
import com.elementalconvergence.data.MagicData;
import com.elementalconvergence.effect.ModEffects;
import com.elementalconvergence.item.ModItems;
import com.elementalconvergence.magic.IMagicHandler;
import com.terraformersmc.modmenu.util.mod.Mod;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.Set;

import static com.elementalconvergence.ElementalConvergence.BASE_MAGIC_ID;

public class MysticMagicHandler implements IMagicHandler {
    public static final int MYSTIC_INDEX= (BASE_MAGIC_ID.length-1)+8;

    public static final int DEFAULT_LVLUP_COOLDOWN = 20;
    private int lvlUpCooldown = 0;


    @Override
    public void handleItemRightClick(PlayerEntity player) {
        //lvl 3 ability

        ItemStack mainHand = player.getMainHandStack();
        ItemStack offHand = player.getOffHandStack();

        IMagicDataSaver dataSaver = (IMagicDataSaver) player;
        MagicData magicData = dataSaver.getMagicData();
        int mysticLevel = magicData.getMagicLevel(MYSTIC_INDEX);
        if (mysticLevel>=3){
            if (mainHand.isOf(ModItems.MYSTICAL_ENERGY) && lvlUpCooldown==0) {

                if (offHand.getEnchantments().isEmpty()) {
                    player.sendMessage(Text.literal("§cThere are no enchanted items in the offhand!"), true);
                }
                else{
                    //Add 1 lvl to every enchant (unless max lvl)
                    Set<RegistryEntry<Enchantment>> enchantSet= offHand.getEnchantments().getEnchantments();

                    for (RegistryEntry<Enchantment> enchant : enchantSet){
                                                int currentLvl = EnchantmentHelper.getLevel(enchant, offHand);
                        if (currentLvl<255) { //double check max lvl enchants to avoid oveflow
                            offHand.addEnchantment(enchant, currentLvl + 1);
                        }
                    }

                    //consume mainhand
                    mainHand.decrement(1);

                    //playsound
                    player.getWorld().playSound(
                            null,
                            player.getX(), player.getY(), player.getZ(),
                            SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE,
                            SoundCategory.PLAYERS,
                            1.0F,
                            0.75F
                    );

                    //maybe particles but not for now

                    //put cooldown on item
                    lvlUpCooldown=DEFAULT_LVLUP_COOLDOWN;
                    player.getItemCooldownManager().set(ModItems.MYSTICAL_ENERGY, DEFAULT_LVLUP_COOLDOWN);
                }
            }
        }
    }

    @Override
    public void handleEntityRightClick(PlayerEntity player, Entity targetEntity) {

    }

    @Override
    public void handlePassive(PlayerEntity player) {
        if (!player.hasStatusEffect(ModEffects.MYSTICAL_CRAZE)){
            player.addStatusEffect(new StatusEffectInstance(ModEffects.MYSTICAL_CRAZE, -1, 0, false, false, false));
        }

        //cooldowns


    }

    @Override
    public void handleAttack(PlayerEntity player, Entity victim) {

    }

    @Override
    public void handleKill(PlayerEntity player, Entity victim) {

    }

    @Override
    public void handleMine(PlayerEntity player) {

    }

    @Override
    public void handleBlockBreak(PlayerEntity player, BlockPos pos, BlockState state, BlockEntity entity) {

    }

    @Override
    public void handlePrimarySpell(PlayerEntity player) {

    }

    @Override
    public void handleSecondarySpell(PlayerEntity player) {

    }

    @Override
    public void handleTertiarySpell(PlayerEntity player) {

    }
}