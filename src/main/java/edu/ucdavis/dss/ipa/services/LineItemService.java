package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.Budget;
import edu.ucdavis.dss.ipa.entities.BudgetScenario;
import edu.ucdavis.dss.ipa.entities.LineItem;
import edu.ucdavis.dss.ipa.entities.TeachingAssignment;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface LineItemService {
    List<LineItem> findByBudgetId(Long budgetId);

    LineItem findById(Long lineItemId);

    void deleteById(long lineItemId);

    LineItem findOrCreate(LineItem lineItemDTO);

    LineItem update(LineItem lineItem);

    LineItem createDuplicate(LineItem originalLineItem, BudgetScenario budgetScenario);

    void deleteMany(List<Integer> lineItemIds);

    void createLineItemFromTeachingAssignmentAndBudgetScenario(TeachingAssignment teachingAssignment, BudgetScenario budgetScenario);
}
