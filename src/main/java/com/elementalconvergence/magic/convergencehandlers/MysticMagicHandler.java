package com.elementalconvergence.magic.convergencehandlers;

import com.elementalconvergence.data.IMagicDataSaver;
import com.elementalconvergence.data.MagicData;
import com.elementalconvergence.effect.ModEffects;
import com.elementalconvergence.enchantment.ModEnchantments;
import com.elementalconvergence.item.ModItems;
import com.elementalconvergence.magic.IMagicHandler;
import com.terraformersmc.modmenu.util.mod.Mod;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.EvokerFangsEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
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

    public static final int DEFAULT_VOLCANIC_COOLDOWN = 2*20;
    private int volcanicCooldown=0;


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
            fangsCooldown=DEFAULT_FANGS_COOLDOWN;
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
        if (volcanicCooldown>0){
            volcanicCooldown--;
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
        int mysticLevel = magicData.getMagicLevel(MYSTIC_INDEX);
        if (mysticLevel>=2 && volcanicCooldown==0) {
            ItemStack chestplate = player.getEquippedStack(EquipmentSlot.CHEST);
            if (chestplate.isEmpty()) {
                return; //avoid null
            }

            RegistryEntry<Enchantment> volcanicEntry = player.getWorld().getRegistryManager().getWrapperOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(ModEnchantments.VOLCANIC_CHARGE);
            int volcanicLevel = EnchantmentHelper.getLevel(volcanicEntry, chestplate);
            if (volcanicLevel > 0) {

                performFireDash(player, player.getWorld(), volcanicLevel);

                player.getItemCooldownManager().set(chestplate.getItem(), DEFAULT_VOLCANIC_COOLDOWN);
                volcanicCooldown=DEFAULT_VOLCANIC_COOLDOWN;
            }

        }
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

    private void performFireDash(PlayerEntity player, World world, int volcanicLevel) {
        // lookat
        Vec3d lookDirection = player.getRotationVector();
        Vec3d horizontalDirection = new Vec3d(lookDirection.x, 0, lookDirection.z).normalize();

        //get dash veloc
        double dashMultiplier = 0.5 * volcanicLevel;
        Vec3d dashVelocity = horizontalDirection.multiply(dashMultiplier);

        // veloc.
        player.setVelocity(dashVelocity.x, 0, dashVelocity.z);
        player.velocityModified = true;

        if (world instanceof ServerWorld serverWorld) {
            createParticleTrail(serverWorld, player, horizontalDirection);
            playDashSounds(serverWorld, player);
        }
    }

    private void createParticleTrail(ServerWorld world, PlayerEntity player, Vec3d direction) {
        Vec3d playerPos = player.getPos();

        for (int i = 0; i < 20; i++) {
            double offset = i * 0.2;
            double x = playerPos.x - direction.x * offset;
            double y = playerPos.y + 0.1;
            double z = playerPos.z - direction.z * offset;

            //fire particles
            world.spawnParticles(
                    ParticleTypes.FLAME,
                    x, y, z,
                    3,
                    0.3, 0.1, 0.3,
                    0.05
            );

            //lava particles
            world.spawnParticles(
                    ParticleTypes.LAVA,
                    x, y, z,
                    1,
                    0.2, 0.1, 0.2,
                    0.02
            );
        }

        //dirt particles
        for (int i = 0; i < 15; i++) {
            double x = playerPos.x + (world.random.nextDouble() - 0.5) * 2;
            double y = playerPos.y + 0.1;
            double z = playerPos.z + (world.random.nextDouble() - 0.5) * 2;

            //poof
            world.spawnParticles(
                    ParticleTypes.POOF,
                    x, y, z,
                    1,
                    0.1, 0.1, 0.1,
                    0.1
            );

            //dirt particles
            world.spawnParticles(
                    new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.DIRT.getDefaultState()),
                    x, y, z,
                    2,
                    0.2, 0.1, 0.2,
                    0.05
            );
        }
    }

    private void playDashSounds(ServerWorld world, PlayerEntity player) {
        world.playSound(
                null,
                player.getBlockPos(),
                SoundEvents.ITEM_FIRECHARGE_USE,
                SoundCategory.PLAYERS,
                1.0f,
                0.8f
        );

        world.playSound(
                null,
                player.getBlockPos(),
                SoundEvents.BLOCK_ANVIL_LAND,
                SoundCategory.PLAYERS,
                0.2f, // volume
                0.5f
        );

        world.playSound(
                null,
                player.getBlockPos(),
                SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP,
                SoundCategory.PLAYERS,
                0.8f, // volume
                1.2f // higher pitch
        );
    }
}

