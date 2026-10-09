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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile({"development", "production", "staging"})
@ConditionalOnProperty(name = "DATAMART_URL")
public class JpaBudgetReconciliationReportService implements BudgetReconciliationReportService {
    @Inject BudgetService budgetService;
    @Inject BudgetScenarioRepository budgetScenarioRepository;
    @Inject TermService termService;
    @Inject ExpenseItemService expenseItemService;
    @Inject BudgetCalculationService budgetCalculationService;
    /* resolves the sibling programs on a shared payroll code — see FteDepartment */
    @Inject WorkgroupService workgroupService;
    /* required: DOPE actuals are what the report reconciles against, so it is gated with them */
    @Inject DopeCostService dopeCostService;
    /* optional: Banner supplies the actual course assignments the By Course view compares against,
       and the TA counts on the By Category TAs row */
    @Autowired(required = false) BannerRepository bannerRepository;

    /* Ladder Faculty carries no SIB plan to compare against — LADDER_FACULTY_COST is $0 in all 49
       FY2026 scenarios, verified 2026-07-30 — so it is a context row rather than a difference. This is
       NOT a funding-source claim: an earlier comment here said "state-funded", which nothing verifies
       (the DOPE query selects FUND_CODE and no code reads it). Removed at the user's direction. */
    private static final Set<String> CONTEXT_ONLY_TYPES =
        Set.of("Ladder Faculty", DopeSummaryCalculator.UNMAPPED);

    private static final String PLACEHOLDER_ROLE = "Staff (placeholder)";

    /* TA and Reader cost is per-course counts x rate rather than a SectionGroupCostInstructor, so it
       sits outside REPLACEMENT_COST — see plannedTotalsFrom */
    private static final Set<String> COUNT_BASED_TYPES = Set.of("TAs", "Readers");

    /**
     * The plan's keys for each reported category, in display order, laid out as alternating
     * [cost, count] pairs. **More than one pair means the category absorbs another** — Unit 18 Pre-Six
     * Lecturer carries Continuing Lecturer - Augmentation too, because an augmentation is a lecturer
     * asked to teach an extra course: still a lecturer title, and temporary. Both sides are merged, so
     * no row is left holding a plan it can never match an actual against.
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
     * One report generation is one transaction. **Keep this even though only the HTTP path calls it now:**
     * the planned side walks lazy associations — `budget.getSchedule()`, the scenario's section-group costs
     * and their instructors — and the HTTP path gets away without a transaction only because
     * `spring.jpa.open-in-view=true` holds a session open for the request. A batch runner that called this
     * outside a request failed every workgroup with `LazyInitializationException` on `Schedule` until this
     * was added; that runner is gone, but the dependency on an app-wide setting is not something this
     * method should rest on, least of all through a Spring Boot upgrade.
     *
     * Not `readOnly = true`, deliberately: `findOrCreateByWorkgroupIdAndYear` below can still insert a
     * Schedule and Budget, and a read-only transaction sets FlushMode.MANUAL, which would drop that write
     * silently rather than failing.
     */
    @Override
    @Transactional
    public BudgetReconciliationReportView generate(long workgroupId, long year) {
        int fiscalYear = (int) year + 1;

        // ---- planned side, shared by both views ----
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
        // query below returns. Each program keeps its own scenario and its own workgroup — the latter
        // matters because calculateSectionGroupInstructorTypeId resolves a cost line's instructor type
        // against the workgroup that owns it, so the group's costs cannot share one.
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

        // per Banner person: empl id (for the DOPE match) and whether they are a real instructor
        // rather than a "The Staff" placeholder
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

        // actual TA counts from Banner, scoped to this budget's course subjects and academic-year
        // terms; null when Banner isn't configured or the query failed
        BannerRepository.TaCounts bannerTaCounts = bannerRepository != null
            ? bannerRepository.getTaCounts(subjectCodes, termCodes) : null;

        return new BudgetReconciliationReportView(workgroupId, workgroup.getCode(), fteDepartment.name(),
            year, fiscalYear,
            departmentCode, String.join("; ", scenarioNames),
            plannedTotalsFrom(planned),
            buildCategories(planned, dopeSummary),
            buildCourses(workgroupByCostId, sectionGroupCosts, assignments, costByPerson),
            // the cost bridge only means something with both sides: Banner people to match, DOPE to
            // match them against. Without Banner the counts would all read zero against a real
            // department total, which invites "nothing matched" when nothing was ever attempted.
            bannerRepository == null ? null : dopeCostService.summarize(dopeSummary, costByPerson),
            bannerTaCounts != null ? bannerTaCounts.assignments() : null,
            bannerTaCounts != null ? bannerTaCounts.individuals() : null);
    }

    /** the budget scenario's combined (whole-year) term totals */
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
     * REPLACEMENT_COST is misleadingly named upstream: calculateTermTotals accumulates EVERY
     * instructor assignment's cost into it, not just replacements. So it is the total of all
     * SectionGroupCostInstructor cost, and subtracting the ten categorized instructor types leaves
     * whatever landed in no category. TA and Reader cost is excluded from that subtraction because it
     * comes from per-course counts and never passes through an assignment.
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

    /* Sums a category's cost keys (offset 0) or count keys (offset 1) from the plan. The map's values
       are alternating [cost, count] pairs, so a merged category contributes more than one of each. */
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

    /**
     * By Category: the approved budget's per-instructor-type cost against the payroll department's
     * DOPE actuals.
     */
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

    /**
     * By Course: planned instructors from the budget scenario paired with Banner's actual
     * assignments, per course. Planned-only when Banner isn't configured; DOPE cost is attached to
     * the actual rows when costByPerson is present.
     */
    private List<CourseStaffingView> buildCourses(
            Map<Long, Workgroup> workgroupByCostId, List<SectionGroupCost> sectionGroupCosts,
            List<BannerAssignment> assignments,
            Map<String, DopeCostService.PersonCostResult> costByPerson) {
        // planned staffing per course, keyed term|subject|courseNumber (natural sort term>subj>course)
        Map<String, List<CourseStaffingPersonView>> plannedByCourse = new LinkedHashMap<>();
        // course titles come from the plan only — a Banner-only course has no IPA row to read one from
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
            String termCode = entry.getKey().split("\\|", -1)[0];
            // instructor of record first, then support: FA, AI, TA, placeholder. Banner returns rows in
            // no useful order, so without this a course's TA can precede the professor teaching it.
            List<PersonAssignments> ordered = new ArrayList<>(entry.getValue().values());
            ordered.sort(Comparator.comparingInt((PersonAssignments p) -> roleOrder(p.role))
                .thenComparing(p -> p.name == null ? "" : p.name));
            for (PersonAssignments person : ordered) {
                CourseStaffingPersonView view =
                    new CourseStaffingPersonView(person.name, person.role, person.sections);
                DopeCostService.PersonCostResult result =
                    costByPerson == null ? null : costByPerson.get(person.personId);
                if (result != null) {
                    // Both figures are scoped to this row: the term's own fiscal months, and only job
                    // codes matching the Banner role — so a TA row shows a TA title and a TA-only FTE,
                    // and the Reader appointment the same person may hold appears in neither
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

    /**
     * The payroll department scoping the actuals, looked up from the workgroup itself — so a caller
     * cannot pair one department's plan with another's payroll.
     */
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
     * Calls the **repository**, not `BudgetScenarioService`, and that distinction is the whole point.
     * `BudgetScenarioService.findbyWorkgroupIdAndYear` runs `createOrUpdateFromLiveData` before returning,
     * so every report generation created or rewrote a Live Data scenario — a write this report then
     * discarded, since it only ever wanted the budget request. Harmless once, but 49 needless scenario
     * rebuilds and their audit rows on a batch run, and unacceptable for a scheduled job. The repository
     * is a plain `@Query` with no such side effect. **Do not "simplify" this to the service method.**
     *
     * Reads every scenario and filters `getIsBudgetRequest` here rather than in SQL, matching what master
     * does in `UploadBudgetReportTask` — master removed the dedicated `findBudgetRequestsByWorkgroupIdAndYear`
     * query rather than maintain two that differ only by a predicate.
     *
     * **No fallback to the latest scenario of any kind** (removed 2026-07-29). That fallback could
     * silently reconcile against a Live Data scenario, which is exactly the ambiguity Dean's-office Q10
     * raises: a report unable to say which budget it compared against. Every in-scope department is
     * expected to have a budget request, so its absence is a real problem worth surfacing — better a 500
     * on one department than a workbook quietly built on the wrong plan.
     */
    private BudgetScenario selectScenario(Workgroup workgroup, long year) {
        return budgetScenarioRepository.findbyWorkgroupIdAndYear(workgroup.getId(), year).stream()
            .filter(BudgetScenario::getIsBudgetRequest)
            .max(Comparator.comparing(BudgetScenario::getCreationDate))
            // names the workgroup, because with a shared payroll code the one that fails is often not the
            // one that was requested — CHN's report fails when JPN has no budget request
            .orElseThrow(() -> new IllegalStateException("No budget request for " + workgroup.getCode()
                + " (workgroup " + workgroup.getId() + ") year " + year));
    }

    /**
     * The workgroups whose plans this report combines: every program sharing the requested workgroup's
     * payroll department, or just the requested one when its code is unshared.
     *
     * **All-or-nothing, deliberately.** If any member lacks a budget request `selectScenario` throws and
     * the whole request fails, rather than reporting a partial plan against the full department's payroll
     * — which is the very mismatch this rollup exists to remove, just smaller.
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

        // Every figure covers the academic year, on every category (2026-07-29). Headcount and FTE are
        // narrowed by month — a person paid only in Jul-Sep is out, and FTE averages over Oct-Jun —
        // while salary is narrowed by the summer rule, which tests job code as well as month. The two
        // mechanisms differ but now cover the same period, which is what removed the old asymmetry
        // between them: a 12-month appointment keeps its Jul-Sep salary because that pay is the
        // academic year spread across twelve months, and its FTE is unchanged because it is present in
        // every month either way.
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
     * The three fiscal months a term is taught and paid in. The fiscal year starts in July, so month 1
     * is July; a term code's last two characters are its `TermDescription` suffix — FALL "10", WINTER
     * "01", SPRING "03". An unrecognised suffix yields an empty set, which leaves FTE blank rather than
     * quietly attributing the wrong months.
     */
    private static Set<Integer> fiscalMonthsFor(String termCode) {
        if (termCode == null || termCode.length() < 2) {
            return Set.of();
        }
        return switch (termCode.substring(termCode.length() - 2)) {
            case "10" -> Set.of(4, 5, 6);    // Oct, Nov, Dec
            case "01" -> Set.of(7, 8, 9);    // Jan, Feb, Mar
            case "03" -> Set.of(10, 11, 12); // Apr, May, Jun
            default -> Set.of();
        };
    }

    /**
     * The instructor types whose DOPE job codes count toward a Banner role's FTE, so a row reads only
     * the appointment it is actually about: a TA row is not inflated by the same person's Reader or
     * faculty appointments. `FA` covers every faculty and lecturer title because Banner does not
     * distinguish them; the DOPE job code in column 8 says which it was.
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
     * The one column that says where a person's pay is: their DOPE job code description(s) when they are
     * paid in this department, otherwise which departments pay them instead. Null when neither is known.
     *
     * Replaced a separate Cost Match column (2026-07-29). The match is always by empl id, so naming the
     * method added nothing, and a blank already means no cost was attributed here. Two consequences:
     * `no DOPE record` and `unmatched (no empl id)` now read identically as blank — `costSummary` still
     * counts them separately — and so does a person matched with only Summer Session pay, whose job code
     * set is empty (see the matchedById issue in the dev notes).
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

    /**
     * Row order within a course's Banner block: `FA` (instructor of record), then `AI`, then `TA`, then
     * anything unrecognised, then the "The Staff" placeholder last since it names nobody. Any FCTG_CODE
     * we have not seen sorts before the placeholder rather than disappearing into it.
     */
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
