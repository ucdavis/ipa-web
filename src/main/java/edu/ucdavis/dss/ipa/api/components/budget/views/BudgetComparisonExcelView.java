package edu.ucdavis.dss.ipa.api.components.budget.views;

import static edu.ucdavis.dss.ipa.entities.enums.BudgetSummary.*;

import edu.ucdavis.dss.ipa.entities.LineItem;
import edu.ucdavis.dss.ipa.entities.enums.BudgetSummary;
import edu.ucdavis.dss.ipa.utilities.ExcelHelper;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
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

            ExcelHelper.setSheetHeader(report, Arrays
                .asList(previousYear.getBudgetScenario().getName(), "", "", "",
                    currentYear.getBudgetScenario().getName()));
            ExcelHelper.writeRowToSheet(report, Arrays
                .asList(yearToAcademicYear(previousYear.getBudget().getSchedule().getYear()), "",
                    "", "",
                    yearToAcademicYear(currentYear.getBudget().getSchedule().getYear()), "", "", "",
                    "Changes"));
            ExcelHelper.writeRowToSheet(report, Arrays.asList(""));

            ExcelHelper.writeRowToSheet(report, Arrays
                .asList("Categories", "Total Cost", "# Courses", "",
                    "Categories", "Total Cost", "# Courses", "", "Cost", "# Courses", "% Cost",
                    "% Courses"));

            // Instructor Costs
            Map<BudgetSummary, BigDecimal> previousTotals = previousYear.getTermTotals().get("combined");
            Map<BudgetSummary, BigDecimal> currentTotals = currentYear.getTermTotals().get("combined");

            ExcelHelper.writeRowToSheet(report, Arrays.asList("Emeriti - Recalled", previousTotals.get(EMERITI_COST), previousTotals.get(EMERITI_COUNT), "", "Emeriti - Recalled", currentTotals.get(EMERITI_COST), currentTotals.get(EMERITI_COUNT)));
            ExcelHelper.writeRowToSheet(report, Arrays.asList("Visiting Professor", previousTotals.get(VISITING_PROFESSOR_COST), previousTotals.get(VISITING_PROFESSOR_COUNT), "", "Visiting Professor", currentTotals.get(VISITING_PROFESSOR_COST), currentTotals.get(VISITING_PROFESSOR_COUNT)));
            ExcelHelper.writeRowToSheet(report, Arrays.asList("Associate Instructor", previousTotals.get(ASSOCIATE_INSTRUCTOR_COST), previousTotals.get(ASSOCIATE_INSTRUCTOR_COUNT), "", "Associate Instructor", currentTotals.get(ASSOCIATE_INSTRUCTOR_COST), currentTotals.get(ASSOCIATE_INSTRUCTOR_COUNT)));
            ExcelHelper.writeRowToSheet(report, Arrays.asList("Unit 18 Pre-Six Lecturer", previousTotals.get(UNIT18_LECTURER_COST), previousTotals.get(UNIT18_LECTURER_COUNT), "", "Unit 18 Pre-Six Lecturer", currentTotals.get(UNIT18_LECTURER_COST), currentTotals.get(UNIT18_LECTURER_COUNT)));
            ExcelHelper.writeRowToSheet(report, Arrays.asList("Continuing Lecturer", previousTotals.get(CONTINUING_LECTURER_COST), previousTotals.get(CONTINUING_LECTURER_COUNT), "", "Continuing Lecturer", currentTotals.get(CONTINUING_LECTURER_COST), currentTotals.get(CONTINUING_LECTURER_COUNT)));
            ExcelHelper.writeRowToSheet(report, Arrays.asList("Ladder Faculty", previousTotals.get(LADDER_FACULTY_COST), previousTotals.get(LADDER_FACULTY_COUNT), "", "Ladder Faculty", currentTotals.get(LADDER_FACULTY_COST), currentTotals.get(LADDER_FACULTY_COUNT)));
            ExcelHelper.writeRowToSheet(report, Arrays.asList("Instructor", previousTotals.get(INSTRUCTOR_COST), previousTotals.get(INSTRUCTOR_COUNT), "", "Instructor", currentTotals.get(INSTRUCTOR_COST), currentTotals.get(INSTRUCTOR_COUNT)));
            ExcelHelper.writeRowToSheet(report, Arrays.asList("Lecturer SOE", previousTotals.get(LECTURER_SOE_COST), previousTotals.get(LECTURER_SOE_COUNT), "", "Lecturer SOE", currentTotals.get(LECTURER_SOE_COST), currentTotals.get(LECTURER_SOE_COUNT)));
            ExcelHelper.writeRowToSheet(report, Arrays.asList("Unassigned", previousTotals.get(UNASSIGNED_COST), previousTotals.get(UNASSIGNED_COUNT), "", "Unassigned", currentTotals.get(UNASSIGNED_COST), currentTotals.get(UNASSIGNED_COUNT)));
            ExcelHelper.writeRowToSheet(report, Arrays.asList("", previousTotals.get(REPLACEMENT_COST), previousTotals.get(COURSE_COUNT), "", "", currentTotals.get(REPLACEMENT_COST), currentTotals.get(COURSE_COUNT)));
            ExcelHelper.writeRowToSheet(report, Arrays.asList(""));

            ExcelHelper.writeRowToSheet(report, Arrays.asList("", "Total Cost", "# Courses", "", "", "Total Cost", "# Courses"));
            ExcelHelper.writeRowToSheet(report, Arrays.asList("TAs", previousTotals.get(TA_COST), previousTotals.get(TA_COUNT), "", "TAs", currentTotals.get(TA_COST), currentTotals.get(TA_COUNT)));
            ExcelHelper.writeRowToSheet(report, Arrays.asList("Readers", previousTotals.get(READER_COST), previousTotals.get(READER_COUNT), "", "Readers", currentTotals.get(READER_COST), currentTotals.get(READER_COUNT)));
            ExcelHelper.writeRowToSheet(report, Arrays.asList("Total", previousTotals.get(TA_COST).add(previousTotals.get(READER_COST)), previousTotals.get(TA_COUNT).add(previousTotals.get(READER_COUNT)), "", "Total", currentTotals.get(TA_COST).add(currentTotals.get(READER_COST)), currentTotals.get(TA_COUNT).add(currentTotals.get(READER_COUNT))));
            ExcelHelper.writeRowToSheet(report, Arrays.asList(""));

            ExcelHelper.writeRowToSheet(report, Arrays.asList(""));
            ExcelHelper.writeRowToSheet(report, Arrays.asList("Total Teaching Costs", getTotalTeachingCost(previousTotals), "", "", "Total Teaching Costs", getTotalTeachingCost(currentTotals)));
            ExcelHelper.writeRowToSheet(report, Arrays.asList(""));

            // Funds section
            Map<String, BigDecimal> previousFunds = generateFundTotals(previousYear.getLineItems());
            Map<String, BigDecimal> currentFunds = generateFundTotals(currentYear.getLineItems());

            ExcelHelper.writeRowToSheet(report, Arrays.asList("Funding", "Amount", "", "", "Funding", "Amount", "", ""));
            ExcelHelper.writeRowToSheet(report, Arrays.asList("Funds From Deans Office", previousFunds.get("Funds From Deans Office") == null ? BigDecimal.ZERO : previousFunds.get("Funds From Deans Office"), "", "", "Funds From Deans Office", currentFunds.get("Funds From Deans Office") == null ? BigDecimal.ZERO : currentFunds.get("Funds From Deans Office"), "", ""));
            ExcelHelper.writeRowToSheet(report, Arrays.asList("Internal Buyout", previousFunds.get("Internal Buyout") == null ? BigDecimal.ZERO : previousFunds.get("Internal Buyout"), "", "", "Internal Buyout", currentFunds.get("Internal Buyout") == null ? BigDecimal.ZERO : currentFunds.get("Internal Buyout"), "", ""));
            ExcelHelper.writeRowToSheet(report, Arrays.asList("Class Cancelled - Funds no longer needed", previousFunds.get("Class Cancelled - Funds no longer needed") == null ? BigDecimal.ZERO : currentFunds.get("Class Cancelled - Funds no longer needed"), "", "", "Class Cancelled - Funds no longer needed", currentFunds.get("Class Cancelled - Funds no longer needed") == null ? BigDecimal.ZERO : previousFunds.get("Class Cancelled - Funds no longer needed"), "", ""));
            ExcelHelper.writeRowToSheet(report, Arrays.asList("Range Adjustment Funds", previousFunds.get("Range Adjustment Funds") == null ? BigDecimal.ZERO : previousFunds.get("Range Adjustment Funds"), "", "", "Range Adjustment Funds", currentFunds.get("Range Adjustment Funds") == null ? BigDecimal.ZERO : currentFunds.get("Range Adjustment Funds"), "", ""));
            ExcelHelper.writeRowToSheet(report, Arrays.asList("Work-Life Balance Funds", previousFunds.get("Work-Life Balance Funds") == null ? BigDecimal.ZERO : previousFunds.get("Work-Life Balance Funds"), "", "", "Work-Life Balance Funds", currentFunds.get("Work-Life Balance Funds") == null ? BigDecimal.ZERO : currentFunds.get("Work-Life Balance Funds"), "", ""));
            ExcelHelper.writeRowToSheet(report, Arrays.asList("Other Funds", previousFunds.get("Other Funds") == null ? BigDecimal.ZERO : previousFunds.get("Other Funds"), "", "", "Other Funds", currentFunds.get("Other Funds") == null ? BigDecimal.ZERO : currentFunds.get("Other Funds"), "", ""));
            ExcelHelper.writeRowToSheet(report, Arrays.asList("External Buyout", previousFunds.get("External Buyout") == null ? BigDecimal.ZERO : previousFunds.get("External Buyout"), "", "", "External Buyout", currentFunds.get("External Buyout") == null ? BigDecimal.ZERO : currentFunds.get("External Buyout"), "", ""));
            ExcelHelper.writeRowToSheet(report, Arrays.asList("", previousFunds.get("total"), "", "", "", currentFunds.get("total"), "", ""));
            ExcelHelper.writeRowToSheet(report, Arrays.asList(""));

            ExcelHelper.writeRowToSheet(report, Arrays.asList("Balance", previousFunds.get("total").subtract(getTotalTeachingCost(previousTotals)), "", "", "Balance", currentFunds.get("total").subtract(getTotalTeachingCost(currentTotals))));
            ExcelHelper.writeRowToSheet(report, Arrays.asList(""));

            // Course Offering
            ExcelHelper.writeRowToSheet(report, Arrays.asList("Courses Offered", "", "", "", "Courses Offered", "", "", ""));
            ExcelHelper.writeRowToSheet(report, Arrays.asList("# Lower Div", "# Upper Div", "# Grad", "Total", "# Lower Div", "# Upper Div", "# Grad", "Total"));
            ExcelHelper.writeRowToSheet(report, Arrays.asList(previousTotals.get(LOWER_DIV_OFFERINGS), previousTotals.get(UPPER_DIV_OFFERINGS), previousTotals.get(GRAD_OFFERINGS), getTotalOfferings(previousTotals), currentTotals.get(LOWER_DIV_OFFERINGS), currentTotals.get(UPPER_DIV_OFFERINGS), currentTotals.get(GRAD_OFFERINGS), getTotalOfferings(currentTotals)));
            ExcelHelper.writeRowToSheet(report, Arrays.asList(""));

            // Seats Offered
            ExcelHelper.writeRowToSheet(report, Arrays.asList("Total Seats Offered", "", "", "", "Total Seats Offered", "", "", ""));
            ExcelHelper.writeRowToSheet(report, Arrays.asList("# Lower Div", "# Upper Div", "# Grad", "Total", "# Lower Div", "# Upper Div", "# Grad", "Total"));
            ExcelHelper.writeRowToSheet(report, Arrays.asList(previousTotals.get(LOWER_DIV_SEATS), previousTotals.get(UPPER_DIV_SEATS), previousTotals.get(GRAD_SEATS), getTotalSeats(previousTotals), currentTotals.get(LOWER_DIV_SEATS), currentTotals.get(UPPER_DIV_SEATS), currentTotals.get(GRAD_SEATS), getTotalSeats(currentTotals)));

            workbook = ExcelHelper.expandHeaders(workbook);
        }
    }

    private BigDecimal getTotalTeachingCost(Map<BudgetSummary, BigDecimal> termTotals) {
        return termTotals.get(REPLACEMENT_COST).add(termTotals.get(TA_COST).add(termTotals.get(READER_COST)));
    }

    private BigDecimal getTotalOfferings(Map<BudgetSummary, BigDecimal> termTotals) {
        return termTotals.get(LOWER_DIV_OFFERINGS).add(termTotals.get(UPPER_DIV_OFFERINGS).add(termTotals.get(GRAD_OFFERINGS)));
    }

    private BigDecimal getTotalSeats(Map<BudgetSummary, BigDecimal> termTotals) {
        return termTotals.get(LOWER_DIV_SEATS).add(termTotals.get(UPPER_DIV_SEATS).add(termTotals.get(GRAD_SEATS)));
    }

    private Map<String, BigDecimal> generateFundTotals(List<LineItem> lineItems) {
        Map<String, BigDecimal> fundTotals = new HashMap<>();
        fundTotals.put("total", BigDecimal.ZERO);

        for (LineItem lineItem : lineItems) {
            String key = lineItem.getLineItemCategory().getDescription();

            if (fundTotals.get(key) == null) {
                fundTotals.put(key, lineItem.getAmount());
            } else {
                fundTotals.put(key, fundTotals.get(key).add(lineItem.getAmount()));
            }

            fundTotals.put("total", fundTotals.get("total").add(lineItem.getAmount()));
        }
        return fundTotals;
    }

    private String yearToAcademicYear(long year) {
        return year + "-" + String.valueOf(year + 1).substring(2,4);
    }
};
