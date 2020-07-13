package edu.ucdavis.dss.ipa.services;

import static edu.ucdavis.dss.ipa.entities.enums.BudgetSummary.*;
import static edu.ucdavis.dss.ipa.entities.enums.InstructorDescription.*;

import edu.ucdavis.dss.ipa.entities.Budget;
import edu.ucdavis.dss.ipa.entities.InstructorCost;
import edu.ucdavis.dss.ipa.entities.InstructorTypeCost;
import edu.ucdavis.dss.ipa.entities.SectionGroupCost;
import edu.ucdavis.dss.ipa.entities.UserRole;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.entities.enums.BudgetSummary;
import java.math.BigDecimal;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import org.springframework.stereotype.Service;

@Service
public class BudgetCalculationService {
    @Inject InstructorCostService instructorCostService;
    @Inject InstructorTypeCostService instructorTypeCostService;
    @Inject UserService userService;

    /**
     * @return {
     *     termCode: {
     *         TA_COUNT: BigDecimal,
     *         TA_COST: BigDecimal,
     *         ...
     *     }
     * }
     */
    public Map<String, Map<BudgetSummary, BigDecimal>> calculateTermTotals(Budget budget, List<SectionGroupCost> sectionGroupCosts, List<String> termCodes, Workgroup workgroup) {
        Map<String, Map<BudgetSummary, BigDecimal>> termTotals = new HashMap<>();

        for (String termCode : termCodes) {
            Map<BudgetSummary, BigDecimal> budgetCountMap = generateBudgetCountMap();
            termTotals.put(termCode, budgetCountMap);
        }

        // add another map for yearly total
        Map<BudgetSummary, BigDecimal> budgetCountMap = generateBudgetCountMap();
        termTotals.put("combined", budgetCountMap);

        // new BigDecimal(String) preferred over BigDecimal.valueOf(double)?
        // new BigDecimal(String.valueOf()) = 7303.83
        // new BigDecimal(float) = 7303.830078125
        // BigDecimal.valueOf(float) = 7303.830078125
        BigDecimal baseTaCost = new BigDecimal(String.valueOf((budget.getTaCost())));
        BigDecimal baseReaderCost = new BigDecimal(String.valueOf((budget.getReaderCost())));

        for (SectionGroupCost sectionGroupCost : sectionGroupCosts) {
            BigDecimal taCount = sectionGroupCost.getTaCount() == null ? BigDecimal.ZERO : new BigDecimal(String.valueOf(sectionGroupCost.getTaCount()));
            BigDecimal readerCount = sectionGroupCost.getReaderCount() == null ? BigDecimal.ZERO : new BigDecimal(String.valueOf(sectionGroupCost.getReaderCount()));

            termTotals.get(sectionGroupCost.getTermCode()).put(TA_COUNT, termTotals.get(sectionGroupCost.getTermCode()).get(TA_COUNT).add(taCount));
            termTotals.get(sectionGroupCost.getTermCode()).put(TA_COST, termTotals.get(sectionGroupCost.getTermCode()).get(TA_COST).add(baseTaCost.multiply(taCount)));
            termTotals.get(sectionGroupCost.getTermCode()).put(READER_COUNT, termTotals.get(sectionGroupCost.getTermCode()).get(READER_COUNT).add(readerCount));
            termTotals.get(sectionGroupCost.getTermCode()).put(READER_COST, termTotals.get(sectionGroupCost.getTermCode()).get(READER_COST).add(baseReaderCost.multiply(readerCount)));
            termTotals.get(sectionGroupCost.getTermCode()).put(UNITS_OFFERED, termTotals.get(sectionGroupCost.getTermCode()).get(UNITS_OFFERED).add(calculateUnits(sectionGroupCost)));

            if (isGrad(sectionGroupCost)) {
                termTotals.get(sectionGroupCost.getTermCode()).put(SCH_GRAD, termTotals.get(sectionGroupCost.getTermCode()).get(SCH_GRAD).add(calculateSCH(sectionGroupCost)));
                termTotals.get("combined").put(SCH_GRAD, termTotals.get("combined").get(SCH_GRAD).add(calculateSCH(sectionGroupCost)));
            } else {
                termTotals.get(sectionGroupCost.getTermCode()).put(SCH_UNDERGRAD, termTotals.get(sectionGroupCost.getTermCode()).get(SCH_UNDERGRAD).add(calculateSCH(sectionGroupCost)));
                termTotals.get("combined").put(SCH_UNDERGRAD, termTotals.get("combined").get(SCH_UNDERGRAD).add(calculateSCH(sectionGroupCost)));
            }

            termTotals.get("combined").put(TA_COUNT, termTotals.get("combined").get(TA_COUNT).add(taCount));
            termTotals.get("combined").put(TA_COST, termTotals.get("combined").get(TA_COST).add(baseTaCost.multiply(taCount)));
            termTotals.get("combined").put(READER_COUNT, termTotals.get("combined").get(READER_COUNT).add(readerCount));
            termTotals.get("combined").put(READER_COST, termTotals.get("combined").get(READER_COST).add(baseReaderCost.multiply(readerCount)));
            termTotals.get("combined").put(UNITS_OFFERED, termTotals.get("combined").get(UNITS_OFFERED).add(calculateUnits(sectionGroupCost)));

            long instructorTypeId = calculateInstructorTypeId(sectionGroupCost, workgroup);

            if(instructorTypeId == EMERITI.typeId()){
                termTotals.get(sectionGroupCost.getTermCode()).put(EMERITI_COST, termTotals.get(sectionGroupCost.getTermCode()).get(EMERITI_COST).add(calculateInstructorCost(budget, sectionGroupCost, workgroup)));
                termTotals.get("combined").put(EMERITI_COST, termTotals.get("combined").get(EMERITI_COST).add(calculateInstructorCost(budget, sectionGroupCost, workgroup)));
            } else if (instructorTypeId == VISITING_PROFESSOR.typeId()){
                termTotals.get(sectionGroupCost.getTermCode()).put(VISITING_PROFESSOR_COST, termTotals.get(sectionGroupCost.getTermCode()).get(VISITING_PROFESSOR_COST).add(calculateInstructorCost(budget, sectionGroupCost, workgroup)));
                termTotals.get("combined").put(VISITING_PROFESSOR_COST, termTotals.get("combined").get(VISITING_PROFESSOR_COST).add(calculateInstructorCost(budget, sectionGroupCost, workgroup)));
            } else if (instructorTypeId == ASSOCIATE_PROFESSOR.typeId()){
               termTotals.get(sectionGroupCost.getTermCode()).put(ASSOCIATE_INSTRUCTOR_COST, termTotals.get(sectionGroupCost.getTermCode()).get(ASSOCIATE_INSTRUCTOR_COST).add(calculateInstructorCost(budget, sectionGroupCost, workgroup)));
                termTotals.get("combined").put(ASSOCIATE_INSTRUCTOR_COST, termTotals.get("combined").get(ASSOCIATE_INSTRUCTOR_COST).add(calculateInstructorCost(budget, sectionGroupCost, workgroup)));
            } else if (instructorTypeId == UNIT18_LECTURER.typeId()){
                termTotals.get(sectionGroupCost.getTermCode()).put(UNIT18_LECTURER_COST, termTotals.get(sectionGroupCost.getTermCode()).get(UNIT18_LECTURER_COST).add(calculateInstructorCost(budget, sectionGroupCost, workgroup)));
                termTotals.get("combined").put(UNIT18_LECTURER_COST, termTotals.get("combined").get(UNIT18_LECTURER_COST).add(calculateInstructorCost(budget, sectionGroupCost, workgroup)));
            } else if (instructorTypeId == CONTINUING_LECTURER.typeId()){
                termTotals.get(sectionGroupCost.getTermCode()).put(CONTINUING_LECTURER_COST, termTotals.get(sectionGroupCost.getTermCode()).get(CONTINUING_LECTURER_COST).add(calculateInstructorCost(budget, sectionGroupCost, workgroup)));
                termTotals.get("combined").put(CONTINUING_LECTURER_COST, termTotals.get("combined").get(CONTINUING_LECTURER_COST).add(calculateInstructorCost(budget, sectionGroupCost, workgroup)));
            } else if (instructorTypeId == LADDER_FACULTY.typeId()){
                termTotals.get(sectionGroupCost.getTermCode()).put(LADDER_FACULTY_COST, termTotals.get(sectionGroupCost.getTermCode()).get(LADDER_FACULTY_COST).add(calculateInstructorCost(budget, sectionGroupCost, workgroup)));
                termTotals.get("combined").put(LADDER_FACULTY_COST, termTotals.get("combined").get(LADDER_FACULTY_COST).add(calculateInstructorCost(budget, sectionGroupCost, workgroup)));
            } else if (instructorTypeId == INSTRUCTOR.typeId()){
                termTotals.get(sectionGroupCost.getTermCode()).put(INSTRUCTOR_COST, termTotals.get(sectionGroupCost.getTermCode()).get(INSTRUCTOR_COST).add(calculateInstructorCost(budget, sectionGroupCost, workgroup)));
                termTotals.get("combined").put(INSTRUCTOR_COST, termTotals.get("combined").get(INSTRUCTOR_COST).add(calculateInstructorCost(budget, sectionGroupCost, workgroup)));
            } else if (instructorTypeId == LECTURER_SOE.typeId()){
                termTotals.get(sectionGroupCost.getTermCode()).put(LECTURER_SOE_COST, termTotals.get(sectionGroupCost.getTermCode()).get(LECTURER_SOE_COST).add(calculateInstructorCost(budget, sectionGroupCost, workgroup)));
                termTotals.get("combined").put(LECTURER_SOE_COST, termTotals.get("combined").get(LECTURER_SOE_COST).add(calculateInstructorCost(budget, sectionGroupCost, workgroup)));
            } else {
                termTotals.get(sectionGroupCost.getTermCode()).put(UNASSIGNED_COST, termTotals.get(sectionGroupCost.getTermCode()).get(UNASSIGNED_COST).add(calculateInstructorCost(budget, sectionGroupCost, workgroup)));
                termTotals.get("combined").put(UNASSIGNED_COST, termTotals.get("combined").get(UNASSIGNED_COST).add(calculateInstructorCost(budget, sectionGroupCost, workgroup)));
            }

            if (Integer.parseInt(sectionGroupCost.getCourseNumber().replaceAll("[^\\d.]", "")) >= 200) {
                termTotals.get(sectionGroupCost.getTermCode()).put(GRAD_OFFERINGS, termTotals.get(sectionGroupCost.getTermCode()).get(GRAD_OFFERINGS).add(BigDecimal.ONE));
                termTotals.get(sectionGroupCost.getTermCode()).put(GRAD_SEATS, termTotals.get(sectionGroupCost.getTermCode()).get(GRAD_SEATS).add(BigDecimal.valueOf(sectionGroupCost.getEnrollment())));

                termTotals.get("combined").put(GRAD_OFFERINGS, termTotals.get("combined").get(GRAD_OFFERINGS).add(BigDecimal.ONE));
                termTotals.get("combined").put(GRAD_SEATS, termTotals.get("combined").get(GRAD_SEATS).add(BigDecimal.valueOf(sectionGroupCost.getEnrollment())));
            } else if (Integer.parseInt(sectionGroupCost.getCourseNumber().replaceAll("[^\\d.]", "")) > 99) {
                termTotals.get(sectionGroupCost.getTermCode()).put(UPPER_DIV_OFFERINGS, termTotals.get(sectionGroupCost.getTermCode()).get(UPPER_DIV_OFFERINGS).add(BigDecimal.ONE));
                termTotals.get(sectionGroupCost.getTermCode()).put(UPPER_DIV_SEATS, termTotals.get(sectionGroupCost.getTermCode()).get(UPPER_DIV_SEATS).add(BigDecimal.valueOf(sectionGroupCost.getEnrollment())));

                termTotals.get("combined").put(UPPER_DIV_OFFERINGS, termTotals.get("combined").get(UPPER_DIV_OFFERINGS).add(BigDecimal.ONE));
                termTotals.get("combined").put(UPPER_DIV_SEATS, termTotals.get("combined").get(UPPER_DIV_SEATS).add(BigDecimal.valueOf(sectionGroupCost.getEnrollment())));
            } else {
                termTotals.get(sectionGroupCost.getTermCode()).put(LOWER_DIV_OFFERINGS, termTotals.get(sectionGroupCost.getTermCode()).get(LOWER_DIV_OFFERINGS).add(BigDecimal.ONE));
                termTotals.get(sectionGroupCost.getTermCode()).put(LOWER_DIV_SEATS, termTotals.get(sectionGroupCost.getTermCode()).get(LOWER_DIV_SEATS).add(BigDecimal.valueOf(sectionGroupCost.getEnrollment())));

                termTotals.get("combined").put(LOWER_DIV_OFFERINGS, termTotals.get("combined").get(LOWER_DIV_OFFERINGS).add(BigDecimal.ONE));
                termTotals.get("combined").put(LOWER_DIV_SEATS, termTotals.get("combined").get(LOWER_DIV_SEATS).add(BigDecimal.valueOf(sectionGroupCost.getEnrollment())));
            }
        }

        return termTotals;
    }

    private static boolean isGrad(SectionGroupCost sectionGroupCost) {
        return Integer.parseInt(sectionGroupCost.getCourseNumber().replaceAll("[^\\d.]", "")) >= 200;
    }

    private static BigDecimal calculateUnits(SectionGroupCost sectionGroupCost) {
        // if sectionGroupCost has unitsHigh, it is a variable unit course, use unitsVariable if exist, otherwise, use zero for the variable unit course
        if ((sectionGroupCost.getUnitsHigh() == null ? 0 : sectionGroupCost.getUnitsHigh()) > 0) {
            return sectionGroupCost.getUnitsVariable() == null ? BigDecimal.ZERO : new BigDecimal(String.valueOf(sectionGroupCost.getUnitsVariable()));
        } else {
            return sectionGroupCost.getUnitsLow() == null ? BigDecimal.ZERO : new BigDecimal(String.valueOf(sectionGroupCost.getUnitsLow()));
        }
    }

    private static BigDecimal calculateSCH(SectionGroupCost sectionGroupCost) {
        return new BigDecimal(String.valueOf(sectionGroupCost.getEnrollment())).multiply(calculateUnits(sectionGroupCost));
    }

    private long calculateInstructorTypeId(
        SectionGroupCost sectionGroupCost, Workgroup workgroup) {
        if (sectionGroupCost.getInstructor() != null) {
            if (sectionGroupCost.getInstructorTypeIdentification() != null) {
                return sectionGroupCost.getInstructorTypeIdentification();
            } else {
                UserRole instructorRole = userService.getOneByLoginId(sectionGroupCost.getInstructor().getLoginId()).getUserRoles().stream().filter(ur -> (ur.getRole().getId() == 15 && workgroup.getId() == ur.getWorkgroup().getId())).findFirst().orElse(null);
                if (instructorRole != null) {
                    return instructorRole.getInstructorType().getId();
                }
            }
        } else {
                if (sectionGroupCost.getInstructorTypeIdentification() != null) {
                    return sectionGroupCost.getInstructorTypeIdentification();
                }
        }

        return 0;
    };

    private BigDecimal calculateInstructorCost(Budget budget, SectionGroupCost sectionGroupCost, Workgroup workgroup) {
        // 1. use sectionGroupCost.getCost if explicitly set
        // 2. use instructorCost if available ("instructor salary")
        // 3. use instructorTypeCost ("category cost")
        long instructorTypeId = calculateInstructorTypeId(sectionGroupCost, workgroup);

        if (sectionGroupCost.getCost() != null) {
            return new BigDecimal(String.valueOf(sectionGroupCost.getCost()));
        } else {
            if (sectionGroupCost.getInstructor() != null) {
                InstructorCost instructorCost = instructorCostService.findByInstructorIdAndBudgetId(sectionGroupCost.getInstructor().getId(), budget.getId());

                if (instructorCost != null && instructorCost.getCost() != null) {
                    return instructorCost.getCost() == null ? BigDecimal.ZERO : new BigDecimal(String.valueOf(instructorCost.getCost()));
                } else {
                    final long instructorTypeIdFinal = instructorTypeId;
                    InstructorTypeCost instructorTypeCost = instructorTypeCostService.findByBudgetId(budget.getId()).stream().filter(itc -> itc.getInstructorTypeIdIfExists() == instructorTypeIdFinal).findFirst().orElse(null);
                    if (instructorTypeCost != null) {
                        return new BigDecimal(String.valueOf(instructorTypeCost.getCost()));
                    }
                }
            } else if (instructorTypeId > 0) {
                final long instructorTypeIdFinal = instructorTypeId;
                InstructorTypeCost instructorTypeCost = instructorTypeCostService.findByBudgetId(budget.getId()).stream().filter(itc -> itc.getInstructorTypeIdIfExists() == instructorTypeIdFinal).findFirst().orElse(null);
                if (instructorTypeCost != null) {
                    return new BigDecimal(String.valueOf(instructorTypeCost.getCost()));
                }
            }
        }

        return BigDecimal.ZERO;
    }

    private static Map<BudgetSummary, BigDecimal> generateBudgetCountMap() {
        Map<BudgetSummary, BigDecimal> budgetCountMap = Stream.of(
            new SimpleEntry<>(TA_COUNT, BigDecimal.ZERO),
            new SimpleEntry<>(TA_COST, BigDecimal.ZERO),
            new SimpleEntry<>(READER_COUNT, BigDecimal.ZERO),
            new SimpleEntry<>(READER_COST, BigDecimal.ZERO),
            new SimpleEntry<>(LADDER_FACULTY_COST, BigDecimal.ZERO),
            new SimpleEntry<>(EMERITI_COST, BigDecimal.ZERO),
            new SimpleEntry<>(ASSOCIATE_INSTRUCTOR_COST, BigDecimal.ZERO),
            new SimpleEntry<>(UNIT18_LECTURER_COST, BigDecimal.ZERO),
            new SimpleEntry<>(CONTINUING_LECTURER_COST, BigDecimal.ZERO),
            new SimpleEntry<>(VISITING_PROFESSOR_COST, BigDecimal.ZERO),
            new SimpleEntry<>(INSTRUCTOR_COST, BigDecimal.ZERO),
            new SimpleEntry<>(LECTURER_SOE_COST, BigDecimal.ZERO),
            new SimpleEntry<>(UNASSIGNED_COST, BigDecimal.ZERO),
            new SimpleEntry<>(LOWER_DIV_OFFERINGS, BigDecimal.ZERO),
            new SimpleEntry<>(UPPER_DIV_OFFERINGS, BigDecimal.ZERO),
            new SimpleEntry<>(GRAD_OFFERINGS, BigDecimal.ZERO),
            new SimpleEntry<>(LOWER_DIV_SEATS, BigDecimal.ZERO),
            new SimpleEntry<>(UPPER_DIV_SEATS, BigDecimal.ZERO),
            new SimpleEntry<>(GRAD_SEATS, BigDecimal.ZERO),
            new SimpleEntry<>(UNITS_OFFERED, BigDecimal.ZERO),
            new SimpleEntry<>(SCH_UNDERGRAD, BigDecimal.ZERO),
            new SimpleEntry<>(SCH_GRAD, BigDecimal.ZERO)
            )
            .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));

        return budgetCountMap;
    }
}
