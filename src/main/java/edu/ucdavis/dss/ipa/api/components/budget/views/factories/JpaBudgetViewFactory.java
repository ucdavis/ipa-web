package edu.ucdavis.dss.ipa.api.components.budget.views.factories;

import edu.ucdavis.dss.ipa.api.components.budget.views.BudgetView;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.services.LineItemCategoryService;
import edu.ucdavis.dss.ipa.services.LineItemService;
import edu.ucdavis.dss.ipa.services.SectionGroupCostService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Service
public class JpaBudgetViewFactory implements BudgetViewFactory {
    @Inject SectionGroupCostService sectionGroupCostService;
    @Inject LineItemService lineItemService;
    @Inject LineItemCategoryService lineItemCategoryService;

    @Override
    public BudgetView createBudgetView(long workgroupId, long year, Budget budget) {
        List<BudgetScenario> budgetScenarios = budget.getBudgetScenarios();
        List<SectionGroupCost> sectionGroupCosts = sectionGroupCostService.findByBudgetId(budget.getId());
        List<LineItem> lineItems = lineItemService.findByBudgetId(budget.getId());
        List<LineItemCategory> lineItemCategories = lineItemCategoryService.findAll();

        BudgetView budgetView = new BudgetView(budgetScenarios, sectionGroupCosts, lineItems, budget, lineItemCategories);
        return budgetView;
    }
}
