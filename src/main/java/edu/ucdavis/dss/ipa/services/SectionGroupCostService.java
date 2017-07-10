package edu.ucdavis.dss.ipa.services;

import java.util.List;

import edu.ucdavis.dss.ipa.entities.*;
import org.springframework.validation.annotation.Validated;

@Validated
public interface SectionGroupCostService {
    List<SectionGroupCost> findByBudgetId(Long budgetId);

    SectionGroupCost createFrom(SectionGroupCost originalSectionGroupCost, BudgetScenario budgetScenario);
}