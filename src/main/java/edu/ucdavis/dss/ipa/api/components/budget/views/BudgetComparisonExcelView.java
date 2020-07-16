package edu.ucdavis.dss.ipa.api.components.budget.views;

import static edu.ucdavis.dss.ipa.entities.enums.BudgetSummary.*;

import edu.ucdavis.dss.ipa.entities.enums.BudgetSummary;
import edu.ucdavis.dss.ipa.utilities.ExcelHelper;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

public class BudgetComparisonExcelView extends AbstractXlsxView {
    private List<List<BudgetScenarioExcelView>> budgetComparisonList;

    public BudgetComparisonExcelView(List<List<BudgetScenarioExcelView>> budgetComparisonList) {
        this.budgetComparisonList = budgetComparisonList;
    }

    @Override
    protected void buildExcelDocument(Map<String, Object> model, Workbook workbook,
                                      HttpServletRequest request, HttpServletResponse response)
        throws Exception {

        for (List<BudgetScenarioExcelView> budgetScenarioExcelViewPair : budgetComparisonList) {
            Sheet report = workbook.createSheet(budgetScenarioExcelViewPair.get(0).getWorkgroup().getName());

            BudgetScenarioExcelView previousYear = budgetScenarioExcelViewPair.get(0);
            BudgetScenarioExcelView currentYear = budgetScenarioExcelViewPair.get(1);

            report = ExcelHelper.setSheetHeader(report, Arrays
                .asList(previousYear.getBudgetScenario().getName(), "", "", "",
                    currentYear.getBudgetScenario().getName()));
            report = ExcelHelper.writeRowToSheet(report, Arrays
                .asList(yearToAcademicYear(previousYear.getBudget().getSchedule().getYear()), "",
                    "", "",
                    yearToAcademicYear(currentYear.getBudget().getSchedule().getYear()), "", "", "",
                    "Changes"));
            report = ExcelHelper.writeRowToSheet(report, Arrays
                .asList("Categories", "Total Cost", "# Courses", "",
                    "Categories", "Total Cost", "# Courses", "", "Cost", "# Courses", "% Cost",
                    "% Courses"));

            // Instructor Costs
            Map<BudgetSummary, BigDecimal> previousTotals = previousYear.getTermTotals().get("combined");
            Map<BudgetSummary, BigDecimal> currentTotals = previousYear.getTermTotals().get("combined");

            report = ExcelHelper.writeRowToSheet(report, Arrays.asList("Emeriti - Recalled", previousTotals.get(EMERITI_COST)));
            report = ExcelHelper.writeRowToSheet(report, Arrays.asList("Visiting Professor", previousTotals.get(VISITING_PROFESSOR_COST)));
            report = ExcelHelper.writeRowToSheet(report, Arrays.asList("Associate Instructor", previousTotals.get(ASSOCIATE_INSTRUCTOR_COST)));
            report = ExcelHelper.writeRowToSheet(report, Arrays.asList("Unit 18 Pre-Six Lecturer", previousTotals.get(UNIT18_LECTURER_COST)));
            report = ExcelHelper.writeRowToSheet(report, Arrays.asList("Continuing Lecturer", previousTotals.get(CONTINUING_LECTURER_COST)));
            report = ExcelHelper.writeRowToSheet(report, Arrays.asList("Ladder Faculty", previousTotals.get(LADDER_FACULTY_COST)));
            report = ExcelHelper.writeRowToSheet(report, Arrays.asList("Instructor", previousTotals.get(INSTRUCTOR_COST)));
            report = ExcelHelper.writeRowToSheet(report, Arrays.asList("Lecturer SOE", previousTotals.get(LECTURER_SOE_COST)));
            report = ExcelHelper.writeRowToSheet(report, Arrays.asList("", previousTotals.get(REPLACEMENT_COST)));
            report = ExcelHelper.writeRowToSheet(report, Arrays.asList(""));

            report = ExcelHelper.writeRowToSheet(report, Arrays.asList("", "Total Cost", "Total Count"));
            report = ExcelHelper.writeRowToSheet(report, Arrays.asList("TAs", previousTotals.get(TA_COST), previousTotals.get(TA_COUNT)));
            report = ExcelHelper.writeRowToSheet(report, Arrays.asList("Readers", previousTotals.get(READER_COST), previousTotals.get(READER_COUNT)));
            report = ExcelHelper.writeRowToSheet(report, Arrays.asList("Total", previousTotals.get(TA_COST).add(previousTotals.get(READER_COST)), previousTotals.get(TA_COUNT).add(previousTotals.get(READER_COUNT))));
            report = ExcelHelper.writeRowToSheet(report, Arrays.asList(""));

            // Funds section

            // Course Offering
            report = ExcelHelper.writeRowToSheet(report, Arrays.asList("Courses Offered", "", "", ""));
            report = ExcelHelper.writeRowToSheet(report, Arrays.asList("# Lower Div", "# Upper Div", "# Grad", "Total"));
            report = ExcelHelper.writeRowToSheet(report, Arrays.asList(previousTotals.get(LOWER_DIV_OFFERINGS), previousTotals.get(UPPER_DIV_OFFERINGS), previousTotals.get(GRAD_OFFERINGS)));
            report = ExcelHelper.writeRowToSheet(report, Arrays.asList(""));

            // Seats Offered
            report = ExcelHelper.writeRowToSheet(report, Arrays.asList("Total Seats Offered", "", "", ""));
            report = ExcelHelper.writeRowToSheet(report, Arrays.asList("# Lower Div", "# Upper Div", "# Grad", "Total"));
            report = ExcelHelper.writeRowToSheet(report, Arrays.asList(previousTotals.get(LOWER_DIV_SEATS), previousTotals.get(UPPER_DIV_SEATS), previousTotals.get(GRAD_SEATS)));

            workbook = ExcelHelper.expandHeaders(workbook);
        }
    }

    private String yearToAcademicYear(long year) {
        return year + "-" + String.valueOf(year + 1).substring(2,4);
    }
};
