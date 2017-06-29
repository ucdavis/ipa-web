package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.Budget;
import edu.ucdavis.dss.ipa.entities.BudgetScenario;
import org.springframework.data.repository.CrudRepository;

public interface BudgetScenarioRepository extends CrudRepository<BudgetScenario, Long> {

    Budget findById(Long id);

    Budget findByScheduleId(Long scheduleId);

    BudgetScenario findByBudgetIdAndName(long id, String budgetScenarioName);
}