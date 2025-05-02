package com.elementalconvergence.magic.handlers;

import com.elementalconvergence.data.IMagicDataSaver;
import com.elementalconvergence.data.MagicData;
import com.elementalconvergence.effect.ModEffects;
import com.elementalconvergence.magic.IMagicHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import virtuoel.pehkui.api.PehkuiConfig;

import java.util.HashMap;
import java.util.Map;

public class WaterMagicHandler implements IMagicHandler {

    public static final int WATER_INDEX=3;

    private boolean toggleDolphin = false;

    public void onNautilusEnchant(PlayerEntity player, ItemStack offHand){
        offHand.decrement(1);

        player.getWorld().playSound(
                null,
                player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE,
                SoundCategory.PLAYERS,
                1.0F,
                1.0F
        );
    }

    @Override
    public void handleItemRightClick(PlayerEntity player) {
        ItemStack mainHand = player.getMainHandStack();
        ItemStack offHand = player.getOffHandStack();

        IMagicDataSaver dataSaver = (IMagicDataSaver) player;
        MagicData magicData = dataSaver.getMagicData();
        int waterLevel = magicData.getMagicLevel(WATER_INDEX);
        ServerWorld world = (ServerWorld) player.getWorld();

        if (waterLevel>=1){
            if (offHand.isOf(Items.PRISMARINE_CRYSTALS)){
                if (mainHand.isOf(Items.TRIDENT)){
                    RegistryEntry<Enchantment> impaleEntry = world.getRegistryManager().getWrapperOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(Enchantments.IMPALING);
                    int impaleLvl = EnchantmentHelper.getLevel(impaleEntry, mainHand);

                    if (impaleLvl<5){
                        mainHand.addEnchantment(impaleEntry, 5);
                        onNautilusEnchant(player, offHand);
                    }
                }

                if (mainHand.isOf(Items.DIAMOND_BOOTS) || mainHand.isOf(Items.GOLDEN_BOOTS) || mainHand.isOf(Items.NETHERITE_BOOTS) || mainHand.isOf(Items.IRON_BOOTS) || mainHand.isOf(Items.CHAINMAIL_BOOTS) || mainHand.isOf(Items.LEATHER_BOOTS)){
                    RegistryEntry<Enchantment> depthEntry = world.getRegistryManager().getWrapperOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(Enchantments.DEPTH_STRIDER);
                    int depthLvl = EnchantmentHelper.getLevel(depthEntry, mainHand);

                    if (depthLvl<3){
                        mainHand.addEnchantment(depthEntry, 3);
                        onNautilusEnchant(player, offHand);
                    }
                }

                if (mainHand.isOf(Items.DIAMOND_HELMET) || mainHand.isOf(Items.GOLDEN_HELMET) || mainHand.isOf(Items.NETHERITE_HELMET) || mainHand.isOf(Items.IRON_HELMET) || mainHand.isOf(Items.CHAINMAIL_HELMET) || mainHand.isOf(Items.LEATHER_HELMET) || mainHand.isOf(Items.TURTLE_HELMET)){
                    RegistryEntry<Enchantment> respirationEntry = world.getRegistryManager().getWrapperOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(Enchantments.RESPIRATION);
                    int respirationLvl = EnchantmentHelper.getLevel(respirationEntry, mainHand);
                    RegistryEntry<Enchantment> affinityEntry = world.getRegistryManager().getWrapperOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(Enchantments.AQUA_AFFINITY);
                    int affinityLvl = EnchantmentHelper.getLevel(affinityEntry, mainHand);

                    if (respirationLvl<3 || affinityLvl<1){
                        mainHand.addEnchantment(respirationEntry, 3);
                        mainHand.addEnchantment(affinityEntry, 1);
                        onNautilusEnchant(player, offHand);
                    }
                }

                if (mainHand.isOf(Items.FISHING_ROD)){
                    RegistryEntry<Enchantment> luckEntry = world.getRegistryManager().getWrapperOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(Enchantments.LUCK_OF_THE_SEA);
                    int luckLvl = EnchantmentHelper.getLevel(luckEntry, mainHand);

                    RegistryEntry<Enchantment> lureEntry = world.getRegistryManager().getWrapperOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(Enchantments.LURE);
                    int lureLvl = EnchantmentHelper.getLevel(luckEntry, mainHand);

                    if (lureLvl<3 || luckLvl<3){
                        mainHand.addEnchantment(lureEntry, 3);
                        mainHand.addEnchantment(luckEntry, 3);
                        onNautilusEnchant(player, offHand);
                    }


                }
            }
        }

    }

    @Override
    public void handleEntityRightClick(PlayerEntity player, Entity targetEntity) {

    }

    @Override
    public void handlePassive(PlayerEntity player) {

        //Debuff
        if (!player.hasStatusEffect(ModEffects.GILLS)){
            player.addStatusEffect(new StatusEffectInstance(ModEffects.GILLS, -1, 0, false, false, false));
        }

        //BUFF
        if (!player.hasStatusEffect(StatusEffects.CONDUIT_POWER) && player.isTouchingWaterOrRain()){
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.CONDUIT_POWER, -1, 2, false, false, false)); //add if in water or rain
        } else if (player.hasStatusEffect(StatusEffects.CONDUIT_POWER) && !player.isTouchingWaterOrRain()) {
            player.removeStatusEffect(StatusEffects.CONDUIT_POWER); //remove if not in water or rain
        }

        if (toggleDolphin && !player.hasStatusEffect(StatusEffects.DOLPHINS_GRACE)){
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.DOLPHINS_GRACE, -1, 2, false, false, true));
        } else if (!toggleDolphin){
            player.removeStatusEffect(StatusEffects.DOLPHINS_GRACE);
        }


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

        IMagicDataSaver dataSaver = (IMagicDataSaver) player;
        MagicData magicData = dataSaver.getMagicData();
        int waterLevel = magicData.getMagicLevel(WATER_INDEX);

        if (waterLevel>=2){
            toggleDolphin=!toggleDolphin;

            // sound
            player.getWorld().playSound(
                    null,
                    player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENTITY_DOLPHIN_SWIM,
                    SoundCategory.PLAYERS,
                    1.0F,
                    1F
            );
        }

    }

    @Override
    public void handleSecondarySpell(PlayerEntity player) {

    }

    @Override
    public void handleTertiarySpell(PlayerEntity player) {

    }

}
