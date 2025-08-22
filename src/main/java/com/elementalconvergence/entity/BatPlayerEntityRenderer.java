package com.elementalconvergence.entity;

import com.elementalconvergence.effect.ModEffects;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.BatEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class BatPlayerEntityRenderer extends PlayerEntityRenderer {
    private static final Identifier BAT_TEXTURE = Identifier.of("textures/entity/bat.png");
    private final BatEntityModel batModel;
    private final PlayerEntityModel<AbstractClientPlayerEntity> playerModel;

    public BatPlayerEntityRenderer(EntityRendererFactory.Context context, boolean slim) {
        super(context, slim);
        this.playerModel = super.getModel();
        this.batModel = new BatEntityModel(context.getPart(EntityModelLayers.BAT));
    }

    @Override
    public void render(AbstractClientPlayerEntity abstractClientPlayerEntity, float f, float g,
                       MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {

        if (abstractClientPlayerEntity.hasStatusEffect(ModEffects.BAT_FORM)) {
            // Render as bat
            renderBat(abstractClientPlayerEntity, f, g, matrixStack, vertexConsumerProvider, i);
        } else {
            // Render as normal player
            super.render(abstractClientPlayerEntity, f, g, matrixStack, vertexConsumerProvider, i);
        }
    }

    private void renderBat(AbstractClientPlayerEntity playerEntity, float yaw, float tickDelta,
                           MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light) {

        matrixStack.push();

        // Scale down to bat size
        matrixStack.scale(0.5f, 0.5f, 0.5f);

        // Adjust position - bats are smaller so we need to adjust
        matrixStack.translate(0, 0.5, 0);

        // Create a dummy bat entity for the model animations
        BatEntity dummyBat = new BatEntity(net.minecraft.entity.EntityType.BAT, playerEntity.getWorld());
        dummyBat.setPos(playerEntity.getX(), playerEntity.getY(), playerEntity.getZ());
        dummyBat.setYaw(playerEntity.getYaw());
        dummyBat.setPitch(playerEntity.getPitch());
        dummyBat.setHeadYaw(playerEntity.getHeadYaw());
        dummyBat.age = playerEntity.age;

        // CRITICAL: Force the bat to NOT be roosting (this makes it fly right-side up)
        dummyBat.setRoosting(false);

        // Force the bat to always have some velocity to trigger flying animation
        double playerSpeed = playerEntity.getVelocity().lengthSquared();
        if (playerSpeed > 0.01) {
            // Player is moving - use their velocity
            dummyBat.setVelocity(playerEntity.getVelocity());
        } else {
            // Player is stationary - give bat a small upward velocity to keep it "flying"
            dummyBat.setVelocity(0, 0.1, 0);
        }

        // IMPORTANT: Manually trigger the flying animation state
        dummyBat.roostingAnimationState.stop();
        dummyBat.flyingAnimationState.startIfNotRunning(dummyBat.age);

        // Realistic wing flapping animation based on actual bat behavior
        float ageInTicks = (float)playerEntity.age + tickDelta;

        // Bats don't flap continuously - they glide between flaps
        // Create a more realistic flapping pattern
        boolean isFlapping = dummyBat.isFlappingWings(); // Uses the real bat logic: age % 10 == 0

        float limbSwing = 0;
        float limbSwingAmount = 0;

        if (playerSpeed > 0.01) {
            // Player is moving - more frequent flapping for active flight
            limbSwing = ageInTicks * 0.3f; // Much slower than before
            limbSwingAmount = isFlapping ? 0.8f : 0.2f; // Stronger flaps when actually flapping
        } else {
            // Player hovering - gentle, occasional flapping to stay airborne
            limbSwing = ageInTicks * 0.15f; // Even slower when hovering
            limbSwingAmount = isFlapping ? 0.5f : 0.1f; // Gentle wing movement
        }

        float headYaw = playerEntity.getHeadYaw();
        float headPitch = playerEntity.getPitch();

        // Set angles with the dummy bat entity
        this.batModel.setAngles(dummyBat, limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch);

        // Render the bat model
        var vertexConsumer = vertexConsumerProvider.getBuffer(this.batModel.getLayer(BAT_TEXTURE));
        this.batModel.render(matrixStack, vertexConsumer, light, getOverlay(playerEntity, 0));

        matrixStack.pop();
    }

    @Override
    public Identifier getTexture(AbstractClientPlayerEntity abstractClientPlayerEntity) {
        if (abstractClientPlayerEntity.hasStatusEffect(ModEffects.BAT_FORM)) {
            return BAT_TEXTURE;
        }
        return super.getTexture(abstractClientPlayerEntity);
    }

    @Override
    protected void scale(AbstractClientPlayerEntity abstractClientPlayerEntity, MatrixStack matrixStack, float f) {
        if (abstractClientPlayerEntity.hasStatusEffect(ModEffects.BAT_FORM)) {
            // Don't apply additional scaling for bat form, we handle it in render method
            return;
        }
        super.scale(abstractClientPlayerEntity, matrixStack, f);
    }
}