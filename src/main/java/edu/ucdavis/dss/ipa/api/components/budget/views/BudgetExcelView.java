package edu.ucdavis.dss.ipa.api.components.budget.views;

import edu.ucdavis.dss.ipa.api.components.budget.constants.CommonGood;
import edu.ucdavis.dss.ipa.api.components.budget.constants.WritingExperience;
import edu.ucdavis.dss.ipa.api.helpers.SpringContext;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.services.BudgetCalculationService;
import edu.ucdavis.dss.ipa.services.SectionGroupService;
import edu.ucdavis.dss.ipa.utilities.ExcelHelper;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.IgnoredErrorType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import static edu.ucdavis.dss.ipa.api.helpers.Utilities.round;
import static edu.ucdavis.dss.ipa.entities.enums.BudgetSummary.*;

public class BudgetExcelView extends AbstractXlsxView {
    private List<BudgetScenarioExcelView> budgetScenarioExcelViews;

    private final DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private BudgetCalculationService getBudgetCalculationService() {
        return SpringContext.getBean(BudgetCalculationService.class);
    }

    private SectionGroupService getSectionGroupService() {
        return SpringContext.getBean(SectionGroupService.class);
    }

    public BudgetExcelView (List<BudgetScenarioExcelView> budgetScenarioExcelViews) {
        this.budgetScenarioExcelViews = budgetScenarioExcelViews;
    }

    /*
        Make sure to edit the frontend if any calculations change!!!
     */
    @Override
    protected void buildExcelDocument(Map<String, Object> model, Workbook workbook, HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setHeader("Content-Type", "multipart/mixed; charset=\"utf-8\"");
        response.setHeader("Content-Disposition", "attachment; filename=\"Budget-Report.xlsx\"");
        Integer MAX_COLUMN_CHARACTERS = 50;
        Integer minimumNoteColumnWidth = 10;
        Sheet budgetSummarySheet = workbook.createSheet("Budget Summary");
        budgetSummarySheet = ExcelHelper.setSheetHeader(budgetSummarySheet, Arrays.asList("Year", "Department", "Scenario Name", "", "Fall Quarter", "Winter Quarter", "Spring Quarter", "Total"));

        Sheet scheduleCostSheet = workbook.createSheet("Schedule Cost");

        scheduleCostSheet = ExcelHelper.setSheetHeader(scheduleCostSheet, Arrays.asList(
           "Year",
           "Department",
           "Scenario Name",
           "Term",
           "Subject Code",
           "Course Number",
           "Title",
           "Tags",
           "Units High",
           "Units Low",
           "Sequence",
           "Enrollment",
           "Actual Enrollment",
           "Sections",
           "Instructor",
           "Instructor Type",
           "Instructor 2",
           "Instructor 2 Type",
           "Regular Instructor",
           "Reason Category",
           "Additional Comments",
           "Students per TA (Planned)",
           "Students per TA (Actual)",
           "TAs",
           "Readers",
           "TA Cost",
           "Reader Cost",
           "Support Cost",
           "Instructor Cost",
           "Total Cost",
           "Course Type",
           "Common Good?",
           "WE"
        ));

        Sheet fundsSheet = workbook.createSheet("Funds");
        fundsSheet = ExcelHelper.setSheetHeader(fundsSheet, Arrays.asList("Year", "Department", "Scenario Name", "Term", "Type", "Category", "Description", "Notes", "Comments", "Account Number", "Document Number", "Amount"));

        Sheet expensesSheet = workbook.createSheet("Other Costs");
        expensesSheet = ExcelHelper.setSheetHeader(expensesSheet,
            Arrays.asList("Year", "Department", "Scenario Name", "Term", "", "", "Description",
            "", "", "", "", "", "", "", "",
            "Instructor Type",
            "", "", "", "", "", "", "",
            "", // TAs
            "", "", "", "",
            "Cost"
        ));

        Sheet instructorSalariesSheet = workbook.createSheet("Instructor Salaries");
        instructorSalariesSheet = ExcelHelper.setSheetHeader(instructorSalariesSheet, Arrays.asList("Year", "Department", "Instructor", "Type", "Cost"));

        Sheet instructorCategoryCostSheet = workbook.createSheet("Instructor Category Cost");
        instructorCategoryCostSheet = ExcelHelper.setSheetHeader(instructorCategoryCostSheet, Arrays.asList("Year", "Department", "Type", "Cost"));

        for (BudgetScenarioExcelView budgetScenarioExcelView : budgetScenarioExcelViews) {
            String year = yearToAcademicYear(budgetScenarioExcelView.getBudget().getSchedule().getYear());
            Boolean isLiveData = budgetScenarioExcelView.getBudgetScenario().getFromLiveData();
            Boolean isBudgetRequest = budgetScenarioExcelView.getBudgetScenario().getIsBudgetRequest();
            String scenarioName = budgetScenarioExcelView.getBudgetScenario().getDisplayName();
            Float baseTaCost = isBudgetRequest ? budgetScenarioExcelView.getBudgetScenario().getTaCost() : budgetScenarioExcelView.getBudget().getTaCost();
            Float baseReaderCost = isBudgetRequest ? budgetScenarioExcelView.getBudgetScenario().getReaderCost() : budgetScenarioExcelView.getBudget().getReaderCost();

            // Create Schedule Cost sheet
            for(SectionGroupCost sectionGroupCost : budgetScenarioExcelView.getSectionGroupCosts().stream().sorted(Comparator.comparing(SectionGroupCost::getTermCode).thenComparing(SectionGroupCost::getSubjectCode).thenComparing(SectionGroupCost::getCourseNumber)).collect(Collectors.toList()) ){
                Float taCost = (sectionGroupCost.getTaCount() == null ? 0.0F : sectionGroupCost.getTaCount()) * baseTaCost;
                Float readerCost = (sectionGroupCost.getReaderCount() == null ? 0.0F: sectionGroupCost.getReaderCount() ) * baseReaderCost;
                Float supportCost = taCost + readerCost;
                Float instructorCost = 0.0F;
                Long currentEnrollment = null;
                if(budgetScenarioExcelView.getCensusMap().get(sectionGroupCost.getTermCode()) != null){
                    if(budgetScenarioExcelView.getCensusMap().get(sectionGroupCost.getTermCode()).get(sectionGroupCost.getSubjectCode() + sectionGroupCost.getCourseNumber()) != null){
                        currentEnrollment = budgetScenarioExcelView.getCensusMap().get(sectionGroupCost.getTermCode()).get(sectionGroupCost.getSubjectCode() + sectionGroupCost.getCourseNumber()).get(sectionGroupCost.getSequencePattern());
                    }
                }
                List<String> namedInstructors = new ArrayList<>();
                List<String> namedInstructorTypes = new ArrayList<>();
                List<String> unnamedInstructors = new ArrayList<>();
                List<Long> teachingAssingmentIds = new ArrayList<>();
                List<SectionGroupCostInstructor> sectionGroupCostInstructors = sectionGroupCost.getSectionGroupCostInstructors();
                for (SectionGroupCostInstructor sectionGroupCostInstructor : sectionGroupCostInstructors){
                    if(sectionGroupCostInstructor.getInstructor() != null){
                        namedInstructors.add(sectionGroupCostInstructor.getInstructor().getInvertedName());
                        namedInstructorTypes.add(sectionGroupCostInstructor.getInstructorType().getDescription());
                    } else if (sectionGroupCostInstructor.getInstructorType() != null) {
                        unnamedInstructors.add(sectionGroupCostInstructor.getInstructorType().getDescription());
                    }
                    if(sectionGroupCostInstructor.getTeachingAssignment() != null) {
                        teachingAssingmentIds.add(sectionGroupCostInstructor.getTeachingAssignment().getId());
                    }
                    instructorCost += getBudgetCalculationService().calculateSectionGroupInstructorCost(budgetScenarioExcelView.workgroup, budgetScenarioExcelView.budget, budgetScenarioExcelView.budgetScenario, sectionGroupCostInstructor).floatValue();
                }

                SectionGroup sectionGroup = getSectionGroupService().findBySectionGroupCostDetails(
                    budgetScenarioExcelView.getWorkgroup().getId(),
                    sectionGroupCost.getCourseNumber(),
                    sectionGroupCost.getSequencePattern(),
                    sectionGroupCost.getTermCode(),
                    sectionGroupCost.getSubjectCode());

                if(isLiveData){
                    if(sectionGroup != null){
                        for(TeachingAssignment teachingAssignment : sectionGroup.getTeachingAssignments()){
                            if(!teachingAssingmentIds.contains(teachingAssignment.getId()) && teachingAssignment.isApproved()){
                                if(teachingAssignment.getInstructor() != null){
                                    namedInstructors.add(teachingAssignment.getInstructor().getInvertedName());
                                    namedInstructorTypes.add(teachingAssignment.getInstructorType().getDescription());
                                } else if (sectionGroupCost.getInstructorType() != null) {
                                    unnamedInstructors.add(teachingAssignment.getInstructorType().getDescription());
                                }
                                instructorCost += getBudgetCalculationService().calculateTeachingAssignmentCost(budgetScenarioExcelView.workgroup ,budgetScenarioExcelView.budget, teachingAssignment).floatValue();
                            }
                        }
                    }
                }

                List<String> courseTags = new ArrayList<>();
                if (sectionGroup != null) {
                    courseTags = sectionGroup.getCourse().getTags().stream().map(t -> t.getName()).collect(Collectors.toList());
                }

                Float actualStudentsPerTA = null;
                if (currentEnrollment != null && sectionGroupCost.getTaCount() != null && sectionGroupCost.getTaCount() != 0) {
                    actualStudentsPerTA = currentEnrollment / sectionGroupCost.getTaCount();
                }

                List<Object> scheduleCostValues = new ArrayList<>();
                scheduleCostValues.addAll(Arrays.asList(
                    year,
                    budgetScenarioExcelView.getWorkgroup().getName(),
                    scenarioName,
                    Term.getRegistrarName(sectionGroupCost.getTermCode()),
                    sectionGroupCost.getSubjectCode(),
                    sectionGroupCost.getCourseNumber(),
                    sectionGroupCost.getTitle(),
                    String.join(", ", courseTags),
                    sectionGroupCost.getUnitsHigh(),
                    sectionGroupCost.getUnitsLow(),
                    sectionGroupCost.getSequencePattern(),
                    sectionGroupCost.getEnrollment(),
                    currentEnrollment,
                    sectionGroupCost.getSectionCount()));

                // Each instructor has two columns: Name and Type
                int numberOfInstructorColumns = 2;
                if (namedInstructors.size() > 0) {
                    for (int i = 0; i < namedInstructors.size(); i++) {
                        scheduleCostValues.add(namedInstructors.get(i));
                        scheduleCostValues.add(namedInstructorTypes.get(i));
                        numberOfInstructorColumns--;
                    }
                }

                if (unnamedInstructors.size() > 0) {
					for (String unnamedInstructor : unnamedInstructors) {
						scheduleCostValues.add(unnamedInstructor);
						scheduleCostValues.add(unnamedInstructor);
						numberOfInstructorColumns--;
					}
                }

                for (int i = 0; i < numberOfInstructorColumns; i++) {
                    scheduleCostValues.add("");
                    scheduleCostValues.add("");
                }

                scheduleCostValues.addAll(
                    Arrays.asList(
                        (sectionGroupCost.getOriginalInstructor() == null ? "" :
                            sectionGroupCost.getOriginalInstructor().getInvertedName()),
                        sectionGroupCost.getReasonCategoryDescription(),
                        sectionGroupCost.getReason(),
                        round(sectionGroupCost.getEnrollmentPerTA()),
                        round(actualStudentsPerTA),
                        sectionGroupCost.getTaCount(),
                        sectionGroupCost.getReaderCount(),
                        taCost,
                        readerCost,
                        supportCost,
                        instructorCost,
                        supportCost + instructorCost,
                        getCourseType(sectionGroupCost),
                        getCommonGood(sectionGroupCost),
                        getWritingExperience(sectionGroupCost)
                    )
                );

                scheduleCostSheet = ExcelHelper.writeRowToSheet(
                    scheduleCostSheet,
                    scheduleCostValues
                );
            }

            budgetSummarySheet = writeSummaryTerms(budgetSummarySheet, budgetScenarioExcelView);

            // Create Funds sheet
            for(LineItem lineItem : budgetScenarioExcelView.getLineItems()){
                List<String> comments = new ArrayList<>();
                for (LineItemComment lineItemComment : lineItem.getLineItemComments()){
                    comments.add(lineItemComment.getComment());
                }
                minimumNoteColumnWidth = Math.max(minimumNoteColumnWidth, (lineItem.getNotes() == null ? 0 : lineItem.getNotes().length()));

                String termCode = lineItem.getTermCode() != null ? Term.getRegistrarName(lineItem.getTermCode()) : "";
                String type = lineItem.getLineItemType() != null ? lineItem.getLineItemType().getDescription() : "";

                List<Object> cellValues = Arrays.asList(
                        year,
                        budgetScenarioExcelView.getWorkgroup().getName(),
                        scenarioName,
                        termCode,
                        type,
                        lineItem.getLineItemCategory().getDescription(),
                        lineItem.getDescription(),
                        lineItem.getNotes(),
                        String.join("\n\r\n\r", comments),
                        lineItem.getAccountNumber(),
                        lineItem.getDocumentNumber(),
                        lineItem.getAmount());
                fundsSheet = ExcelHelper.writeRowToSheet(fundsSheet, cellValues);
            }

            // Create Expenses sheet
            List<String> termCodes = budgetScenarioExcelView.getTermCodes();
            for(ExpenseItem expenseItem : budgetScenarioExcelView.getBudgetScenario().getExpenseItems()){

                if(termCodes.contains(expenseItem.getTermCode())){
                    List<Object> cellValues = Arrays.asList(
                        year,
                        budgetScenarioExcelView.getWorkgroup().getName(),
                        scenarioName,
                        Term.getRegistrarName(expenseItem.getTermCode()),
                        "", "",
                        expenseItem.getDescription(),
                        "", "", "", "", "", "", "", "",
                        expenseItem.getExpenseItemInstructorTypeDescription(),
                        "", "", "", "", "", "", "",
                        "", // TAs value
                        "", // Readers value
                        "", "", "",
                        expenseItem.getAmount()
                        // "", "", ""
                        );

                    ExcelHelper.writeRowToSheet(expensesSheet, cellValues);

                    expensesSheet.setColumnHidden(4, true);
                    expensesSheet.setColumnHidden(5, true);
                    for (int colIndex = 7; colIndex < 28; colIndex++) {
                        if (colIndex == 15) { continue; } // Instructor Type
                        expensesSheet.setColumnHidden(colIndex, true);
                    }
                }

            }

            // Creating Instructor Salaries sheet
            for(Instructor instructor : budgetScenarioExcelView.getActiveInstructors()){
                // Get data into correct shape
                InstructorCost instructorCost = budgetScenarioExcelView.getInstructorCosts().stream().filter(ic -> ic.getInstructor().getId() == instructor.getId()).findFirst().orElse(null);

                String instructorName = instructor.getInvertedName();

                // Calculate instructor type.  Make sure to compare with frontend if you need to change.
                Set<User> users = budgetScenarioExcelView.users;

                User user = budgetScenarioExcelView.users.stream().filter(u -> u.getLoginId().equalsIgnoreCase(instructor.getLoginId())).findFirst().orElse(null);

                UserRole userRole = user.getUserRoles().stream().filter(ur -> (
                        ur.getRole().getId() == 15 &&
                        budgetScenarioExcelView.getWorkgroup().getId() == ur.getWorkgroup().getId())).findFirst().orElse(null);
                String instructorTypeDescription = "";
                if(userRole != null && userRole.getInstructorType() != null) {
                    instructorTypeDescription = userRole.getInstructorType().getDescription();
                }else{
                    List<TeachingAssignment> tas = budgetScenarioExcelView.getTeachingAssignments();
                    Long instructorId = instructor.getId();
                    Long scheduleId = budgetScenarioExcelView.getBudget().getSchedule().getId();
                    TeachingAssignment ta = budgetScenarioExcelView.getTeachingAssignments().stream().filter(t -> t.getInstructor() != null).filter(t -> (t.getInstructor().getId() == instructor.getId() && t.getSchedule().getId() == budgetScenarioExcelView.getBudget().getSchedule().getId())).findFirst().orElse(null);
                    if(ta != null){
                        instructorTypeDescription = ta.getInstructorType().getDescription();
                    }
                }

                BigDecimal instructorCostValue = instructorCost == null ? null : instructorCost.getCost();

                List<Object> cellValues = Arrays.asList(
                        year,
                        budgetScenarioExcelView.getWorkgroup().getName(),
                        instructorName,
                        instructorTypeDescription,
                        instructorCostValue);
                instructorSalariesSheet = ExcelHelper.writeRowToSheet(instructorSalariesSheet, cellValues);
            }

            // Create Instructor Category Costs sheet
            instructorCategoryCostSheet = ExcelHelper.writeRowToSheet(
                    instructorCategoryCostSheet,
                    Arrays.asList(
                            year,
                            budgetScenarioExcelView.getWorkgroup().getName(),
                            "TA",
                            baseTaCost
                    )
            );
            instructorCategoryCostSheet = ExcelHelper.writeRowToSheet(
                    instructorCategoryCostSheet,
                    Arrays.asList(
                            year,
                            budgetScenarioExcelView.getWorkgroup().getName(),
                            "Reader",
                            baseReaderCost
                    )
            );
            HashMap<String, Float> instructorTypeCostMap = new HashMap<>();
            for(InstructorType instructorType : budgetScenarioExcelView.getInstructorTypes()){
                instructorTypeCostMap.put(
                        instructorType.getDescription(),
                        null
                );
            }
            for(InstructorTypeCost instructorTypeCost : budgetScenarioExcelView.getInstructorTypeCosts()){
                if (instructorTypeCost.getCost() != null) {
                    instructorTypeCostMap.put(
                        instructorTypeCost.getInstructorType().getDescription(),
                        instructorTypeCost.getCost()
                    );
                }
            }

            List<String> instructorCategoryTypes = new ArrayList<>(instructorTypeCostMap.keySet());
            Collections.sort(instructorCategoryTypes);
            for(String instructorCategory : instructorCategoryTypes){
                instructorCategoryCostSheet = ExcelHelper.writeRowToSheet(
                        instructorCategoryCostSheet,
                        Arrays.asList(
                                year,
                                budgetScenarioExcelView.getWorkgroup().getName(),
                                instructorCategory,
                                instructorTypeCostMap.get(instructorCategory)
                        )
                );
            }

        }

        // Expand columns to length of largest value
        workbook = ExcelHelper.expandHeaders(workbook);
        workbook = ExcelHelper.ignoreErrors(workbook, Arrays.asList(IgnoredErrorType.NUMBER_STORED_AS_TEXT));

        // Override expanding the notes column on the funds tab
        // This is because it can be quite large
        fundsSheet.setColumnWidth(4, 256 * Math.min(MAX_COLUMN_CHARACTERS, minimumNoteColumnWidth));
    }

    private Sheet writeSummaryTerms(Sheet sheet, BudgetScenarioExcelView budgetScenarioExcelView) {
        List<String> summaryColumns = budgetScenarioExcelView.getTermCodes();
        summaryColumns.add("combined");

        List<String> rows = Arrays.asList(
            "TA Count",
            "TA Cost",
            "Reader Count",
            "Reader Cost",
            "Support Cost",
            "",
            "Associate Instructor",
            "Continuing Lecturer",
            "Continuing Lecturer - Augmentation",
            "Emeriti - Recalled",
            "Instructor",
            "Ladder Faculty",
            "Lecturer SOE",
            "New Faculty Hire",
            "Unit 18 Pre-Six Lecturer",
            "Visiting Professor",
            "Unassigned",
            "Replacement Cost",
            "Other Cost",
            "",
            "Total Teaching Costs",
            "Funds Cost",
            "Balance",
            "",
            "Units Offered",
            "Enrollment",
            "Student Credit Hours (Undergrad)",
            "Student Credit Hours (Graduate)",
            "Student Credit Hours",
            "",
            "Lower Div Offerings",
            "Upper Div Offerings",
            "Graduate Offerings",
            "Total Offerings",
            ""
        );

        for(String row : rows){
            if(row == ""){
                List<Object> emptyCells = new ArrayList<>();
                for(int i =0; i <= sheet.getRow(0).getLastCellNum(); i++){
                    emptyCells.add("");
                }
                sheet = ExcelHelper.writeRowToSheet(
                        sheet,
                        emptyCells
                );
            } else{
                sheet = ExcelHelper.writeRowToSheet(
                        sheet,
                        summaryRowData(row, budgetScenarioExcelView, summaryColumns)
                );
            }
        }
        return sheet;
    }

    private List<Object> summaryRowData(String field, BudgetScenarioExcelView budgetScenarioExcelView, List<String> termCodes) {
        List<Object> data = new ArrayList<>();

        data.add(yearToAcademicYear(budgetScenarioExcelView.getBudget().getSchedule().getYear()));
        data.add(budgetScenarioExcelView.getWorkgroup().getName());
        data.add(budgetScenarioExcelView.budgetScenario.getDisplayName());
        data.add(field);

        for(String termCode: termCodes){
            switch(field){
                case "TA Count":
                    data.add(budgetScenarioExcelView.termTotals.get(termCode).get(TA_COUNT));
                    break;
                case "TA Cost":
                    data.add(budgetScenarioExcelView.termTotals.get(termCode).get(TA_COST));
                    break;
                case "Reader Count":
                    data.add(budgetScenarioExcelView.termTotals.get(termCode).get(READER_COUNT));
                    break;
                case "Reader Cost":
                    data.add(budgetScenarioExcelView.termTotals.get(termCode).get(READER_COST));
                    break;
                case "Support Cost":
                    data.add(budgetScenarioExcelView.termTotals.get(termCode).get(TA_COST).add(budgetScenarioExcelView.termTotals.get(termCode).get(READER_COST)));
                    break;
                case "Associate Instructor":
                    data.add(budgetScenarioExcelView.termTotals.get(termCode).get(ASSOCIATE_INSTRUCTOR_COST));
                    break;
                case "Continuing Lecturer":
                    data.add(budgetScenarioExcelView.termTotals.get(termCode).get(CONTINUING_LECTURER_COST));
                    break;
                case "Continuing Lecturer - Augmentation":
                    data.add(budgetScenarioExcelView.termTotals.get(termCode).get(CONTINUING_LECTURER_AUGMENTATION_COST));
                    break;
                case "Emeriti - Recalled":
                    data.add(budgetScenarioExcelView.termTotals.get(termCode).get(EMERITI_COST));
                    break;
                case "Instructor":
                    data.add(budgetScenarioExcelView.termTotals.get(termCode).get(INSTRUCTOR_COST));
                    break;
                case "Ladder Faculty":
                    data.add(budgetScenarioExcelView.termTotals.get(termCode).get(LADDER_FACULTY_COST));
                    break;
                case "Lecturer SOE":
                    data.add(budgetScenarioExcelView.termTotals.get(termCode).get(LECTURER_SOE_COST));
                    break;
                case "New Faculty Hire":
                    data.add(budgetScenarioExcelView.termTotals.get(termCode).get(NEW_FACULTY_HIRE_COST));
                    break;
                case "Unit 18 Pre-Six Lecturer":
                    data.add(budgetScenarioExcelView.termTotals.get(termCode).get(UNIT18_LECTURER_COST));
                    break;
                case "Visiting Professor":
                    data.add(budgetScenarioExcelView.termTotals.get(termCode).get(VISITING_PROFESSOR_COST));
                    break;
                case "Unassigned":
                    data.add(budgetScenarioExcelView.termTotals.get(termCode).get(UNASSIGNED_COST));
                    break;
                case "Replacement Cost":
                    data.add(budgetScenarioExcelView.termTotals.get(termCode).get(REPLACEMENT_COST));
                    break;
                case "Total Teaching Costs":
                    data.add(budgetScenarioExcelView.termTotals.get(termCode).get(TOTAL_TEACHING_COST));
                    break;
                case "Funds Cost":
                    data.add(budgetScenarioExcelView.termTotals.get(termCode).get(TOTAL_FUNDS).compareTo(BigDecimal.ZERO) == 0 ? "" : budgetScenarioExcelView.termTotals.get(termCode).get(TOTAL_FUNDS));
                    break;
                case "Other Cost":
                    data.add(budgetScenarioExcelView.termTotals.get(termCode).get(TOTAL_EXPENSES).compareTo(BigDecimal.ZERO) == 0 ? "" : budgetScenarioExcelView.termTotals.get(termCode).get(TOTAL_EXPENSES));
                    break;
                case "Balance":
                    data.add(budgetScenarioExcelView.termTotals.get(termCode).get(TOTAL_BALANCE).compareTo(BigDecimal.ZERO) == 0 ? "" : budgetScenarioExcelView.termTotals.get(termCode).get(TOTAL_BALANCE));
                    break;
                case "Units Offered":
                    data.add(budgetScenarioExcelView.termTotals.get(termCode).get(UNITS_OFFERED));
                    break;
                case "Enrollment":
                    data.add(budgetScenarioExcelView.termTotals.get(termCode).get(TOTAL_SEATS));
                    break;
                case "Student Credit Hours (Undergrad)":
                    data.add(budgetScenarioExcelView.termTotals.get(termCode).get(SCH_UNDERGRAD));
                    break;
                case "Student Credit Hours (Graduate)":
                    data.add(budgetScenarioExcelView.termTotals.get(termCode).get(SCH_GRAD));
                    break;
                case "Student Credit Hours":
                    data.add(budgetScenarioExcelView.termTotals.get(termCode).get(SCH_UNDERGRAD).add(budgetScenarioExcelView.termTotals.get(termCode).get(SCH_GRAD)));
                    break;
                case "Lower Div Offerings":
                    data.add(budgetScenarioExcelView.termTotals.get(termCode).get(LOWER_DIV_OFFERINGS));
                    break;
                case "Upper Div Offerings":
                    data.add(budgetScenarioExcelView.termTotals.get(termCode).get(UPPER_DIV_OFFERINGS));
                    break;
                case "Graduate Offerings":
                    data.add(budgetScenarioExcelView.termTotals.get(termCode).get(GRAD_OFFERINGS));
                    break;
                case "Total Offerings":
                    data.add(budgetScenarioExcelView.termTotals.get(termCode).get(LOWER_DIV_OFFERINGS).add(budgetScenarioExcelView.termTotals.get(termCode).get(UPPER_DIV_OFFERINGS)).add(budgetScenarioExcelView.termTotals.get(termCode).get(GRAD_OFFERINGS)));
                    break;
            }
        }

        return data;
    }

    private String yearToAcademicYear(long year) {
        return year + "-" + String.valueOf(year + 1).substring(2,4);
    }

    private String getCourseType(SectionGroupCost sectionGroupCost) {
        int courseNumbers = Integer.parseInt(sectionGroupCost.getCourseNumber().replaceAll("[^\\d.]", ""));

        if (courseNumbers < 100) {
            return "Lower";
        } else if (courseNumbers >= 200) {
            return "Grad";
        } else {
            return "Upper";
        }
    }

    private String getCommonGood(SectionGroupCost sectionGroupCost) {
        return CommonGood.COURSES.contains(sectionGroupCost.getSubjectCode() + sectionGroupCost.getCourseNumber())
            ? "CG"
            : "Other";
    }


    private String getWritingExperience(SectionGroupCost sectionGroupCost) {
        return WritingExperience.COURSES.contains(sectionGroupCost.getSubjectCode() + sectionGroupCost.getCourseNumber())
            ? "WE"
            : "";
    }
}
