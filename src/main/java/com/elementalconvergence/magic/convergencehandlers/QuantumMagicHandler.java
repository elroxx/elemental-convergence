package com.elementalconvergence.magic.convergencehandlers;

import com.elementalconvergence.ElementalConvergence;
import com.elementalconvergence.data.IMagicDataSaver;
import com.elementalconvergence.data.MagicData;
import com.elementalconvergence.effect.ModEffects;
import com.elementalconvergence.magic.IMagicHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.RaycastContext;
import org.samo_lego.fabrictailor.casts.TailoredPlayer;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

import java.util.UUID;

import static com.elementalconvergence.ElementalConvergence.BASE_MAGIC_ID;

public class QuantumMagicHandler implements IMagicHandler {
    public static final int QUANTUM_INDEX= (BASE_MAGIC_ID.length-1)+7;

    public static final int DEFAULT_PHASE_COOLDOWN = 10;
    private int phaseCooldown=0;



    //Quantum debuff is mostly in main class
    @Override
    public void handleItemRightClick(PlayerEntity player) {

    }

    @Override
    public void handleEntityRightClick(PlayerEntity player, Entity targetEntity) {

        IMagicDataSaver dataSaver = (IMagicDataSaver) player;
        MagicData magicData = dataSaver.getMagicData();
        int quantumLevel = magicData.getMagicLevel(QUANTUM_INDEX);
        if (quantumLevel >= 3) {
            if (player.isSneaking() && targetEntity instanceof LivingEntity livingEntity && !(targetEntity instanceof PlayerEntity || targetEntity instanceof WitherEntity || targetEntity instanceof EnderDragonEntity)) {
                ItemStack eggStack = createMobEgg(livingEntity);

                if (eggStack != null) {
                    if (!player.getInventory().insertStack(eggStack)) {
                        // if full inv just drop it
                        player.dropItem(eggStack, false);
                    }

                    // drops the entity into the void below
                    livingEntity.refreshPositionAndAngles(livingEntity.getX(), -200.0, livingEntity.getZ(), livingEntity.getYaw(), livingEntity.getPitch());

                    //capture sound
                    player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.BLOCK_SNIFFER_EGG_PLOP,
                            SoundCategory.PLAYERS, 1.0F, 1.5F);

                    // success message!!!!!!!!! Woohoooo (in color as well)
                    player.sendMessage(Text.literal("§aCaptured " + livingEntity.getName().getString() + "!"), true);

                }
            }
        }
    }

    @Override
    public void handlePassive(PlayerEntity player) {


        //cooldowns
        if (phaseCooldown>0){
            phaseCooldown--;
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
        int quantumLevel = magicData.getMagicLevel(QUANTUM_INDEX);
        //movement ability
        if (quantumLevel >= 2 && phaseCooldown==0) {

            phaseCooldown=DEFAULT_PHASE_COOLDOWN;

            if (player.hasStatusEffect(ModEffects.QUANTUM_PHASING)){
                //remove phasing mode
                player.removeStatusEffect(ModEffects.QUANTUM_PHASING);
                player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ILLUSIONER_PREPARE_MIRROR,
                        SoundCategory.PLAYERS, 1.0F, 0.75F);
            }
            else{
                //put phasing mode on
                player.addStatusEffect(new StatusEffectInstance(ModEffects.QUANTUM_PHASING, -1, 0, false, false, true));
                player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ILLUSIONER_PREPARE_MIRROR,
                        SoundCategory.PLAYERS, 1.0F, 1.25F);
            }
        }

    }

    @Override
    public void handleSecondarySpell(PlayerEntity player) {

    }

    @Override
    public void handleTertiarySpell(PlayerEntity player) {

    }

    private ItemStack createMobEgg(LivingEntity entity) {
        try {
            // get entity type
            String entityTypeId = Registries.ENTITY_TYPE.getId(entity.getType()).toString();

            ItemStack eggStack = new ItemStack(Items.DOLPHIN_SPAWN_EGG); //always dolphin coz i like the color


            // nbt component
            NbtCompound entityNbt = new NbtCompound();
            entity.writeNbt(entityNbt);

            // remove the useless parts
            cleanEntityNbt(entityNbt);

            //store type in item
            entityNbt.putString("id", entityTypeId);

            //store everything else in item
            eggStack.set(DataComponentTypes.ENTITY_DATA, NbtComponent.of(entityNbt));

            //put name as well WITH COLOR WOOHOO
            eggStack.set(DataComponentTypes.CUSTOM_NAME,
                    Text.literal("§e" + entity.getName().getString() + " Egg"));

            return eggStack;

        } catch (Exception e) {
            return null;
        }
    }

    private void cleanEntityNbt(NbtCompound nbt) {
        //we dont care about those specific info.
        nbt.remove("UUID");
        nbt.remove("Pos");
        nbt.remove("Motion");
        nbt.remove("Rotation");
        nbt.remove("FallDistance");
        nbt.remove("Fire");
        nbt.remove("Air");
        nbt.remove("OnGround");
        nbt.remove("Dimension");
        nbt.remove("PortalCooldown");
        nbt.remove("WorldUUIDLeast");
        nbt.remove("WorldUUIDMost");
    }
}