package com.elementalconvergence.magic.convergencehandlers;

import com.elementalconvergence.data.IMagicDataSaver;
import com.elementalconvergence.data.MagicData;
import com.elementalconvergence.effect.ModEffects;
import com.elementalconvergence.entity.MinionSlimeEntity;
import com.elementalconvergence.magic.IMagicHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

import static com.elementalconvergence.ElementalConvergence.BASE_MAGIC_ID;

public class VoidMagicHandler implements IMagicHandler {
    public static final int VOID_INDEX= (BASE_MAGIC_ID.length-1)+10;

    public static final int DEFAULT_DIMENSIONSICK_COOLDOWN = 10;
    private int dimensionSickCooldown=0;

    public static final int DEFAULT_VOID_DRILL_COOLDOWN = 20*10; //10 seconds

    public static final int DEFAULT_VOID_SWAP_COOLDOWN = 20;
    private int voidSwapCooldown=0;
    public static final RegistryKey<World> VOID_DIMENSION = World.END; //to change once i have the void dimension done
    //void dimension needs to go from 0 to 96 because i want to do *4 everywhere (and its 384 blocks in the overworld.

    //DONT TAKE DAMAGE IN END OR IN VOID
    //CANT USE THE ABILITY IN THE NETHER, ONLY OVERWORLD


    @Override
    public void handleItemRightClick(PlayerEntity player) {

    }

    @Override
    public void handleEntityRightClick(PlayerEntity player, Entity targetEntity) {

    }

    @Override
    public void handlePassive(PlayerEntity player) {

        //debuff dimension sickness
        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
        RegistryKey<World> currentWorld = serverPlayer.getServerWorld().getRegistryKey();
        if (currentWorld.equals(World.OVERWORLD) || currentWorld.equals(World.NETHER)) {

            //if player can fly and he isnt in creative, remove this ability
            if (player.getAbilities().allowFlying && !player.isCreative()) {
                player.getAbilities().allowFlying = false;
                player.getAbilities().flying = false;
                ((ServerPlayerEntity) player).sendAbilitiesUpdate();
            }

            if (dimensionSickCooldown == 0) {
                float hpLeft = player.getHealth()-1.0f;
                if (hpLeft>=2.0f) { //so if one heart is left
                    DamageSource withered = player.getWorld().getDamageSources().wither();
                    player.damage(withered, 1.0f);
                }
                dimensionSickCooldown = DEFAULT_DIMENSIONSICK_COOLDOWN;
            } else {
                dimensionSickCooldown--;
            }
        }
        else{
            dimensionSickCooldown=DEFAULT_DIMENSIONSICK_COOLDOWN;
        }

        //give creative flight in his own custom dimension
        if (currentWorld.equals(VOID_DIMENSION)){
            if (!player.getAbilities().allowFlying) {
                player.getAbilities().allowFlying = true;
                player.getAbilities().flying = true;
                ((ServerPlayerEntity) player).sendAbilitiesUpdate();
            }
        }

        //cooldowns
        if (voidSwapCooldown>0){
            voidSwapCooldown--;
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
        //dimension swap:
        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
        if (serverPlayer.getServerWorld().getRegistryKey().equals(VOID_DIMENSION)){

        }
    }

    @Override
    public void handleSecondarySpell(PlayerEntity player) {

    }

    @Override
    public void handleTertiarySpell(PlayerEntity player) {

    }
}

