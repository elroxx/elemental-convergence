package com.elementalconvergence.magic;

import com.elementalconvergence.magic.handlers.*;
import com.elementalconvergence.ElementalConvergence;

public class MagicRegistry {
    private static final IMagicHandler[] MAGIC_HANDLERS = new IMagicHandler[ElementalConvergence.BASE_MAGIC_ID.length]; // Size matches your magic types

    public static void initialize() {
        MAGIC_HANDLERS[0] = new EarthMagicHandler();
        MAGIC_HANDLERS[1] = new AirMagicHandler();
        MAGIC_HANDLERS[2] = new FireMagicHandler();
        MAGIC_HANDLERS[3] = new WaterMagicHandler();
        MAGIC_HANDLERS[4] = new ShadowMagicHandler();
        MAGIC_HANDLERS[5] = new LightMagicHandler();
        MAGIC_HANDLERS[6] = new LifeMagicHandler();
        MAGIC_HANDLERS[7] = new DeathMagicHandler();
      
        System.out.println("MAGICREGISTRY INITIALIZED");
    }

    public static IMagicHandler getHandler(int magicIndex) {
        if (magicIndex < 0 || magicIndex >= MAGIC_HANDLERS.length) {
            return null;
        }
        return MAGIC_HANDLERS[magicIndex];
    }
}