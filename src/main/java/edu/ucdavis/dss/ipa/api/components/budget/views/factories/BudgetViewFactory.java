package edu.ucdavis.dss.ipa.api.components.budget.views.factories;

import edu.ucdavis.dss.ipa.api.components.budget.views.BudgetExcelView;
import edu.ucdavis.dss.ipa.api.components.budget.views.BudgetScenarioExcelView;
import edu.ucdavis.dss.ipa.api.components.budget.views.BudgetScenarioView;
import edu.ucdavis.dss.ipa.api.components.budget.views.BudgetView;
import edu.ucdavis.dss.ipa.entities.Budget;
import edu.ucdavis.dss.ipa.entities.BudgetScenario;
import java.util.List;

public interface BudgetViewFactory {
    BudgetView createBudgetView(long workgroupId, long year, Budget budget);

    BudgetScenarioView createBudgetScenarioView(BudgetScenario budgetScenario);

    BudgetExcelView createBudgetExcelView(List<BudgetScenario> budgetScenarios);

    BudgetScenarioExcelView createBudgetScenarioExcelView(BudgetScenario budgetScenario);
}