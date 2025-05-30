package com.elementalconvergence.item.renderer;

import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

public class HaloModel extends Model {

    private final ModelPart halo;

    public HaloModel(ModelPart root) {
        super(RenderLayer::getEntitySolid); // Use solid rendering for colored models
        this.halo = root.getChild("halo");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();

        // Define the halo ring parts
        ModelPartData haloData = modelPartData.addChild("halo",
                ModelPartBuilder.create(),
                ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        // Front segment (above head)
        haloData.addChild("halo_outer_front",
                ModelPartBuilder.create()
                        .uv(0, 0)
                        .cuboid(-2.0F, -3.0F, -3.0F, 4.0F, 1.0F, 1.0F),
                ModelTransform.NONE);

        // Back segment
        haloData.addChild("halo_outer_back",
                ModelPartBuilder.create()
                        .uv(0, 1)
                        .cuboid(-2.0F, -3.0F, 2.0F, 4.0F, 1.0F, 1.0F),
                ModelTransform.NONE);

        // Left segment
        haloData.addChild("halo_left",
                ModelPartBuilder.create()
                        .uv(4, 0)
                        .cuboid(-4.0F, -3.0F, -1.0F, 1.0F, 1.0F, 2.0F),
                ModelTransform.NONE);

        // Right segment
        haloData.addChild("halo_right",
                ModelPartBuilder.create()
                        .uv(5, 0)
                        .cuboid(3.0F, -3.0F, -1.0F, 1.0F, 1.0F, 2.0F),
                ModelTransform.NONE);

        // Corner segments
        haloData.addChild("halo_front_left_corner",
                ModelPartBuilder.create()
                        .uv(6, 0)
                        .cuboid(-3.0F, -3.0F, -2.0F, 1.0F, 1.0F, 1.0F),
                ModelTransform.NONE);

        haloData.addChild("halo_front_right_corner",
                ModelPartBuilder.create()
                        .uv(7, 0)
                        .cuboid(2.0F, -3.0F, -2.0F, 1.0F, 1.0F, 1.0F),
                ModelTransform.NONE);

        haloData.addChild("halo_back_left_corner",
                ModelPartBuilder.create()
                        .uv(6, 1)
                        .cuboid(-3.0F, -3.0F, 1.0F, 1.0F, 1.0F, 1.0F),
                ModelTransform.NONE);

        haloData.addChild("halo_back_right_corner",
                ModelPartBuilder.create()
                        .uv(7, 1)
                        .cuboid(2.0F, -3.0F, 1.0F, 1.0F, 1.0F, 1.0F),
                ModelTransform.NONE);

        return TexturedModelData.of(modelData, 16, 16);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
        halo.render(matrices, vertices, light, overlay, color);
    }

    public void setAngles(LivingEntity entity, float limbAngle, float limbDistance,
                          float animationProgress, float headYaw, float headPitch) {
        // The halo doesn't need to move with the head, it floats above
        // You could add gentle floating animation here if desired
    }
}