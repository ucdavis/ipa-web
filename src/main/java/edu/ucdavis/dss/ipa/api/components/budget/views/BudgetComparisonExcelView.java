package edu.ucdavis.dss.ipa.api.components.budget.views;

import static edu.ucdavis.dss.ipa.entities.enums.BudgetSummary.*;
import static edu.ucdavis.dss.ipa.entities.enums.FundType.ADDITIONAL_DEANS_OFFICE;
import static edu.ucdavis.dss.ipa.entities.enums.FundType.CLASS_CANCELLED;
import static edu.ucdavis.dss.ipa.entities.enums.FundType.DEANS_OFFICE;
import static edu.ucdavis.dss.ipa.entities.enums.FundType.EXTERNAL_BUYOUT;
import static edu.ucdavis.dss.ipa.entities.enums.FundType.INTERNAL_BUYOUT;
import static edu.ucdavis.dss.ipa.entities.enums.FundType.NOT_GENT;
import static edu.ucdavis.dss.ipa.entities.enums.FundType.OTHER;
import static edu.ucdavis.dss.ipa.entities.enums.FundType.RANGE_ADJUSTMENT;
import static edu.ucdavis.dss.ipa.entities.enums.FundType.TOTAL;
import static edu.ucdavis.dss.ipa.entities.enums.FundType.WORK_LIFE;
import static org.apache.poi.ss.util.WorkbookUtil.createSafeSheetName;

import edu.ucdavis.dss.ipa.entities.LineItem;
import edu.ucdavis.dss.ipa.entities.enums.BudgetSummary;
import edu.ucdavis.dss.ipa.entities.enums.FundType;
import edu.ucdavis.dss.ipa.utilities.ExcelHelper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.IgnoredErrorType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

public class BudgetComparisonExcelView extends AbstractXlsxView {
    private List<List<BudgetScenarioExcelView>> budgetComparisonList;

    private final DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public BudgetComparisonExcelView(List<List<BudgetScenarioExcelView>> budgetComparisonList) {
        this.budgetComparisonList = budgetComparisonList;
    }

    @Override
    protected void buildExcelDocument(Map<String, Object> model, Workbook workbook,
                                      HttpServletRequest request, HttpServletResponse response)
        throws Exception {

        for (List<BudgetScenarioExcelView> budgetScenarioExcelViewPair : budgetComparisonList) {
            Sheet report = workbook.createSheet(createSafeSheetName(budgetScenarioExcelViewPair.get(0).getWorkgroup()
                .getCode()));

            BudgetScenarioExcelView previousYear = budgetScenarioExcelViewPair.get(0);
            BudgetScenarioExcelView currentYear = budgetScenarioExcelViewPair.get(1);

            String previousScenarioName = previousYear.getBudgetScenario().getDisplayName();
            String currentScenarioName = currentYear.getBudgetScenario().getDisplayName();

            ExcelHelper.setSheetHeader(report, Arrays
                .asList(previousScenarioName, "", "", "", "",
                    currentScenarioName, "", "", "", "", "", "", "", ""));
            ExcelHelper.writeRowToSheet(report, Arrays
                .asList(yearToAcademicYear(previousYear.getBudget().getSchedule().getYear()),
                    "", "", "", "",
                    yearToAcademicYear(currentYear.getBudget().getSchedule().getYear()),
                    "", "", "", "",
                    "Changes"));
            ExcelHelper.writeRowToSheet(report, Arrays.asList(""));

            ExcelHelper.writeRowToSheet(report, Arrays
                .asList("Categories", "Total Cost", "Assignments"/*"# Courses"*/, "", "",
                    "Categories", "Total Cost", "Assignments"/*"# Courses"*/, "", "",
                    "Cost", "Assignments"/*"# Courses"*/, "% Cost", "% Courses"));

            // Instructor Costs
            Map<BudgetSummary, BigDecimal> previousTotals = previousYear.getTermTotals().get("combined");
            Map<BudgetSummary, BigDecimal> currentTotals = currentYear.getTermTotals().get("combined");

            ExcelHelper.writeRowToSheet(report, Arrays.asList("Ladder Faculty", previousTotals.get(LADDER_FACULTY_COST), previousTotals.get(LADDER_FACULTY_COUNT), "", "", "Ladder Faculty", currentTotals.get(LADDER_FACULTY_COST), currentTotals.get(LADDER_FACULTY_COUNT), "", "", currentTotals.get(LADDER_FACULTY_COST).subtract(previousTotals.get(LADDER_FACULTY_COST)), currentTotals.get(LADDER_FACULTY_COUNT).subtract(previousTotals.get(LADDER_FACULTY_COUNT)), getPercentChange(previousTotals.get(LADDER_FACULTY_COST), currentTotals.get(LADDER_FACULTY_COST)), getPercentChange(previousTotals.get(LADDER_FACULTY_COUNT), currentTotals.get(LADDER_FACULTY_COUNT))));
            ExcelHelper.writeRowToSheet(report, Arrays.asList("New Faculty Hire", previousTotals.get(NEW_FACULTY_HIRE_COST), previousTotals.get(NEW_FACULTY_HIRE_COUNT), "", "", "New Faculty Hire", currentTotals.get(NEW_FACULTY_HIRE_COST), currentTotals.get(NEW_FACULTY_HIRE_COUNT), "", "", currentTotals.get(NEW_FACULTY_HIRE_COST).subtract(previousTotals.get(NEW_FACULTY_HIRE_COST)), currentTotals.get(NEW_FACULTY_HIRE_COUNT).subtract(previousTotals.get(NEW_FACULTY_HIRE_COUNT)), getPercentChange(previousTotals.get(NEW_FACULTY_HIRE_COST), currentTotals.get(NEW_FACULTY_HIRE_COST)), getPercentChange(previousTotals.get(NEW_FACULTY_HIRE_COUNT), currentTotals.get(NEW_FACULTY_HIRE_COUNT))));
            ExcelHelper.writeRowToSheet(report, Arrays.asList("Lecturer SOE", previousTotals.get(LECTURER_SOE_COST), previousTotals.get(LECTURER_SOE_COUNT), "", "", "Lecturer SOE", currentTotals.get(LECTURER_SOE_COST), currentTotals.get(LECTURER_SOE_COUNT), "", "", currentTotals.get(LECTURER_SOE_COST).subtract(previousTotals.get(LECTURER_SOE_COST)), currentTotals.get(LECTURER_SOE_COUNT).subtract(previousTotals.get(LECTURER_SOE_COUNT)), getPercentChange(previousTotals.get(LECTURER_SOE_COST), currentTotals.get(LECTURER_SOE_COST)), getPercentChange(previousTotals.get(LECTURER_SOE_COUNT), currentTotals.get(LECTURER_SOE_COUNT))));
            ExcelHelper.writeRowToSheet(report, Arrays.asList("Continuing Lecturer", previousTotals.get(CONTINUING_LECTURER_COST), previousTotals.get(CONTINUING_LECTURER_COUNT), "", "", "Continuing Lecturer", currentTotals.get(CONTINUING_LECTURER_COST), currentTotals.get(CONTINUING_LECTURER_COUNT), "", "", currentTotals.get(CONTINUING_LECTURER_COST).subtract(previousTotals.get(CONTINUING_LECTURER_COST)), currentTotals.get(CONTINUING_LECTURER_COUNT).subtract(previousTotals.get(CONTINUING_LECTURER_COUNT)), getPercentChange(previousTotals.get(CONTINUING_LECTURER_COST), currentTotals.get(CONTINUING_LECTURER_COST)), getPercentChange(previousTotals.get(CONTINUING_LECTURER_COUNT), currentTotals.get(CONTINUING_LECTURER_COUNT))));
            ExcelHelper.writeRowToSheet(report, Arrays.asList("Emeriti - Recalled", previousTotals.get(EMERITI_COST), previousTotals.get(EMERITI_COUNT), "", "", "Emeriti - Recalled", currentTotals.get(EMERITI_COST), currentTotals.get(EMERITI_COUNT), "", "", currentTotals.get(EMERITI_COST).subtract(previousTotals.get(EMERITI_COST)), currentTotals.get(EMERITI_COUNT).subtract(previousTotals.get(EMERITI_COUNT)), getPercentChange(previousTotals.get(EMERITI_COST), currentTotals.get(EMERITI_COST)), getPercentChange(previousTotals.get(EMERITI_COUNT), currentTotals.get(EMERITI_COUNT))));
            ExcelHelper.writeRowToSheet(report, Arrays.asList("Visiting Professor", previousTotals.get(VISITING_PROFESSOR_COST), previousTotals.get(VISITING_PROFESSOR_COUNT),  "", "", "Visiting Professor", currentTotals.get(VISITING_PROFESSOR_COST), currentTotals.get(VISITING_PROFESSOR_COUNT), "", "", currentTotals.get(VISITING_PROFESSOR_COST).subtract(previousTotals.get(VISITING_PROFESSOR_COST)), currentTotals.get(VISITING_PROFESSOR_COUNT).subtract(previousTotals.get(VISITING_PROFESSOR_COUNT)), getPercentChange(previousTotals.get(VISITING_PROFESSOR_COST), currentTotals.get(VISITING_PROFESSOR_COST)), getPercentChange(previousTotals.get(VISITING_PROFESSOR_COUNT), currentTotals.get(VISITING_PROFESSOR_COUNT))));
            ExcelHelper.writeRowToSheet(report, Arrays.asList("Unit 18 Pre-Six Lecturer", previousTotals.get(UNIT18_LECTURER_COST), previousTotals.get(UNIT18_LECTURER_COUNT), "", "", "Unit 18 Pre-Six Lecturer", currentTotals.get(UNIT18_LECTURER_COST), currentTotals.get(UNIT18_LECTURER_COUNT), "", "", currentTotals.get(UNIT18_LECTURER_COST).subtract(previousTotals.get(UNIT18_LECTURER_COST)), currentTotals.get(UNIT18_LECTURER_COUNT).subtract(previousTotals.get(UNIT18_LECTURER_COUNT)), getPercentChange(previousTotals.get(UNIT18_LECTURER_COST), currentTotals.get(UNIT18_LECTURER_COST)), getPercentChange(previousTotals.get(UNIT18_LECTURER_COUNT), currentTotals.get(UNIT18_LECTURER_COUNT))));
            ExcelHelper.writeRowToSheet(report, Arrays.asList("Continuing Lecturer - Augmentation", previousTotals.get(CONTINUING_LECTURER_AUGMENTATION_COST), previousTotals.get(CONTINUING_LECTURER_AUGMENTATION_COUNT), "", "", "Continuing Lecturer - Augmentation", currentTotals.get(CONTINUING_LECTURER_AUGMENTATION_COST), currentTotals.get(CONTINUING_LECTURER_AUGMENTATION_COUNT), "", "", currentTotals.get(CONTINUING_LECTURER_AUGMENTATION_COST).subtract(previousTotals.get(CONTINUING_LECTURER_AUGMENTATION_COST)), currentTotals.get(CONTINUING_LECTURER_AUGMENTATION_COUNT).subtract(previousTotals.get(CONTINUING_LECTURER_AUGMENTATION_COUNT)), getPercentChange(previousTotals.get(CONTINUING_LECTURER_AUGMENTATION_COST), currentTotals.get(CONTINUING_LECTURER_AUGMENTATION_COST)), getPercentChange(previousTotals.get(CONTINUING_LECTURER_AUGMENTATION_COUNT), currentTotals.get(CONTINUING_LECTURER_AUGMENTATION_COUNT))));
            ExcelHelper.writeRowToSheet(report, Arrays.asList("Associate Instructor", previousTotals.get(ASSOCIATE_INSTRUCTOR_COST), previousTotals.get(ASSOCIATE_INSTRUCTOR_COUNT), "", "", "Associate Instructor", currentTotals.get(ASSOCIATE_INSTRUCTOR_COST), currentTotals.get(ASSOCIATE_INSTRUCTOR_COUNT), "", "", currentTotals.get(ASSOCIATE_INSTRUCTOR_COST).subtract(previousTotals.get(ASSOCIATE_INSTRUCTOR_COST)), currentTotals.get(ASSOCIATE_INSTRUCTOR_COUNT).subtract(previousTotals.get(ASSOCIATE_INSTRUCTOR_COUNT)), getPercentChange(previousTotals.get(ASSOCIATE_INSTRUCTOR_COST), currentTotals.get(ASSOCIATE_INSTRUCTOR_COST)), getPercentChange(previousTotals.get(ASSOCIATE_INSTRUCTOR_COUNT), currentTotals.get(ASSOCIATE_INSTRUCTOR_COUNT))));
            ExcelHelper.writeRowToSheet(report, Arrays.asList("Instructor", previousTotals.get(INSTRUCTOR_COST), previousTotals.get(INSTRUCTOR_COUNT), "", "", "Instructor", currentTotals.get(INSTRUCTOR_COST), currentTotals.get(INSTRUCTOR_COUNT), "", "", currentTotals.get(INSTRUCTOR_COST).subtract(previousTotals.get(INSTRUCTOR_COST)), currentTotals.get(INSTRUCTOR_COUNT).subtract(previousTotals.get(INSTRUCTOR_COUNT)), getPercentChange(previousTotals.get(INSTRUCTOR_COST), currentTotals.get(INSTRUCTOR_COST)), getPercentChange(previousTotals.get(INSTRUCTOR_COUNT), currentTotals.get(INSTRUCTOR_COUNT))));
            ExcelHelper.writeRowToSheet(report, Arrays.asList("Unassigned", previousTotals.get(UNASSIGNED_COST), previousTotals.get(UNASSIGNED_COUNT), "", "", "Unassigned", currentTotals.get(UNASSIGNED_COST), currentTotals.get(UNASSIGNED_COUNT), "", "", currentTotals.get(UNASSIGNED_COST).subtract(previousTotals.get(UNASSIGNED_COST)), currentTotals.get(UNASSIGNED_COUNT).subtract(previousTotals.get(UNASSIGNED_COUNT)), getPercentChange(previousTotals.get(UNASSIGNED_COST), currentTotals.get(UNASSIGNED_COST)), getPercentChange(previousTotals.get(UNASSIGNED_COUNT), currentTotals.get(UNASSIGNED_COUNT))));

            BigDecimal previousAssignedCount = previousTotals.get(COURSE_COUNT);
            BigDecimal currentAssignedCount = currentTotals.get(COURSE_COUNT);
            ExcelHelper.writeRowToSheet(report, Arrays.asList("", previousTotals.get(REPLACEMENT_COST), previousAssignedCount, "", "", "", currentTotals.get(REPLACEMENT_COST), currentAssignedCount, "", "", currentTotals.get(REPLACEMENT_COST).subtract(previousTotals.get(REPLACEMENT_COST)), currentAssignedCount.subtract(previousAssignedCount), getPercentChange(previousTotals.get(REPLACEMENT_COST), currentTotals.get(REPLACEMENT_COST)), getPercentChange(previousAssignedCount, currentAssignedCount)));
            ExcelHelper.writeRowToSheet(report, Arrays.asList(""));

            ExcelHelper.writeRowToSheet(report, Arrays.asList("", "Total Cost", "# Courses", "", "", "", "Total Cost", "# Courses", "", "", "Cost", "Count", "% Cost", "% Count"));
            ExcelHelper.writeRowToSheet(report, Arrays.asList("TAs", previousTotals.get(TA_COST), previousTotals.get(TA_COUNT), "", "", "TAs", currentTotals.get(TA_COST), currentTotals.get(TA_COUNT), "", "", currentTotals.get(TA_COST).subtract(previousTotals.get(TA_COST)), currentTotals.get(TA_COUNT).subtract(previousTotals.get(TA_COUNT)), getPercentChange(previousTotals.get(TA_COST), currentTotals.get(TA_COST)), getPercentChange(previousTotals.get(TA_COUNT), currentTotals.get(TA_COUNT))));
            ExcelHelper.writeRowToSheet(report, Arrays.asList("Readers", previousTotals.get(READER_COST), previousTotals.get(READER_COUNT), "", "", "Readers", currentTotals.get(READER_COST), currentTotals.get(READER_COUNT), "", "", currentTotals.get(READER_COST).subtract(previousTotals.get(READER_COST)), currentTotals.get(READER_COUNT).subtract(previousTotals.get(READER_COUNT)), getPercentChange(previousTotals.get(READER_COST), currentTotals.get(READER_COST)), getPercentChange(previousTotals.get(READER_COUNT), currentTotals.get(READER_COUNT))));


            BigDecimal previousSupportCost = previousTotals.get(TA_COST).add(previousTotals.get(READER_COST));
            BigDecimal previousSupportCount = previousTotals.get(TA_COUNT).add(previousTotals.get(READER_COUNT));
            BigDecimal currentSupportCost = currentTotals.get(TA_COST).add(currentTotals.get(READER_COST));
            BigDecimal currentSupportCount = currentTotals.get(TA_COUNT).add(currentTotals.get(READER_COUNT));
            ExcelHelper.writeRowToSheet(report, Arrays.asList("Total", previousSupportCost, previousSupportCount, "", "", "Total", currentSupportCost, currentSupportCount, "", "", currentSupportCost.subtract(previousSupportCost), currentSupportCount.subtract(previousSupportCount), getPercentChange(previousSupportCost, currentSupportCost), getPercentChange(previousSupportCount, currentSupportCount)));
            ExcelHelper.writeRowToSheet(report, Arrays.asList(""));

            // Other Costs
            ExcelHelper.writeRowToSheet(report, Arrays
                    .asList("", "Amount", "", "", "",
                            "", "Amount", "", "", "",
                            "Cost", "% Change", "", ""));

            ExcelHelper.writeRowToSheet(report, Arrays.asList("Other Costs", previousTotals.get(TOTAL_EXPENSES), "", "", "", "Other Costs", currentTotals.get(TOTAL_EXPENSES), "", "", "", currentTotals.get(TOTAL_EXPENSES).subtract(previousTotals.get(TOTAL_EXPENSES)), getPercentChange(previousTotals.get(TOTAL_EXPENSES), currentTotals.get(TOTAL_EXPENSES))));

            ExcelHelper.writeRowToSheet(report, Arrays.asList(""));
            ExcelHelper.writeRowToSheet(report, Arrays.asList("Total Teaching Costs", getTotalTeachingCost(previousTotals), "", "", "", "Total Teaching Costs", getTotalTeachingCost(currentTotals), "", "", "", getTotalTeachingCost(currentTotals).subtract(getTotalTeachingCost(previousTotals))));
            ExcelHelper.writeRowToSheet(report, Arrays.asList(""));

            // Funds section
            Map<FundType, BigDecimal> previousFunds = generateFundTotals(previousYear.getLineItems());
            Map<FundType, BigDecimal> currentFunds = generateFundTotals(currentYear.getLineItems());

            ExcelHelper.writeRowToSheet(report, Arrays.asList("Funding", "Amount", "", "", "", "Funding", "Amount", "", "", "", "Funding", "Amount", "% Change"));
            ExcelHelper.writeRowToSheet(report, Arrays.asList(DEANS_OFFICE.getDescription(), getFund(previousFunds, DEANS_OFFICE), "", "", "", DEANS_OFFICE.getDescription(), getFund(currentFunds, DEANS_OFFICE), "", "", "", DEANS_OFFICE.getDescription(), getFund(currentFunds, DEANS_OFFICE).subtract(getFund(previousFunds, DEANS_OFFICE)), getPercentChange(getFund(previousFunds, DEANS_OFFICE), getFund(currentFunds, DEANS_OFFICE))));
            ExcelHelper.writeRowToSheet(report, Arrays.asList(INTERNAL_BUYOUT.getDescription(), getFund(previousFunds, INTERNAL_BUYOUT), "", "", "", INTERNAL_BUYOUT.getDescription(), getFund(currentFunds, INTERNAL_BUYOUT), "", "", "", INTERNAL_BUYOUT.getDescription(), getFund(currentFunds, INTERNAL_BUYOUT).subtract(getFund(previousFunds, INTERNAL_BUYOUT)), getPercentChange(getFund(previousFunds, INTERNAL_BUYOUT), getFund(currentFunds, INTERNAL_BUYOUT))));
            ExcelHelper.writeRowToSheet(report, Arrays.asList(CLASS_CANCELLED.getDescription(), getFund(previousFunds, CLASS_CANCELLED), "", "", "", CLASS_CANCELLED.getDescription(), getFund(currentFunds, CLASS_CANCELLED), "", "", "", CLASS_CANCELLED.getDescription(), getFund(currentFunds, CLASS_CANCELLED).subtract(getFund(previousFunds, CLASS_CANCELLED)), getPercentChange(getFund(previousFunds, CLASS_CANCELLED), getFund(currentFunds, CLASS_CANCELLED))));
            ExcelHelper.writeRowToSheet(report, Arrays.asList(RANGE_ADJUSTMENT.getDescription(), getFund(previousFunds, RANGE_ADJUSTMENT), "", "", "", RANGE_ADJUSTMENT.getDescription(), getFund(currentFunds, RANGE_ADJUSTMENT), "", "", "", RANGE_ADJUSTMENT.getDescription(), getFund(currentFunds, RANGE_ADJUSTMENT).subtract(getFund(previousFunds, RANGE_ADJUSTMENT)), getPercentChange(getFund(previousFunds, RANGE_ADJUSTMENT), getFund(currentFunds, RANGE_ADJUSTMENT))));
            ExcelHelper.writeRowToSheet(report, Arrays.asList(WORK_LIFE.getDescription(), getFund(previousFunds, WORK_LIFE), "", "", "", WORK_LIFE.getDescription(), getFund(currentFunds, WORK_LIFE), "", "", "", WORK_LIFE.getDescription(), getFund(currentFunds, WORK_LIFE).subtract(getFund(previousFunds, WORK_LIFE)), getPercentChange(getFund(previousFunds, WORK_LIFE), getFund(currentFunds, WORK_LIFE))));
            ExcelHelper.writeRowToSheet(report, Arrays.asList(OTHER.getDescription(), getFund(previousFunds, OTHER), "", "", "", OTHER.getDescription(), getFund(currentFunds, OTHER), "", "", "", OTHER.getDescription(), getFund(currentFunds, OTHER).subtract(getFund(previousFunds, OTHER)), getPercentChange(getFund(previousFunds, OTHER), getFund(currentFunds, OTHER))));
            ExcelHelper.writeRowToSheet(report, Arrays.asList(EXTERNAL_BUYOUT.getDescription(), getFund(previousFunds, EXTERNAL_BUYOUT), "", "", "", EXTERNAL_BUYOUT.getDescription(), getFund(currentFunds, EXTERNAL_BUYOUT), "", "", "", EXTERNAL_BUYOUT.getDescription(), getFund(currentFunds, EXTERNAL_BUYOUT).subtract(getFund(previousFunds, EXTERNAL_BUYOUT)), getPercentChange(getFund(previousFunds, EXTERNAL_BUYOUT), getFund(currentFunds, EXTERNAL_BUYOUT))));
            ExcelHelper.writeRowToSheet(report, Arrays.asList(ADDITIONAL_DEANS_OFFICE.getDescription(), getFund(previousFunds, ADDITIONAL_DEANS_OFFICE), "", "", "", ADDITIONAL_DEANS_OFFICE.getDescription(), getFund(currentFunds, ADDITIONAL_DEANS_OFFICE), "", "", "", ADDITIONAL_DEANS_OFFICE.getDescription(), getFund(currentFunds, ADDITIONAL_DEANS_OFFICE).subtract(getFund(previousFunds, ADDITIONAL_DEANS_OFFICE)), getPercentChange(getFund(previousFunds, ADDITIONAL_DEANS_OFFICE), getFund(currentFunds, ADDITIONAL_DEANS_OFFICE))));
            ExcelHelper.writeRowToSheet(report, Arrays.asList(NOT_GENT.getDescription(), getFund(previousFunds, NOT_GENT), "", "", "", NOT_GENT.getDescription(), getFund(currentFunds, NOT_GENT), "", "", "", NOT_GENT.getDescription(), getFund(currentFunds, NOT_GENT).subtract(getFund(previousFunds, NOT_GENT)), getPercentChange(getFund(previousFunds, NOT_GENT), getFund(currentFunds, NOT_GENT))));
            ExcelHelper.writeRowToSheet(report, Arrays.asList("", getFund(previousFunds, TOTAL), "", "", "", "", getFund(currentFunds, TOTAL), "", "", "", "", getFund(currentFunds, TOTAL).subtract(getFund(previousFunds, TOTAL))));
            ExcelHelper.writeRowToSheet(report, Arrays.asList(""));

            ExcelHelper.writeRowToSheet(report, Arrays.asList("Balance", previousFunds.get(TOTAL).subtract(getTotalTeachingCost(previousTotals)), "", "", "", "Balance", currentFunds.get(TOTAL).subtract(getTotalTeachingCost(currentTotals))));
            ExcelHelper.writeRowToSheet(report, Arrays.asList(""));

            // Course Offering
            ExcelHelper.writeRowToSheet(report, Arrays.asList(""));
            ExcelHelper.writeRowToSheet(report, Arrays.asList("", "# Lower Div", "# Upper Div", "# Grad", "Total", "", "# Lower Div", "# Upper Div", "# Grad", "Total", "", "# Lower Div", "# Upper Div", "# Grad", "Total"));
//            ExcelHelper.writeRowToSheet(report, Arrays.asList("", "", "", "", "Courses Offered", "", "", "", "Courses Offered"));
            ExcelHelper.writeRowToSheet(report, Arrays.asList("Courses Offered", previousTotals.get(LOWER_DIV_OFFERINGS), previousTotals.get(UPPER_DIV_OFFERINGS), previousTotals.get(GRAD_OFFERINGS), getTotalOfferings(previousTotals), "Courses Offered", currentTotals.get(LOWER_DIV_OFFERINGS), currentTotals.get(UPPER_DIV_OFFERINGS), currentTotals.get(GRAD_OFFERINGS), getTotalOfferings(currentTotals), "Courses Offered", currentTotals.get(LOWER_DIV_OFFERINGS).subtract(previousTotals.get(LOWER_DIV_OFFERINGS)), currentTotals.get(UPPER_DIV_OFFERINGS).subtract(previousTotals.get(UPPER_DIV_OFFERINGS)), currentTotals.get(GRAD_OFFERINGS).subtract(previousTotals.get(GRAD_OFFERINGS)), getTotalOfferings(currentTotals).subtract(getTotalOfferings(previousTotals))));
            ExcelHelper.writeRowToSheet(report, Arrays.asList("Total Costs", previousTotals.get(LOWER_DIV_COST), previousTotals.get(UPPER_DIV_COST), previousTotals.get(GRAD_COST), previousTotals.get(TOTAL_TEACHING_COST), "Total Costs", currentTotals.get(LOWER_DIV_COST), currentTotals.get(UPPER_DIV_COST), currentTotals.get(GRAD_COST), currentTotals.get(TOTAL_TEACHING_COST)));
            ExcelHelper.writeRowToSheet(report, Arrays.asList(""));

            // Seats Offered
            ExcelHelper.writeRowToSheet(report, Arrays.asList(""));
            ExcelHelper.writeRowToSheet(report, Arrays.asList("", "# Lower Div", "# Upper Div", "# Grad", "Total", "", "# Lower Div", "# Upper Div", "# Grad", "Total", "", "# Lower Div", "# Upper Div", "# Grad", "Total"));
            ExcelHelper.writeRowToSheet(report, Arrays.asList("Total Seats Offered", previousTotals.get(LOWER_DIV_SEATS), previousTotals.get(UPPER_DIV_SEATS), previousTotals.get(GRAD_SEATS), getTotalSeats(previousTotals), "Total Seats Offered", currentTotals.get(LOWER_DIV_SEATS), currentTotals.get(UPPER_DIV_SEATS), currentTotals.get(GRAD_SEATS), getTotalSeats(currentTotals), "Total Seats Offered", currentTotals.get(LOWER_DIV_SEATS).subtract(previousTotals.get(LOWER_DIV_SEATS)), currentTotals.get(UPPER_DIV_SEATS).subtract(previousTotals.get(UPPER_DIV_SEATS)), currentTotals.get(GRAD_SEATS).subtract(previousTotals.get(GRAD_SEATS)), getTotalSeats(currentTotals).subtract(getTotalSeats(previousTotals))));
        }

        ExcelHelper.expandHeaders(workbook);
        ExcelHelper.ignoreErrors(workbook, Arrays.asList(IgnoredErrorType.NUMBER_STORED_AS_TEXT));
    }

    private BigDecimal getFund(Map<FundType, BigDecimal> fundTotals, FundType fundType) {
        return fundTotals.get(fundType) == null ? BigDecimal.ZERO : fundTotals.get(fundType);
    }

    private BigDecimal getTotalTeachingCost(Map<BudgetSummary, BigDecimal> termTotals) {
        return termTotals.get(REPLACEMENT_COST).add( termTotals.get(TA_COST).add(termTotals.get(READER_COST).add(termTotals.get(TOTAL_EXPENSES))));
    }

    private BigDecimal getTotalOfferings(Map<BudgetSummary, BigDecimal> termTotals) {
        return termTotals.get(LOWER_DIV_OFFERINGS).add(termTotals.get(UPPER_DIV_OFFERINGS).add(termTotals.get(GRAD_OFFERINGS)));
    }

    private BigDecimal getTotalSeats(Map<BudgetSummary, BigDecimal> termTotals) {
        return termTotals.get(LOWER_DIV_SEATS).add(termTotals.get(UPPER_DIV_SEATS).add(termTotals.get(GRAD_SEATS)));
    }

    private Map<FundType, BigDecimal> generateFundTotals(List<LineItem> lineItems) {
        Map<FundType, BigDecimal> fundTotals = new HashMap<>();
        fundTotals.put(FundType.TOTAL, BigDecimal.ZERO);

        for (LineItem lineItem : lineItems) {
            FundType type = FundType.getById(lineItem.getLineItemCategory().getId());

            if (fundTotals.get(type) == null) {
                fundTotals.put(type, lineItem.getAmount());
            } else {
                fundTotals.put(type, fundTotals.get(type).add(lineItem.getAmount()));
            }

            fundTotals.put(FundType.TOTAL, fundTotals.get(FundType.TOTAL).add(lineItem.getAmount()));
        }
        return fundTotals;
    }

    // Returns 0 if previous value is 0
    private String getPercentChange(BigDecimal prev, BigDecimal current) {
        if (prev.compareTo(BigDecimal.ZERO) == 0) {
            return "0%";
        } else {
            return current.subtract(prev).divide(prev, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP).toString() + "%";
        }
    }

    private String yearToAcademicYear(long year) {
        return year + "-" + String.valueOf(year + 1).substring(2,4);
    }
};
