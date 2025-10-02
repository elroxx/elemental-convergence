package com.elementalconvergence.entity;

import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.*;

public class ModEntitiesClient {

    public static void initializeClient() {
        // Register entity renderers (client-side only)
        EntityRendererRegistry.register(ModEntities.SHADOWBALL, (context) ->
                new FlyingItemEntityRenderer<>(context));

        EntityRendererRegistry.register(ModEntities.MINION_ZOMBIE, MinionZombieRenderer::new);

        EntityRendererRegistry.register(ModEntities.PEGASUS,
                (EntityRendererFactory.Context context) -> new HorseEntityRenderer(context));

        EntityRendererRegistry.register(ModEntities.POULET,
                (EntityRendererFactory.Context context) -> new ChickenEntityRenderer(context));

        EntityRendererRegistry.register(ModEntities.MINION_BEE,
                (EntityRendererFactory.Context context) -> new BeeEntityRenderer(context));

        EntityRendererRegistry.register(ModEntities.MINION_SLIME,
                (EntityRendererFactory.Context context) -> new SlimeEntityRenderer(context));

        EntityRendererRegistry.register(ModEntities.LASHING_POTATO_HOOK, LashingPotatoHookEntityRenderer::new);


        System.out.println("ModEntities client renderers initialized");
    }
}
