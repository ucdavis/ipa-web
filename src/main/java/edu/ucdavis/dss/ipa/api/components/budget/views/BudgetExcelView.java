package edu.ucdavis.dss.ipa.api.components.budget.views;

import static edu.ucdavis.dss.ipa.entities.enums.BudgetSummary.*;

import edu.ucdavis.dss.ipa.api.helpers.SpringContext;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.services.InstructorCostService;
import edu.ucdavis.dss.ipa.services.InstructorTypeCostService;
import edu.ucdavis.dss.ipa.services.UserService;
import edu.ucdavis.dss.ipa.utilities.ExcelHelper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.IgnoredErrorType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.security.access.method.P;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

public class BudgetExcelView extends AbstractXlsxView {
    private List<BudgetScenarioExcelView> budgetScenarioExcelViews;
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
        budgetSummarySheet = ExcelHelper.setSheetHeader(budgetSummarySheet, Arrays.asList("Department", "Scenario Name", "", "Fall Quarter", "Winter Quarter", "Spring Quarter", "Total"));

        Sheet scheduleCostSheet = workbook.createSheet("Schedule Cost");

        scheduleCostSheet = ExcelHelper.setSheetHeader(scheduleCostSheet, Arrays.asList(
           "Department",
           "Scenario Name",
           "Term",
           "Subject Code",
           "Course Number",
           "Title",
           "Units High",
           "Units Low",
           "Sequence",
           "Enrollment",
           "Current Enrollment",
           "Sections",
           "Instructor",
           "Regular Instructor",
           "Reason",
           "TAs",
           "Readers",
           "TA Cost",
           "Reader Cost",
           "Support Cost",
           "Instructor Cost",
           "Total Cost"
        ));

        Sheet fundsSheet = workbook.createSheet("Funds");
        fundsSheet = ExcelHelper.setSheetHeader(fundsSheet, Arrays.asList("Department", "Scenario Name", "Type", "Description", "Notes", "Comments", "Amount"));

        Sheet instructorSalariesSheet = workbook.createSheet("Instructor Salaries");
        instructorSalariesSheet = ExcelHelper.setSheetHeader(instructorSalariesSheet, Arrays.asList("Department", "Instructor", "Type", "Cost"));

        Sheet instructorCategoryCostSheet = workbook.createSheet("Instructor Category Cost");
        instructorCategoryCostSheet = ExcelHelper.setSheetHeader(instructorCategoryCostSheet, Arrays.asList("Department", "Type", "Cost"));


        for (BudgetScenarioExcelView budgetScenarioExcelView : budgetScenarioExcelViews) {
            Long scenarioId = budgetScenarioExcelView.getBudgetScenario().getId();

            // Create Schedule Cost sheet
            for(SectionGroupCost sectionGroupCost : budgetScenarioExcelView.getSectionGroupCosts().stream().sorted(Comparator.comparing(SectionGroupCost::getTermCode).thenComparing(SectionGroupCost::getSubjectCode).thenComparing(SectionGroupCost::getCourseNumber)).collect(Collectors.toList()) ){
                Float taCost = (sectionGroupCost.getTaCount() == null ? 0.0F : sectionGroupCost.getTaCount()) * budgetScenarioExcelView.getBudget().getTaCost();
                Float readerCost = (sectionGroupCost.getReaderCount() == null ? 0.0F: sectionGroupCost.getReaderCount() ) * budgetScenarioExcelView.getBudget().getReaderCost();
                Float supportCost = taCost + readerCost;
                Float sectionCost = sectionGroupCost.getCost() == null ? 0.0F : sectionGroupCost.getCost().floatValue();
                Long currentEnrollment = null;
                if(budgetScenarioExcelView.getCensusMap().get(sectionGroupCost.getTermCode()) != null){
                    if(budgetScenarioExcelView.getCensusMap().get(sectionGroupCost.getTermCode()).get(sectionGroupCost.getSubjectCode() + sectionGroupCost.getCourseNumber()) != null){
                        currentEnrollment = budgetScenarioExcelView.getCensusMap().get(sectionGroupCost.getTermCode()).get(sectionGroupCost.getSubjectCode() + sectionGroupCost.getCourseNumber()).get(sectionGroupCost.getSequencePattern());
                    }
                }

                scheduleCostSheet = ExcelHelper.writeRowToSheet(
                        scheduleCostSheet,
                        Arrays.asList(
                                budgetScenarioExcelView.getWorkgroup().getName(),
                                budgetScenarioExcelView.getBudgetScenario().getName(),
                                Term.getRegistrarName(sectionGroupCost.getTermCode()),
                                sectionGroupCost.getSubjectCode(),
                                sectionGroupCost.getCourseNumber(),
                                sectionGroupCost.getTitle(),
                                sectionGroupCost.getUnitsHigh(),
                                sectionGroupCost.getUnitsLow(),
                                sectionGroupCost.getSequencePattern(),
                                sectionGroupCost.getEnrollment(),
                                currentEnrollment,
                                sectionGroupCost.getSectionCount(),
                                (sectionGroupCost.getInstructor() == null ? "" : sectionGroupCost.getInstructor().getFullName()),
                                (sectionGroupCost.getOriginalInstructor() == null ? "" : sectionGroupCost.getOriginalInstructor().getFullName()),
                                sectionGroupCost.getReason(),
                                sectionGroupCost.getTaCount(),
                                sectionGroupCost.getReaderCount(),
                                taCost,
                                readerCost,
                                supportCost,
                                sectionCost,
                                supportCost + sectionCost
                        )
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
                List<Object> cellValues = Arrays.asList(
                        budgetScenarioExcelView.getWorkgroup().getName(),
                        budgetScenarioExcelView.getBudgetScenario().getName(),
                        lineItem.getLineItemCategory().getDescription(),
                        lineItem.getDescription(),
                        lineItem.getNotes(),
                        String.join("\n\r\n\r", comments),
                        lineItem.getAmount());
                fundsSheet = ExcelHelper.writeRowToSheet(fundsSheet, cellValues);
            }

            // Creating Instructor Salaries sheet
            for(Instructor instructor : budgetScenarioExcelView.getActiveInstructors()){
                // Get data into correct shape
                InstructorCost instructorCost = budgetScenarioExcelView.getInstructorCosts().stream().filter(ic -> ic.getInstructor().getId() == instructor.getId()).findFirst().orElse(null);

                String instructorName = instructor.getLastName() + " " + instructor.getFirstName();

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
                            budgetScenarioExcelView.getWorkgroup().getName(),
                            "TA",
                            budgetScenarioExcelView.getBudget().getTaCost()
                    )
            );
            instructorCategoryCostSheet = ExcelHelper.writeRowToSheet(
                    instructorCategoryCostSheet,
                    Arrays.asList(
                            budgetScenarioExcelView.getWorkgroup().getName(),
                            "Reader",
                            budgetScenarioExcelView.getBudget().getReaderCost()
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

        // Override expanding the comments and notes columns on the funds tab
        // This is because they can both be quite large
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
            "Emeriti - Recalled",
            "Instructor",
            "Ladder Faculty",
            "Lecturer SOE",
            "Unit 18 Pre-Six Lecturer",
            "Visiting Professor",
            "Unassigned",
            "Replacement Cost",
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

        data.add(budgetScenarioExcelView.getWorkgroup().getName());
        data.add(budgetScenarioExcelView.budgetScenario.getName());
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
}
