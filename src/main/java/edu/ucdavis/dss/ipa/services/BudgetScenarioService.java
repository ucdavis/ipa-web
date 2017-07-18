package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.Budget;
import edu.ucdavis.dss.ipa.entities.BudgetScenario;
import org.springframework.validation.annotation.Validated;

@Validated
public interface BudgetScenarioService {
    BudgetScenario findOrCreate(Budget budget, String budgetScenarioName);

    BudgetScenario findById(long budgetScenarioId);

    void deleteById(long budgetScenarioId);

    BudgetScenario createFromExisting(Long scenarioId, String name);
}