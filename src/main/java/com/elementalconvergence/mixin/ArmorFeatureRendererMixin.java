package com.elementalconvergence.mixin;

import com.elementalconvergence.data.IMagicDataSaver;
import com.elementalconvergence.magic.handlers.ShadowMagicHandler;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmorFeatureRenderer.class)
public abstract class ArmorFeatureRendererMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void onRender(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, LivingEntity entity, float limbAngle, float limbDistance, float tickDelta, float customAngle, float headYaw, float scale, CallbackInfo ci) {
        if (entity.isInvisible() && entity instanceof PlayerEntity) {
            //PlayerEntity player = (PlayerEntity) entity;
            //IMagicDataSaver dataSaver = (IMagicDataSaver) player;
            //int selectedMagic = dataSaver.getMagicData().getSelectedMagic();
            //System.out.println(selectedMagic);
            //if (selectedMagic== ShadowMagicHandler.SHADOW_INDEX) {
                ci.cancel(); // Cancels armor rendering when the player is invisible
            //}
        }
    }
}
