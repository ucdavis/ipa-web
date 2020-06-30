package edu.ucdavis.dss.ipa.api.components.budget.views;

import edu.ucdavis.dss.ipa.api.components.budget.views.factories.BudgetViewFactory;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.services.BudgetScenarioService;
import edu.ucdavis.dss.ipa.services.BudgetService;
import edu.ucdavis.dss.ipa.services.UserRoleService;

import java.util.*;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsView;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

        Sheet funds = workbook.createSheet("Funds");
        setExcelHeader(sheet, Arrays.asList("Department", "Type", "Description", "Amount"));


        Sheet instructorSalariesSheet = workbook.createSheet("Instructor Salaries");
        setExcelHeader(instructorSalariesSheet, Arrays.asList("Department", "Instructor", "Type", "Cost"));

        int salarySheetRow = 0;
        int fundsRow = 0;
        for (BudgetView budgetView : budgetViews) {
            // Funds
            for(LineItem li : budgetView.getLineItems()){
                fundsRow += 1;
                Row lineItemRow = funds.createRow(fundsRow);
                lineItemRow.createCell(0).setCellValue(budgetView.getWorkgroup().getName());
                lineItemRow.createCell(1).setCellValue(li.getLineItemCategory().getDescription());
                lineItemRow.createCell(2).setCellValue(li.getDescription());
                lineItemRow.createCell(3).setCellValue(Objects.toString(li.getAmount(), ""));
            }


            System.err.println("Instructor count in excel is " + budgetView.getActiveInstructors().size());

            for (int i = 0; i < budgetView.getActiveInstructors().size(); i++) {
                salarySheetRow += 1;
                Row instructorSalaryRow = instructorSalariesSheet.createRow(salarySheetRow);
                instructorSalaryRow.createCell(0).setCellValue(budgetView.getWorkgroup().getName());

                Instructor instructor = budgetView.getActiveInstructors().get(i);
                InstructorCost instructorCost = budgetView.getInstructorCosts().stream().filter(ic -> ic.getInstructor().getId() == instructor.getId()).findFirst().orElse(null);

                String instructorName = instructor.getLastName() + " " + instructor.getFirstName();
                instructorSalaryRow.createCell(1).setCellValue(instructorName);

                Set<User> users = budgetView.users;
                User user = budgetView.users.stream().filter(u -> u.getLoginId().equals(instructor.getLoginId())).findFirst().orElse(null);
                System.err.println(instructor.getId());
                System.err.println(user.getId());
                System.err.println("Workgroup in excel " + budgetView.getWorkgroup().getId() + " " + user.getDisplayName());
                UserRole userRole = user.getUserRoles().stream().filter(ur -> (ur.getRole().getId() == 15 && budgetView.getWorkgroup().getId() == ur.getWorkgroup().getId())).findFirst().orElse(null);

                if(userRole != null && userRole.getInstructorType() != null) {
                    instructorSalaryRow.createCell(2).setCellValue(userRole.getInstructorType().getDescription());
                }else{
                    List<TeachingAssignment> tas = budgetView.getTeachingAssignments();
                    Long instructorId = instructor.getId();
                    Long scheduleId = budgetView.getBudget().getSchedule().getId();
                    TeachingAssignment ta = budgetView.getTeachingAssignments().stream().filter(t -> t.getInstructor() != null).filter(t -> (t.getInstructor().getId() == instructor.getId() && t.getSchedule().getId() == budgetView.getBudget().getSchedule().getId())).findFirst().orElse(null);
                    if(ta != null){
                        instructorSalaryRow.createCell(2).setCellValue(ta.getInstructorType().getDescription());
                    }
                }

                if (instructorCost != null) {
                    instructorSalaryRow.createCell(3).setCellValue(Objects.toString(instructorCost.getCost(), ""));
                }
            }
        }

        /*instructorSalariesSheet.autoSizeColumn(0);
        instructorSalariesSheet.autoSizeColumn(1);
        instructorSalariesSheet.autoSizeColumn(2);*/
        for(int i = 0; i < workbook.getNumberOfSheets(); i++){
            Sheet s = workbook.getSheetAt(i);
            if (s.getPhysicalNumberOfRows() > 0) {
                Row row = s.getRow(s.getFirstRowNum());
                Iterator<Cell> cellIterator = row.cellIterator();
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    int columnIndex = cell.getColumnIndex();
                    s.autoSizeColumn(columnIndex);
                }
            }
        }
    }

    private void setExcelHeader(Sheet sheet, List<String> headers) {
        Row excelHeader = sheet.createRow(0);
        for(int i = 0; i < headers.size(); i++){
            excelHeader.createCell(i).setCellValue(headers.get(i));
        }
    }
}
