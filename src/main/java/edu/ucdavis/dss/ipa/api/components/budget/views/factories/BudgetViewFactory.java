package edu.ucdavis.dss.ipa.api.components.budget.views.factories;

import edu.ucdavis.dss.ipa.api.components.budget.views.BudgetView;

public interface BudgetViewFactory {
    BudgetView createBudgetView(long workgroupId, long year);
}
