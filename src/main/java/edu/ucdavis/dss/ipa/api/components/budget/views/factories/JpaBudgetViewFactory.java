package edu.ucdavis.dss.ipa.api.components.budget.views.factories;

import edu.ucdavis.dss.ipa.api.components.budget.views.BudgetView;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Service
public class JpaBudgetViewFactory implements BudgetViewFactory {
    @Inject SectionGroupCostService sectionGroupCostService;
    @Inject LineItemService lineItemService;
    @Inject LineItemCategoryService lineItemCategoryService;
    @Inject SectionGroupService sectionGroupService;
    @Inject CourseService courseService;
    @Inject SectionService sectionService;
    @Inject InstructorCostService instructorCostService;

    @Override
    public BudgetView createBudgetView(long workgroupId, long year, Budget budget) {
        List<BudgetScenario> budgetScenarios = budget.getBudgetScenarios();
        List<SectionGroupCost> sectionGroupCosts = sectionGroupCostService.findByBudgetId(budget.getId());
        List<LineItem> lineItems = lineItemService.findByBudgetId(budget.getId());
        List<LineItemCategory> lineItemCategories = lineItemCategoryService.findAll();
        List<SectionGroup> sectionGroups = sectionGroupService.findVisibleByWorkgroupIdAndYear(workgroupId, year);
        List<Section> sections = sectionService.findVisibleByWorkgroupIdAndYear(workgroupId, year);
        List<InstructorCost> instructorCosts = budget.getInstructorCosts();

        BudgetView budgetView = new BudgetView(budgetScenarios, sectionGroupCosts, lineItems, budget, lineItemCategories, sectionGroups, sections, instructorCosts);

        return budgetView;
    }
}
