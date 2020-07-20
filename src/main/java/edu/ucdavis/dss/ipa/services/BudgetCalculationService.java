package edu.ucdavis.dss.ipa.services;

import static edu.ucdavis.dss.ipa.entities.enums.BudgetSummary.*;
import static edu.ucdavis.dss.ipa.entities.enums.InstructorDescription.*;

import edu.ucdavis.dss.ipa.entities.*;
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
     *     },
     *     termCode: {
     *         ...
     *     }
     *     "combined": {
     *         ...
     *     }
     * }
     */
    public Map<String, Map<BudgetSummary, BigDecimal>> calculateTermTotals(Budget budget, List<SectionGroupCost> sectionGroupCosts, List<String> termCodes, Workgroup workgroup, List<LineItem> lineItems) {
        Map<String, Map<BudgetSummary, BigDecimal>> termTotals = new HashMap<>();

        for (String termCode : termCodes) {
            Map<BudgetSummary, BigDecimal> budgetSummaryMap = generateBudgetSummaryMap();
            termTotals.put(termCode, budgetSummaryMap);
        }

        // add another map for yearly total
        Map<BudgetSummary, BigDecimal> budgetSummaryMap = generateBudgetSummaryMap();
        termTotals.put("combined", budgetSummaryMap);

        // new BigDecimal(String) preferred over BigDecimal.valueOf(double)?
        // new BigDecimal(String.valueOf()) = 7303.83
        // new BigDecimal(float) = 7303.830078125
        // BigDecimal.valueOf(float) = 7303.830078125
        BigDecimal baseTaCost = new BigDecimal(String.valueOf(budget.getTaCost()));
        BigDecimal baseReaderCost = new BigDecimal(String.valueOf(budget.getReaderCost()));

        Map<BudgetSummary, BigDecimal> combinedTermSummary = termTotals.get("combined");

        for (SectionGroupCost sectionGroupCost : sectionGroupCosts) {
            BigDecimal taCount = sectionGroupCost.getTaCount() == null ? BigDecimal.ZERO : new BigDecimal(String.valueOf(sectionGroupCost.getTaCount()));
            BigDecimal readerCount = sectionGroupCost.getReaderCount() == null ? BigDecimal.ZERO : new BigDecimal(String.valueOf(sectionGroupCost.getReaderCount()));

            Map<BudgetSummary, BigDecimal> currentTermSummary = termTotals.get(sectionGroupCost.getTermCode());

            currentTermSummary.put(TA_COUNT, currentTermSummary.get(TA_COUNT).add(taCount));
            currentTermSummary.put(TA_COST, currentTermSummary.get(TA_COST).add(baseTaCost.multiply(taCount)));
            currentTermSummary.put(READER_COUNT, currentTermSummary.get(READER_COUNT).add(readerCount));
            currentTermSummary.put(READER_COST, currentTermSummary.get(READER_COST).add(baseReaderCost.multiply(readerCount)));
            currentTermSummary.put(UNITS_OFFERED, currentTermSummary.get(UNITS_OFFERED).add(calculateUnits(sectionGroupCost)));

            if (isGrad(sectionGroupCost)) {
                currentTermSummary.put(SCH_GRAD, currentTermSummary.get(SCH_GRAD).add(calculateSCH(sectionGroupCost)));
                combinedTermSummary.put(SCH_GRAD, combinedTermSummary.get(SCH_GRAD).add(calculateSCH(sectionGroupCost)));
            } else {
                currentTermSummary.put(SCH_UNDERGRAD, currentTermSummary.get(SCH_UNDERGRAD).add(calculateSCH(sectionGroupCost)));
                combinedTermSummary.put(SCH_UNDERGRAD, combinedTermSummary.get(SCH_UNDERGRAD).add(calculateSCH(sectionGroupCost)));
            }

            combinedTermSummary.put(TA_COUNT, combinedTermSummary.get(TA_COUNT).add(taCount));
            combinedTermSummary.put(TA_COST, combinedTermSummary.get(TA_COST).add(baseTaCost.multiply(taCount)));
            combinedTermSummary.put(READER_COUNT, combinedTermSummary.get(READER_COUNT).add(readerCount));
            combinedTermSummary.put(READER_COST, combinedTermSummary.get(READER_COST).add(baseReaderCost.multiply(readerCount)));
            combinedTermSummary.put(UNITS_OFFERED, combinedTermSummary.get(UNITS_OFFERED).add(calculateUnits(sectionGroupCost)));

            long instructorTypeId = calculateInstructorTypeId(sectionGroupCost, workgroup);

            if(instructorTypeId == EMERITI.typeId()){
                currentTermSummary.put(EMERITI_COUNT, currentTermSummary.get(EMERITI_COUNT).add(BigDecimal.ONE));
                combinedTermSummary.put(EMERITI_COUNT, combinedTermSummary.get(EMERITI_COUNT).add(BigDecimal.ONE));

                currentTermSummary.put(EMERITI_COST, currentTermSummary.get(EMERITI_COST).add(calculateInstructorCost(budget, sectionGroupCost, workgroup)));
                combinedTermSummary.put(EMERITI_COST, combinedTermSummary.get(EMERITI_COST).add(calculateInstructorCost(budget, sectionGroupCost, workgroup)));
            } else if (instructorTypeId == VISITING_PROFESSOR.typeId()){
                currentTermSummary.put(VISITING_PROFESSOR_COUNT, currentTermSummary.get(VISITING_PROFESSOR_COUNT).add(BigDecimal.ONE));
                combinedTermSummary.put(VISITING_PROFESSOR_COUNT, combinedTermSummary.get(VISITING_PROFESSOR_COUNT).add(BigDecimal.ONE));

                currentTermSummary.put(VISITING_PROFESSOR_COST, currentTermSummary.get(VISITING_PROFESSOR_COST).add(calculateInstructorCost(budget, sectionGroupCost, workgroup)));
                combinedTermSummary.put(VISITING_PROFESSOR_COST, combinedTermSummary.get(VISITING_PROFESSOR_COST).add(calculateInstructorCost(budget, sectionGroupCost, workgroup)));
            } else if (instructorTypeId == ASSOCIATE_PROFESSOR.typeId()){
                currentTermSummary.put(ASSOCIATE_INSTRUCTOR_COUNT, currentTermSummary.get(ASSOCIATE_INSTRUCTOR_COUNT).add(BigDecimal.ONE));
                combinedTermSummary.put(ASSOCIATE_INSTRUCTOR_COUNT, combinedTermSummary.get(ASSOCIATE_INSTRUCTOR_COUNT).add(BigDecimal.ONE));

                currentTermSummary.put(ASSOCIATE_INSTRUCTOR_COST, currentTermSummary.get(ASSOCIATE_INSTRUCTOR_COST).add(calculateInstructorCost(budget, sectionGroupCost, workgroup)));
                combinedTermSummary.put(ASSOCIATE_INSTRUCTOR_COST, combinedTermSummary.get(ASSOCIATE_INSTRUCTOR_COST).add(calculateInstructorCost(budget, sectionGroupCost, workgroup)));
            } else if (instructorTypeId == UNIT18_LECTURER.typeId()){
                currentTermSummary.put(UNIT18_LECTURER_COUNT, currentTermSummary.get(UNIT18_LECTURER_COUNT).add(BigDecimal.ONE));
                combinedTermSummary.put(UNIT18_LECTURER_COUNT, combinedTermSummary.get(UNIT18_LECTURER_COUNT).add(BigDecimal.ONE));

                currentTermSummary.put(UNIT18_LECTURER_COST, currentTermSummary.get(UNIT18_LECTURER_COST).add(calculateInstructorCost(budget, sectionGroupCost, workgroup)));
                combinedTermSummary.put(UNIT18_LECTURER_COST, combinedTermSummary.get(UNIT18_LECTURER_COST).add(calculateInstructorCost(budget, sectionGroupCost, workgroup)));
            } else if (instructorTypeId == CONTINUING_LECTURER.typeId()){
                currentTermSummary.put(CONTINUING_LECTURER_COUNT, currentTermSummary.get(CONTINUING_LECTURER_COUNT).add(BigDecimal.ONE));
                combinedTermSummary.put(CONTINUING_LECTURER_COUNT, combinedTermSummary.get(CONTINUING_LECTURER_COUNT).add(BigDecimal.ONE));

                currentTermSummary.put(CONTINUING_LECTURER_COST, currentTermSummary.get(CONTINUING_LECTURER_COST).add(calculateInstructorCost(budget, sectionGroupCost, workgroup)));
                combinedTermSummary.put(CONTINUING_LECTURER_COST, combinedTermSummary.get(CONTINUING_LECTURER_COST).add(calculateInstructorCost(budget, sectionGroupCost, workgroup)));
            } else if (instructorTypeId == LADDER_FACULTY.typeId()){
                currentTermSummary.put(LADDER_FACULTY_COUNT, currentTermSummary.get(LADDER_FACULTY_COUNT).add(BigDecimal.ONE));
                combinedTermSummary.put(LADDER_FACULTY_COUNT, combinedTermSummary.get(LADDER_FACULTY_COUNT).add(BigDecimal.ONE));

                currentTermSummary.put(LADDER_FACULTY_COST, currentTermSummary.get(LADDER_FACULTY_COST).add(calculateInstructorCost(budget, sectionGroupCost, workgroup)));
                combinedTermSummary.put(LADDER_FACULTY_COST, combinedTermSummary.get(LADDER_FACULTY_COST).add(calculateInstructorCost(budget, sectionGroupCost, workgroup)));
            } else if (instructorTypeId == INSTRUCTOR.typeId()){
                currentTermSummary.put(INSTRUCTOR_COUNT, currentTermSummary.get(INSTRUCTOR_COUNT).add(BigDecimal.ONE));
                combinedTermSummary.put(INSTRUCTOR_COUNT, combinedTermSummary.get(INSTRUCTOR_COUNT).add(BigDecimal.ONE));

                currentTermSummary.put(INSTRUCTOR_COST, currentTermSummary.get(INSTRUCTOR_COST).add(calculateInstructorCost(budget, sectionGroupCost, workgroup)));
                combinedTermSummary.put(INSTRUCTOR_COST, combinedTermSummary.get(INSTRUCTOR_COST).add(calculateInstructorCost(budget, sectionGroupCost, workgroup)));
            } else if (instructorTypeId == LECTURER_SOE.typeId()){
                currentTermSummary.put(LECTURER_SOE_COUNT, currentTermSummary.get(LECTURER_SOE_COUNT).add(BigDecimal.ONE));
                combinedTermSummary.put(LECTURER_SOE_COUNT, combinedTermSummary.get(LECTURER_SOE_COUNT).add(BigDecimal.ONE));

                currentTermSummary.put(LECTURER_SOE_COST, currentTermSummary.get(LECTURER_SOE_COST).add(calculateInstructorCost(budget, sectionGroupCost, workgroup)));
                combinedTermSummary.put(LECTURER_SOE_COST, combinedTermSummary.get(LECTURER_SOE_COST).add(calculateInstructorCost(budget, sectionGroupCost, workgroup)));
            } else {
                currentTermSummary.put(UNASSIGNED_COUNT, currentTermSummary.get(UNASSIGNED_COUNT).add(BigDecimal.ONE));
                combinedTermSummary.put(UNASSIGNED_COUNT, combinedTermSummary.get(UNASSIGNED_COUNT).add(BigDecimal.ONE));

                currentTermSummary.put(UNASSIGNED_COST, currentTermSummary.get(UNASSIGNED_COST).add(calculateInstructorCost(budget, sectionGroupCost, workgroup)));
                combinedTermSummary.put(UNASSIGNED_COST, combinedTermSummary.get(UNASSIGNED_COST).add(calculateInstructorCost(budget, sectionGroupCost, workgroup)));
            }

            combinedTermSummary.put(COURSE_COUNT, combinedTermSummary.get(COURSE_COUNT).add(BigDecimal.ONE));

            currentTermSummary.put(REPLACEMENT_COST, currentTermSummary.get(REPLACEMENT_COST).add(calculateInstructorCost(budget, sectionGroupCost, workgroup)));
            combinedTermSummary.put(REPLACEMENT_COST, combinedTermSummary.get(REPLACEMENT_COST).add(calculateInstructorCost(budget, sectionGroupCost, workgroup)));
            currentTermSummary.put(TOTAL_TEACHING_COST, currentTermSummary.get(TOTAL_TEACHING_COST)
                    .add(calculateInstructorCost(budget, sectionGroupCost, workgroup))
                    .add(baseTaCost.multiply(taCount))
                    .add(baseReaderCost.multiply(readerCount)));
            combinedTermSummary.put(TOTAL_TEACHING_COST, combinedTermSummary.get(TOTAL_TEACHING_COST)
                    .add(calculateInstructorCost(budget, sectionGroupCost, workgroup))
                    .add(baseTaCost.multiply(taCount))
                    .add(baseReaderCost.multiply(readerCount)));

            if (Integer.parseInt(sectionGroupCost.getCourseNumber().replaceAll("[^\\d.]", "")) >= 200) {
                currentTermSummary.put(GRAD_OFFERINGS, currentTermSummary.get(GRAD_OFFERINGS).add(BigDecimal.ONE));
                currentTermSummary.put(GRAD_SEATS, currentTermSummary.get(GRAD_SEATS).add(BigDecimal.valueOf(sectionGroupCost.getEnrollment())));

                combinedTermSummary.put(GRAD_OFFERINGS, combinedTermSummary.get(GRAD_OFFERINGS).add(BigDecimal.ONE));
                combinedTermSummary.put(GRAD_SEATS, combinedTermSummary.get(GRAD_SEATS).add(BigDecimal.valueOf(sectionGroupCost.getEnrollment())));
            } else if (Integer.parseInt(sectionGroupCost.getCourseNumber().replaceAll("[^\\d.]", "")) > 99) {
                currentTermSummary.put(UPPER_DIV_OFFERINGS, currentTermSummary.get(UPPER_DIV_OFFERINGS).add(BigDecimal.ONE));
                currentTermSummary.put(UPPER_DIV_SEATS, currentTermSummary.get(UPPER_DIV_SEATS).add(BigDecimal.valueOf(sectionGroupCost.getEnrollment())));

                combinedTermSummary.put(UPPER_DIV_OFFERINGS, combinedTermSummary.get(UPPER_DIV_OFFERINGS).add(BigDecimal.ONE));
                combinedTermSummary.put(UPPER_DIV_SEATS, combinedTermSummary.get(UPPER_DIV_SEATS).add(BigDecimal.valueOf(sectionGroupCost.getEnrollment())));
            } else {
                currentTermSummary.put(LOWER_DIV_OFFERINGS, currentTermSummary.get(LOWER_DIV_OFFERINGS).add(BigDecimal.ONE));
                currentTermSummary.put(LOWER_DIV_SEATS, currentTermSummary.get(LOWER_DIV_SEATS).add(BigDecimal.valueOf(sectionGroupCost.getEnrollment())));

                combinedTermSummary.put(LOWER_DIV_OFFERINGS, combinedTermSummary.get(LOWER_DIV_OFFERINGS).add(BigDecimal.ONE));
                combinedTermSummary.put(LOWER_DIV_SEATS, combinedTermSummary.get(LOWER_DIV_SEATS).add(BigDecimal.valueOf(sectionGroupCost.getEnrollment())));
            }
            currentTermSummary.put(TOTAL_SEATS, currentTermSummary.get(TOTAL_SEATS).add(BigDecimal.valueOf(sectionGroupCost.getEnrollment())));
            combinedTermSummary.put(TOTAL_SEATS, combinedTermSummary.get(TOTAL_SEATS).add(BigDecimal.valueOf(sectionGroupCost.getEnrollment())));
        }

        BigDecimal funds = BigDecimal.ZERO;
        for(LineItem lineItem: lineItems){
            funds = funds.add(lineItem.getAmount());
        }
        combinedTermSummary.put(TOTAL_FUNDS, funds);
        combinedTermSummary.put(TOTAL_BALANCE, funds.subtract(combinedTermSummary.get(TOTAL_TEACHING_COST)));

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
                // named instructor assignment, check for instructor salary, else check for category cost for named instructor
                InstructorCost instructorCost = instructorCostService.findByInstructorIdAndBudgetId(sectionGroupCost.getInstructor().getId(), budget.getId());

                if (instructorCost != null && instructorCost.getCost() != null) {
                    return instructorCost.getCost() == null ? BigDecimal.ZERO : new BigDecimal(String.valueOf(instructorCost.getCost()));
                } else {
                    final long instructorTypeIdFinal = instructorTypeId;
                    InstructorTypeCost instructorTypeCost = instructorTypeCostService.findByBudgetId(budget.getId()).stream().filter(itc -> itc.getInstructorTypeIdIfExists() == instructorTypeIdFinal).findFirst().orElse(null);
                    if (instructorTypeCost != null && instructorTypeCost.getCost() != null) {
                        return new BigDecimal(String.valueOf(instructorTypeCost.getCost()));
                    }
                }
            } else if (instructorTypeId > 0) {
                // unnamed instructor type assignment
                final long instructorTypeIdFinal = instructorTypeId;
                InstructorTypeCost instructorTypeCost = instructorTypeCostService.findByBudgetId(budget.getId())
                        .stream().filter(itc -> itc.getInstructorTypeIdIfExists() == instructorTypeIdFinal).findFirst()
                        .orElse(null);
                if (instructorTypeCost != null) {
                    return new BigDecimal(String.valueOf(instructorTypeCost.getCost()));
                }
            }
        }

        // no cost found, return 0
        return BigDecimal.ZERO;
    }

    private static Map<BudgetSummary, BigDecimal> generateBudgetSummaryMap() {
        Map<BudgetSummary, BigDecimal> budgetSummaryMap = Stream.of(
            new SimpleEntry<>(TA_COUNT, BigDecimal.ZERO),
            new SimpleEntry<>(TA_COST, BigDecimal.ZERO),
            new SimpleEntry<>(READER_COUNT, BigDecimal.ZERO),
            new SimpleEntry<>(READER_COST, BigDecimal.ZERO),
            new SimpleEntry<>(LADDER_FACULTY_COUNT, BigDecimal.ZERO),
            new SimpleEntry<>(LADDER_FACULTY_COST, BigDecimal.ZERO),
            new SimpleEntry<>(EMERITI_COUNT, BigDecimal.ZERO),
            new SimpleEntry<>(EMERITI_COST, BigDecimal.ZERO),
            new SimpleEntry<>(ASSOCIATE_INSTRUCTOR_COUNT, BigDecimal.ZERO),
            new SimpleEntry<>(ASSOCIATE_INSTRUCTOR_COST, BigDecimal.ZERO),
            new SimpleEntry<>(UNIT18_LECTURER_COUNT, BigDecimal.ZERO),
            new SimpleEntry<>(UNIT18_LECTURER_COST, BigDecimal.ZERO),
            new SimpleEntry<>(CONTINUING_LECTURER_COUNT, BigDecimal.ZERO),
            new SimpleEntry<>(CONTINUING_LECTURER_COST, BigDecimal.ZERO),
            new SimpleEntry<>(VISITING_PROFESSOR_COUNT, BigDecimal.ZERO),
            new SimpleEntry<>(VISITING_PROFESSOR_COST, BigDecimal.ZERO),
            new SimpleEntry<>(INSTRUCTOR_COUNT, BigDecimal.ZERO),
            new SimpleEntry<>(INSTRUCTOR_COST, BigDecimal.ZERO),
            new SimpleEntry<>(LECTURER_SOE_COUNT, BigDecimal.ZERO),
            new SimpleEntry<>(LECTURER_SOE_COST, BigDecimal.ZERO),
            new SimpleEntry<>(UNASSIGNED_COUNT, BigDecimal.ZERO),
            new SimpleEntry<>(UNASSIGNED_COST, BigDecimal.ZERO),
            new SimpleEntry<>(COURSE_COUNT, BigDecimal.ZERO),
            new SimpleEntry<>(REPLACEMENT_COST, BigDecimal.ZERO),
            new SimpleEntry<>(TOTAL_TEACHING_COST, BigDecimal.ZERO),
            new SimpleEntry<>(TOTAL_BALANCE, BigDecimal.ZERO),
            new SimpleEntry<>(TOTAL_FUNDS, BigDecimal.ZERO),
            new SimpleEntry<>(LOWER_DIV_OFFERINGS, BigDecimal.ZERO),
            new SimpleEntry<>(UPPER_DIV_OFFERINGS, BigDecimal.ZERO),
            new SimpleEntry<>(GRAD_OFFERINGS, BigDecimal.ZERO),
            new SimpleEntry<>(LOWER_DIV_SEATS, BigDecimal.ZERO),
            new SimpleEntry<>(UPPER_DIV_SEATS, BigDecimal.ZERO),
            new SimpleEntry<>(GRAD_SEATS, BigDecimal.ZERO),
            new SimpleEntry<>(TOTAL_SEATS, BigDecimal.ZERO),
            new SimpleEntry<>(UNITS_OFFERED, BigDecimal.ZERO),
            new SimpleEntry<>(SCH_UNDERGRAD, BigDecimal.ZERO),
            new SimpleEntry<>(SCH_GRAD, BigDecimal.ZERO)
            )
            .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));

        return budgetSummaryMap;
    }
}
