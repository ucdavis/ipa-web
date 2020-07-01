package edu.ucdavis.dss.ipa.api.components.budget.views;

import edu.ucdavis.dss.ipa.api.components.budget.views.factories.BudgetViewFactory;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.services.BudgetScenarioService;
import edu.ucdavis.dss.ipa.services.BudgetService;
import edu.ucdavis.dss.ipa.services.UserRoleService;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;

import edu.ucdavis.dss.ipa.utilities.ExcelHelper;
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

        Sheet budgetSummarySheet = workbook.createSheet("Budget Summary");
        budgetSummarySheet = ExcelHelper.setSheetHeader(budgetSummarySheet, Arrays.asList("", "Department","Fall Quarter", "Winter Quarter", "Spring Quarter", "Total"));

        Sheet fundsSheet = workbook.createSheet("Funds");
        fundsSheet = ExcelHelper.setSheetHeader(fundsSheet, Arrays.asList("Department", "Type", "Description", "Amount"));

        Sheet instructorSalariesSheet = workbook.createSheet("Instructor Salaries");
        instructorSalariesSheet = ExcelHelper.setSheetHeader(instructorSalariesSheet, Arrays.asList("Department", "Instructor", "Type", "Cost"));

        Sheet instructorCategoryCostSheet = workbook.createSheet("Instructor Category Cost");
        instructorCategoryCostSheet = ExcelHelper.setSheetHeader(instructorCategoryCostSheet, Arrays.asList("Type", "Cost"));

        for (BudgetView budgetView : budgetViews) {
            // Create Funds sheet
            for(LineItem li : budgetView.getLineItems()){
                List<String> cellValues = Arrays.asList(
                        budgetView.getWorkgroup().getName(),
                        li.getLineItemCategory().getDescription(),
                        li.getDescription(),
                        Objects.toString(li.getAmount(), ""));
                fundsSheet = ExcelHelper.writeRowToSheet(fundsSheet, cellValues);
            }

            HashMap<String, BigDecimal> instructorCostMap = new HashMap<>();
            List<InstructorType> instructorTypes = budgetView.getInstructorTypes();
            for(InstructorType instructorType : instructorTypes){
                System.err.println(instructorType.getDescription());
                instructorCostMap.put(instructorType.getDescription(), BigDecimal.ZERO);
            }

            // Creating Instructor Salaries sheet and calculating Instructor Category Cost
            for(Instructor instructor : budgetView.getActiveInstructors()){
                // Get data into correct shape
                InstructorCost instructorCost = budgetView.getInstructorCosts().stream().filter(ic -> ic.getInstructor().getId() == instructor.getId()).findFirst().orElse(null);

                String instructorName = instructor.getLastName() + " " + instructor.getFirstName();

                // Calculate instructor type.  Make sure to compare with frontend if you need to change.
                Set<User> users = budgetView.users;
                User user = budgetView.users.stream().filter(u -> u.getLoginId().equals(instructor.getLoginId())).findFirst().orElse(null);
                UserRole userRole = user.getUserRoles().stream().filter(ur -> (ur.getRole().getId() == 15 && budgetView.getWorkgroup().getId() == ur.getWorkgroup().getId())).findFirst().orElse(null);
                String instructorTypeDescription = "";
                if(userRole != null && userRole.getInstructorType() != null) {
                    instructorTypeDescription = userRole.getInstructorType().getDescription();
                }else{
                    List<TeachingAssignment> tas = budgetView.getTeachingAssignments();
                    Long instructorId = instructor.getId();
                    Long scheduleId = budgetView.getBudget().getSchedule().getId();
                    TeachingAssignment ta = budgetView.getTeachingAssignments().stream().filter(t -> t.getInstructor() != null).filter(t -> (t.getInstructor().getId() == instructor.getId() && t.getSchedule().getId() == budgetView.getBudget().getSchedule().getId())).findFirst().orElse(null);
                    if(ta != null){
                        instructorTypeDescription = ta.getInstructorType().getDescription();
                    }
                }

                String instructorCostValue = "";
                if (instructorCost != null) {
                    instructorCostValue = Objects.toString(instructorCost.getCost(), "");
                    if(instructorTypeDescription != ""){
                        System.err.println("Instructor type is " + instructorTypeDescription + " cost " + instructorCost.getCost());
                        instructorCostMap.put(
                                instructorTypeDescription,
                                instructorCostMap.get(instructorTypeDescription).add(null != instructorCost.getCost() ? instructorCost.getCost() : BigDecimal.ZERO, new MathContext(2))
                        );
                    }
                }

                List<String> cellValues = Arrays.asList(
                        budgetView.getWorkgroup().getName(),
                        instructorName,
                        instructorTypeDescription,
                        instructorCostValue);
                instructorSalariesSheet = ExcelHelper.writeRowToSheet(instructorSalariesSheet, cellValues);
            }

            // Creating Instructor Category Cost Sheet (values calculated above)
            for(Map.Entry<String, BigDecimal> entry : instructorCostMap.entrySet()){
                instructorCategoryCostSheet = ExcelHelper.writeRowToSheet(
                        instructorCategoryCostSheet,
                        Arrays.asList(
                                entry.getKey(),
                                Objects.toString(entry.getValue())
                        )
                );
            }
        }

        // Expand columns to length of largest value
        workbook = ExcelHelper.expandHeaders(workbook);
    }

}
