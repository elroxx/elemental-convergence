package com.elementalconvergence.entity;

import com.elementalconvergence.ElementalConvergence;
import com.elementalconvergence.entity.SpiderGrapplingHookEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.render.*;
import org.joml.Matrix4f;

public class SpiderGrapplingHookEntityRenderer extends EntityRenderer<SpiderGrapplingHookEntity> {
    private static final Identifier TEXTURE = ElementalConvergence.id("textures/entity/spider_grappling_hook.png");

    public SpiderGrapplingHookEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public void render(SpiderGrapplingHookEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);

        // Render the web string connection to player (like lashing potato vine)
        if (entity.getOwner() instanceof net.minecraft.entity.player.PlayerEntity player) {
            renderWebString(entity, player, tickDelta, matrices, vertexConsumers);
        }
    }

    private void renderWebString(SpiderGrapplingHookEntity hook, net.minecraft.entity.player.PlayerEntity player,
                                 float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {

        Vec3d hookPos = hook.getLerpedPos(tickDelta);
        Vec3d playerPos = player.getLerpedPos(tickDelta).add(0, player.getHeight() * 0.8, 0);

        double deltaX = hookPos.x - playerPos.x;
        double deltaY = hookPos.y - playerPos.y;
        double deltaZ = hookPos.z - playerPos.z;

        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
        int segments = Math.max(1, (int)(distance * 2));

        matrices.push();
        matrices.translate(playerPos.x - hook.getX(), playerPos.y - hook.getY(), playerPos.z - hook.getZ());

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getLines());
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();

        // Calculate normal vector for the line
        Vec3d lineDir = new Vec3d(deltaX, deltaY, deltaZ).normalize();

        // Render white web string (straight line like lashing potato vine)
        for (int i = 0; i < segments; i++) {
            float progress1 = (float)i / segments;
            float progress2 = (float)(i + 1) / segments;

            float x1 = (float)(deltaX * progress1);
            float y1 = (float)(deltaY * progress1);
            float z1 = (float)(deltaZ * progress1);

            float x2 = (float)(deltaX * progress2);
            float y2 = (float)(deltaY * progress2);
            float z2 = (float)(deltaZ * progress2);

            // White web string with proper normals
            vertexConsumer.vertex(matrix4f, x1, y1, z1).color(255, 255, 255, 230).normal((float)lineDir.x, (float)lineDir.y, (float)lineDir.z);
            vertexConsumer.vertex(matrix4f, x2, y2, z2).color(255, 255, 255, 230).normal((float)lineDir.x, (float)lineDir.y, (float)lineDir.z);
        }

        matrices.pop();
    }

    @Override
    public Identifier getTexture(SpiderGrapplingHookEntity entity) {
        return TEXTURE;
    }
}