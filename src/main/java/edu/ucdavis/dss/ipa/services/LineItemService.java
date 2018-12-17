package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.BudgetScenario;
import edu.ucdavis.dss.ipa.entities.LineItem;
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

    void deleteMany(List<Long> lineItemIds);

    List<LineItem> findbyWorkgroupIdAndYear(long workgroupId, long year);

    List<LineItem> duplicateFunds(BudgetScenario budgetScenario, BudgetScenario originalBudgetScenario);
}
