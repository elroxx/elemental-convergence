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
import net.minecraft.util.math.RotationAxis;

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
            //skip player transforms
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

        //scale size
        matrixStack.scale(1.5f, 1.5f, 1.5f);

        // Face same direction as player's body
        float bodyYaw = playerEntity.getYaw();
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - bodyYaw));

        // Flip upside down
        matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0F));
        matrixStack.translate(0.0, -2.0, 0.0);

        //Sync state
        dummyBat.setPos(playerEntity.getX(), playerEntity.getY(), playerEntity.getZ());
        dummyBat.setYaw(bodyYaw);
        dummyBat.setPitch(playerEntity.getPitch());
        dummyBat.setRoosting(false);

        //advance anim
        dummyBat.tick();
        dummyBat.age = playerEntity.age;

        //motion
        if (playerEntity.getVelocity().lengthSquared() > 0.001) {
            dummyBat.setVelocity(playerEntity.getVelocity());
        } else {
            dummyBat.setVelocity(0, 0.02, 0);
        }

        //head following cam
        float ageInTicks = (float) playerEntity.age + tickDelta;
        float relHeadYaw = playerEntity.getHeadYaw() - bodyYaw;
        float headPitch = -playerEntity.getPitch();

        this.batModel.setAngles(dummyBat, 0.0f, 0.0f, ageInTicks, relHeadYaw, headPitch);

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