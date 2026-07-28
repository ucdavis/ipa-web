package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.banner.dto.BannerAssignment;
import edu.ucdavis.dss.datamart.DopeSummary;
import edu.ucdavis.dss.datamart.DopeSummaryCalculator;
import edu.ucdavis.dss.datamart.DopeTotals;
import edu.ucdavis.dss.datamart.dto.DopeRecord;
import edu.ucdavis.dss.ipa.api.components.budgetReconciliationReport.views.BudgetReconciliationCategoryView;
import edu.ucdavis.dss.ipa.api.components.budgetReconciliationReport.views.BudgetReconciliationReportView;
import edu.ucdavis.dss.ipa.api.components.budgetReconciliationReport.views.CourseStaffingPersonView;
import edu.ucdavis.dss.ipa.api.components.budgetReconciliationReport.views.CourseStaffingView;
import edu.ucdavis.dss.ipa.entities.Budget;
import edu.ucdavis.dss.ipa.entities.BudgetScenario;
import edu.ucdavis.dss.ipa.entities.ExpenseItem;
import edu.ucdavis.dss.ipa.entities.LineItem;
import edu.ucdavis.dss.ipa.entities.SectionGroupCost;
import edu.ucdavis.dss.ipa.entities.SectionGroupCostInstructor;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.entities.enums.BudgetSummary;
import edu.ucdavis.dss.ipa.entities.enums.InstructorType;
import edu.ucdavis.dss.ipa.entities.enums.TermDescription;
import edu.ucdavis.dss.ipa.repositories.BannerRepository;
import edu.ucdavis.dss.ipa.services.BudgetCalculationService;
import edu.ucdavis.dss.ipa.services.BudgetReconciliationReportService;
import edu.ucdavis.dss.ipa.services.BudgetScenarioService;
import edu.ucdavis.dss.ipa.services.BudgetService;
import edu.ucdavis.dss.ipa.services.DopeCostService;
import edu.ucdavis.dss.ipa.services.ExpenseItemService;
import edu.ucdavis.dss.ipa.services.TermService;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@Profile({"development", "production", "staging"})
@ConditionalOnProperty(name = "DATAMART_URL")
public class JpaBudgetReconciliationReportService implements BudgetReconciliationReportService {
    @Inject BudgetService budgetService;
    @Inject BudgetScenarioService budgetScenarioService;
    @Inject TermService termService;
    @Inject ExpenseItemService expenseItemService;
    @Inject BudgetCalculationService budgetCalculationService;
    /* required: DOPE actuals are what the report reconciles against, so it is gated with them */
    @Inject DopeCostService dopeCostService;
    /* optional: Banner supplies the actual course assignments the By Course view compares against,
       and the TA counts on the By Category TAs row */
    @Autowired(required = false) BannerRepository bannerRepository;

    /* Ladder Faculty is state-funded ($0 in the SIB budget) — context row, not a variance */
    private static final Set<String> CONTEXT_ONLY_TYPES =
        Set.of("Ladder Faculty", DopeSummaryCalculator.UNMAPPED);

    private static final String PLACEHOLDER_ROLE = "Staff (placeholder)";

    private static final Map<String, BudgetSummary[]> BUDGET_SUMMARY_BY_INSTRUCTOR_TYPE = buildBudgetSummaryMap();

    private static Map<String, BudgetSummary[]> buildBudgetSummaryMap() {
        Map<String, BudgetSummary[]> map = new LinkedHashMap<>();
        map.put("Ladder Faculty", new BudgetSummary[] {BudgetSummary.LADDER_FACULTY_COST, BudgetSummary.LADDER_FACULTY_COUNT});
        map.put("New Faculty Hire", new BudgetSummary[] {BudgetSummary.NEW_FACULTY_HIRE_COST, BudgetSummary.NEW_FACULTY_HIRE_COUNT});
        map.put("Lecturer SOE", new BudgetSummary[] {BudgetSummary.LECTURER_SOE_COST, BudgetSummary.LECTURER_SOE_COUNT});
        map.put("Continuing Lecturer", new BudgetSummary[] {BudgetSummary.CONTINUING_LECTURER_COST, BudgetSummary.CONTINUING_LECTURER_COUNT});
        map.put("Emeriti - Recalled", new BudgetSummary[] {BudgetSummary.EMERITI_COST, BudgetSummary.EMERITI_COUNT});
        map.put("Visiting Professor", new BudgetSummary[] {BudgetSummary.VISITING_PROFESSOR_COST, BudgetSummary.VISITING_PROFESSOR_COUNT});
        map.put("Unit 18 Pre-Six Lecturer", new BudgetSummary[] {BudgetSummary.UNIT18_LECTURER_COST, BudgetSummary.UNIT18_LECTURER_COUNT});
        map.put("Continuing Lecturer - Augmentation", new BudgetSummary[] {BudgetSummary.CONTINUING_LECTURER_AUGMENTATION_COST, BudgetSummary.CONTINUING_LECTURER_AUGMENTATION_COUNT});
        map.put("Associate Instructor", new BudgetSummary[] {BudgetSummary.ASSOCIATE_INSTRUCTOR_COST, BudgetSummary.ASSOCIATE_INSTRUCTOR_COUNT});
        map.put("Instructor", new BudgetSummary[] {BudgetSummary.INSTRUCTOR_COST, BudgetSummary.INSTRUCTOR_COUNT});
        map.put("TAs", new BudgetSummary[] {BudgetSummary.TA_COST, BudgetSummary.TA_COUNT});
        map.put("Readers", new BudgetSummary[] {BudgetSummary.READER_COST, BudgetSummary.READER_COUNT});
        return map;
    }

    @Override
    public BudgetReconciliationReportView generate(long workgroupId, long year) {
        int fiscalYear = (int) year + 1;

        // ---- planned side, shared by both views ----
        Budget budget = budgetService.findOrCreateByWorkgroupIdAndYear(workgroupId, year);
        BudgetScenario budgetScenario = selectScenario(workgroupId, year);
        Workgroup workgroup = budget.getSchedule().getWorkgroup();

        String departmentCode = departmentCodeFor(workgroup);

        List<String> termCodes = new ArrayList<>();
        for (String termCodeShort : Arrays.asList(TermDescription.FALL.getShortTermCode(),
            TermDescription.WINTER.getShortTermCode(), TermDescription.SPRING.getShortTermCode())) {
            termCodes.add(termService.getTermCodeFromYearAndTerm(budget.getSchedule().getYear(), termCodeShort));
        }

        List<SectionGroupCost> sectionGroupCosts = budgetScenario.getSectionGroupCosts().stream()
            .filter(sgc -> (sgc.isDisabled() == false && termCodes.contains(sgc.getTermCode())))
            .collect(Collectors.toList());
        List<String> subjectCodes = sectionGroupCosts.stream()
            .map(SectionGroupCost::getSubjectCode).filter(Objects::nonNull).distinct()
            .collect(Collectors.toList());

        // ---- actuals side, shared by both views: one DOPE query and one tally serve the whole report ----
        List<DopeRecord> dopeRecords = dopeCostService.getDopeRecords(departmentCode, fiscalYear);
        if (dopeRecords == null) {
            throw new IllegalStateException("Datamart query failed for department " + departmentCode);
        }
        DopeSummary dopeSummary = DopeSummaryCalculator.calculate(dopeRecords);

        List<BannerAssignment> assignments = List.of();
        if (bannerRepository != null) {
            assignments = bannerRepository.getCourseAssignments(subjectCodes, termCodes);
            if (assignments == null) {
                throw new IllegalStateException("Banner query failed for workgroup " + workgroupId);
            }
        }

        // per Banner person: distinct courses (the even-split denominator), empl id (for the DOPE
        // match), and whether they are a real instructor rather than a "The Staff" placeholder
        Map<String, Set<String>> coursesByPerson = new LinkedHashMap<>();
        Map<String, String> emplIdByPerson = new HashMap<>();
        Set<String> instructorPersonIds = new HashSet<>();
        for (BannerAssignment assignment : assignments) {
            String personId = personId(assignment);
            coursesByPerson.computeIfAbsent(personId, k -> new HashSet<>()).add(courseKey(
                assignment.getTermCode(), assignment.getSubjectCode(), assignment.getCourseNumber()));
            if (assignment.getEmplId() != null) {
                emplIdByPerson.putIfAbsent(personId, DopeCostService.normalizeEmplId(assignment.getEmplId()));
            }
            if (!PLACEHOLDER_ROLE.equals(actualRole(assignment.getFunctionalCategory()))) {
                instructorPersonIds.add(personId);
            }
        }

        Map<String, DopeCostService.PersonCostResult> costByPerson = dopeCostService.resolveCosts(
            dopeRecords, instructorPersonIds, emplIdByPerson, coursesByPerson, fiscalYear);

        return new BudgetReconciliationReportView(workgroupId, workgroup.getCode(), year, fiscalYear,
            departmentCode, budgetScenario.getName(),
            buildCategories(budget, budgetScenario, workgroup, termCodes, sectionGroupCosts,
                subjectCodes, dopeSummary),
            buildCourses(workgroup, sectionGroupCosts, assignments, costByPerson),
            // the cost bridge only means something with both sides: Banner people to match, DOPE to
            // match them against. Without Banner the counts would all read zero against a real
            // department total, which invites "nothing matched" when nothing was ever attempted.
            bannerRepository == null ? null : dopeCostService.summarize(dopeSummary, costByPerson));
    }

    /**
     * By Category: the approved budget's per-instructor-type cost against the payroll department's
     * DOPE actuals.
     */
    private List<BudgetReconciliationCategoryView> buildCategories(
            Budget budget, BudgetScenario budgetScenario, Workgroup workgroup, List<String> termCodes,
            List<SectionGroupCost> sectionGroupCosts, List<String> subjectCodes, DopeSummary dopeSummary) {
        List<LineItem> lineItems = budgetScenario.getLineItems().stream()
            .filter(li -> li.getHidden() == false).collect(Collectors.toList());
        List<ExpenseItem> expenseItems = expenseItemService.findByBudgetScenarioId(budgetScenario.getId());

        Map<String, Map<BudgetSummary, BigDecimal>> termTotals = budgetCalculationService.calculateTermTotals(
            budget, budgetScenario, sectionGroupCosts, termCodes, workgroup, lineItems, expenseItems);
        Map<BudgetSummary, BigDecimal> planned = termTotals.get("combined");

        // actual TA assignments from Banner: one row per TA-section-term appointment, scoped to this
        // budget's course subjects and academic-year terms. Supplementary and only attached to the
        // TAs row; null when Banner isn't configured or the query fails.
        BannerRepository.TaCounts bannerTaCounts = bannerRepository != null
            ? bannerRepository.getTaCounts(subjectCodes, termCodes) : null;
        Integer bannerTaAssignments = bannerTaCounts != null ? bannerTaCounts.assignments() : null;
        Integer bannerTaIndividuals = bannerTaCounts != null ? bannerTaCounts.individuals() : null;

        List<BudgetReconciliationCategoryView> categories = new ArrayList<>();
        for (Map.Entry<String, BudgetSummary[]> entry : BUDGET_SUMMARY_BY_INSTRUCTOR_TYPE.entrySet()) {
            categories.add(buildCategory(entry.getKey(), planned.get(entry.getValue()[0]),
                planned.get(entry.getValue()[1]),
                "TAs".equals(entry.getKey()) ? bannerTaAssignments : null,
                "TAs".equals(entry.getKey()) ? bannerTaIndividuals : null,
                dopeSummary.getByInstructorType().get(entry.getKey())));
        }
        categories.add(buildCategory(DopeSummaryCalculator.UNMAPPED, null, null, null, null,
            dopeSummary.getByInstructorType().get(DopeSummaryCalculator.UNMAPPED)));

        return categories;
    }

    /**
     * By Course: planned instructors from the budget scenario paired with Banner's actual
     * assignments, per course. Planned-only when Banner isn't configured; DOPE cost is attached to
     * the actual rows when costByPerson is present.
     */
    private List<CourseStaffingView> buildCourses(
            Workgroup workgroup, List<SectionGroupCost> sectionGroupCosts,
            List<BannerAssignment> assignments,
            Map<String, DopeCostService.PersonCostResult> costByPerson) {
        // planned staffing per course, keyed term|subject|courseNumber (natural sort term>subj>course)
        Map<String, List<CourseStaffingPersonView>> plannedByCourse = new LinkedHashMap<>();
        for (SectionGroupCost sectionGroupCost : sectionGroupCosts) {
            String key = courseKey(sectionGroupCost.getTermCode(), sectionGroupCost.getSubjectCode(),
                sectionGroupCost.getCourseNumber());
            List<CourseStaffingPersonView> planned =
                plannedByCourse.computeIfAbsent(key, k -> new ArrayList<>());
            for (SectionGroupCostInstructor sgci : sectionGroupCost.getSectionGroupCostInstructors()) {
                String name = sgci.getInstructor() != null ? sgci.getInstructor().getFullName() : null;
                InstructorType instructorType = instructorTypeFor(sgci, workgroup);
                planned.add(new CourseStaffingPersonView(name,
                    instructorType != null ? instructorType.getDescription() : null, null));
            }
        }

        // dedupe actual staffing within a course: one entry per person+role, sections = CRN count
        Map<String, LinkedHashMap<String, PersonAssignments>> actualAgg = new LinkedHashMap<>();
        for (BannerAssignment assignment : assignments) {
            String courseKey = courseKey(assignment.getTermCode(), assignment.getSubjectCode(),
                assignment.getCourseNumber());
            String personId = personId(assignment);
            String personKey = personId + "|" + assignment.getFunctionalCategory();
            PersonAssignments person = actualAgg.computeIfAbsent(courseKey, k -> new LinkedHashMap<>())
                .computeIfAbsent(personKey, k -> new PersonAssignments(personId,
                    assignment.getFullName(), actualRole(assignment.getFunctionalCategory())));
            person.sections++;
        }

        Map<String, List<CourseStaffingPersonView>> actualByCourse = new LinkedHashMap<>();
        for (Map.Entry<String, LinkedHashMap<String, PersonAssignments>> entry : actualAgg.entrySet()) {
            List<CourseStaffingPersonView> people = new ArrayList<>();
            for (PersonAssignments person : entry.getValue().values()) {
                CourseStaffingPersonView view =
                    new CourseStaffingPersonView(person.name, person.role, person.sections);
                DopeCostService.PersonCostResult result =
                    costByPerson == null ? null : costByPerson.get(person.personId);
                if (result != null) {
                    view.setCost(result.label(), result.jobCode(), result.personSalary(),
                        result.personCost(), result.allocatedSalary(), result.allocatedCost(),
                        result.summerSalary());
                }
                people.add(view);
            }
            actualByCourse.put(entry.getKey(), people);
        }

        SortedSet<String> keys = new TreeSet<>();
        keys.addAll(plannedByCourse.keySet());
        keys.addAll(actualByCourse.keySet());

        List<CourseStaffingView> courses = new ArrayList<>();
        for (String key : keys) {
            String[] parts = key.split("\\|", -1);
            courses.add(new CourseStaffingView(parts[0], parts[1], parts[2],
                plannedByCourse.getOrDefault(key, List.of()),
                actualByCourse.getOrDefault(key, List.of())));
        }

        return courses;
    }

    /**
     * The payroll DEPT_CD scoping the actuals, taken from the workgroup itself — so a caller cannot
     * pair one department's plan with another's payroll.
     */
    private String departmentCodeFor(Workgroup workgroup) {
        String departmentCode = workgroup.getDepartmentCode();

        if (departmentCode == null || departmentCode.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format(
                "Workgroup %d (%s) has no department code, so there are no payroll actuals to "
                    + "reconcile. Either it is outside this report's Letters & Science scope, or its "
                    + "DepartmentCode has not been populated yet.",
                workgroup.getId(), workgroup.getName()));
        }

        return departmentCode;
    }

    /* the approved budget request, falling back to the most recent scenario */
    private BudgetScenario selectScenario(long workgroupId, long year) {
        List<BudgetScenario> budgetScenarios = budgetScenarioService.findbyWorkgroupIdAndYear(workgroupId, year);

        return budgetScenarios.stream().filter(BudgetScenario::getIsBudgetRequest)
            .max(Comparator.comparing(BudgetScenario::getCreationDate))
            .orElseGet(() -> budgetScenarios.stream()
                .max(Comparator.comparing(BudgetScenario::getCreationDate))
                .orElseThrow(() -> new IllegalStateException(
                    "No budget scenario found for workgroup " + workgroupId + " year " + year)));
    }

    private BudgetReconciliationCategoryView buildCategory(String instructorType, BigDecimal plannedCost,
                                                           BigDecimal plannedCount,
                                                           Integer bannerTaAssignments,
                                                           Integer bannerTaIndividuals, DopeTotals tally) {
        boolean includedInComparison = !CONTEXT_ONLY_TYPES.contains(instructorType);

        if (tally == null) {
            tally = new DopeTotals();
        }

        // for summer-bearing types, headcount/FTE/salary all exclude Jul-Sep so they line up with
        // the academic-year plan; 12-month appointments keep full-year figures
        boolean summerBearing = DopeSummaryCalculator.SUMMER_SESSION_BEARING_TYPES.contains(instructorType);
        int actualPeople = summerBearing ? tally.getAcademicYearPeople() : tally.getPeople();
        BigDecimal actualFte = summerBearing ? tally.getAcademicYearFte() : tally.getFte();
        BigDecimal comparableSalary = summerBearing ? tally.getAcademicYearSalary() : tally.getSalary();
        BigDecimal variance = includedInComparison && plannedCost != null
            ? comparableSalary.subtract(plannedCost) : null;

        return new BudgetReconciliationCategoryView(instructorType, includedInComparison, summerBearing,
            plannedCost, plannedCount, bannerTaAssignments, bannerTaIndividuals,
            actualPeople, actualFte, tally.getTotalCompensation(),
            tally.getSalary(), tally.getJulSepCompensation(), tally.getJulSepSalary(),
            comparableSalary, variance);
    }

    /* the planned instructor type for an assignment, categorized the way calculateTermTotals does */
    private InstructorType instructorTypeFor(SectionGroupCostInstructor sgci, Workgroup workgroup) {
        long instructorTypeId = budgetCalculationService.calculateSectionGroupInstructorTypeId(sgci, workgroup);
        return Arrays.stream(InstructorType.values())
            .filter(type -> type.getId() == instructorTypeId).findFirst().orElse(null);
    }

    private static String personId(BannerAssignment assignment) {
        return assignment.getPidm() != null ? assignment.getPidm() : assignment.getFullName();
    }

    private static String courseKey(String termCode, String subjectCode, String courseNumber) {
        return termCode + "|" + subjectCode + "|" + courseNumber;
    }

    /* FCTG_CODE NF is the "The Staff" placeholder on 9-series (independent/group study) courses,
       not a real countable instructor — label it so; other codes (FA/AI/TA) pass through. */
    private static String actualRole(String functionalCategory) {
        return "NF".equalsIgnoreCase(functionalCategory) ? PLACEHOLDER_ROLE : functionalCategory;
    }

    /* one person's assignments to a single course, accumulated across its section CRNs */
    private static class PersonAssignments {
        final String personId;
        final String name;
        final String role;
        int sections;

        PersonAssignments(String personId, String name, String role) {
            this.personId = personId;
            this.name = name;
            this.role = role;
        }
    }
}
