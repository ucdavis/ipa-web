package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.banner.dto.BannerAssignment;
import edu.ucdavis.dss.datamart.DopeSummary;
import edu.ucdavis.dss.datamart.DopeSummaryCalculator;
import edu.ucdavis.dss.datamart.DopeTotals;
import edu.ucdavis.dss.datamart.dto.DopeRecord;
import edu.ucdavis.dss.ipa.api.components.budgetReconciliationReport.FteDepartment;
import edu.ucdavis.dss.ipa.api.components.budgetReconciliationReport.views.BudgetReconciliationCategoryView;
import edu.ucdavis.dss.ipa.api.components.budgetReconciliationReport.views.BudgetReconciliationReportView;
import edu.ucdavis.dss.ipa.api.components.budgetReconciliationReport.views.CourseStaffingPersonView;
import edu.ucdavis.dss.ipa.api.components.budgetReconciliationReport.views.CourseStaffingView;
import edu.ucdavis.dss.ipa.api.components.budgetReconciliationReport.views.PlannedTotalsView;
import edu.ucdavis.dss.ipa.entities.Budget;
import edu.ucdavis.dss.ipa.entities.BudgetScenario;
import edu.ucdavis.dss.ipa.entities.ExpenseItem;
import edu.ucdavis.dss.ipa.entities.LineItem;
import edu.ucdavis.dss.ipa.entities.SectionGroupCost;
import edu.ucdavis.dss.ipa.entities.SectionGroupCostInstructor;
import edu.ucdavis.dss.ipa.entities.Term;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.entities.enums.BudgetSummary;
import edu.ucdavis.dss.ipa.entities.enums.InstructorType;
import edu.ucdavis.dss.ipa.entities.enums.TermDescription;
import edu.ucdavis.dss.ipa.repositories.BannerRepository;
import edu.ucdavis.dss.ipa.repositories.BudgetScenarioRepository;
import edu.ucdavis.dss.ipa.services.BudgetCalculationService;
import edu.ucdavis.dss.ipa.services.BudgetReconciliationReportService;
import edu.ucdavis.dss.ipa.services.BudgetService;
import edu.ucdavis.dss.ipa.services.DopeCostService;
import edu.ucdavis.dss.ipa.services.ExpenseItemService;
import edu.ucdavis.dss.ipa.services.TermService;
import edu.ucdavis.dss.ipa.services.WorkgroupService;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile({"development", "production", "staging"})
@ConditionalOnNotWebApplication
public class JpaBudgetReconciliationReportService implements BudgetReconciliationReportService {
    @Inject BudgetService budgetService;
    @Inject BudgetScenarioRepository budgetScenarioRepository;
    @Inject TermService termService;
    @Inject ExpenseItemService expenseItemService;
    @Inject BudgetCalculationService budgetCalculationService;
    @Inject WorkgroupService workgroupService;
    @Inject DopeCostService dopeCostService;
    @Inject BannerRepository bannerRepository;

    /* Ladder Faculty carries no SIB plan to compare against, so it is a context row rather than a
       difference. This is not a claim about its funding source. */
    private static final Set<String> CONTEXT_ONLY_TYPES =
        Set.of("Ladder Faculty", DopeSummaryCalculator.UNMAPPED);

    private static final String PLACEHOLDER_ROLE = "Staff (placeholder)";

    /* TA and Reader cost is per-course counts x rate rather than a SectionGroupCostInstructor, so it
       sits outside REPLACEMENT_COST */
    private static final Set<String> COUNT_BASED_TYPES = Set.of("TAs", "Readers");

    /**
     * The plan's keys for each reported category, in display order, laid out as alternating
     * [cost, count] pairs. More than one pair means the category absorbs another: Unit 18 Pre-Six
     * Lecturer carries Continuing Lecturer - Augmentation too, matching the payroll mapping in
     * DopeSummaryCalculator. The two must change together.
     */
    private static final Map<String, BudgetSummary[]> BUDGET_SUMMARY_BY_INSTRUCTOR_TYPE = buildBudgetSummaryMap();

    private static Map<String, BudgetSummary[]> buildBudgetSummaryMap() {
        Map<String, BudgetSummary[]> map = new LinkedHashMap<>();
        map.put("Ladder Faculty", new BudgetSummary[] {BudgetSummary.LADDER_FACULTY_COST, BudgetSummary.LADDER_FACULTY_COUNT});
        map.put("New Faculty Hire", new BudgetSummary[] {BudgetSummary.NEW_FACULTY_HIRE_COST, BudgetSummary.NEW_FACULTY_HIRE_COUNT});
        map.put("Lecturer SOE", new BudgetSummary[] {BudgetSummary.LECTURER_SOE_COST, BudgetSummary.LECTURER_SOE_COUNT});
        map.put("Continuing Lecturer", new BudgetSummary[] {BudgetSummary.CONTINUING_LECTURER_COST, BudgetSummary.CONTINUING_LECTURER_COUNT});
        map.put("Emeriti - Recalled", new BudgetSummary[] {BudgetSummary.EMERITI_COST, BudgetSummary.EMERITI_COUNT});
        map.put("Visiting Professor", new BudgetSummary[] {BudgetSummary.VISITING_PROFESSOR_COST, BudgetSummary.VISITING_PROFESSOR_COUNT});
        map.put("Unit 18 Pre-Six Lecturer", new BudgetSummary[] {
            BudgetSummary.UNIT18_LECTURER_COST, BudgetSummary.UNIT18_LECTURER_COUNT,
            BudgetSummary.CONTINUING_LECTURER_AUGMENTATION_COST, BudgetSummary.CONTINUING_LECTURER_AUGMENTATION_COUNT});
        map.put("Associate Instructor", new BudgetSummary[] {BudgetSummary.ASSOCIATE_INSTRUCTOR_COST, BudgetSummary.ASSOCIATE_INSTRUCTOR_COUNT});
        map.put("Instructor", new BudgetSummary[] {BudgetSummary.INSTRUCTOR_COST, BudgetSummary.INSTRUCTOR_COUNT});
        map.put("TAs", new BudgetSummary[] {BudgetSummary.TA_COST, BudgetSummary.TA_COUNT});
        map.put("Readers", new BudgetSummary[] {BudgetSummary.READER_COST, BudgetSummary.READER_COUNT});
        return map;
    }

    /**
     * The planned side walks lazy associations, and the job has no open-in-view session, so the report
     * needs its own transaction. Not readOnly: findOrCreateByWorkgroupIdAndYear can insert a Schedule
     * and Budget, and a read-only transaction would drop that write silently.
     */
    @Override
    @Transactional
    public BudgetReconciliationReportView generate(long workgroupId, long year) {
        int fiscalYear = (int) year + 1;

        Budget budget = budgetService.findOrCreateByWorkgroupIdAndYear(workgroupId, year);
        Workgroup workgroup = budget.getSchedule().getWorkgroup();

        FteDepartment fteDepartment = fteDepartmentFor(workgroup);
        String departmentCode = fteDepartment.payrollCode();
        List<Workgroup> payrollGroup = payrollDepartmentGroup(workgroup, fteDepartment);

        List<String> termCodes = new ArrayList<>();
        for (String termCodeShort : Arrays.asList(TermDescription.FALL.getShortTermCode(),
            TermDescription.WINTER.getShortTermCode(), TermDescription.SPRING.getShortTermCode())) {
            termCodes.add(termService.getTermCodeFromYearAndTerm(budget.getSchedule().getYear(), termCodeShort));
        }

        // The plan is the union across the payroll group, so it faces the same population the single DOPE
        // query below returns. Each program keeps its own workgroup because
        // calculateSectionGroupInstructorTypeId resolves a cost line's instructor type against the
        // workgroup that owns it.
        List<SectionGroupCost> sectionGroupCosts = new ArrayList<>();
        Map<Long, Workgroup> workgroupByCostId = new HashMap<>();
        Map<BudgetSummary, BigDecimal> planned = new EnumMap<>(BudgetSummary.class);
        List<String> scenarioNames = new ArrayList<>();

        for (Workgroup member : payrollGroup) {
            Budget memberBudget = member.getId() == workgroup.getId() ? budget
                : budgetService.findOrCreateByWorkgroupIdAndYear(member.getId(), year);
            BudgetScenario memberScenario = selectScenario(member, year);

            List<SectionGroupCost> memberCosts = memberScenario.getSectionGroupCosts().stream()
                .filter(sgc -> (sgc.isDisabled() == false && termCodes.contains(sgc.getTermCode())))
                .collect(Collectors.toList());
            memberCosts.forEach(cost -> workgroupByCostId.put(cost.getId(), member));
            sectionGroupCosts.addAll(memberCosts);

            plannedTermTotals(memberBudget, memberScenario, member, termCodes, memberCosts)
                .forEach((key, value) -> {
                    if (value != null) {
                        planned.merge(key, value, BigDecimal::add);
                    }
                });

            scenarioNames.add(payrollGroup.size() == 1 ? memberScenario.getName()
                : member.getCode() + ": " + memberScenario.getName());
        }

        List<String> subjectCodes = sectionGroupCosts.stream()
            .map(SectionGroupCost::getSubjectCode).filter(Objects::nonNull).distinct()
            .collect(Collectors.toList());

        List<DopeRecord> dopeRecords = dopeCostService.getDopeRecords(departmentCode, fiscalYear);
        if (dopeRecords == null) {
            throw new IllegalStateException("Datamart query failed for department " + departmentCode);
        }
        DopeSummary dopeSummary = DopeSummaryCalculator.calculate(dopeRecords);

        List<BannerAssignment> assignments = bannerRepository.getCourseAssignments(subjectCodes, termCodes);
        if (assignments == null) {
            throw new IllegalStateException("Banner query failed for workgroup " + workgroupId);
        }

        Map<String, String> emplIdByPerson = new HashMap<>();
        Set<String> instructorPersonIds = new HashSet<>();
        for (BannerAssignment assignment : assignments) {
            String personId = personId(assignment);
            if (assignment.getEmplId() != null) {
                emplIdByPerson.putIfAbsent(personId, DopeCostService.normalizeEmplId(assignment.getEmplId()));
            }
            if (!PLACEHOLDER_ROLE.equals(actualRole(assignment.getFunctionalCategory()))) {
                instructorPersonIds.add(personId);
            }
        }

        Map<String, DopeCostService.PersonCostResult> costByPerson = dopeCostService.resolveCosts(
            dopeRecords, instructorPersonIds, emplIdByPerson, fiscalYear);

        BannerRepository.TaCounts bannerTaCounts = bannerRepository.getTaCounts(subjectCodes, termCodes);
        if (bannerTaCounts == null) {
            throw new IllegalStateException("Banner TA counts query failed for workgroup " + workgroupId);
        }

        return new BudgetReconciliationReportView(workgroupId, workgroup.getCode(), fteDepartment.name(),
            year, fiscalYear,
            departmentCode, String.join("; ", scenarioNames),
            plannedTotalsFrom(planned),
            buildCategories(planned, dopeSummary),
            buildCourses(workgroupByCostId, sectionGroupCosts, assignments, costByPerson),
            dopeCostService.summarize(dopeSummary, costByPerson),
            bannerTaCounts.assignments(),
            bannerTaCounts.individuals());
    }

    private Map<BudgetSummary, BigDecimal> plannedTermTotals(
            Budget budget, BudgetScenario budgetScenario, Workgroup workgroup,
            List<String> termCodes, List<SectionGroupCost> sectionGroupCosts) {
        List<LineItem> lineItems = budgetScenario.getLineItems().stream()
            .filter(li -> li.getHidden() == false).collect(Collectors.toList());
        List<ExpenseItem> expenseItems = expenseItemService.findByBudgetScenarioId(budgetScenario.getId());

        return budgetCalculationService.calculateTermTotals(budget, budgetScenario, sectionGroupCosts,
            termCodes, workgroup, lineItems, expenseItems).get("combined");
    }

    /**
     * The scenario's bottom line, decomposed so the reported categories add up to it.
     *
     * REPLACEMENT_COST is misleadingly named upstream: calculateTermTotals accumulates every
     * instructor assignment's cost into it, not just replacements. Subtracting the categorized
     * instructor types leaves whatever landed in no category.
     */
    private PlannedTotalsView plannedTotalsFrom(Map<BudgetSummary, BigDecimal> planned) {
        BigDecimal categorizedInstructorCost = BigDecimal.ZERO;
        for (Map.Entry<String, BudgetSummary[]> entry : BUDGET_SUMMARY_BY_INSTRUCTOR_TYPE.entrySet()) {
            if (COUNT_BASED_TYPES.contains(entry.getKey())) {
                continue;
            }
            categorizedInstructorCost = categorizedInstructorCost.add(sumPlanned(planned, entry.getValue(), 0));
        }

        return new PlannedTotalsView(
            planned.get(BudgetSummary.REPLACEMENT_COST).subtract(categorizedInstructorCost),
            planned.get(BudgetSummary.TOTAL_EXPENSES),
            planned.get(BudgetSummary.TOTAL_TEACHING_COST));
    }

    /* offset 0 sums a category's cost keys, offset 1 its count keys */
    private static BigDecimal sumPlanned(Map<BudgetSummary, BigDecimal> planned, BudgetSummary[] keys,
                                         int offset) {
        BigDecimal total = BigDecimal.ZERO;
        for (int i = offset; i < keys.length; i += 2) {
            BigDecimal value = planned.get(keys[i]);
            if (value != null) {
                total = total.add(value);
            }
        }
        return total;
    }

    private List<BudgetReconciliationCategoryView> buildCategories(
            Map<BudgetSummary, BigDecimal> planned, DopeSummary dopeSummary) {
        List<BudgetReconciliationCategoryView> categories = new ArrayList<>();
        for (Map.Entry<String, BudgetSummary[]> entry : BUDGET_SUMMARY_BY_INSTRUCTOR_TYPE.entrySet()) {
            categories.add(buildCategory(entry.getKey(),
                sumPlanned(planned, entry.getValue(), 0),
                sumPlanned(planned, entry.getValue(), 1),
                dopeSummary.getByInstructorType().get(entry.getKey())));
        }
        categories.add(buildCategory(DopeSummaryCalculator.UNMAPPED, null, null,
            dopeSummary.getByInstructorType().get(DopeSummaryCalculator.UNMAPPED)));

        return categories;
    }

    private List<CourseStaffingView> buildCourses(
            Map<Long, Workgroup> workgroupByCostId, List<SectionGroupCost> sectionGroupCosts,
            List<BannerAssignment> assignments,
            Map<String, DopeCostService.PersonCostResult> costByPerson) {
        Map<String, List<CourseStaffingPersonView>> plannedByCourse = new LinkedHashMap<>();
        // titles come from the plan only; Banner's assignment table carries none
        Map<String, String> titleByCourse = new HashMap<>();
        for (SectionGroupCost sectionGroupCost : sectionGroupCosts) {
            String key = courseKey(sectionGroupCost.getTermCode(), sectionGroupCost.getSubjectCode(),
                sectionGroupCost.getCourseNumber());
            titleByCourse.putIfAbsent(key, sectionGroupCost.getTitle());
            List<CourseStaffingPersonView> planned =
                plannedByCourse.computeIfAbsent(key, k -> new ArrayList<>());
            for (SectionGroupCostInstructor sgci : sectionGroupCost.getSectionGroupCostInstructors()) {
                String name = sgci.getInstructor() != null ? sgci.getInstructor().getFullName() : null;
                InstructorType instructorType =
                    instructorTypeFor(sgci, workgroupByCostId.get(sectionGroupCost.getId()));
                planned.add(new CourseStaffingPersonView(name,
                    instructorType != null ? instructorType.getDescription() : null, null));
            }
        }

        // Banner has one row per person and section (CRN); list each person once per role
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
            String termCode = entry.getKey().split("\\|", -1)[0];
            List<PersonAssignments> ordered = new ArrayList<>(entry.getValue().values());
            ordered.sort(Comparator.comparingInt((PersonAssignments p) -> roleOrder(p.role))
                .thenComparing(p -> p.name == null ? "" : p.name));
            for (PersonAssignments person : ordered) {
                CourseStaffingPersonView view =
                    new CourseStaffingPersonView(person.name, person.role, person.sections);
                DopeCostService.PersonCostResult result =
                    costByPerson.get(person.personId);
                if (result != null) {
                    // scoped to this row's term and Banner role, so a TA row leaves out the same person's
                    // Reader appointment
                    Set<String> roleTypes = instructorTypesFor(person.role);
                    Set<Integer> termMonths = fiscalMonthsFor(termCode);
                    view.setCost(result.label(), payrollDetail(result, termMonths, roleTypes),
                        result.fteFor(termMonths, roleTypes));
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
                titleByCourse.get(key),
                plannedByCourse.getOrDefault(key, List.of()),
                actualByCourse.getOrDefault(key, List.of())));
        }

        return courses;
    }

    private FteDepartment fteDepartmentFor(Workgroup workgroup) {
        FteDepartment fteDepartment = FteDepartment.forWorkgroupCode(workgroup.getCode());

        if (fteDepartment == null) {
            throw new IllegalArgumentException(String.format(
                "Workgroup %d (%s) has no payroll department, so there are no payroll actuals to "
                    + "reconcile. It is outside this report's Letters & Science scope.",
                workgroup.getId(), workgroup.getName()));
        }

        return fteDepartment;
    }

    /**
     * The latest budget request for the workgroup and year.
     *
     * Calls the repository, not BudgetScenarioService: the service's findbyWorkgroupIdAndYear runs
     * createOrUpdateFromLiveData, which would rebuild a Live Data scenario on every run. Do not
     * "simplify" this to the service method.
     *
     * No fallback to another scenario type: the report must say which budget it compared against, and
     * every in-scope department is expected to have a budget request.
     */
    private BudgetScenario selectScenario(Workgroup workgroup, long year) {
        return budgetScenarioRepository.findbyWorkgroupIdAndYear(workgroup.getId(), year).stream()
            .filter(BudgetScenario::getIsBudgetRequest)
            .max(Comparator.comparing(BudgetScenario::getCreationDate))
            // names the workgroup, because with a shared payroll code the one that fails is often not the
            // one that was requested
            .orElseThrow(() -> new IllegalStateException("No budget request for " + workgroup.getCode()
                + " (workgroup " + workgroup.getId() + ") year " + year));
    }

    /**
     * Every program sharing the requested workgroup's payroll department, or just the requested one
     * when its code is unshared.
     *
     * All-or-nothing, deliberately: if any member is missing, the report fails rather than comparing a
     * partial plan against the full department's payroll.
     */
    private List<Workgroup> payrollDepartmentGroup(Workgroup requested, FteDepartment fteDepartment) {
        List<Workgroup> group = new ArrayList<>();
        for (String code : fteDepartment.programs()) {
            Workgroup member = code.equals(requested.getCode())
                ? requested : workgroupService.findOneByCode(code);
            if (member == null) {
                throw new IllegalStateException("Payroll department " + fteDepartment.payrollCode()
                    + " lists program " + code + ", but no workgroup has that code");
            }
            group.add(member);
        }
        return group;
    }

    private BudgetReconciliationCategoryView buildCategory(String instructorType, BigDecimal plannedCost,
                                                           BigDecimal plannedCount, DopeTotals tally) {
        boolean includedInComparison = !CONTEXT_ONLY_TYPES.contains(instructorType);

        if (tally == null) {
            tally = new DopeTotals();
        }

        // Every figure covers the academic year. Headcount and FTE are narrowed by month (Oct-Jun), while
        // salary is narrowed by the summer rule. A 12-month appointment keeps its Jul-Sep salary because
        // that pay is the academic year spread across twelve months.
        int actualPeople = tally.getAcademicYearPeople();
        BigDecimal actualFte = tally.getAcademicYearFte();
        BigDecimal comparableSalary = tally.getAcademicYearSalary();
        BigDecimal difference = includedInComparison && plannedCost != null
            ? comparableSalary.subtract(plannedCost) : null;

        return new BudgetReconciliationCategoryView(instructorType, includedInComparison,
            plannedCost, plannedCount, actualPeople, actualFte, tally.getTotalCompensation(),
            tally.getSalary(), tally.getSummerSalary(), comparableSalary, difference);
    }

    /* the planned instructor type for an assignment, categorized the way calculateTermTotals does */
    private InstructorType instructorTypeFor(SectionGroupCostInstructor sgci, Workgroup workgroup) {
        long instructorTypeId = budgetCalculationService.calculateSectionGroupInstructorTypeId(sgci, workgroup);
        return Arrays.stream(InstructorType.values())
            .filter(type -> type.getId() == instructorTypeId).findFirst().orElse(null);
    }

    /**
     * The three fiscal months a term is taught and paid in; fiscal month 1 is July. An unrecognised
     * suffix yields an empty set, which leaves FTE blank rather than attributing the wrong months.
     */
    private static Set<Integer> fiscalMonthsFor(String termCode) {
        if (termCode == null || termCode.length() < 2) {
            return Set.of();
        }
        return switch (Term.getTwoDigitTermCode(termCode)) {
            case "10" -> Set.of(4, 5, 6);    // Oct, Nov, Dec
            case "01" -> Set.of(7, 8, 9);    // Jan, Feb, Mar
            case "03" -> Set.of(10, 11, 12); // Apr, May, Jun
            default -> Set.of();
        };
    }

    /**
     * The instructor types whose DOPE job codes count toward a Banner role's FTE. FA covers every
     * faculty and lecturer title because Banner does not distinguish them.
     */
    private static Set<String> instructorTypesFor(String bannerRole) {
        if (bannerRole == null) {
            return Set.of();
        }
        return switch (bannerRole.toUpperCase()) {
            case "TA" -> Set.of("TAs");
            case "AI" -> Set.of("Associate Instructor");
            case "FA" -> Set.of("Ladder Faculty", "Lecturer SOE", "Continuing Lecturer",
                "Unit 18 Pre-Six Lecturer", "Visiting Professor", "Emeriti - Recalled",
                "Instructor", "New Faculty Hire");
            default -> Set.of();
        };
    }

    /**
     * Where a person's pay is: their DOPE job code descriptions when they are paid in this department,
     * otherwise which departments pay them instead. No DOPE record and no empl id both read as blank
     * (costSummary counts them separately), as does a person with only Summer Session pay.
     */
    private static String payrollDetail(DopeCostService.PersonCostResult result,
                                       Set<Integer> termMonths, Set<String> roleTypes) {
        return switch (result.status()) {
            case "id" -> result.jobCodesFor(termMonths, roleTypes);
            case "elsewhere" -> result.label();
            default -> null;
        };
    }

    private static String personId(BannerAssignment assignment) {
        return assignment.getPidm() != null ? assignment.getPidm() : assignment.getFullName();
    }

    private static String courseKey(String termCode, String subjectCode, String courseNumber) {
        return termCode + "|" + subjectCode + "|" + courseNumber;
    }

    /* FA is the instructor of record; the "The Staff" placeholder goes last since it names nobody */
    private static int roleOrder(String role) {
        if (PLACEHOLDER_ROLE.equals(role)) {
            return 9;
        }
        if (role == null) {
            return 8;
        }
        return switch (role.toUpperCase()) {
            case "FA" -> 0;
            case "AI" -> 1;
            case "TA" -> 2;
            default -> 5;
        };
    }

    /* FCTG_CODE NF is the "The Staff" placeholder on 9-series (independent/group study) courses,
       not a real countable instructor */
    private static String actualRole(String functionalCategory) {
        return "NF".equalsIgnoreCase(functionalCategory) ? PLACEHOLDER_ROLE : functionalCategory;
    }

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
