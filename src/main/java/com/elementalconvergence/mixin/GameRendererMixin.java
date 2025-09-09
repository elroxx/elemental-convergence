package com.elementalconvergence.mixin;

import com.elementalconvergence.effect.ModEffects;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Unique
    private long worldJoinTime = -1; //stored when joining

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null || client.world == null) {
            worldJoinTime = -1; //reset when not in a world
            return;
        }

        // store current time
        if (worldJoinTime == -1) {
            worldJoinTime = System.currentTimeMillis();
        }

        if (System.currentTimeMillis() - worldJoinTime < 10000) {
            return; //allow rendering for first 10 seconds to try and get out of softlock
        }

        if (client.player.hasStatusEffect(ModEffects.FULL_BLINDNESS) && !client.player.hasStatusEffect(StatusEffects.GLOWING)) {
            VertexConsumerProvider.Immediate vertexConsumers = client.getBufferBuilders().getEntityVertexConsumers();
            DrawContext drawContext = new DrawContext(client, vertexConsumers);

            drawContext.getMatrices().push();
            drawContext.fill(0, 0, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight(), 0xFF000000);
            drawContext.getMatrices().pop();

            ci.cancel(); // CANCEL RENDERING. IT CANCELS BEFORE IT CAN PUT MY BLACKSCREEN THO. MIGHT NEED TO CHANGE TO NORMAL SUFFOCATION OVERLAY THO
        }
    }

}
