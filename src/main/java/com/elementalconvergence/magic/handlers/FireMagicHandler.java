package com.elementalconvergence.magic.handlers;

import com.elementalconvergence.magic.IMagicHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;


public class FireMagicHandler implements IMagicHandler {
    private int fireResCooldown=50;
    private int waterHurtCooldown=10;


    @Override
    public void handleRightClick(PlayerEntity player) {
        //player.sendMessage(Text.of("test"));
    }

    @Override
    public void handlePassive(PlayerEntity player) {
        //Fire Res passive
        if (this.fireResCooldown<=0) {
            fireResCooldown=100;
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 219, 0, false, false, false));
        }
        else{
            fireResCooldown--;
        }

        //Getting hurt with rain or water
        if (player.isTouchingWaterOrRain()){
            if (this.waterHurtCooldown<=0) {
                DamageSource drowning = player.getWorld().getDamageSources().drown();
                player.damage(drowning, 2);
                waterHurtCooldown=10;
            }
            else{
                waterHurtCooldown--;
            }
        }
        else{
            waterHurtCooldown=10;
        }
    }

    @Override
    public void handleAttack(PlayerEntity player, Entity victim) {
        if (victim instanceof LivingEntity){
            if (victim.isOnFire()){
                DamageSource playerdmg = player.getWorld().getDamageSources().playerAttack(player);
                victim.damage(playerdmg, 3); //Deal 1.5 heart more dmg on entities on fire.
            }
        }
    }

    @Override
    public void handlePrimarySpell(PlayerEntity player) {
        player.sendMessage(Text.of("test"));
        System.out.println("Primary spell cast on " +
                (player.getWorld().isClient() ? "CLIENT" : "SERVER") +
                " side by player: " + player.getName().getString());
    }

    @Override
    public void handleSecondarySpell(PlayerEntity player) {

    }

    @Override
    public void handleTertiarySpell(PlayerEntity player) {

    }
}
