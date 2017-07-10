package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.Budget;
import edu.ucdavis.dss.ipa.entities.BudgetScenario;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.SectionGroupCost;
import edu.ucdavis.dss.ipa.repositories.BudgetRepository;
import edu.ucdavis.dss.ipa.repositories.BudgetScenarioRepository;
import edu.ucdavis.dss.ipa.services.BudgetScenarioService;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.SectionGroupCostService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Service
public class JpaBudgetScenarioService implements BudgetScenarioService {
    @Inject BudgetRepository budgetRepository;
    @Inject ScheduleService scheduleService;
    @Inject BudgetScenarioRepository budgetScenarioRepository;
    @Inject SectionGroupCostService sectionGroupCostService;

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


    /**
     * Create a new budget scenario as a 'fork' of the supplied scenario
     * @param scenarioId
     * @param name
     * @return
     */
    @Override
    public BudgetScenario createFromExisting(Long scenarioId, String name) {
        BudgetScenario originalBudgetScenario = budgetScenarioRepository.findById(scenarioId);

        if (originalBudgetScenario == null) {
            return null;
        }

        BudgetScenario budgetScenario = null;
        budgetScenario = new BudgetScenario();
        budgetScenario.setBudget(originalBudgetScenario.getBudget());
        budgetScenario.setName(name);
        budgetScenario = budgetScenarioRepository.save(budgetScenario);

        List<SectionGroupCost> sectionGroupCostList = new ArrayList<>();

        // Clone sectionGroupCosts from one scenario to another
        for(SectionGroupCost originalSectionGroupCost : originalBudgetScenario.getSectionGroupCosts()) {
            SectionGroupCost sectionGroupCost = sectionGroupCostService.createFrom(originalSectionGroupCost, budgetScenario);
            sectionGroupCostList.add(sectionGroupCost);
        }

        budgetScenario.setSectionGroupCosts(sectionGroupCostList);
        budgetScenario = budgetScenarioRepository.save(budgetScenario);

        return budgetScenario;
    }

    @Override
    public BudgetScenario findById(long budgetScenarioId) {
        return budgetScenarioRepository.findById(budgetScenarioId);
    }

    @Override
    public void deleteById(long budgetScenarioId) {
        budgetScenarioRepository.delete(budgetScenarioId);
    }
}
