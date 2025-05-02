package com.elementalconvergence.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

import java.util.UUID;

public class MinionZombieEntity extends ZombieEntity {

    private PlayerEntity summoner; // The player who summoned this minion. HE WILL GET ATTACKED IF HE DIES.

    public MinionZombieEntity(EntityType<? extends ZombieEntity> entityType, World world) {
        super(entityType, world);
    }

    // Register default attributes for the minion (e.g., health, attack damage)
    public static DefaultAttributeContainer.Builder createMinionZombieAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 5.0) // Health
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25) // Movement speed
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0); // Attack damage
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        // REMOVE BASE TARGETTING
        this.targetSelector.getGoals().clear();

        this.targetSelector.add(1, new ActiveTargetGoal<>(this, LivingEntity.class, 10, true, false, entity -> {
            // If summoner is null or entity is null, don't attack anything
            if (this.summoner == null || entity == null) {
                return false;
            }

            // ONLY ATTACK PEOPLE SUMMONER ATTACKED
            return entity != this.summoner && entity == this.summoner.getAttacking();
        }));

        // GO ATTACK IN MELEE
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.0, false));

        // REVENGE GOAL === HITTING WHOEVER HIT HIM
        this.targetSelector.add(3, new RevengeGoal(this));
    }

    @Override
    protected void dropEquipment(ServerWorld world, DamageSource source, boolean causedByPlayer) {
        // OVERRIDE TO STOP DROPPING THE EQUIPMENT
    }

    @Override
    public boolean canPickUpLoot() {
        return false;
    }

    public void setSummoner(PlayerEntity summoner) {
        this.summoner = summoner;
    }


    public void equipArmorBasedOnHoe(ItemStack mainHand) {
        // CLEAR EQUIPMENT
        this.equipStack(EquipmentSlot.HEAD, ItemStack.EMPTY);
        this.equipStack(EquipmentSlot.CHEST, ItemStack.EMPTY);
        this.equipStack(EquipmentSlot.LEGS, ItemStack.EMPTY);
        this.equipStack(EquipmentSlot.FEET, ItemStack.EMPTY);
        this.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);

        ItemStack helmet = ItemStack.EMPTY;
        ItemStack chestplate = ItemStack.EMPTY;
        ItemStack leggings = ItemStack.EMPTY;
        ItemStack boots = ItemStack.EMPTY;
        ItemStack sword = ItemStack.EMPTY;

        if (mainHand.isOf(Items.WOODEN_HOE)) {
            helmet = new ItemStack(Items.LEATHER_HELMET);
            sword = new ItemStack(Items.IRON_HOE);
        }
        else if (mainHand.isOf(Items.STONE_HOE)) {
            helmet = new ItemStack(Items.LEATHER_HELMET);
            chestplate = new ItemStack(Items.LEATHER_CHESTPLATE);
            boots = new ItemStack(Items.LEATHER_BOOTS);
            sword = new ItemStack(Items.WOODEN_SWORD);
        } else if (mainHand.isOf(Items.IRON_HOE)) {
            helmet = new ItemStack(Items.IRON_HELMET);
            chestplate = new ItemStack(Items.IRON_CHESTPLATE);
            boots = new ItemStack(Items.IRON_BOOTS);
            sword = new ItemStack(Items.STONE_SWORD);
        } else if (mainHand.isOf(Items.DIAMOND_HOE)) {
            helmet = new ItemStack(Items.DIAMOND_HELMET);
            chestplate = new ItemStack(Items.DIAMOND_CHESTPLATE);
            boots = new ItemStack(Items.DIAMOND_BOOTS);
            sword = new ItemStack(Items.IRON_SWORD);
        } else if (mainHand.isOf(Items.NETHERITE_HOE)) {
            helmet = new ItemStack(Items.NETHERITE_HELMET);
            chestplate = new ItemStack(Items.NETHERITE_CHESTPLATE);
            leggings = new ItemStack(Items.NETHERITE_LEGGINGS);
            boots = new ItemStack(Items.NETHERITE_BOOTS);
            sword = new ItemStack(Items.DIAMOND_SWORD);
        }

        // Equip the armor pieces
        this.equipStack(EquipmentSlot.HEAD, helmet);
        this.equipStack(EquipmentSlot.CHEST, chestplate);
        this.equipStack(EquipmentSlot.LEGS, leggings);
        this.equipStack(EquipmentSlot.FEET, boots);
        this.equipStack(EquipmentSlot.MAINHAND, sword);

        // Set drop chances to 0 for all equipment slots
        this.setEquipmentDropChance(EquipmentSlot.HEAD, 0.0f);
        this.setEquipmentDropChance(EquipmentSlot.CHEST, 0.0f);
        this.setEquipmentDropChance(EquipmentSlot.LEGS, 0.0f);
        this.setEquipmentDropChance(EquipmentSlot.FEET, 0.0f);
        this.setEquipmentDropChance(EquipmentSlot.MAINHAND, 0.0f);
    }

}
