package com.elementalconvergence.magic.convergencehandlers;

import com.elementalconvergence.data.IMagicDataSaver;
import com.elementalconvergence.data.MagicData;
import com.elementalconvergence.effect.ModEffects;
import com.elementalconvergence.enchantment.ModEnchantments;
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
import net.minecraft.entity.mob.EvokerFangsEntity;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Set;

import static com.elementalconvergence.ElementalConvergence.BASE_MAGIC_ID;

public class MysticMagicHandler implements IMagicHandler {
    public static final int MYSTIC_INDEX= (BASE_MAGIC_ID.length-1)+8;

    public static final int DEFAULT_LVLUP_COOLDOWN = 20;
    private int lvlUpCooldown = 0;

    public static final int DEFAULT_FANGS_COOLDOWN = 2*20;
    private int fangsCooldown=0;


    @Override
    public void handleItemRightClick(PlayerEntity player) {
        //lvl 3 ability

        ItemStack mainHand = player.getMainHandStack();
        ItemStack offHand = player.getOffHandStack();

        IMagicDataSaver dataSaver = (IMagicDataSaver) player;
        MagicData magicData = dataSaver.getMagicData();
        int mysticLevel = magicData.getMagicLevel(MYSTIC_INDEX);

        //lvl 3 ability with mystical energy
        if (mysticLevel>=3){
            if (mainHand.isOf(ModItems.MYSTICAL_ENERGY) && lvlUpCooldown==0) {

                if (offHand.getEnchantments().isEmpty()) {
                    player.sendMessage(Text.literal("§cThere are no enchanted items in the offhand!"), true);
                }
                else{
                    //Add 1 lvl to every enchant
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
                            SoundEvents.ENTITY_PLAYER_LEVELUP,
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

        //fangs power up
        if (mysticLevel>=1 && fangsCooldown==0){
            spawnEvokerFangLine(player, player.getWorld(), mainHand);
            player.getItemCooldownManager().set(mainHand.getItem(), DEFAULT_FANGS_COOLDOWN);
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
        if (!player.hasStatusEffect(ModEffects.MYSTICAL_TOUCH)){
            player.addStatusEffect(new StatusEffectInstance(ModEffects.MYSTICAL_TOUCH, -1, 0, false, false, false));
        }

        //cooldowns
        if (lvlUpCooldown>0){
            lvlUpCooldown--;
        }
        if (fangsCooldown>0){
            fangsCooldown--;
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

    }

    @Override
    public void handleSecondarySpell(PlayerEntity player) {

    }

    @Override
    public void handleTertiarySpell(PlayerEntity player) {

    }

    private void spawnEvokerFangLine(PlayerEntity player, World world, ItemStack sword) {
        //fangs inner workings
        //fangs lvl
        RegistryEntry<Enchantment> fangsEntry = player.getWorld().getRegistryManager().getWrapperOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(ModEnchantments.FANGS);
        int fangsLvl = EnchantmentHelper.getLevel(fangsEntry, sword);
        if (fangsLvl == 0) {
            return; // no fangs lvl
        }

        int range = fangsLvl * 2;

        // look at
        Vec3d lookDirection = player.getRotationVector();
        Vec3d playerPos = player.getPos();

        // block by block so normalize for blocks
        double dirX = lookDirection.x;
        double dirZ = lookDirection.z;

        // fangs in line
        for (int i = 1; i <= range; i++) {
            // pos for fang
            double targetX = playerPos.x + (dirX * i);
            double targetZ = playerPos.z + (dirZ * i);

            //get top lvl for block
            BlockPos spawnPos = findSurfacePos(world, targetX, targetZ, playerPos.y);

            if (spawnPos != null) {
                // evoker fang summoning
                EvokerFangsEntity fang = new EvokerFangsEntity(world, spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0.0f, 0, player);
                world.spawnEntity(fang);
            }
        }
    }

    private BlockPos findSurfacePos(World world, double x, double z, double playerY) {
        int blockX = (int) Math.floor(x);
        int blockZ = (int) Math.floor(z);

        // can go up at max 50 blocks
        int startY = (int) playerY + 10;
        int minY = world.getBottomY();

        // downwards search only at first
        for (int y = startY; y >= minY; y--) {
            BlockPos currentPos = new BlockPos(blockX, y, blockZ);
            BlockPos abovePos = currentPos.up();

            // if current block is air
            if (!world.getBlockState(currentPos).isAir() &&
                    world.getBlockState(abovePos).isAir() &&
                    world.getBlockState(abovePos.up()).isAir()) { //make sure room for fang
                return abovePos; // return air block above the one that was found
            }
        }

        // if nothing suitable, go upwards
        for (int y = (int) playerY; y <= world.getTopY(); y++) {
            BlockPos currentPos = new BlockPos(blockX, y, blockZ);
            BlockPos abovePos = currentPos.up();

            if (!world.getBlockState(currentPos).isAir() &&
                    world.getBlockState(abovePos).isAir() &&
                    world.getBlockState(abovePos.up()).isAir()) {
                return abovePos;
            }
        }

        return null; // No suitable position found
    }
}
