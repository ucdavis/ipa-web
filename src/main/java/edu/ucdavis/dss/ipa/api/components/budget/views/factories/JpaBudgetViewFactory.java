package edu.ucdavis.dss.ipa.api.components.budget.views.factories;

import edu.ucdavis.dss.ipa.api.components.budget.views.BudgetView;
import org.springframework.stereotype.Service;

@Service
public class JpaBudgetViewFactory implements BudgetViewFactory {

    @Override
    public BudgetView createBudgetView(long workgroupId, long year) {

        return new BudgetView();
    }
}
