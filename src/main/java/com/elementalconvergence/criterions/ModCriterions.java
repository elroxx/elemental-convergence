package com.elementalconvergence.criterions;

import com.elementalconvergence.ElementalConvergence;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.criterion.Criteria;

import java.util.HashMap;

public class ModCriterions {

    public static final SelectedMagicCriterion SELECTED_MAGIC_CRITERION = Criteria.register(ElementalConvergence.MOD_ID + "/selected_magic", new SelectedMagicCriterion());
    public static final HasParentCriterion HAS_PARENT_CRITERION = Criteria.register(ElementalConvergence.MOD_ID + "/has_parent", new HasParentCriterion());
    public static final isSelectedMagicConcurrentCriterion IS_SELECTED_MAGIC_CONCURRENT_CRITERION = Criteria.register(ElementalConvergence.MOD_ID + "/is_selected_magic_concurrent", new isSelectedMagicConcurrentCriterion());

    //Blank to init the SelectedMagicCriterion
    public static void initialize() {

    }
}
