package com.elementalconvergence.mixin;

import com.elementalconvergence.effect.ModEffects;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameOverlayRenderer.class)
public abstract class InGameOverlayRendererMixin {

    @Inject(method = "renderOverlays", at = @At("HEAD"), cancellable = true)
    private static void cancelSuffocationOverlay(MinecraftClient client, MatrixStack matrices, CallbackInfo ci) {
        PlayerEntity player = client.player;
        if (player != null && player.hasStatusEffect(ModEffects.QUANTUM_PHASING)) {
            ci.cancel(); // don’t draw the overlay
        }
    }
}
