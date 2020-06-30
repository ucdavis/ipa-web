package edu.ucdavis.dss.ipa.api.components.budget.views;

import edu.ucdavis.dss.ipa.api.components.budget.views.factories.BudgetViewFactory;
import edu.ucdavis.dss.ipa.entities.Budget;
import edu.ucdavis.dss.ipa.entities.BudgetScenario;
import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.InstructorCost;
import edu.ucdavis.dss.ipa.entities.InstructorTypeCost;
import edu.ucdavis.dss.ipa.entities.User;
import edu.ucdavis.dss.ipa.entities.UserRole;
import edu.ucdavis.dss.ipa.services.BudgetScenarioService;
import edu.ucdavis.dss.ipa.services.BudgetService;
import edu.ucdavis.dss.ipa.services.UserRoleService;
import java.util.Optional;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsView;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BudgetExcelView extends AbstractXlsView {
    @Inject BudgetViewFactory budgetViewFactory;
    @Inject BudgetService budgetService;
    @Inject BudgetScenarioService budgetScenarioService;
    @Inject UserRoleService userRoleService;
    private List<BudgetView> budgetViews = null;
    public BudgetExcelView ( List<BudgetView> budgetViews ) {
        this.budgetViews = budgetViews;
    }

    /*
        Make sure to edit the frontend if any calculations change!!!
     */
    @Override
    protected void buildExcelDocument(Map<String, Object> model, Workbook workbook, HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setHeader("Content-Type", "multipart/mixed; charset=\"utf-8\"");
        response.setHeader("Content-Disposition", "attachment; filename=\"Budget-Report.xls\"");

        Sheet sheet = workbook.createSheet("Budget Summary");
        setExcelHeader(sheet, Arrays.asList("1", "2"));

        Sheet instructorSalariesSheet = workbook.createSheet("Instructor Salaries");
        setExcelHeader(instructorSalariesSheet, Arrays.asList("Instructor", "Type", "Cost"));

        for (BudgetView budgetView : budgetViews) {
            System.err.println("Instructor count in excel is " + budgetView.getActiveInstructors().size());

            for (int i = 0; i < budgetView.getActiveInstructors().size(); i++) {
                Row instructorSalaryRow = instructorSalariesSheet.createRow(i + 1);

                Instructor instructor = budgetView.getActiveInstructors().get(i);
                InstructorCost instructorCost = budgetView.getInstructorCosts().stream().filter(ic -> ic.getInstructor().getId() == instructor.getId()).findFirst().orElse(null);

                String instructorName = instructor.getLastName() + " " + instructor.getFirstName();
                instructorSalaryRow.createCell(0).setCellValue(instructorName);

                User user = budgetView.users.stream().filter(u -> u.getLoginId().equals(instructor.getLoginId())).findFirst().orElse(null);
                UserRole userRole = user.getUserRoles().stream().filter(ur -> ur.getRole().getId() == 15).findFirst().orElse(null);

                instructorSalaryRow.createCell(1).setCellValue(userRole.getInstructorType().getDescription());

                if (instructorCost != null) {
                    instructorSalaryRow.createCell(2).setCellValue(String.valueOf(instructorCost.getCost()));
                }
            }
        }

        instructorSalariesSheet.autoSizeColumn(0);
        instructorSalariesSheet.autoSizeColumn(1);
        instructorSalariesSheet.autoSizeColumn(2);
    }

    private void setExcelHeader(Sheet sheet, List<String> headers) {
        Row excelHeader = sheet.createRow(0);
        for(int i = 0; i < headers.size(); i++){
            excelHeader.createCell(i).setCellValue(headers.get(i));
        }
    }
}
