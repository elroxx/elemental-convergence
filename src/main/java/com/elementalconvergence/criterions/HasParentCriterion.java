package com.elementalconvergence.criterions;

import com.elementalconvergence.data.IMagicDataSaver;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class HasParentCriterion extends AbstractCriterion<HasParentCriterion.Conditions> {

    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player) {
        trigger(player, conditions -> conditions.requirementsMet(player));
    }

    public record Conditions(Optional<LootContextPredicate> playerPredicate, String parentAdvancement, String criterionName, String advancementName) implements AbstractCriterion.Conditions {
        public static final Codec<HasParentCriterion.Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                LootContextPredicate.CODEC.optionalFieldOf("player").forGetter(Conditions::player),
                Codec.STRING.fieldOf("parentAdvancement").forGetter(Conditions::parentAdvancement),
                Codec.STRING.fieldOf("criterionName").forGetter(Conditions::criterionName),
                Codec.STRING.fieldOf("advancementName").forGetter(Conditions::advancementName)
        ).apply(instance, Conditions::new));

        @Override
        public Optional<LootContextPredicate> player() {
            return playerPredicate;
        }

        public boolean requirementsMet(ServerPlayerEntity player) {
            //elemental-convergence:root
            //System.out.println(parentAdvancement);
            Identifier id = Identifier.of(parentAdvancement);
            AdvancementEntry advancementParent = player.getServer().getAdvancementLoader().get(id);

            boolean parentSuccess=player.getAdvancementTracker().getProgress(advancementParent).isDone();

            if (!parentSuccess){
                clearingCriterion(player);
            }

            return parentSuccess;
        }

        public void clearingCriterion(ServerPlayerEntity player){
            Identifier advancementID = Identifier.of(advancementName);
            AdvancementEntry advancementEntryToRemove = player.getServer().getAdvancementLoader().get(advancementID);
            player.getAdvancementTracker().revokeCriterion(advancementEntryToRemove, criterionName);
        }

    }
}
