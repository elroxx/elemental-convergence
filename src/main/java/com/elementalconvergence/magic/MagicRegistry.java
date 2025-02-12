package com.elementalconvergence.magic;

import com.elementalconvergence.data.IPlayerMiningMixin;
import com.elementalconvergence.magic.convergencehandlers.RatMagicHandler;
import com.elementalconvergence.magic.handlers.*;
import com.elementalconvergence.ElementalConvergence;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import org.samo_lego.fabrictailor.casts.TailoredPlayer;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

public class MagicRegistry {
    private static final IMagicHandler[] MAGIC_HANDLERS = new IMagicHandler[ElementalConvergence.FULL_MAGIC_ID.length]; // Size matches your magic types

    public static void initialize() {
        //base magics
        MAGIC_HANDLERS[0] = new EarthMagicHandler();
        MAGIC_HANDLERS[1] = new AirMagicHandler();
        MAGIC_HANDLERS[2] = new FireMagicHandler();
        MAGIC_HANDLERS[3] = new WaterMagicHandler();
        MAGIC_HANDLERS[4] = new ShadowMagicHandler();
        MAGIC_HANDLERS[5] = new LightMagicHandler();
        MAGIC_HANDLERS[6] = new LifeMagicHandler();
        MAGIC_HANDLERS[7] = new DeathMagicHandler();
        System.out.print("BASE MAGICREGISTRY INITIALIZED");

        MAGIC_HANDLERS[RatMagicHandler.RAT_INDEX] = new RatMagicHandler();

        System.out.println("MAGICREGISTRY INITIALIZED");
    }

    public static IMagicHandler getHandler(int magicIndex) {
        if (magicIndex < 0 || magicIndex >= MAGIC_HANDLERS.length) {
            return null;
        }
        return MAGIC_HANDLERS[magicIndex];
    }

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

        playerHeight.setScale(BASE_SCALE); //Reset player scale
        playerWidth.setScale(BASE_SCALE);
        playerReach.setScale(BASE_SCALE); //Reset player Reach (BLOCK)
        playerEntityReach.setScale(BASE_SCALE); //Reset player Reach (ENTITY)
        playerHeldItem.setScale(BASE_SCALE); //Reset held item size

        ((TailoredPlayer) player).fabrictailor_clearSkin();//RESET THE MODIFIED SKIN
        ((RatMagicHandler)MAGIC_HANDLERS[RatMagicHandler.RAT_INDEX]).resetRatSkinToggle(); //RESET THE HASSKINON FOR RATSKIN
    }
}