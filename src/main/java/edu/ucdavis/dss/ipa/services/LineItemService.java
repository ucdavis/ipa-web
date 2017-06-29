package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.Budget;
import edu.ucdavis.dss.ipa.entities.LineItem;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface LineItemService {
    List<LineItem> findByBudgetId(Long budgetId);
}
