package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.repositories.BudgetRepository;
import edu.ucdavis.dss.ipa.repositories.BudgetScenarioRepository;
import edu.ucdavis.dss.ipa.services.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JpaBudgetScenarioService implements BudgetScenarioService {
    @Inject ScheduleService scheduleService;
    @Inject BudgetScenarioRepository budgetScenarioRepository;
    @Inject BudgetRepository budgetRepository;

    @Inject SectionGroupCostService sectionGroupCostService;
    @Inject SectionGroupService sectionGroupService;
    @Inject LineItemService lineItemService;
    @Inject CourseService courseService;
    @Inject LineItemCategoryService lineItemCategoryService;
    @Inject InstructorCostService instructorCostService;
    @Inject InstructorTypeCostService instructorTypeCostService;
    @Inject SectionGroupCostCommentService sectionGroupCostCommentService;
    @Inject SectionGroupCostInstructorService sectionGroupCostInstructorService;
    @Inject LineItemCommentService lineItemCommentService;
    @Inject ExpenseItemService expenseItemService;

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
        budgetScenario.setActiveTermsBlob("1010000001"); // default to Fall/Winter/Spring
        budgetScenario.setFromLiveData(false);
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
        budgetScenario.setFromLiveData(false);

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
    public BudgetScenario createFromExisting(Long workgroupId, Long scenarioId, String name, boolean copyFunds) {
        BudgetScenario originalBudgetScenario = budgetScenarioRepository.findById(scenarioId).orElse(null);

        if (originalBudgetScenario == null) {
            return null;
        }

        BudgetScenario budgetScenario = new BudgetScenario();
        budgetScenario.setBudget(originalBudgetScenario.getBudget());
        budgetScenario.setName(name);
        budgetScenario.setActiveTermsBlob(originalBudgetScenario.getActiveTermsBlob());
        budgetScenario.setFromLiveData(false);
        budgetScenario = budgetScenarioRepository.save(budgetScenario);

        List<SectionGroupCost> sectionGroupCostList = budgetScenario.getSectionGroupCosts();

        // Clone sectionGroupCosts
        for(SectionGroupCost originalSectionGroupCost : originalBudgetScenario.getSectionGroupCosts()) {
            SectionGroupCost sectionGroupCost = sectionGroupCostService.createOrUpdateFrom(originalSectionGroupCost, budgetScenario);
            sectionGroupCostInstructorService.copyInstructors(workgroupId, originalSectionGroupCost, sectionGroupCost);
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
        List<ExpenseItem> expenseItems = budgetScenario.getExpenseItems();
        expenseItems.addAll(expenseItemService.duplicateExpenses(budgetScenario, originalBudgetScenario));
        budgetScenario.setExpenseItems(expenseItems);

        budgetScenario = budgetScenarioRepository.save(budgetScenario);

        return budgetScenario;
    }

    public BudgetScenario createBudgetRequestScenario(long workgroupId, long scenarioId) {
        BudgetScenario originalScenario = budgetScenarioRepository.findById(scenarioId).orElse(null);

        if (originalScenario == null) { return null; }

        // create new budgetScenario with isSnapshot true, copy Budget TaCost, ReaderCost
        BudgetScenario budgetRequestScenario = new BudgetScenario();
        budgetRequestScenario.setBudget(originalScenario.getBudget());

        String budgetRequestPrefix;
        if (originalScenario.getBudget().getBudgetScenarios().stream().anyMatch(bs -> bs.getIsBudgetRequest() == true)) {
            budgetRequestPrefix = "Revised";
        } else {
            budgetRequestPrefix = "Initial";
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        Instant timestamp = Instant.now();
        ZonedDateTime PacificTime = timestamp.atZone(ZoneId.of("America/Los_Angeles"));
        budgetRequestScenario.setName(budgetRequestPrefix + " Budget Request - " + formatter.format(PacificTime));

        budgetRequestScenario.setActiveTermsBlob(originalScenario.getActiveTermsBlob());
        budgetRequestScenario.setFromLiveData(false);
        budgetRequestScenario.setIsBudgetRequest(true);
        budgetRequestScenario.setTaCost(originalScenario.getBudget().getTaCost());
        budgetRequestScenario.setReaderCost(originalScenario.getBudget().getReaderCost());
        budgetRequestScenario = budgetScenarioRepository.save(budgetRequestScenario);

        List<SectionGroupCost> sectionGroupCostList = budgetRequestScenario.getSectionGroupCosts();
        for (SectionGroupCost originalSectionGroupCost : originalScenario.getSectionGroupCosts()) {
            SectionGroupCost newSectionGroupCost = sectionGroupCostService.createOrUpdateFrom(originalSectionGroupCost, budgetRequestScenario);

            sectionGroupCostCommentService.copyComments(originalSectionGroupCost, newSectionGroupCost);
            sectionGroupCostInstructorService.copyInstructors(workgroupId, originalSectionGroupCost, newSectionGroupCost);

            sectionGroupCostList.add(newSectionGroupCost);
        }
        budgetRequestScenario.setSectionGroupCosts(sectionGroupCostList);

        List<LineItem> lineItemList = budgetRequestScenario.getLineItems();
        for (LineItem originalLineItem : originalScenario.getLineItems()) {
            LineItem newLineItem = lineItemService.createDuplicate(originalLineItem, budgetRequestScenario);

            lineItemCommentService.copyComments(originalLineItem, newLineItem);
            lineItemList.add(newLineItem);
        }
        budgetRequestScenario.setLineItems(lineItemList);
        List<ExpenseItem> expenseItems = budgetRequestScenario.getExpenseItems();
        expenseItems.addAll(expenseItemService.duplicateExpenses(budgetRequestScenario, originalScenario));
        budgetRequestScenario.setExpenseItems(expenseItems);

        instructorCostService.snapshotInstructorCosts(budgetRequestScenario, originalScenario);
        instructorTypeCostService.snapshotInstructorTypeCosts(budgetRequestScenario, originalScenario);

        return budgetScenarioRepository.save(budgetRequestScenario);
    }

    @Override
    public BudgetScenario approveBudgetRequestScenario(long workgroupId, long scenarioId) {
        BudgetScenario approvedScenario = budgetScenarioRepository.findById(scenarioId).orElse(null);

        // only one budget request should be approved at a time
        List<BudgetScenario> budgetRequestScenarios = budgetScenarioRepository.findbyWorkgroupIdAndYear(workgroupId,
            approvedScenario.getBudget().getSchedule().getYear());

        List<BudgetScenario> existingApprovedScenarios =
            budgetRequestScenarios.stream().filter(scenario -> scenario.getApproved() == true)
                .collect(Collectors.toList());

        for (BudgetScenario scenario : existingApprovedScenarios) {
            scenario.setApproved(false);
            budgetScenarioRepository.save(scenario);
        }

        approvedScenario.setApproved(true);
        return budgetScenarioRepository.save(approvedScenario);
    }

    @Override
    public BudgetScenario update(BudgetScenario budgetScenario) {
        return budgetScenarioRepository.save(budgetScenario);
    }

    @Override
    public List<BudgetScenario> findbyWorkgroupIdAndYear(long workgroupId, long year) {
        List<BudgetScenario> budgetScenarios = budgetScenarioRepository.findbyWorkgroupIdAndYear(workgroupId, year);

        BudgetScenario liveDataScenario = this.createOrUpdateFromLiveData(workgroupId, year);
        Boolean budgetAlreadyExisted = false;

        for (BudgetScenario budgetScenario : budgetScenarios) {
            if (liveDataScenario.getId() == budgetScenario.getId()) {
                budgetScenario = liveDataScenario;
                budgetAlreadyExisted = true;
            }
        }

        if (budgetAlreadyExisted == false) {
            budgetScenarios.add(liveDataScenario);
        }

        return budgetScenarios;
    }

    @Override
    public List<BudgetScenario> findByWorkgroupId(long workgroupId) {
        List<BudgetScenario> budgetScenarios = budgetScenarioRepository.findbyWorkgroupId(workgroupId);

        return budgetScenarios;
    }

    private BudgetScenario createOrUpdateFromLiveData(long workgroupId, long year) {
        BudgetScenario liveDataScenario = budgetScenarioRepository.findbyWorkgroupIdAndYearAndFromLiveData(workgroupId, year, true);

        if (liveDataScenario != null) {
            return this.updateFromLiveData(liveDataScenario, false);
        } else {
            return this.createFromLiveData(workgroupId, year);
        }
    }

    private BudgetScenario createFromLiveData(Long workgroupId, Long year) {
        Schedule schedule = scheduleService.findByWorkgroupIdAndYear(workgroupId, year);
        Budget budget = budgetRepository.findByScheduleId(schedule.getId());

        BudgetScenario liveDataScenario = new BudgetScenario();
        liveDataScenario.setName("Live Data");
        liveDataScenario.setBudget(budget);
        liveDataScenario.setActiveTermsBlob("1010000001"); // default to Fall/Winter/Spring
        liveDataScenario.setFromLiveData(true);
        liveDataScenario = this.budgetScenarioRepository.save(liveDataScenario);

        return this.updateFromLiveData(liveDataScenario, true);
    }

    private BudgetScenario updateFromLiveData(BudgetScenario liveDataScenario, Boolean newLiveDataScenario) {
        Schedule schedule = liveDataScenario.getBudget().getSchedule();
        List<SectionGroup> sectionGroups = sectionGroupService.findByScheduleId(schedule.getId());
        List<SectionGroupCost> sectionGroupCosts = liveDataScenario.getSectionGroupCosts();

        Map<String, SectionGroup> sectionGroupKeys = new HashMap<String, SectionGroup>();

        // Build sectionGroup hash
        for (SectionGroup sectionGroup : sectionGroups) {
            String key = sectionGroup.getCourse().getSubjectCode() + sectionGroup.getCourse().getCourseNumber() + sectionGroup.getCourse().getSequencePattern() + sectionGroup.getTermCode() + sectionGroup.getCourse().getEffectiveTermCode();
            sectionGroupKeys.put(key, sectionGroup);
        }

        List<SectionGroupCost> sectionGroupCostToRemove = new ArrayList<>();

        // Need to remove any sectionGroupCosts?
        for(SectionGroupCost sectionGroupCost : sectionGroupCosts) {
            String key = sectionGroupCost.getSubjectCode() + sectionGroupCost.getCourseNumber() + sectionGroupCost.getSequencePattern() + sectionGroupCost.getTermCode() + sectionGroupCost.getEffectiveTermCode();

            if (sectionGroupKeys.get(key) == null) {
                sectionGroupCostToRemove.add(sectionGroupCost);
            }
        }

        for (SectionGroupCost sectionGroupCost : sectionGroupCostToRemove) {
            sectionGroupCosts.remove(sectionGroupCost);
            sectionGroupCostService.delete(sectionGroupCost.getId());
        }

        // Need to add any sectionGroupCosts?
        Map<String, SectionGroupCost> sectionGroupCostKeys = new HashMap<String, SectionGroupCost>();

        for(SectionGroupCost sectionGroupCost : sectionGroupCosts) {
            String key = sectionGroupCost.getSubjectCode() + sectionGroupCost.getCourseNumber() + sectionGroupCost.getSequencePattern() + sectionGroupCost.getTermCode() + sectionGroupCost.getEffectiveTermCode();
            sectionGroupCostKeys.put(key, sectionGroupCost);
        }

        for (SectionGroup sectionGroup : sectionGroups) {
            String key = sectionGroup.getCourse().getSubjectCode() + sectionGroup.getCourse().getCourseNumber() + sectionGroup.getCourse().getSequencePattern() + sectionGroup.getTermCode() + sectionGroup.getCourse().getEffectiveTermCode();

            if (sectionGroupCostKeys.get(key) == null) {
                SectionGroupCost sectionGroupCost = sectionGroupCostService.createFromSectionGroup(sectionGroup, liveDataScenario);
                String sectionGroupCostKey = sectionGroupCost.getSubjectCode() + sectionGroupCost.getCourseNumber() + sectionGroupCost.getSequencePattern() + sectionGroupCost.getTermCode() + sectionGroupCost.getEffectiveTermCode();

                sectionGroupCosts.add(sectionGroupCost);
                sectionGroupCostKeys.put(sectionGroupCostKey, sectionGroupCost);
            }
        }

        // Need to update any sectionGroupCosts?
        for (SectionGroupCost sectionGroupCost : sectionGroupCosts) {
            String key = sectionGroupCost.getSubjectCode() + sectionGroupCost.getCourseNumber() + sectionGroupCost.getSequencePattern() + sectionGroupCost.getTermCode() + sectionGroupCost.getEffectiveTermCode();
            SectionGroup sectionGroup = sectionGroupKeys.get(key);

            sectionGroupCost = sectionGroupCostService.updateFromSectionGroup(sectionGroup, liveDataScenario);
        }

        liveDataScenario.setSectionGroupCosts(sectionGroupCosts);
        liveDataScenario = this.update(liveDataScenario);

        if (newLiveDataScenario) {
            // Calculate activeTermsBlob
            liveDataScenario.recalculateActiveTermsBlob();
            liveDataScenario = this.update(liveDataScenario);
        }

        return liveDataScenario;
    }

    @Override
    public BudgetScenario findById(long budgetScenarioId) {
        return budgetScenarioRepository.findById(budgetScenarioId).orElse(null);
    }

    @Override
    public void deleteById(long budgetScenarioId) {
        budgetScenarioRepository.deleteById(budgetScenarioId);
    }
}
