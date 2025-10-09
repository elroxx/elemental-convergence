package com.elementalconvergence.entity;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ProjectileEntityRenderer;
import net.minecraft.util.Identifier;

public class ConductiveArrowRenderer extends ProjectileEntityRenderer<ConductiveArrowEntity> {

    //vanilla spectral arrow texture
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/projectiles/spectral_arrow.png");

    public ConductiveArrowRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public Identifier getTexture(ConductiveArrowEntity entity) {
        return TEXTURE;
    }
}