package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.Budget;
import edu.ucdavis.dss.ipa.entities.BudgetScenario;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface BudgetScenarioService {
    BudgetScenario findOrCreate(Budget budget, String budgetScenarioName);

    BudgetScenario findById(long budgetScenarioId);

    void deleteById(long budgetScenarioId);

    BudgetScenario createFromExisting(Long budgetId, Long scenarioId, String name, boolean copyFunds);

    BudgetScenario update(BudgetScenario budgetScenario);

    List<BudgetScenario> findbyWorkgroupIdAndYear(long workgroupId, long year);

    BudgetScenario createSnapshot(long workgroupId, long scenarioId);
}
