package com.elementalconvergence.criterions;

import com.elementalconvergence.data.IMagicDataSaver;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Optional;

public class SelectedMagicCriterion extends AbstractCriterion<SelectedMagicCriterion.Conditions> {

    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player) {
        IMagicDataSaver dataSaver = (IMagicDataSaver) player;
        int selectedMagic = dataSaver.getMagicData().getSelectedMagic();
        trigger(player, conditions -> conditions.requirementsMet(selectedMagic));
    }

    public record Conditions(Optional<LootContextPredicate> playerPredicate, int requiredMagic) implements AbstractCriterion.Conditions {
        public static Codec<SelectedMagicCriterion.Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                LootContextPredicate.CODEC.optionalFieldOf("player").forGetter(Conditions::player),
                Codec.INT.fieldOf("requiredMagic").forGetter(Conditions::requiredMagic)
        ).apply(instance, Conditions::new));

        @Override
        public Optional<LootContextPredicate> player() {
            return playerPredicate;
        }

        public boolean requirementsMet(int selectedMagic) {
            return selectedMagic==requiredMagic; // AbstractCriterion#trigger helpfully checks the playerPredicate for us.
        }

    }
}
