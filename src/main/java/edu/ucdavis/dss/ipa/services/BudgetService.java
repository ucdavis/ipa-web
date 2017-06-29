package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.Budget;
import org.springframework.validation.annotation.Validated;

@Validated
public interface BudgetService {
    Budget findOrCreateByWorkgroupIdAndYear(long workgroupId, long year);

    Budget findById(long budgetId);
}
