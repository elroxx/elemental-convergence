package com.elementalconvergence.magic;

import com.elementalconvergence.data.IPlayerMiningMixin;
import com.elementalconvergence.magic.convergencehandlers.GravityMagicHandler;
import com.elementalconvergence.magic.convergencehandlers.RatMagicHandler;
import com.elementalconvergence.magic.handlers.*;
import com.elementalconvergence.ElementalConvergence;
import gravity_changer.api.GravityChangerAPI;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import org.samo_lego.fabrictailor.casts.TailoredPlayer;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MagicRegistry {
    private static final Map<UUID, IMagicHandler[]> playerMagicHandlers = new HashMap<>(); // Map so that each player has a specific magicHandler

    public static IMagicHandler[] createHandlersForPlayer() {
        IMagicHandler[] magic_handlers = new IMagicHandler[ElementalConvergence.FULL_MAGIC_ID.length];

        //base magics
        magic_handlers[0] = new EarthMagicHandler();
        magic_handlers[1] = new AirMagicHandler();
        magic_handlers[2] = new FireMagicHandler();
        magic_handlers[3] = new WaterMagicHandler();
        magic_handlers[4] = new ShadowMagicHandler();
        magic_handlers[5] = new LightMagicHandler();
        magic_handlers[6] = new LifeMagicHandler();
        magic_handlers[7] = new DeathMagicHandler();
        System.out.print("BASE MAGICREGISTRY CREATED");

        magic_handlers[RatMagicHandler.RAT_INDEX] = new RatMagicHandler();
        magic_handlers[GravityMagicHandler.GRAVITY_INDEX] = new GravityMagicHandler();

        System.out.println("MAGICREGISTRY CREATED");

        return magic_handlers;
    }

    public static IMagicHandler[] getHandlersForPlayer(PlayerEntity player) {
        return playerMagicHandlers.computeIfAbsent(player.getUuid(), k -> createHandlersForPlayer());
    }

    public static IMagicHandler getHandler(PlayerEntity player, int magicIndex) {
        if (magicIndex < 0 || magicIndex >= ElementalConvergence.FULL_MAGIC_ID.length) {
            return null;
        }
        IMagicHandler[] handlers = getHandlersForPlayer(player);
        return handlers[magicIndex];
    }

    // Clean up handlers when a player leaves
    public static void removePlayer(PlayerEntity player) {
        playerMagicHandlers.remove(player.getUuid());
    }


    public static void initialize() {
        // Only to init all the static things and constants
        ElementalConvergence.LOGGER.info("MagicRegistry system initialized");
    }

    //FOR RESETTING
    public static final float DEFAULT_MOVE_SPEED=0.1f;
    public static final float DEFAULT_JUMP_HEIGHT=0.42f;
    public static final float DEFAULT_KB_RES=0.0f;
    public static final float BASE_SCALE = 1.0f;

    public static void resetPlayerStats(PlayerEntity player){
        player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(20.0F); //Reset max health
        ((IPlayerMiningMixin) player).setMiningSpeedMultiplier(1.0f); //Reset mining speed

        player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(DEFAULT_MOVE_SPEED); //Reset move speed
        player.getAttributeInstance(EntityAttributes.GENERIC_JUMP_STRENGTH).setBaseValue(DEFAULT_JUMP_HEIGHT); //Reset jump height
        player.getAttributeInstance(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(DEFAULT_KB_RES); //Reset knockback resistance

        ScaleData playerHeight = ScaleTypes.HEIGHT.getScaleData(player);
        ScaleData playerWidth = ScaleTypes.WIDTH.getScaleData(player);
        ScaleData playerReach = ScaleTypes.BLOCK_REACH.getScaleData(player);
        ScaleData playerEntityReach = ScaleTypes.ENTITY_REACH.getScaleData(player);
        ScaleData playerHeldItem = ScaleTypes.HELD_ITEM.getScaleData(player);
        ScaleData playerAttack = ScaleTypes.ATTACK.getScaleData(player);
        ScaleData playerKnockback = ScaleTypes.KNOCKBACK.getScaleData(player);

        playerHeight.setScale(BASE_SCALE); //Reset player scale
        playerWidth.setScale(BASE_SCALE);
        playerReach.setScale(BASE_SCALE); //Reset player Reach (BLOCK)
        playerEntityReach.setScale(BASE_SCALE); //Reset player Reach (ENTITY)
        playerHeldItem.setScale(BASE_SCALE); //Reset held item size
        playerAttack.setScale(BASE_SCALE);
        playerKnockback.setScale(BASE_SCALE);

        ((TailoredPlayer) player).fabrictailor_clearSkin();//RESET THE MODIFIED SKIN
        ((RatMagicHandler)getHandler(player, RatMagicHandler.RAT_INDEX)).resetRatSkinToggle(); //RESET THE HASSKINON FOR RATSKIN

        //Resetting the gravity of the player
        GravityChangerAPI.resetGravity(player);
    }
}