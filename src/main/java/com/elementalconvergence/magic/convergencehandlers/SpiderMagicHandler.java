package com.elementalconvergence.magic.convergencehandlers;

import com.elementalconvergence.data.IMagicDataSaver;
import com.elementalconvergence.data.MagicData;
import com.elementalconvergence.effect.ModEffects;
import com.elementalconvergence.item.ModItems;
import com.elementalconvergence.magic.IMagicHandler;
import gravity_changer.api.GravityChangerAPI;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.Set;

import static com.elementalconvergence.ElementalConvergence.BASE_MAGIC_ID;
import static com.elementalconvergence.world.dimension.ModDimensions.VOID_WORLD_KEY;

public class SpiderMagicHandler implements IMagicHandler {
    public static final int SPIDER_INDEX= (BASE_MAGIC_ID.length-1)+11;

    public static final int SPIDER_LIGHT_THRESHOLD = 9;

    //buff: wall climb
    //debuff: can't attack in a light level that is too high (probably can attack up to when light level is 9. After 9, can't attack)
    //buff normal zombies, skeleton, creeper and spider+cave spiders dont attack you. (MAYBE NOT??) //actually i dont want, buff is wall climb.
    //Passive: Spider webs are solid blocks.

    //lvl 1: When right clicking with a stack of string in the air. Create a line of cobwebs in the sky. Consume 1 string per block placed. Place them 1 by 1 with a small delay like the vein miner ability.
    //lvl 2: web slinging
    //lvl 3: when right clicking with a spider's abdomen, stun everybody in front in a cone. This gives them weakness 2 for like 10 seconds, slowness 2 for 10 seconds, poison 1 for 5 seconds and places a cobweb on their face and feet. 15 seconds cooldown.

    //advancements:
    //1: cobweb
    //2: spider's abdomen (reusable item that will be used for the next ability)
    //3: danger pottery shard

    //spider's abdomen is like b:blackwoold c:scaffolding, l:loom
    //bcb
    //bLb
    //bcb



    @Override
    public void handleItemRightClick(PlayerEntity player) {

    }

    @Override
    public void handleEntityRightClick(PlayerEntity player, Entity targetEntity) {

    }

    @Override
    public void handlePassive(PlayerEntity player) {

        if (!player.hasStatusEffect(ModEffects.ARACHNID)){
            player.addStatusEffect(new StatusEffectInstance(ModEffects.ARACHNID, -1, 0, false, false, false));
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
}

