package com.elementalconvergence.mixin;

import com.elementalconvergence.effect.ModEffects;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.FontStorage;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Function;

@Mixin(TextRenderer.class)
public class TextRendererMixin {


    //encahnt font
    private static final Identifier ENCHANTING_FONT = Identifier.of("minecraft", "alt");

    @Redirect(method = "getFontStorage", at = @At(value = "INVOKE", target = "Ljava/util/function/Function;apply(Ljava/lang/Object;)Ljava/lang/Object;"))
    private Object redirectFontStorage(Function<Identifier, FontStorage> fontStorageAccessor, Object identifier) {
        MinecraftClient client = MinecraftClient.getInstance();

        // check if in game (and have a player)
        if (client.player != null && client.world != null) {
            PlayerEntity player = client.player;


            //check if mystical craze
            if (player.hasStatusEffect(ModEffects.MYSTICAL_CRAZE)) {
                // change font
                return fontStorageAccessor.apply(ENCHANTING_FONT);
            }
        }

        // otherwise normal font
        return fontStorageAccessor.apply((Identifier) identifier);
    }
}
