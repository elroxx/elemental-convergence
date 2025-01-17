package com.elementalconvergence.criterions;

import com.elementalconvergence.ElementalConvergence;
import net.minecraft.advancement.criterion.Criteria;

public class ModCriterions {
    public static final SelectedMagicCriterion SELECTED_MAGIC_CRITERION = Criteria.register(ElementalConvergence.MOD_ID + "/selected_magic", new SelectedMagicCriterion());
    public static final HasParentCriterion HAS_PARENT_CRITERION = Criteria.register(ElementalConvergence.MOD_ID + "/has_parent", new HasParentCriterion());

    //Blank to init the SelectedMagicCriterion
    public static void initialize() {
    }
}
