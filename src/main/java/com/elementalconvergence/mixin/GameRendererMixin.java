package com.elementalconvergence.mixin;

import com.elementalconvergence.effect.ModEffects;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player != null && client.player.hasStatusEffect(ModEffects.FULL_BLINDNESS)) {
            VertexConsumerProvider.Immediate vertexConsumers = client.getBufferBuilders().getEntityVertexConsumers();
            DrawContext drawContext = new DrawContext(client, vertexConsumers);

            drawContext.getMatrices().push();
            drawContext.fill(0, 0, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight(), 0xFF000000);
            drawContext.getMatrices().pop();

            ci.cancel(); // CANCEL RENDERING. IT CANCELS BEFORE IT CAN PUT MY BLACKSCREEN THO
        }
    }
}
