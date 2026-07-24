package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.banner.dto.BannerAssignment;
import edu.ucdavis.dss.datamart.DopeSummary;
import edu.ucdavis.dss.datamart.DopeSummaryCalculator;
import edu.ucdavis.dss.datamart.DopeTotals;
import edu.ucdavis.dss.datamart.dto.DopeRecord;
import edu.ucdavis.dss.ipa.api.components.staffingByCourseReport.views.CourseStaffingPersonView;
import edu.ucdavis.dss.ipa.api.components.staffingByCourseReport.views.CourseStaffingView;
import edu.ucdavis.dss.ipa.api.components.staffingByCourseReport.views.StaffingByCourseReportView;
import edu.ucdavis.dss.ipa.api.components.staffingByCourseReport.views.StaffingCostSummaryView;
import edu.ucdavis.dss.ipa.entities.Budget;
import edu.ucdavis.dss.ipa.entities.BudgetScenario;
import edu.ucdavis.dss.ipa.entities.SectionGroupCost;
import edu.ucdavis.dss.ipa.entities.SectionGroupCostInstructor;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.entities.enums.InstructorType;
import edu.ucdavis.dss.ipa.entities.enums.TermDescription;
import edu.ucdavis.dss.ipa.repositories.BannerRepository;
import edu.ucdavis.dss.ipa.repositories.DatamartRepository;
import edu.ucdavis.dss.ipa.services.BudgetCalculationService;
import edu.ucdavis.dss.ipa.services.BudgetScenarioService;
import edu.ucdavis.dss.ipa.services.BudgetService;
import edu.ucdavis.dss.ipa.services.StaffingByCourseReportService;
import edu.ucdavis.dss.ipa.services.TermService;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
import org.springframework.stereotype.Service;

@Service
@Profile({"development"})
@ConditionalOnProperty(name = "BANNER_DATABASE_URL")
public class JpaStaffingByCourseReportService implements StaffingByCourseReportService {
    @Inject BudgetService budgetService;
    @Inject BudgetScenarioService budgetScenarioService;
    @Inject TermService termService;
    @Inject BudgetCalculationService budgetCalculationService;
    @Inject BannerRepository bannerRepository;
    /* optional: DOPE cost is attached only when the Datamart is configured */
    @Autowired(required = false) DatamartRepository datamartRepository;

    private static final String PLACEHOLDER_ROLE = "Staff (placeholder)";

    @Override
    public StaffingByCourseReportView generate(long workgroupId, long year, String departmentCode) {
        Budget budget = budgetService.findOrCreateByWorkgroupIdAndYear(workgroupId, year);
        BudgetScenario budgetScenario = selectScenario(workgroupId, year);
        Workgroup workgroup = budget.getSchedule().getWorkgroup();

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

        // planned staffing per course, keyed term|subject|courseNumber (natural sort term>subj>course)
        Map<String, List<CourseStaffingPersonView>> plannedByCourse = new LinkedHashMap<>();
        for (SectionGroupCost sectionGroupCost : sectionGroupCosts) {
            String key = courseKey(sectionGroupCost.getTermCode(), sectionGroupCost.getSubjectCode(),
                sectionGroupCost.getCourseNumber());
            List<CourseStaffingPersonView> planned =
                plannedByCourse.computeIfAbsent(key, k -> new ArrayList<>());
            for (SectionGroupCostInstructor sgci : sectionGroupCost.getSectionGroupCostInstructors()) {
                String name = sgci.getInstructor() != null ? sgci.getInstructor().getFullName() : null;
                planned.add(new CourseStaffingPersonView(name, plannedRole(sgci, workgroup), null));
            }
        }

        List<BannerAssignment> assignments = bannerRepository.getCourseAssignments(subjectCodes, termCodes);
        if (assignments == null) {
            throw new IllegalStateException("Banner query failed for workgroup " + workgroupId);
        }

        // per Banner person: distinct courses (even-split denominator) and empl id (for matching)
        Map<String, Set<String>> coursesByPerson = new LinkedHashMap<>();
        Map<String, String> personEmplId = new HashMap<>();
        for (BannerAssignment assignment : assignments) {
            String personId = personId(assignment);
            coursesByPerson.computeIfAbsent(personId, k -> new HashSet<>()).add(courseKey(
                assignment.getTermCode(), assignment.getSubjectCode(), assignment.getCourseNumber()));
            if (assignment.getEmplId() != null) {
                personEmplId.putIfAbsent(personId, normalizeEmplId(assignment.getEmplId()));
            }
        }

        // DOPE cost by empl id for the dept (null when the Datamart isn't available — cost stays off)
        List<DopeRecord> dopeRecords = loadDopeRecords(departmentCode, year);
        Map<String, PersonCost> costByEmplId = dopeRecords != null ? buildCostByEmplId(dopeRecords) : null;

        // dedupe actual staffing within a course: one entry per person+role, sections = CRN count.
        // real (non-placeholder) instructors are the ones we resolve cost for.
        Map<String, LinkedHashMap<String, PersonAssignments>> actualAgg = new LinkedHashMap<>();
        Set<String> instructorPersonIds = new HashSet<>();
        for (BannerAssignment assignment : assignments) {
            String courseKey = courseKey(assignment.getTermCode(), assignment.getSubjectCode(),
                assignment.getCourseNumber());
            String personId = personId(assignment);
            String role = actualRole(assignment.getFunctionalCategory());
            String personKey = personId + "|" + assignment.getFunctionalCategory();
            PersonAssignments person = actualAgg.computeIfAbsent(courseKey, k -> new LinkedHashMap<>())
                .computeIfAbsent(personKey, k -> new PersonAssignments(personId, assignment.getFullName(), role));
            person.sections++;
            if (!PLACEHOLDER_ROLE.equals(role)) {
                instructorPersonIds.add(personId);
            }
        }

        // resolve each instructor's cost by empl id, classifying non-matches (funded elsewhere vs no
        // DOPE record) — null when the Datamart isn't available
        Map<String, PersonCostResult> costByPerson = costByEmplId == null ? null
            : resolveCosts(instructorPersonIds, personEmplId, costByEmplId, coursesByPerson, (int) year + 1);

        Map<String, List<CourseStaffingPersonView>> actualByCourse = new LinkedHashMap<>();
        for (Map.Entry<String, LinkedHashMap<String, PersonAssignments>> entry : actualAgg.entrySet()) {
            List<CourseStaffingPersonView> people = new ArrayList<>();
            for (PersonAssignments person : entry.getValue().values()) {
                CourseStaffingPersonView view =
                    new CourseStaffingPersonView(person.name, person.role, person.sections);
                PersonCostResult result = costByPerson == null ? null : costByPerson.get(person.personId);
                if (result != null) {
                    view.setCost(result.label, result.jobCode, result.personSalary, result.personCost,
                        result.allocatedSalary, result.allocatedCost, result.summerSalary);
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

        StaffingCostSummaryView costSummary = dopeRecords == null ? null
            : buildSummary(dopeRecords, costByPerson);

        return new StaffingByCourseReportView(workgroupId, year, budgetScenario.getName(), courses,
            costSummary);
    }

    private StaffingCostSummaryView buildSummary(List<DopeRecord> dopeRecords,
                                                 Map<String, PersonCostResult> costByPerson) {
        // department totals via the person-first calculator (accurate FTE, not a raw row sum)
        DopeSummary dopeSummary = DopeSummaryCalculator.calculate(dopeRecords);
        BigDecimal salary = BigDecimal.ZERO;
        BigDecimal compensation = BigDecimal.ZERO;
        BigDecimal fte = BigDecimal.ZERO;
        for (DopeTotals totals : dopeSummary.getByInstructorType().values()) {
            salary = salary.add(totals.getSalary());
            compensation = compensation.add(totals.getTotalCompensation());
            fte = fte.add(totals.getFte());
        }

        BigDecimal attributedSalary = BigDecimal.ZERO;
        BigDecimal attributedCompensation = BigDecimal.ZERO;
        int matchedById = 0, fundedElsewhere = 0, noDopeRecord = 0, noEmplId = 0;
        for (PersonCostResult result : costByPerson.values()) {
            switch (result.status) {
                case "id":
                    matchedById++;
                    attributedSalary = attributedSalary.add(result.personSalary);
                    attributedCompensation = attributedCompensation.add(result.personCost);
                    break;
                case "elsewhere": fundedElsewhere++; break;
                case "no_record": noDopeRecord++; break;
                default: noEmplId++; break;
            }
        }

        return new StaffingCostSummaryView(attributedSalary, attributedCompensation,
            matchedById, fundedElsewhere, noDopeRecord, noEmplId, salary, compensation.subtract(salary),
            compensation, fte, dopeSummary.getDistinctEmployees());
    }

    /**
     * Resolve each instructor's cost by exact empl-id match to the department's DOPE. Instructors
     * paid outside the department are classified with a dept-agnostic DOPE lookup (funded elsewhere
     * vs no DOPE record at all) but carry NO cost — their pay belongs to another department.
     */
    private Map<String, PersonCostResult> resolveCosts(Set<String> instructorPersonIds,
                                                       Map<String, String> personEmplId,
                                                       Map<String, PersonCost> costByEmplId,
                                                       Map<String, Set<String>> coursesByPerson,
                                                       int fiscalYear) {
        Map<String, PersonCostResult> resultByPerson = new HashMap<>();
        Set<String> unresolvedEmplIds = new HashSet<>();

        for (String personId : instructorPersonIds) {
            String emplId = personEmplId.get(personId);
            if (emplId == null || emplId.isBlank()) {
                resultByPerson.put(personId, PersonCostResult.status("no_id", "unmatched (no empl id)"));
                continue;
            }
            PersonCost cost = costByEmplId.get(emplId);
            if (cost != null) {
                int courseCount = Math.max(1, coursesByPerson.getOrDefault(personId, Set.of()).size());
                BigDecimal divisor = new BigDecimal(courseCount);
                resultByPerson.put(personId, PersonCostResult.matched(
                    String.join(", ", cost.jobCodeDescriptions), cost.salary, cost.compensation,
                    cost.salary.divide(divisor, 2, RoundingMode.HALF_UP),
                    cost.compensation.divide(divisor, 2, RoundingMode.HALF_UP),
                    cost.summerSalary));
            } else {
                unresolvedEmplIds.add(emplId);
            }
        }

        // one dept-agnostic lookup to explain the not-in-department instructors
        Map<String, Set<String>> departmentsByEmployee =
            datamartRepository.getDepartmentsByEmployee(unresolvedEmplIds, fiscalYear);

        for (String personId : instructorPersonIds) {
            if (resultByPerson.containsKey(personId)) {
                continue;
            }
            String emplId = personEmplId.get(personId);
            Set<String> departments = departmentsByEmployee == null ? null : departmentsByEmployee.get(emplId);
            if (departments != null && !departments.isEmpty()) {
                resultByPerson.put(personId, PersonCostResult.status("elsewhere",
                    "funded elsewhere (" + String.join(", ", new TreeSet<>(departments)) + ")"));
            } else {
                resultByPerson.put(personId, PersonCostResult.status("no_record", "no DOPE record"));
            }
        }

        return resultByPerson;
    }

    /* UCPath emplid and Banner WOBEUCD_EMP_ID compared trimmed (Oracle CHAR columns pad with spaces) */
    private static String normalizeEmplId(String emplId) {
        return emplId == null ? "" : emplId.trim();
    }

    /* DOPE records for the dept-year; null when the Datamart isn't available or no departmentCode */
    private List<DopeRecord> loadDopeRecords(String departmentCode, long year) {
        if (datamartRepository == null || departmentCode == null || departmentCode.isBlank()) {
            return null;
        }
        return datamartRepository.getDopeRecords(departmentCode, (int) year + 1);
    }

    /* per-person DOPE totals keyed by trimmed empl id (money is an exact sum; FTE is computed
       separately by the calculator for the summary). Summer Session pay is kept out of the
       academic-year salary/compensation (to match the Fall/Winter/Spring courses shown and the
       reconciliation report's summer treatment) but tallied into summerSalary as a context figure. */
    private Map<String, PersonCost> buildCostByEmplId(List<DopeRecord> records) {
        Map<String, PersonCost> byEmplId = new HashMap<>();
        for (DopeRecord record : records) {
            PersonCost cost = byEmplId.computeIfAbsent(normalizeEmplId(record.getEmployeeId()),
                k -> new PersonCost());
            boolean isSummer = isSummerPay(record);
            if (record.getMonetaryAmount() != null) {
                boolean isSalary = "SALARY".equals(record.getExpenseType());
                if (isSummer) {
                    if (isSalary) {
                        cost.summerSalary = cost.summerSalary.add(record.getMonetaryAmount());
                    }
                } else {
                    cost.compensation = cost.compensation.add(record.getMonetaryAmount());
                    if (isSalary) {
                        cost.salary = cost.salary.add(record.getMonetaryAmount());
                    }
                }
            }
            // summer job codes stay out of the DOPE Job Code column — only AY codes describe the match
            if (!isSummer && record.getJobCodeDescription() != null) {
                cost.jobCodeDescriptions.add(record.getJobCodeDescription());
            }
        }
        return byEmplId;
    }

    /* Summer Session pay, excluded from the course-attributed cost (the view shows only
       Fall/Winter/Spring courses). Mirrors the Budget Reconciliation report's summer exclusion:
       (a) explicit Summer Session job codes (e.g. LECT IN SUMMER SESSION) are dropped outright, and
       (b) for the summer-bearing student categories (TA/AI/Reader) Jul-Sep pay is Summer Session.
       12-month faculty/lecturer pay is kept whole (their salary is spread across summer months). */
    private static boolean isSummerPay(DopeRecord record) {
        String jobCode = record.getJobCodeDescription();
        if (jobCode != null && jobCode.toUpperCase().contains("SUMMER")) {
            return true;
        }
        return record.getFiscalMonth() <= 3 && DopeSummaryCalculator.isSummerSessionBearing(jobCode);
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

    private String plannedRole(SectionGroupCostInstructor sgci, Workgroup workgroup) {
        long instructorTypeId = budgetCalculationService.calculateSectionGroupInstructorTypeId(sgci, workgroup);
        return Arrays.stream(InstructorType.values())
            .filter(type -> type.getId() == instructorTypeId).findFirst()
            .map(InstructorType::getDescription).orElse(null);
    }

    private BudgetScenario selectScenario(long workgroupId, long year) {
        List<BudgetScenario> budgetScenarios = budgetScenarioService.findbyWorkgroupIdAndYear(workgroupId, year);

        return budgetScenarios.stream().filter(BudgetScenario::getIsBudgetRequest)
            .max(Comparator.comparing(BudgetScenario::getCreationDate))
            .orElseGet(() -> budgetScenarios.stream()
                .max(Comparator.comparing(BudgetScenario::getCreationDate))
                .orElseThrow(() -> new IllegalStateException(
                    "No budget scenario found for workgroup " + workgroupId + " year " + year)));
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

    /* per-person DOPE totals; salary/compensation are academic-year (summer excluded), summerSalary
       is the excluded Jul-Sep / Summer Session salary kept for context */
    private static class PersonCost {
        BigDecimal salary = BigDecimal.ZERO;
        BigDecimal compensation = BigDecimal.ZERO;
        BigDecimal summerSalary = BigDecimal.ZERO;
        final Set<String> jobCodeDescriptions = new TreeSet<>();
    }

    /* per-instructor cost resolution: status code ("id"/"elsewhere"/"no_record"/"no_id"), the
       human-readable costMatch label, and (only when matched in-dept) the pay figures */
    private static class PersonCostResult {
        final String status;
        final String label;
        final String jobCode;
        final BigDecimal personSalary;
        final BigDecimal personCost;
        final BigDecimal allocatedSalary;
        final BigDecimal allocatedCost;
        final BigDecimal summerSalary;

        PersonCostResult(String status, String label, String jobCode, BigDecimal personSalary,
                         BigDecimal personCost, BigDecimal allocatedSalary, BigDecimal allocatedCost,
                         BigDecimal summerSalary) {
            this.status = status;
            this.label = label;
            this.jobCode = jobCode;
            this.personSalary = personSalary;
            this.personCost = personCost;
            this.allocatedSalary = allocatedSalary;
            this.allocatedCost = allocatedCost;
            this.summerSalary = summerSalary;
        }

        static PersonCostResult matched(String jobCode, BigDecimal personSalary, BigDecimal personCost,
                                        BigDecimal allocatedSalary, BigDecimal allocatedCost,
                                        BigDecimal summerSalary) {
            return new PersonCostResult("id", "matched (empl id)", jobCode, personSalary, personCost,
                allocatedSalary, allocatedCost, summerSalary);
        }

        static PersonCostResult status(String status, String label) {
            return new PersonCostResult(status, label, null, null, null, null, null, null);
        }
    }
}
