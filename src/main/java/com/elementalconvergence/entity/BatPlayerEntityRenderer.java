package com.elementalconvergence.entity;

import com.elementalconvergence.effect.ModEffects;
import net.minecraft.client.MinecraftClient;
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
    private BatEntity dummyBat;

    public BatPlayerEntityRenderer(EntityRendererFactory.Context context, boolean slim) {
        super(context, slim);
        this.playerModel = super.getModel();
        this.batModel = new BatEntityModel(context.getPart(EntityModelLayers.BAT));
    }

    @Override
    public void render(AbstractClientPlayerEntity player, float f, float g,
                       MatrixStack matrixStack, VertexConsumerProvider vertexConsumers, int light) {

        if (player.hasStatusEffect(ModEffects.BAT_FORM)) {
            // Don’t call super.render() here, skip all player transforms
            renderBat(player, f, g, matrixStack, vertexConsumers, light);
        } else {
            super.render(player, f, g, matrixStack, vertexConsumers, light);
        }
    }


    private void renderBat(AbstractClientPlayerEntity playerEntity, float yaw, float tickDelta,
                           MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light) {

        BatEntity dummyBat = getDummyBat(playerEntity);
        if (dummyBat == null) return;

        matrixStack.push();

        // Scale down to bat size
        matrixStack.scale(0.5f, 0.5f, 0.5f);

        // Lift slightly (bats are smaller than players)
        matrixStack.translate(0, 0.5, 0);

        // Sync player state -> dummy bat
        dummyBat.setPos(playerEntity.getX(), playerEntity.getY(), playerEntity.getZ());
        dummyBat.setYaw(playerEntity.getYaw());
        dummyBat.setPitch(playerEntity.getPitch());
        dummyBat.setHeadYaw(playerEntity.getHeadYaw());
        dummyBat.setRoosting(false); // absolutely force flying

        // Run bat’s own tick so animations progress
        dummyBat.tick();
        dummyBat.age = playerEntity.age; // keep animation time in sync with player

        // Give velocity (so it doesn’t “fall asleep”)
        if (playerEntity.getVelocity().lengthSquared() > 0.001) {
            dummyBat.setVelocity(playerEntity.getVelocity());
        } else {
            dummyBat.setVelocity(0, 0.02, 0); // gentle hover motion
        }

        // Vanilla animation state drives wing flapping
        float ageInTicks = (float) playerEntity.age + tickDelta;
        this.batModel.setAngles(dummyBat, 0.0f, 0.0f, ageInTicks,
                playerEntity.getHeadYaw(), playerEntity.getPitch());

        // Render the bat
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

    private BatEntity getDummyBat(PlayerEntity player) {
        if (this.dummyBat == null && player.getWorld() != null) {
            this.dummyBat = new BatEntity(net.minecraft.entity.EntityType.BAT, player.getWorld());
            this.dummyBat.setRoosting(false);
        }
        return this.dummyBat;
    }
}