package com.elementalconvergence.enchantment;

import com.elementalconvergence.ElementalConvergence;
import com.mojang.serialization.MapCodec;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public class ModEnchantments {

    //public static final RegistryKey<Enchantment> THUNDERING = of("thundering");
    //public static MapCodec<LightningEnchantmentEffect> LIGHTNING_EFFECT = register("lightning_effect", LightningEnchantmentEffect.CODEC);

    public static final RegistryKey<Enchantment> LOCKING_CURSE = registerEnchantment("locking_curse");
    public static final MapCodec<LockingCurseEffect> LOCKING_CURSE_EFFECT = registerEnchantmentEffect("locking_curse_effect", LockingCurseEffect.CODEC);
    public static final RegistryKey<Enchantment> BOUNCY_ARROW = registerEnchantment("bouncy_arrow");

    public static final RegistryKey<Enchantment> LAVA_WALKER = registerEnchantment("lava_walker");
    public static final RegistryKey<Enchantment> VOLCANIC_CHARGE = registerEnchantment("volcanic_charge");
    public static final RegistryKey<Enchantment> FANGS = registerEnchantment("fangs");




    private static RegistryKey<Enchantment> registerEnchantment(String path) {
        return RegistryKey.of(RegistryKeys.ENCHANTMENT, ElementalConvergence.id(path));
    }

    private static <T extends EnchantmentEntityEffect> MapCodec<T> registerEnchantmentEffect(String path, MapCodec<T> codec) {
        return Registry.register(Registries.ENCHANTMENT_ENTITY_EFFECT_TYPE, ElementalConvergence.id(path), codec);
    }

    public static void initialize() {

    }
}
