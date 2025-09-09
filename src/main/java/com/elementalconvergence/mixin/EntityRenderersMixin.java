package com.elementalconvergence.mixin;

import com.elementalconvergence.entity.BatPlayerEntityRenderer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.EntityRenderers;
import net.minecraft.client.util.SkinTextures;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(EntityRenderers.class)
public class EntityRenderersMixin {

    @Shadow @Final @Mutable
    private static Map<SkinTextures.Model, EntityRendererFactory<AbstractClientPlayerEntity>> PLAYER_RENDERER_FACTORIES;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void replacePlayerRendererFactories(CallbackInfo ci) {
        // Replace the player renderer factories with our custom ones
        PLAYER_RENDERER_FACTORIES = Map.of(
                SkinTextures.Model.WIDE, (context) -> new BatPlayerEntityRenderer(context, false),
                SkinTextures.Model.SLIM, (context) -> new BatPlayerEntityRenderer(context, true)
        );
    }
}
