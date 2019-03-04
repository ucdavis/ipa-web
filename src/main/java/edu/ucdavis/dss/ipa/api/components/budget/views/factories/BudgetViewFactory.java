package edu.ucdavis.dss.ipa.api.components.budget.views.factories;

import edu.ucdavis.dss.ipa.api.components.budget.views.BudgetScenarioView;
import edu.ucdavis.dss.ipa.api.components.budget.views.BudgetView;
import edu.ucdavis.dss.ipa.entities.Budget;
import edu.ucdavis.dss.ipa.entities.BudgetScenario;

public interface BudgetViewFactory {
    BudgetView createBudgetView(long workgroupId, long year, Budget budget);

    BudgetScenarioView createBudgetScenarioView(BudgetScenario budgetScenario);

    BudgetView createBudgetExcelView(long workgroupId, long year, Budget budget);
}