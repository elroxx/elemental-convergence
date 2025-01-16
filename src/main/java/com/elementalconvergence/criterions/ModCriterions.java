package com.elementalconvergence.criterions;

import com.elementalconvergence.ElementalConvergence;
import net.minecraft.advancement.criterion.Criteria;

public class ModCriterions {
    public static final SelectedMagicCriterion SELECTED_MAGIC_CRITERION = Criteria.register(ElementalConvergence.MOD_ID + "/selected_magic", new SelectedMagicCriterion());

    //Blank to init the SelectedMagicCriterion
    public static void initialize() {
    }
}
