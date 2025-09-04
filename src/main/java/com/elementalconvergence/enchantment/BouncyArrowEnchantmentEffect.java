package com.elementalconvergence.enchantment;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public record BouncyArrowEnchantmentEffect(EnchantmentLevelBasedValue bounces) implements EnchantmentEntityEffect {
    public static final MapCodec<BouncyArrowEnchantmentEffect> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    EnchantmentLevelBasedValue.CODEC.fieldOf("bounces").forGetter(BouncyArrowEnchantmentEffect::bounces)
            ).apply(instance, BouncyArrowEnchantmentEffect::new)
    );

    @Override
    public void apply(ServerWorld world, int level, EnchantmentEffectContext context, Entity target, Vec3d pos) {
        // This effect is applied to the arrow projectile
        if (target instanceof PersistentProjectileEntity arrow) {
            // Set custom NBT data to track bounces remaining
            NbtCompound nbt = arrow.writeNbt(new NbtCompound());
            int maxBounces = (int) this.bounces.getValue(level);

            nbt.putInt("BouncesRemaining", maxBounces);
            nbt.putBoolean("IsBouncy", true);

            // Update the arrow's NBT data
            arrow.readNbt(nbt);
        }
    }

    @Override
    public MapCodec<? extends EnchantmentEntityEffect> getCodec() {
        return CODEC;
    }
}
