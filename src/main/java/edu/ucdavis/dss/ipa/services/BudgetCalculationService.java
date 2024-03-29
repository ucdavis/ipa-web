package edu.ucdavis.dss.ipa.services;

import static edu.ucdavis.dss.ipa.entities.enums.BudgetSummary.*;
import static edu.ucdavis.dss.ipa.entities.enums.InstructorType.*;

import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.entities.enums.BudgetSummary;
import java.math.BigDecimal;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import jakarta.inject.Inject;
import org.springframework.stereotype.Service;

@Service
public class BudgetCalculationService {
    @Inject InstructorCostService instructorCostService;
    @Inject InstructorTypeCostService instructorTypeCostService;
    @Inject UserService userService;
    @Inject TeachingAssignmentService teachingAssignmentService;

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
    public Map<String, Map<BudgetSummary, BigDecimal>> calculateTermTotals(Budget budget,
                                                                           BudgetScenario budgetScenario,
                                                                           List<SectionGroupCost> sectionGroupCosts,
                                                                           List<String> termCodes,
                                                                           Workgroup workgroup,
                                                                           List<LineItem> lineItems,
                                                                           List<ExpenseItem> expenseItems) {
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
        BigDecimal baseTaCost = budgetScenario.getIsBudgetRequest() ? new BigDecimal(String.valueOf(budgetScenario.getTaCost())) : new BigDecimal(String.valueOf(budget.getTaCost()));
        BigDecimal baseReaderCost = budgetScenario.getIsBudgetRequest() ? new BigDecimal(String.valueOf(budgetScenario.getReaderCost())) : new BigDecimal(String.valueOf(budget.getReaderCost()));

        Map<BudgetSummary, BigDecimal> combinedTermSummary = termTotals.get("combined");
        List<Long> teachingAssignmentIds = new ArrayList<>();
        for (SectionGroupCost sectionGroupCost : sectionGroupCosts) {
            List<SectionGroupCostInstructor> sectionGroupCostInstructors = sectionGroupCost.getSectionGroupCostInstructors();

            BigDecimal taCount = sectionGroupCost.getTaCount() == null ? BigDecimal.ZERO : new BigDecimal(String.valueOf(sectionGroupCost.getTaCount()));
            BigDecimal readerCount = sectionGroupCost.getReaderCount() == null ? BigDecimal.ZERO : new BigDecimal(String.valueOf(sectionGroupCost.getReaderCount()));

            Map<BudgetSummary, BigDecimal> currentTermSummary = termTotals.get(sectionGroupCost.getTermCode());

            if (budgetScenario.getFromLiveData() == false && sectionGroupCostInstructors.size() < 1) {
                currentTermSummary.put(UNASSIGNED_COUNT, currentTermSummary.get(UNASSIGNED_COUNT).add(BigDecimal.ONE));
                combinedTermSummary.put(UNASSIGNED_COUNT, combinedTermSummary.get(UNASSIGNED_COUNT).add(BigDecimal.ONE));

                currentTermSummary.put(COURSE_COUNT, currentTermSummary.get(COURSE_COUNT).add(BigDecimal.ONE));
                combinedTermSummary.put(COURSE_COUNT, combinedTermSummary.get(COURSE_COUNT).add(BigDecimal.ONE));
            }

            if (budgetScenario.getFromLiveData() == true && sectionGroupCost.getInstructor() == null && sectionGroupCost.getInstructorType() == null) {
                currentTermSummary.put(UNASSIGNED_COUNT, currentTermSummary.get(UNASSIGNED_COUNT).add(BigDecimal.ONE));
                combinedTermSummary.put(UNASSIGNED_COUNT, combinedTermSummary.get(UNASSIGNED_COUNT).add(BigDecimal.ONE));

                currentTermSummary.put(COURSE_COUNT, currentTermSummary.get(COURSE_COUNT).add(BigDecimal.ONE));
                combinedTermSummary.put(COURSE_COUNT, combinedTermSummary.get(COURSE_COUNT).add(BigDecimal.ONE));
            }

            for(SectionGroupCostInstructor sectionGroupCostInstructor : sectionGroupCostInstructors){
                BigDecimal instructorCost = calculateSectionGroupInstructorCost(workgroup, budget, budgetScenario, sectionGroupCostInstructor);
                long instructorTypeId = calculateSectionGroupInstructorTypeId(sectionGroupCostInstructor, workgroup);
                if(sectionGroupCostInstructor.getTeachingAssignment() != null){
                    teachingAssignmentIds.add(sectionGroupCostInstructor.getTeachingAssignment().getId());
                }
                if(instructorTypeId == EMERITI.getId()){
                    currentTermSummary.put(EMERITI_COUNT, currentTermSummary.get(EMERITI_COUNT).add(BigDecimal.ONE));
                    combinedTermSummary.put(EMERITI_COUNT, combinedTermSummary.get(EMERITI_COUNT).add(BigDecimal.ONE));

                    currentTermSummary.put(EMERITI_COST, currentTermSummary.get(EMERITI_COST).add(instructorCost));
                    combinedTermSummary.put(EMERITI_COST, combinedTermSummary.get(EMERITI_COST).add(instructorCost));
                } else if (instructorTypeId == VISITING_PROFESSOR.getId()){
                    currentTermSummary.put(VISITING_PROFESSOR_COUNT, currentTermSummary.get(VISITING_PROFESSOR_COUNT).add(BigDecimal.ONE));
                    combinedTermSummary.put(VISITING_PROFESSOR_COUNT, combinedTermSummary.get(VISITING_PROFESSOR_COUNT).add(BigDecimal.ONE));

                    currentTermSummary.put(VISITING_PROFESSOR_COST, currentTermSummary.get(VISITING_PROFESSOR_COST).add(instructorCost));
                    combinedTermSummary.put(VISITING_PROFESSOR_COST, combinedTermSummary.get(VISITING_PROFESSOR_COST).add(instructorCost));
                } else if (instructorTypeId == ASSOCIATE_INSTRUCTOR.getId()){
                    currentTermSummary.put(ASSOCIATE_INSTRUCTOR_COUNT, currentTermSummary.get(ASSOCIATE_INSTRUCTOR_COUNT).add(BigDecimal.ONE));
                    combinedTermSummary.put(ASSOCIATE_INSTRUCTOR_COUNT, combinedTermSummary.get(ASSOCIATE_INSTRUCTOR_COUNT).add(BigDecimal.ONE));

                    currentTermSummary.put(ASSOCIATE_INSTRUCTOR_COST, currentTermSummary.get(ASSOCIATE_INSTRUCTOR_COST).add(instructorCost));
                    combinedTermSummary.put(ASSOCIATE_INSTRUCTOR_COST, combinedTermSummary.get(ASSOCIATE_INSTRUCTOR_COST).add(instructorCost));
                } else if (instructorTypeId == UNIT18_LECTURER.getId()){
                    currentTermSummary.put(UNIT18_LECTURER_COUNT, currentTermSummary.get(UNIT18_LECTURER_COUNT).add(BigDecimal.ONE));
                    combinedTermSummary.put(UNIT18_LECTURER_COUNT, combinedTermSummary.get(UNIT18_LECTURER_COUNT).add(BigDecimal.ONE));

                    currentTermSummary.put(UNIT18_LECTURER_COST, currentTermSummary.get(UNIT18_LECTURER_COST).add(instructorCost));
                    combinedTermSummary.put(UNIT18_LECTURER_COST, combinedTermSummary.get(UNIT18_LECTURER_COST).add(instructorCost));
                } else if (instructorTypeId == CONTINUING_LECTURER.getId()){
                    currentTermSummary.put(CONTINUING_LECTURER_COUNT, currentTermSummary.get(CONTINUING_LECTURER_COUNT).add(BigDecimal.ONE));
                    combinedTermSummary.put(CONTINUING_LECTURER_COUNT, combinedTermSummary.get(CONTINUING_LECTURER_COUNT).add(BigDecimal.ONE));

                    currentTermSummary.put(CONTINUING_LECTURER_COST, currentTermSummary.get(CONTINUING_LECTURER_COST).add(instructorCost));
                    combinedTermSummary.put(CONTINUING_LECTURER_COST, combinedTermSummary.get(CONTINUING_LECTURER_COST).add(instructorCost));
                } else if (instructorTypeId == CONTINUING_LECTURER_AUGMENTATION.getId()){
                    currentTermSummary.put(CONTINUING_LECTURER_AUGMENTATION_COUNT, currentTermSummary.get(CONTINUING_LECTURER_AUGMENTATION_COUNT).add(BigDecimal.ONE));
                    combinedTermSummary.put(CONTINUING_LECTURER_AUGMENTATION_COUNT, combinedTermSummary.get(CONTINUING_LECTURER_AUGMENTATION_COUNT).add(BigDecimal.ONE));

                    currentTermSummary.put(CONTINUING_LECTURER_AUGMENTATION_COST, currentTermSummary.get(CONTINUING_LECTURER_AUGMENTATION_COST).add(instructorCost));
                    combinedTermSummary.put(CONTINUING_LECTURER_AUGMENTATION_COST, combinedTermSummary.get(CONTINUING_LECTURER_AUGMENTATION_COST).add(instructorCost));
                } else if (instructorTypeId == LADDER_FACULTY.getId()){
                    currentTermSummary.put(LADDER_FACULTY_COUNT, currentTermSummary.get(LADDER_FACULTY_COUNT).add(BigDecimal.ONE));
                    combinedTermSummary.put(LADDER_FACULTY_COUNT, combinedTermSummary.get(LADDER_FACULTY_COUNT).add(BigDecimal.ONE));

                    currentTermSummary.put(LADDER_FACULTY_COST, currentTermSummary.get(LADDER_FACULTY_COST).add(instructorCost));
                    combinedTermSummary.put(LADDER_FACULTY_COST, combinedTermSummary.get(LADDER_FACULTY_COST).add(instructorCost));
                } else if (instructorTypeId == INSTRUCTOR.getId()){
                    currentTermSummary.put(INSTRUCTOR_COUNT, currentTermSummary.get(INSTRUCTOR_COUNT).add(BigDecimal.ONE));
                    combinedTermSummary.put(INSTRUCTOR_COUNT, combinedTermSummary.get(INSTRUCTOR_COUNT).add(BigDecimal.ONE));

                    currentTermSummary.put(INSTRUCTOR_COST, currentTermSummary.get(INSTRUCTOR_COST).add(instructorCost));
                    combinedTermSummary.put(INSTRUCTOR_COST, combinedTermSummary.get(INSTRUCTOR_COST).add(instructorCost));
                } else if (instructorTypeId == LECTURER_SOE.getId()){
                    currentTermSummary.put(LECTURER_SOE_COUNT, currentTermSummary.get(LECTURER_SOE_COUNT).add(BigDecimal.ONE));
                    combinedTermSummary.put(LECTURER_SOE_COUNT, combinedTermSummary.get(LECTURER_SOE_COUNT).add(BigDecimal.ONE));

                    currentTermSummary.put(LECTURER_SOE_COST, currentTermSummary.get(LECTURER_SOE_COST).add(instructorCost));
                    combinedTermSummary.put(LECTURER_SOE_COST, combinedTermSummary.get(LECTURER_SOE_COST).add(instructorCost));
                } else if (instructorTypeId == NEW_FACULTY_HIRE.getId()){
                    currentTermSummary.put(NEW_FACULTY_HIRE_COUNT, currentTermSummary.get(NEW_FACULTY_HIRE_COUNT).add(BigDecimal.ONE));
                    combinedTermSummary.put(NEW_FACULTY_HIRE_COUNT, combinedTermSummary.get(NEW_FACULTY_HIRE_COUNT).add(BigDecimal.ONE));

                    currentTermSummary.put(NEW_FACULTY_HIRE_COST, currentTermSummary.get(NEW_FACULTY_HIRE_COST).add(instructorCost));
                    combinedTermSummary.put(NEW_FACULTY_HIRE_COST, combinedTermSummary.get(NEW_FACULTY_HIRE_COST).add(instructorCost));
                }

                if (isGrad(sectionGroupCost)) {
                    combinedTermSummary.put(GRAD_COST, combinedTermSummary.get(GRAD_COST).add(instructorCost));
                } else if (isUpper(sectionGroupCost)) {
                    combinedTermSummary.put(UPPER_DIV_COST, combinedTermSummary.get(UPPER_DIV_COST).add(instructorCost));
                } else {
                    combinedTermSummary.put(LOWER_DIV_COST, combinedTermSummary.get(LOWER_DIV_COST).add(instructorCost));
                }

                currentTermSummary.put(REPLACEMENT_COST, currentTermSummary.get(REPLACEMENT_COST).add(instructorCost));
                combinedTermSummary.put(REPLACEMENT_COST, combinedTermSummary.get(REPLACEMENT_COST).add(instructorCost));
                currentTermSummary.put(TOTAL_TEACHING_COST, currentTermSummary.get(TOTAL_TEACHING_COST).add(instructorCost));
                combinedTermSummary.put(TOTAL_TEACHING_COST, combinedTermSummary.get(TOTAL_TEACHING_COST).add(instructorCost));
                currentTermSummary.put(COURSE_COUNT, currentTermSummary.get(COURSE_COUNT).add(BigDecimal.ONE));
                combinedTermSummary.put(COURSE_COUNT, combinedTermSummary.get(COURSE_COUNT).add(BigDecimal.ONE));
            }

            currentTermSummary.put(TA_COUNT, currentTermSummary.get(TA_COUNT).add(taCount));
            currentTermSummary.put(TA_COST, currentTermSummary.get(TA_COST).add(baseTaCost.multiply(taCount)));
            currentTermSummary.put(READER_COUNT, currentTermSummary.get(READER_COUNT).add(readerCount));
            currentTermSummary.put(READER_COST, currentTermSummary.get(READER_COST).add(baseReaderCost.multiply(readerCount)));
            currentTermSummary.put(UNITS_OFFERED, currentTermSummary.get(UNITS_OFFERED).add(calculateUnits(sectionGroupCost)));

            if (isGrad(sectionGroupCost)) {
                currentTermSummary.put(SCH_GRAD, currentTermSummary.get(SCH_GRAD).add(calculateSCH(sectionGroupCost)));
                combinedTermSummary.put(SCH_GRAD, combinedTermSummary.get(SCH_GRAD).add(calculateSCH(sectionGroupCost)));

                combinedTermSummary.put(GRAD_COST, combinedTermSummary.get(GRAD_COST).add(baseTaCost.multiply(taCount)));
                combinedTermSummary.put(GRAD_COST, combinedTermSummary.get(GRAD_COST).add(baseReaderCost.multiply(readerCount)));
            } else {
                currentTermSummary.put(SCH_UNDERGRAD, currentTermSummary.get(SCH_UNDERGRAD).add(calculateSCH(sectionGroupCost)));
                combinedTermSummary.put(SCH_UNDERGRAD, combinedTermSummary.get(SCH_UNDERGRAD).add(calculateSCH(sectionGroupCost)));

                if (isUpper(sectionGroupCost)) {
                    combinedTermSummary.put(UPPER_DIV_COST, combinedTermSummary.get(UPPER_DIV_COST).add(baseTaCost.multiply(taCount)));
                    combinedTermSummary.put(UPPER_DIV_COST, combinedTermSummary.get(UPPER_DIV_COST).add(baseReaderCost.multiply(readerCount)));
                } else {
                    combinedTermSummary.put(LOWER_DIV_COST, combinedTermSummary.get(LOWER_DIV_COST).add(baseTaCost.multiply(taCount)));
                    combinedTermSummary.put(LOWER_DIV_COST, combinedTermSummary.get(LOWER_DIV_COST).add(baseReaderCost.multiply(readerCount)));
                }
            }

            combinedTermSummary.put(TA_COUNT, combinedTermSummary.get(TA_COUNT).add(taCount));
            combinedTermSummary.put(TA_COST, combinedTermSummary.get(TA_COST).add(baseTaCost.multiply(taCount)));
            combinedTermSummary.put(READER_COUNT, combinedTermSummary.get(READER_COUNT).add(readerCount));
            combinedTermSummary.put(READER_COST, combinedTermSummary.get(READER_COST).add(baseReaderCost.multiply(readerCount)));
            combinedTermSummary.put(UNITS_OFFERED, combinedTermSummary.get(UNITS_OFFERED).add(calculateUnits(sectionGroupCost)));

            currentTermSummary.put(TOTAL_TEACHING_COST, currentTermSummary.get(TOTAL_TEACHING_COST)
                    .add(baseTaCost.multiply(taCount))
                    .add(baseReaderCost.multiply(readerCount)));
            combinedTermSummary.put(TOTAL_TEACHING_COST, combinedTermSummary.get(TOTAL_TEACHING_COST)
                    .add(baseTaCost.multiply(taCount))
                    .add(baseReaderCost.multiply(readerCount)));

            BigDecimal sectionGroupSeats = calculateSeats(sectionGroupCost);

            if (Integer.parseInt(sectionGroupCost.getCourseNumber().replaceAll("[^\\d.]", "")) >= 200) {
                currentTermSummary.put(GRAD_OFFERINGS, currentTermSummary.get(GRAD_OFFERINGS).add(BigDecimal.ONE));
                currentTermSummary.put(GRAD_SEATS, currentTermSummary.get(GRAD_SEATS).add(sectionGroupSeats));

                combinedTermSummary.put(GRAD_OFFERINGS, combinedTermSummary.get(GRAD_OFFERINGS).add(BigDecimal.ONE));
                combinedTermSummary.put(GRAD_SEATS, combinedTermSummary.get(GRAD_SEATS).add(sectionGroupSeats));
            } else if (Integer.parseInt(sectionGroupCost.getCourseNumber().replaceAll("[^\\d.]", "")) > 99) {
                currentTermSummary.put(UPPER_DIV_OFFERINGS, currentTermSummary.get(UPPER_DIV_OFFERINGS).add(BigDecimal.ONE));
                currentTermSummary.put(UPPER_DIV_SEATS, currentTermSummary.get(UPPER_DIV_SEATS).add(sectionGroupSeats));

                combinedTermSummary.put(UPPER_DIV_OFFERINGS, combinedTermSummary.get(UPPER_DIV_OFFERINGS).add(BigDecimal.ONE));
                combinedTermSummary.put(UPPER_DIV_SEATS, combinedTermSummary.get(UPPER_DIV_SEATS).add(sectionGroupSeats));
            } else {
                currentTermSummary.put(LOWER_DIV_OFFERINGS, currentTermSummary.get(LOWER_DIV_OFFERINGS).add(BigDecimal.ONE));
                currentTermSummary.put(LOWER_DIV_SEATS, currentTermSummary.get(LOWER_DIV_SEATS).add(sectionGroupSeats));

                combinedTermSummary.put(LOWER_DIV_OFFERINGS, combinedTermSummary.get(LOWER_DIV_OFFERINGS).add(BigDecimal.ONE));
                combinedTermSummary.put(LOWER_DIV_SEATS, combinedTermSummary.get(LOWER_DIV_SEATS).add(sectionGroupSeats));
            }
            currentTermSummary.put(TOTAL_SEATS, currentTermSummary.get(TOTAL_SEATS).add(sectionGroupSeats));
            combinedTermSummary.put(TOTAL_SEATS, combinedTermSummary.get(TOTAL_SEATS).add(sectionGroupSeats));
        }

        if(budgetScenario.getFromLiveData()){
            List<TeachingAssignment> teachingAssignments = teachingAssignmentService.findApprovedByWorkgroupIdAndYear(workgroup.getId(), budget.getSchedule().getYear());
            for(TeachingAssignment teachingAssignment : teachingAssignments){
                if(termCodes.contains(teachingAssignment.getTermCode()) && !teachingAssignmentIds.contains(teachingAssignment.getId()) && teachingAssignment.getSectionGroup() != null && teachingAssignment.getSectionGroup().getId() > 0){
                    Map<BudgetSummary, BigDecimal> currentTermSummary = termTotals.get(teachingAssignment.getTermCode());
                    BigDecimal instructorCost = calculateTeachingAssignmentCost(workgroup, budget, teachingAssignment);
                    long instructorTypeId = calculateTeachingAssignmentTypeId(teachingAssignment, workgroup);

                    if(instructorTypeId == EMERITI.getId()){
                        currentTermSummary.put(EMERITI_COUNT, currentTermSummary.get(EMERITI_COUNT).add(BigDecimal.ONE));
                        combinedTermSummary.put(EMERITI_COUNT, combinedTermSummary.get(EMERITI_COUNT).add(BigDecimal.ONE));

                        currentTermSummary.put(EMERITI_COST, currentTermSummary.get(EMERITI_COST).add(instructorCost));
                        combinedTermSummary.put(EMERITI_COST, combinedTermSummary.get(EMERITI_COST).add(instructorCost));
                    } else if (instructorTypeId == VISITING_PROFESSOR.getId()){
                        currentTermSummary.put(VISITING_PROFESSOR_COUNT, currentTermSummary.get(VISITING_PROFESSOR_COUNT).add(BigDecimal.ONE));
                        combinedTermSummary.put(VISITING_PROFESSOR_COUNT, combinedTermSummary.get(VISITING_PROFESSOR_COUNT).add(BigDecimal.ONE));

                        currentTermSummary.put(VISITING_PROFESSOR_COST, currentTermSummary.get(VISITING_PROFESSOR_COST).add(instructorCost));
                        combinedTermSummary.put(VISITING_PROFESSOR_COST, combinedTermSummary.get(VISITING_PROFESSOR_COST).add(instructorCost));
                    } else if (instructorTypeId == ASSOCIATE_INSTRUCTOR.getId()){
                        currentTermSummary.put(ASSOCIATE_INSTRUCTOR_COUNT, currentTermSummary.get(ASSOCIATE_INSTRUCTOR_COUNT).add(BigDecimal.ONE));
                        combinedTermSummary.put(ASSOCIATE_INSTRUCTOR_COUNT, combinedTermSummary.get(ASSOCIATE_INSTRUCTOR_COUNT).add(BigDecimal.ONE));

                        currentTermSummary.put(ASSOCIATE_INSTRUCTOR_COST, currentTermSummary.get(ASSOCIATE_INSTRUCTOR_COST).add(instructorCost));
                        combinedTermSummary.put(ASSOCIATE_INSTRUCTOR_COST, combinedTermSummary.get(ASSOCIATE_INSTRUCTOR_COST).add(instructorCost));
                    } else if (instructorTypeId == UNIT18_LECTURER.getId()){
                        currentTermSummary.put(UNIT18_LECTURER_COUNT, currentTermSummary.get(UNIT18_LECTURER_COUNT).add(BigDecimal.ONE));
                        combinedTermSummary.put(UNIT18_LECTURER_COUNT, combinedTermSummary.get(UNIT18_LECTURER_COUNT).add(BigDecimal.ONE));

                        currentTermSummary.put(UNIT18_LECTURER_COST, currentTermSummary.get(UNIT18_LECTURER_COST).add(instructorCost));
                        combinedTermSummary.put(UNIT18_LECTURER_COST, combinedTermSummary.get(UNIT18_LECTURER_COST).add(instructorCost));
                    } else if (instructorTypeId == CONTINUING_LECTURER.getId()){
                        currentTermSummary.put(CONTINUING_LECTURER_COUNT, currentTermSummary.get(CONTINUING_LECTURER_COUNT).add(BigDecimal.ONE));
                        combinedTermSummary.put(CONTINUING_LECTURER_COUNT, combinedTermSummary.get(CONTINUING_LECTURER_COUNT).add(BigDecimal.ONE));

                        currentTermSummary.put(CONTINUING_LECTURER_COST, currentTermSummary.get(CONTINUING_LECTURER_COST).add(instructorCost));
                        combinedTermSummary.put(CONTINUING_LECTURER_COST, combinedTermSummary.get(CONTINUING_LECTURER_COST).add(instructorCost));
                    } else if (instructorTypeId == CONTINUING_LECTURER_AUGMENTATION.getId()){
                        currentTermSummary.put(CONTINUING_LECTURER_AUGMENTATION_COUNT, currentTermSummary.get(CONTINUING_LECTURER_AUGMENTATION_COUNT).add(BigDecimal.ONE));
                        combinedTermSummary.put(CONTINUING_LECTURER_AUGMENTATION_COUNT, combinedTermSummary.get(CONTINUING_LECTURER_AUGMENTATION_COUNT).add(BigDecimal.ONE));

                        currentTermSummary.put(CONTINUING_LECTURER_AUGMENTATION_COST, currentTermSummary.get(CONTINUING_LECTURER_AUGMENTATION_COST).add(instructorCost));
                        combinedTermSummary.put(CONTINUING_LECTURER_AUGMENTATION_COST, combinedTermSummary.get(CONTINUING_LECTURER_AUGMENTATION_COST).add(instructorCost));
                    } else if (instructorTypeId == LADDER_FACULTY.getId()){
                        currentTermSummary.put(LADDER_FACULTY_COUNT, currentTermSummary.get(LADDER_FACULTY_COUNT).add(BigDecimal.ONE));
                        combinedTermSummary.put(LADDER_FACULTY_COUNT, combinedTermSummary.get(LADDER_FACULTY_COUNT).add(BigDecimal.ONE));

                        currentTermSummary.put(LADDER_FACULTY_COST, currentTermSummary.get(LADDER_FACULTY_COST).add(instructorCost));
                        combinedTermSummary.put(LADDER_FACULTY_COST, combinedTermSummary.get(LADDER_FACULTY_COST).add(instructorCost));
                    } else if (instructorTypeId == INSTRUCTOR.getId()){
                        currentTermSummary.put(INSTRUCTOR_COUNT, currentTermSummary.get(INSTRUCTOR_COUNT).add(BigDecimal.ONE));
                        combinedTermSummary.put(INSTRUCTOR_COUNT, combinedTermSummary.get(INSTRUCTOR_COUNT).add(BigDecimal.ONE));

                        currentTermSummary.put(INSTRUCTOR_COST, currentTermSummary.get(INSTRUCTOR_COST).add(instructorCost));
                        combinedTermSummary.put(INSTRUCTOR_COST, combinedTermSummary.get(INSTRUCTOR_COST).add(instructorCost));
                    } else if (instructorTypeId == LECTURER_SOE.getId()){
                        currentTermSummary.put(LECTURER_SOE_COUNT, currentTermSummary.get(LECTURER_SOE_COUNT).add(BigDecimal.ONE));
                        combinedTermSummary.put(LECTURER_SOE_COUNT, combinedTermSummary.get(LECTURER_SOE_COUNT).add(BigDecimal.ONE));

                        currentTermSummary.put(LECTURER_SOE_COST, currentTermSummary.get(LECTURER_SOE_COST).add(instructorCost));
                        combinedTermSummary.put(LECTURER_SOE_COST, combinedTermSummary.get(LECTURER_SOE_COST).add(instructorCost));
                    } else if (instructorTypeId == NEW_FACULTY_HIRE.getId()){
                        currentTermSummary.put(NEW_FACULTY_HIRE_COUNT, currentTermSummary.get(NEW_FACULTY_HIRE_COUNT).add(BigDecimal.ONE));
                        combinedTermSummary.put(NEW_FACULTY_HIRE_COUNT, combinedTermSummary.get(NEW_FACULTY_HIRE_COUNT).add(BigDecimal.ONE));

                        currentTermSummary.put(NEW_FACULTY_HIRE_COST, currentTermSummary.get(NEW_FACULTY_HIRE_COST).add(instructorCost));
                        combinedTermSummary.put(NEW_FACULTY_HIRE_COST, combinedTermSummary.get(NEW_FACULTY_HIRE_COST).add(instructorCost));
                    }

                    currentTermSummary.put(REPLACEMENT_COST, currentTermSummary.get(REPLACEMENT_COST).add(instructorCost));
                    combinedTermSummary.put(REPLACEMENT_COST, combinedTermSummary.get(REPLACEMENT_COST).add(instructorCost));
                    currentTermSummary.put(TOTAL_TEACHING_COST, currentTermSummary.get(TOTAL_TEACHING_COST).add(instructorCost));
                    combinedTermSummary.put(TOTAL_TEACHING_COST, combinedTermSummary.get(TOTAL_TEACHING_COST).add(instructorCost));
                    currentTermSummary.put(COURSE_COUNT, currentTermSummary.get(COURSE_COUNT).add(BigDecimal.ONE));
                    combinedTermSummary.put(COURSE_COUNT, combinedTermSummary.get(COURSE_COUNT).add(BigDecimal.ONE));
                }
            }
        }

        BigDecimal funds = BigDecimal.ZERO;
        for(LineItem lineItem: lineItems){
            funds = funds.add(lineItem.getAmount());
        }
        BigDecimal expenses = BigDecimal.ZERO;
        for(ExpenseItem expenseItem : expenseItems){
            if(termCodes.contains(expenseItem.getTermCode())){
                termTotals.get(expenseItem.getTermCode()).put(TOTAL_EXPENSES, termTotals.get(expenseItem.getTermCode()).get(TOTAL_EXPENSES).add(expenseItem.getAmount()));
                termTotals.get(expenseItem.getTermCode()).put(TOTAL_TEACHING_COST, termTotals.get(expenseItem.getTermCode()).get(TOTAL_TEACHING_COST).add(expenseItem.getAmount()));
                expenses = expenses.add(expenseItem.getAmount());
            }
        }
        combinedTermSummary.put(TOTAL_TEACHING_COST, combinedTermSummary.get(TOTAL_TEACHING_COST).add(expenses));
        combinedTermSummary.put(TOTAL_FUNDS, funds);
        combinedTermSummary.put(TOTAL_EXPENSES, expenses);
        combinedTermSummary.put(TOTAL_BALANCE, funds.subtract(combinedTermSummary.get(TOTAL_TEACHING_COST)));

        return termTotals;
    }

    private static boolean isGrad(SectionGroupCost sectionGroupCost) {
        return Integer.parseInt(sectionGroupCost.getCourseNumber().replaceAll("[^\\d.]", "")) >= 200;
    }

    private static boolean isUpper(SectionGroupCost sectionGroupCost) {
        return Integer.parseInt(sectionGroupCost.getCourseNumber().replaceAll("[^\\d.]", "")) > 99;
    }

    private static BigDecimal calculateUnits(SectionGroupCost sectionGroupCost) {
        // if sectionGroupCost has unitsHigh, it is a variable unit course, use unitsVariable if exist, otherwise, use zero for the variable unit course
        if ((sectionGroupCost.getUnitsHigh() == null ? 0 : sectionGroupCost.getUnitsHigh()) > 0) {
            return sectionGroupCost.getUnitsVariable() == null ? BigDecimal.ZERO : new BigDecimal(String.valueOf(sectionGroupCost.getUnitsVariable()));
        } else {
            return sectionGroupCost.getUnitsLow() == null ? BigDecimal.ZERO : new BigDecimal(String.valueOf(sectionGroupCost.getUnitsLow()));
        }
    }

    private static BigDecimal calculateSeats(SectionGroupCost sectionGroupCost) {
        if (sectionGroupCost.getEnrollment() == null) {
            return BigDecimal.ZERO;
        } else {
            return new BigDecimal(sectionGroupCost.getEnrollment());
        }
    }

    private static BigDecimal calculateSCH(SectionGroupCost sectionGroupCost) {
        return calculateSeats(sectionGroupCost).multiply(calculateUnits(sectionGroupCost));
    }

    private long calculateSectionGroupInstructorTypeId(
            SectionGroupCostInstructor sectionGroupCostInstructor, Workgroup workgroup) {
        if(sectionGroupCostInstructor.getInstructorType() != null){
            return sectionGroupCostInstructor.getInstructorType().getId();
        } else if (sectionGroupCostInstructor.getInstructor() != null){
            UserRole instructorRole = userService.getOneByLoginId(sectionGroupCostInstructor.getInstructor().getLoginId()).getUserRoles().stream().filter(ur -> (ur.getRole().getId() == 15 && workgroup.getId() == ur.getWorkgroup().getId())).findFirst().orElse(null);
            if (instructorRole != null) {
                return instructorRole.getInstructorType().getId();
            }
        }
        return 0;
    };

    private long calculateTeachingAssignmentTypeId(
            TeachingAssignment teachingAssignment, Workgroup workgroup) {
        if(teachingAssignment.getInstructorType() != null){
            return teachingAssignment.getInstructorType().getId();
        } else if (teachingAssignment.getInstructor() != null){
            UserRole instructorRole = userService.getOneByLoginId(teachingAssignment.getInstructor().getLoginId()).getUserRoles().stream().filter(ur -> (ur.getRole().getId() == 15 && workgroup.getId() == ur.getWorkgroup().getId())).findFirst().orElse(null);
            if (instructorRole != null) {
                return instructorRole.getInstructorType().getId();
            }
        }
        return 0;
    };

    public BigDecimal calculateSectionGroupInstructorCost(Workgroup workgroup, Budget budget, BudgetScenario budgetScenario, SectionGroupCostInstructor sectionGroupCostInstructor) {
        if(sectionGroupCostInstructor.getCost() != null){
            return sectionGroupCostInstructor.getCost();
        }
        if (sectionGroupCostInstructor.getInstructor() != null) {
            InstructorCost instructorCost;

            if (budgetScenario.getIsBudgetRequest()) {
                instructorCost = instructorCostService.findByInstructorIdAndBudgetScenarioId(sectionGroupCostInstructor.getInstructor().getId(), budgetScenario.getId());
            } else {
                instructorCost = instructorCostService.findByInstructorIdAndBudgetId(sectionGroupCostInstructor.getInstructor().getId(), budget.getId());
            }

            if (instructorCost != null && instructorCost.getCost() != null) {
                return instructorCost.getCost();
            } else {
                InstructorTypeCost instructorTypeCost;

                if (budgetScenario.getIsBudgetRequest()) {
                    instructorTypeCost = instructorTypeCostService.findByInstructorTypeIdAndBudgetScenarioId(sectionGroupCostInstructor.getInstructorType().getId(), budgetScenario.getId());
                } else {
                    instructorTypeCost = instructorTypeCostService.findByInstructorTypeIdAndBudgetId(sectionGroupCostInstructor.getInstructorType().getId(), budget.getId());
                }

                if (instructorTypeCost != null && instructorTypeCost.getCost() != null){
                    return new BigDecimal(String.valueOf(instructorTypeCost.getCost()));
                }
            }
        } else if (sectionGroupCostInstructor.getInstructorType() != null){
            InstructorTypeCost instructorTypeCost;

            if (budgetScenario.getIsBudgetRequest()) {
                instructorTypeCost = instructorTypeCostService.findByInstructorTypeIdAndBudgetScenarioId(sectionGroupCostInstructor.getInstructorType().getId(),
                    budgetScenario.getId());
            } else {
                instructorTypeCost = instructorTypeCostService.findByInstructorTypeIdAndBudgetId(sectionGroupCostInstructor.getInstructorType().getId(), budget.getId());
            }

            if (instructorTypeCost != null && instructorTypeCost.getCost() != null){
                return new BigDecimal(String.valueOf(instructorTypeCost.getCost()));
            }
        }


        return BigDecimal.ZERO;
    }

    public BigDecimal calculateTeachingAssignmentCost(Workgroup workgroup, Budget budget, TeachingAssignment teachingAssignment) {
        if (teachingAssignment.getInstructor() != null) {
            InstructorCost instructorCost = instructorCostService.findByInstructorIdAndBudgetId(teachingAssignment.getInstructor().getId(), budget.getId());
            if (instructorCost != null && instructorCost.getCost() != null) {
                return instructorCost.getCost();
            } else {
                InstructorTypeCost instructorTypeCost = instructorTypeCostService.findByInstructorTypeIdAndBudgetId(teachingAssignment.getInstructorType().getId(), budget.getId());
                if (instructorTypeCost != null && instructorTypeCost.getCost() != null){
                    return new BigDecimal(String.valueOf(instructorTypeCost.getCost()));
                }
            }
        } else if (teachingAssignment.getInstructorType() != null){
            InstructorTypeCost instructorTypeCost = instructorTypeCostService.findByInstructorTypeIdAndBudgetId(teachingAssignment.getInstructorType().getId(), budget.getId());
            if (instructorTypeCost != null && instructorTypeCost.getCost() != null){
                return new BigDecimal(String.valueOf(instructorTypeCost.getCost()));
            }
        }


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
            new SimpleEntry<>(CONTINUING_LECTURER_AUGMENTATION_COUNT, BigDecimal.ZERO),
            new SimpleEntry<>(CONTINUING_LECTURER_AUGMENTATION_COST, BigDecimal.ZERO),
            new SimpleEntry<>(VISITING_PROFESSOR_COUNT, BigDecimal.ZERO),
            new SimpleEntry<>(VISITING_PROFESSOR_COST, BigDecimal.ZERO),
            new SimpleEntry<>(INSTRUCTOR_COUNT, BigDecimal.ZERO),
            new SimpleEntry<>(INSTRUCTOR_COST, BigDecimal.ZERO),
            new SimpleEntry<>(LECTURER_SOE_COUNT, BigDecimal.ZERO),
            new SimpleEntry<>(LECTURER_SOE_COST, BigDecimal.ZERO),
            new SimpleEntry<>(NEW_FACULTY_HIRE_COUNT, BigDecimal.ZERO),
            new SimpleEntry<>(NEW_FACULTY_HIRE_COST, BigDecimal.ZERO),
            new SimpleEntry<>(UNASSIGNED_COUNT, BigDecimal.ZERO),
            new SimpleEntry<>(UNASSIGNED_COST, BigDecimal.ZERO),
            new SimpleEntry<>(COURSE_COUNT, BigDecimal.ZERO),
            new SimpleEntry<>(REPLACEMENT_COST, BigDecimal.ZERO),
            new SimpleEntry<>(TOTAL_TEACHING_COST, BigDecimal.ZERO),
            new SimpleEntry<>(TOTAL_BALANCE, BigDecimal.ZERO),
            new SimpleEntry<>(TOTAL_FUNDS, BigDecimal.ZERO),
            new SimpleEntry<>(TOTAL_EXPENSES, BigDecimal.ZERO),
            new SimpleEntry<>(LOWER_DIV_OFFERINGS, BigDecimal.ZERO),
            new SimpleEntry<>(UPPER_DIV_OFFERINGS, BigDecimal.ZERO),
            new SimpleEntry<>(GRAD_OFFERINGS, BigDecimal.ZERO),
            new SimpleEntry<>(LOWER_DIV_COST, BigDecimal.ZERO),
            new SimpleEntry<>(UPPER_DIV_COST, BigDecimal.ZERO),
            new SimpleEntry<>(GRAD_COST, BigDecimal.ZERO),
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
