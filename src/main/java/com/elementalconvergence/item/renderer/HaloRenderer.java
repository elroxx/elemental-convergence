package com.elementalconvergence.item.renderer;

import com.elementalconvergence.ElementalConvergence;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class HaloRenderer implements ArmorRenderer {

    private HaloModel haloModel;

    public HaloRenderer() {
        // Initialize the model when the renderer is created
        this.haloModel = new HaloModel(HaloModel.getTexturedModelData().createModel());
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, ItemStack stack,
                       LivingEntity entity, EquipmentSlot slot, int light, BipedEntityModel<LivingEntity> contextModel) {

        if (slot != EquipmentSlot.HEAD) {
            return;
        }

        // Set angles for the halo model
        haloModel.setAngles(entity, 0, 0, entity.age, 0, 0);

        // Apply transformations to position the halo above the head
        matrices.push();

        // Position the halo above the head
        contextModel.head.rotate(matrices);  // Follow head rotation
        matrices.translate(0, -0.75, 0);     // Move above head
        matrices.scale(1.0F, 1.0F, 1.0F);   // Adjust size if needed

        // Render with solid yellow color using a reliable Minecraft texture
        haloModel.render(matrices,
                vertexConsumers.getBuffer(RenderLayer.getEntitySolid(Identifier.of("elemental-convergence", "textures/models/armor/halo.png"))),
                light,
                OverlayTexture.DEFAULT_UV,
                0xFFFFFFFF); // Gold/yellow color

        matrices.pop();
    }
}