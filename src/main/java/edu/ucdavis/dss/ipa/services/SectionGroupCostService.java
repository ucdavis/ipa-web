package edu.ucdavis.dss.ipa.services;

import java.util.List;

import edu.ucdavis.dss.ipa.entities.*;
import org.springframework.validation.annotation.Validated;

@Validated
public interface SectionGroupCostService {
    List<SectionGroupCost> findByBudgetId(Long budgetId);

    SectionGroupCost createOrUpdateFrom(SectionGroupCost originalSectionGroupCost, BudgetScenario budgetScenario);

    SectionGroupCost createFromSectionGroup(SectionGroup sectionGroup, BudgetScenario budgetScenario);

    SectionGroupCost findById(long sectionGroupCostId);

    SectionGroupCost update(SectionGroupCost sectionGroupCostDTO);
}