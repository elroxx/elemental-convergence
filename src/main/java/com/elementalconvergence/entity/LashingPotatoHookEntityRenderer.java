package com.elementalconvergence.entity;

import com.elementalconvergence.item.ModItems;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.FishingBobberEntityRenderer;
import net.minecraft.client.render.entity.GuardianEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public class LashingPotatoHookEntityRenderer extends EntityRenderer<LashingPotatoHookEntity> {
    private final ItemRenderer itemRenderer;
    private static final Identifier EXPLOSION_BEAM_TEXTURE = Identifier.ofVanilla("textures/entity/guardian_beam.png");

    public LashingPotatoHookEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    public void render(LashingPotatoHookEntity hookEntity, float yaw, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light) {
        PlayerEntity playerEntity = hookEntity.getPlayerOwner();
        if (playerEntity != null) {
            matrixStack.push();
            this.itemRenderer.renderItem(new ItemStack(Items.COBWEB), ModelTransformationMode.GROUND, light, OverlayTexture.DEFAULT_UV, matrixStack, vertexConsumerProvider, hookEntity.getWorld(), hookEntity.getId());
            Vec3d handPos = playerEntity.getPos().add(0, 1, 0); //playerEntity.getHandPosOffset(ModItems.LASHING_POTATO_HOOK);
            //Vec3d handPos = FishingBobberEntityRenderer.getHandPos(playerEntity, tickDelta, ModItems.LASHING_POTATO_HOOK, this.dispatcher);
            Vec3d hookPos = new Vec3d(MathHelper.lerp((double)tickDelta, hookEntity.prevX, hookEntity.getX()), MathHelper.lerp((double)tickDelta, hookEntity.prevY, hookEntity.getY()) + (double)hookEntity.getStandingEyeHeight(), MathHelper.lerp((double)tickDelta, hookEntity.prevZ, hookEntity.getZ()));
            float age = (float)hookEntity.age + tickDelta;
            float animationTime = age * 0.15F % 1.0F;
            Vec3d direction = handPos.subtract(hookPos);
            float distance = (float)(direction.length() + 0.1);
            direction = direction.normalize();
            float pitch = (float)Math.acos(direction.y);
            float yawAngle = (float)Math.atan2(direction.z, direction.x);
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((((float)Math.PI / 2F) - yawAngle) * (180F / (float)Math.PI)));
            matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitch * (180F / (float)Math.PI)));
            float waveOffset = age * 0.05F * -1.5F;
            float radius = 0.2F;
            float x1 = MathHelper.cos(waveOffset + (float)Math.PI) * radius;
            float z1 = MathHelper.sin(waveOffset + (float)Math.PI) * radius;
            float x2 = MathHelper.cos(waveOffset + 0.0F) * radius;
            float z2 = MathHelper.sin(waveOffset + 0.0F) * radius;
            float x3 = MathHelper.cos(waveOffset + ((float)Math.PI / 2F)) * radius;
            float z3 = MathHelper.sin(waveOffset + ((float)Math.PI / 2F)) * radius;
            float x4 = MathHelper.cos(waveOffset + ((float)Math.PI * 1.5F)) * radius;
            float z4 = MathHelper.sin(waveOffset + ((float)Math.PI * 1.5F)) * radius;
            float minU = 0.0F;
            float maxU = 0.4999F;
            float minV = -1.0F + animationTime;
            float maxV = distance * 2.5F + minV;
            VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getEntityCutoutNoCull(EXPLOSION_BEAM_TEXTURE));
            MatrixStack.Entry entry = matrixStack.peek();
            addVertex(vertexConsumer, entry, x1, distance, z1, maxU, maxV);
            addVertex(vertexConsumer, entry, x1, 0.0F, z1, maxU, minV);
            addVertex(vertexConsumer, entry, x2, 0.0F, z2, minU, minV);
            addVertex(vertexConsumer, entry, x2, distance, z2, minU, maxV);
            addVertex(vertexConsumer, entry, x3, distance, z3, maxU, maxV);
            addVertex(vertexConsumer, entry, x3, 0.0F, z3, maxU, minV);
            addVertex(vertexConsumer, entry, x4, 0.0F, z4, minU, minV);
            addVertex(vertexConsumer, entry, x4, distance, z4, minU, maxV);
            matrixStack.pop();
            super.render(hookEntity, yaw, tickDelta, matrixStack, vertexConsumerProvider, light);
        }
    }

    private static void addVertex(VertexConsumer vertexConsumer, MatrixStack.Entry entry, float x, float y, float z, float u, float v) {
        vertexConsumer.vertex(entry, x, y, z).color(255, 255, 255, 255).texture(u, v).overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(0.0F, 1.0F, 0.0F);
    }

    public Identifier getTexture(LashingPotatoHookEntity hookEntity) {
        return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
    }
}
