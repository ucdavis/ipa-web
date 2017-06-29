package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.Budget;
import edu.ucdavis.dss.ipa.entities.BudgetScenario;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.repositories.BudgetRepository;
import edu.ucdavis.dss.ipa.repositories.BudgetScenarioRepository;
import edu.ucdavis.dss.ipa.services.BudgetScenarioService;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class JpaBudgetScenarioService implements BudgetScenarioService {
    @Inject BudgetRepository budgetRepository;
    @Inject ScheduleService scheduleService;
    @Inject BudgetScenarioRepository budgetScenarioRepository;

    @Override
    public BudgetScenario findOrCreate(Budget budget, String budgetScenarioName) {

        BudgetScenario budgetScenario = budgetScenarioRepository.findByBudgetIdAndName(budget.getId(), budgetScenarioName);

        if (budgetScenario != null) {
            // Matching budgetScenario already existed
            return budgetScenario;
        }

        // Create new budgetScenario
        budgetScenario = new BudgetScenario();
        budgetScenario.setBudget(budget);
        budgetScenario.setName(budgetScenarioName);
        budgetScenario = budgetScenarioRepository.save(budgetScenario);

        return budgetScenario;
    }
}
