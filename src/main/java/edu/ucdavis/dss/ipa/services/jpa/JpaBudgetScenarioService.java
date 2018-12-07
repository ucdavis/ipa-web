package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.Budget;
import edu.ucdavis.dss.ipa.entities.BudgetScenario;
import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.LineItem;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.entities.SectionGroupCost;
import edu.ucdavis.dss.ipa.entities.TeachingAssignment;
import edu.ucdavis.dss.ipa.entities.Term;
import edu.ucdavis.dss.ipa.repositories.BudgetScenarioRepository;
import edu.ucdavis.dss.ipa.services.BudgetScenarioService;
import edu.ucdavis.dss.ipa.services.BudgetService;
import edu.ucdavis.dss.ipa.services.CourseService;
import edu.ucdavis.dss.ipa.services.LineItemCategoryService;
import edu.ucdavis.dss.ipa.services.LineItemService;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.SectionGroupCostService;
import edu.ucdavis.dss.ipa.services.SectionGroupService;
import edu.ucdavis.dss.ipa.services.TeachingAssignmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class JpaBudgetScenarioService implements BudgetScenarioService {
    @Inject ScheduleService scheduleService;
    @Inject BudgetScenarioRepository budgetScenarioRepository;
    @Inject SectionGroupCostService sectionGroupCostService;
    @Inject SectionGroupService sectionGroupService;
    @Inject LineItemService lineItemService;
    @Inject CourseService courseService;
    @Inject LineItemCategoryService lineItemCategoryService;
    @Inject BudgetService budgetService;
    @Inject TeachingAssignmentService teachingAssignmentService;

    @Override
    @Transactional
    public BudgetScenario findOrCreate(Budget budget, String budgetScenarioName) {
        BudgetScenario budgetScenario = budgetScenarioRepository.findByBudgetIdAndName(budget.getId(), budgetScenarioName);

        if (budgetScenario != null) {
            // Matching budgetScenario already exists
            return budgetScenario;
        }

        // Create new budgetScenario
        budgetScenario = new BudgetScenario();
        budgetScenario.setBudget(budget);
        budgetScenario.setName(budgetScenarioName);
        budgetScenario.setActiveTermsBlob("0000000000");
        budgetScenario = budgetScenarioRepository.save(budgetScenario);

        List<Course> courses = courseService.findVisibleByWorkgroupIdAndYear(budget.getSchedule().getWorkgroup().getId(), budget.getSchedule().getYear());
        List<SectionGroup> sectionGroups = sectionGroupService.findByCourses(courses);

        for (SectionGroup sectionGroup : sectionGroups) {
            budgetScenario.setTermInActiveTermsBlob(sectionGroup.getTermCode(), true);
        }

        List<SectionGroupCost> sectionGroupCosts = budgetScenario.getSectionGroupCosts();

        for (SectionGroup sectionGroup : sectionGroups) {
            SectionGroupCost sectionGroupCost = sectionGroupCostService.createFromSectionGroup(sectionGroup, budgetScenario);
            sectionGroupCosts.add(sectionGroupCost);
        }

        budgetScenario.setSectionGroupCosts(sectionGroupCosts);

        // Generate Line items automatically from buyouts
        List<LineItem> lineItems = budgetScenario.getLineItems();
        List<LineItem> newLineItems = new ArrayList<>();

        for (TeachingAssignment teachingAssignment : budget.getSchedule().getTeachingAssignments()) {
            if (teachingAssignment.isApproved() == false) {
                continue;
            }

            if (teachingAssignment.isBuyout()) {
                LineItem lineItemDTO = new LineItem();
                lineItemDTO.setBudgetScenario(budgetScenario);
                lineItemDTO.setAmount(new BigDecimal(0));
                lineItemDTO.setLineItemCategory(lineItemCategoryService.findById(2));

                String description = teachingAssignment.getInstructor().getFullName() + " Buyout Funds for " + Term.getRegistrarName(teachingAssignment.getTermCode());
                lineItemDTO.setDescription(description);

                LineItem newLineItem = lineItemService.findOrCreate(lineItemDTO);
                newLineItems.add(newLineItem);
            } else if (teachingAssignment.isWorkLifeBalance()) {
                LineItem lineItemDTO = new LineItem();
                lineItemDTO.setBudgetScenario(budgetScenario);
                lineItemDTO.setAmount(new BigDecimal(0));
                lineItemDTO.setLineItemCategory(lineItemCategoryService.findById(5));

                String description = teachingAssignment.getInstructor().getFullName() + " Work-Life Balance Funds for " + Term.getRegistrarName(teachingAssignment.getTermCode());
                lineItemDTO.setDescription(description);

                LineItem newLineItem = lineItemService.findOrCreate(lineItemDTO);
                newLineItems.add(newLineItem);
            }
        }

        lineItems.addAll(newLineItems);
        budgetScenario.setLineItems(lineItems);

        budgetScenario = budgetScenarioRepository.save(budgetScenario);

        return budgetScenario;
    }

    /**
     * Duplicate the supplied scenario
     * @param scenarioId
     * @param name
     * @return
     */
    @Transactional
    @Override
    public BudgetScenario createFromExisting(Long scenarioId, String name, boolean copyFunds) {
        BudgetScenario originalBudgetScenario = budgetScenarioRepository.findById(scenarioId);

        if (originalBudgetScenario == null) {
            return null;
        }

        BudgetScenario budgetScenario = null;
        budgetScenario = new BudgetScenario();
        budgetScenario.setBudget(originalBudgetScenario.getBudget());
        budgetScenario.setName(name);
        budgetScenario.setActiveTermsBlob(originalBudgetScenario.getActiveTermsBlob());
        budgetScenario = budgetScenarioRepository.save(budgetScenario);

        List<SectionGroupCost> sectionGroupCostList = budgetScenario.getSectionGroupCosts();

        // Clone sectionGroupCosts
        for(SectionGroupCost originalSectionGroupCost : originalBudgetScenario.getSectionGroupCosts()) {
            SectionGroupCost sectionGroupCost = sectionGroupCostService.createOrUpdateFrom(originalSectionGroupCost, budgetScenario);
            sectionGroupCostList.add(sectionGroupCost);
        }

        budgetScenario.setSectionGroupCosts(sectionGroupCostList);
        budgetScenario = budgetScenarioRepository.save(budgetScenario);

        // Clone lineItems if option is selected
        if (copyFunds) {
            List<LineItem> lineItems = budgetScenario.getLineItems();
            lineItems.addAll(lineItemService.duplicateFunds(budgetScenario, originalBudgetScenario));
            budgetScenario.setLineItems(lineItems);
        }

        budgetScenario = budgetScenarioRepository.save(budgetScenario);

        return budgetScenario;
    }

    @Override
    public BudgetScenario update(BudgetScenario budgetScenario) {
        return budgetScenarioRepository.save(budgetScenario);
    }

    @Override
    public List<BudgetScenario> findbyWorkgroupIdAndYear(long workgroupId, long year) {
        return budgetScenarioRepository.findbyWorkgroupIdAndYear(workgroupId, year);
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
