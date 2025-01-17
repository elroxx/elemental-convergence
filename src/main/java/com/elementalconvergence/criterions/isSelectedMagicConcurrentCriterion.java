package com.elementalconvergence.criterions;

import com.elementalconvergence.data.IMagicDataSaver;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class isSelectedMagicConcurrentCriterion extends AbstractCriterion<isSelectedMagicConcurrentCriterion.Conditions> {

    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player) {
        trigger(player, conditions -> conditions.requirementsMet(player));
    }

    public record Conditions(Optional<LootContextPredicate> playerPredicate, String criterionName, String advancementName, int magicIndex) implements AbstractCriterion.Conditions {
        public static final Codec<isSelectedMagicConcurrentCriterion.Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                LootContextPredicate.CODEC.optionalFieldOf("player").forGetter(Conditions::player),
                Codec.STRING.fieldOf("criterionName").forGetter(Conditions::criterionName),
                Codec.STRING.fieldOf("advancementName").forGetter(Conditions::advancementName),
                Codec.INT.fieldOf("magicIndex").forGetter(Conditions::magicIndex)
        ).apply(instance, Conditions::new));

        @Override
        public Optional<LootContextPredicate> player() {
            return playerPredicate;
        }

        public boolean requirementsMet(ServerPlayerEntity player) {

            Identifier id = Identifier.of(advancementName);
            AdvancementEntry advancementToTrack = player.getServer().getAdvancementLoader().get(id);


            boolean criterionHasBeenObtained = player.getAdvancementTracker().getProgress(advancementToTrack).getCriterionProgress(criterionName).isObtained();


            IMagicDataSaver dataSaver = (IMagicDataSaver) player;
            int selectedMagic = dataSaver.getMagicData().getSelectedMagic();

            if (criterionHasBeenObtained && selectedMagic==magicIndex) {
                return true;
            }else if (criterionHasBeenObtained){
                clearingCriterion(player);
                return false;
            }else{
                return false;
            }

        }

        public void clearingCriterion(ServerPlayerEntity player){
            Identifier advancementID = Identifier.of(advancementName);
            AdvancementEntry advancementEntryToRemove = player.getServer().getAdvancementLoader().get(advancementID);
            player.getAdvancementTracker().revokeCriterion(advancementEntryToRemove, criterionName);
        }

    }
}
