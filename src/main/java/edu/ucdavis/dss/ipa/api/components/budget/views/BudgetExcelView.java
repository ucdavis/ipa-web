package edu.ucdavis.dss.ipa.api.components.budget.views;

import edu.ucdavis.dss.ipa.api.components.budget.views.factories.BudgetViewFactory;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.services.BudgetScenarioService;
import edu.ucdavis.dss.ipa.services.BudgetService;
import edu.ucdavis.dss.ipa.services.InstructorCostService;
import edu.ucdavis.dss.ipa.services.UserRoleService;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;
import java.util.stream.Collectors;

import edu.ucdavis.dss.ipa.services.jpa.JpaInstructorCostService;
import edu.ucdavis.dss.ipa.utilities.ExcelHelper;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BudgetExcelView extends AbstractXlsxView {
    @Inject BudgetViewFactory budgetViewFactory;
    @Inject BudgetService budgetService;
    @Inject BudgetScenarioService budgetScenarioService;
    @Inject UserRoleService userRoleService;
    InstructorCostService instructorCostService =  new JpaInstructorCostService();

    private Map<Long, BudgetView> budgetViewsMap = null;
    public BudgetExcelView ( Map<Long, BudgetView> budgetViewsMap ) {
        this.budgetViewsMap = budgetViewsMap;
    }

    public class BudgetSummaryTerms {

        public class BudgetSummaryTerm{
            private double taCount;
            private double readerCount;
            private double associateInstructorCost;
            private double lowerDivUnits;
            private double upperDivUnits;

            public double calculateInstructorCost(BudgetView budgetView, SectionGroupCost sectionGroupCost){
                if(sectionGroupCost.getCost() == null){
                    Long budgetId = sectionGroupCost.getBudgetScenario().getId();
                    if(sectionGroupCost.getInstructor() != null){
                        Long instructorId = sectionGroupCost.getInstructor().getId();
                        InstructorCost instructorCost = budgetView.getInstructorCosts().stream().filter(ic -> ic.getInstructorTypeIdentification() == 3 && ic.getInstructor().getId() == sectionGroupCost.getInstructor().getId()).findFirst().orElse(null);
                        if(instructorCost == null){
                            return 0.0;
                        } else{
                            double cost = instructorCost.getCost().doubleValue();
                            System.err.println("Found cost, cost was " + cost + " Id is " + instructorCost.getId() + " Section group cost ID was " + sectionGroupCost.getId());
                            return cost;
                        }
                    } else{
                        return 0.0;
                    }
                } else{
                    return sectionGroupCost.getCost().doubleValue();
                }
            }

            public BudgetSummaryTerm(BudgetView budgetView, SectionGroupCost sectionGroupCost){
                this.taCount = (double) (sectionGroupCost.getTaCount() == null ? 0.0F : sectionGroupCost.getTaCount());
                this.readerCount = (double) (sectionGroupCost.getReaderCount() == null ? 0.0F: sectionGroupCost.getReaderCount() );
                this.lowerDivUnits = (double) (sectionGroupCost.getUnitsLow() == null ? 0.0F : sectionGroupCost.getUnitsLow());
                this.upperDivUnits = (double) (sectionGroupCost.getUnitsHigh() == null ? 0.0F : sectionGroupCost.getUnitsHigh());
                this.associateInstructorCost = calculateInstructorCost(budgetView, sectionGroupCost);
                //this.replacementCost = (double) (sectionGroupCost.getCost() == null ? sectionGroupCost.getInstructor() : sectionGroupCost.getCost());
            }

            public void add(BudgetView budgetView, SectionGroupCost sectionGroupCost){
                this.taCount += (int) (sectionGroupCost.getTaCount() == null ? 0.0F : sectionGroupCost.getTaCount());
                this.readerCount += (int) (sectionGroupCost.getReaderCount() == null ? 0.0F: sectionGroupCost.getReaderCount() );
                this.lowerDivUnits += (double) (sectionGroupCost.getUnitsLow() == null ? 0.0F : sectionGroupCost.getUnitsLow());
                this.upperDivUnits += (double) (sectionGroupCost.getUnitsHigh() == null ? 0.0F : sectionGroupCost.getUnitsHigh());
                this.associateInstructorCost += calculateInstructorCost(budgetView, sectionGroupCost);
            }
        }

        private HashMap<String, BudgetSummaryTerm> terms = new HashMap<String, BudgetSummaryTerm>();
        private double taCost;
        private double readerCost;
        private String department;
        private BudgetView budgetView;

        public BudgetSummaryTerms(BudgetView budgetView, String department, float taCost, float readerCost){
            this.department = department;
            this.taCost = (double) taCost;
            this.readerCost = (double) readerCost;
            this.budgetView = budgetView;
        }
        public void addSection(SectionGroupCost sectionGroupCost){
            if(this.terms.get(sectionGroupCost.getTermCode()) == null){
                terms.put(sectionGroupCost.getTermCode(), new BudgetSummaryTerm(budgetView, sectionGroupCost));
            } else{
                terms.get(sectionGroupCost.getTermCode()).add(budgetView, sectionGroupCost);
            }
        }

        private double getTaCount(String termCode){
            if(terms.get(termCode) != null){
                return terms.get(termCode).taCount;
            } else{
                return 0;
            }
        }

        private double getTaCost(String termCode){
            return getTaCount(termCode) * taCost;
        }

        private double getReaderCount(String termCode){
            if(terms.get(termCode) != null){
                return terms.get(termCode).readerCount;
            } else{
                return 0;
            }
        }

        private double getReaderCost(String termCode){
            return getReaderCount(termCode) * readerCost;
        }

        private double getSupportCost(String termCode){
            return getTaCost(termCode) + getReaderCost(termCode);
        }

        private double getAssociateInstructorCost(String termCode){
            if(terms.get(termCode) != null){
                return terms.get(termCode).associateInstructorCost;
            } else{
                return 0.0;
            }
        }

        private List<Object> rowData(String field){
            List<Object> data = new ArrayList<Object>();
            //Set<String> termCodes = Term.getTermCodesByYear(2020);
            //System.err.println(termCodes);
            List<String> termCodes = Arrays.asList(
               "202005",
               "202007",
               "202010",
               "202101",
               "202103"
            );
            if(field != ""){
                data.add(department);
                data.add(field);
            }

            if(field == "TA Count"){
                for(String termCode : termCodes){
                    data.add(getTaCount(termCode));
                }
            } else if(field == "TA Cost"){
                for(String termCode : termCodes){
                    data.add(getTaCost(termCode));
                }
            } else if(field == "Reader Count"){
                for(String termCode : termCodes) {
                    data.add(getReaderCount(termCode));
                }
            } else if (field == "Reader Cost"){
                for(String termCode : termCodes) {
                    data.add(getReaderCost(termCode));
                }
            } else if (field == "Support Cost"){
                for(String termCode : termCodes) {
                    data.add(getSupportCost(termCode));
                }
            } else if (field == "Associate Instructor"){
                for(String termCode: termCodes){
                    data.add(getAssociateInstructorCost(termCode));
                }
            }
            return data;
        }

        public Sheet writeTerms(Sheet sheet){
            List<String> rows = Arrays.asList(
                    "TA Count",
                    "TA Cost",
                    "Reader Count",
                    "Reader Cost",
                    "Support Cost",
                    "",
                    "Associate Instructor"
            );
            for(String row : rows){
                sheet = ExcelHelper.writeRowToSheet(
                        sheet,
                        rowData(row)
                );
            }
            return sheet;
        }
    }

    /*
        Make sure to edit the frontend if any calculations change!!!
     */
    @Override
    protected void buildExcelDocument(Map<String, Object> model, Workbook workbook, HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setHeader("Content-Type", "multipart/mixed; charset=\"utf-8\"");
        response.setHeader("Content-Disposition", "attachment; filename=\"Budget-Report.xlsx\"");

        Sheet budgetSummarySheet = workbook.createSheet("Budget Summary");
        budgetSummarySheet = ExcelHelper.setSheetHeader(budgetSummarySheet, Arrays.asList("Department", "","Summer Session 1","Summer Session 2","Fall Quarter", "Winter Quarter", "Spring Quarter", "Total"));

        Sheet scheduleCostSheet = workbook.createSheet("Schedule Cost");

        scheduleCostSheet = ExcelHelper.setSheetHeader(scheduleCostSheet, Arrays.asList(
           "Department",
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
        fundsSheet = ExcelHelper.setSheetHeader(fundsSheet, Arrays.asList("Department", "Type", "Description", "Amount"));

        Sheet instructorSalariesSheet = workbook.createSheet("Instructor Salaries");
        instructorSalariesSheet = ExcelHelper.setSheetHeader(instructorSalariesSheet, Arrays.asList("Department", "Instructor", "Type", "Cost"));

        Sheet instructorCategoryCostSheet = workbook.createSheet("Instructor Category Cost");
        instructorCategoryCostSheet = ExcelHelper.setSheetHeader(instructorCategoryCostSheet, Arrays.asList("Department", "Type", "Cost"));

        for (Map.Entry<Long, BudgetView> entry : budgetViewsMap.entrySet()) {
            Long scenarioId = entry.getKey();
            BudgetView budgetView = entry.getValue();

            BudgetSummaryTerms budgetTerms = new BudgetSummaryTerms(
                    budgetView,
                    budgetView.getWorkgroup().getName(),
                    budgetView.getBudget().getTaCost(),
                    budgetView.getBudget().getReaderCost());

            // Create Schedule Cost sheet
            for(SectionGroupCost sectionGroupCost : budgetView.getSectionGroupCosts().stream().filter(sgc -> sgc.getBudgetScenario().getId() == scenarioId).sorted(Comparator.comparing(SectionGroupCost::getTermCode).thenComparing(SectionGroupCost::getSubjectCode).thenComparing(SectionGroupCost::getCourseNumber)).collect(Collectors.toList()) ){
                Float taCost = (sectionGroupCost.getTaCount() == null ? 0.0F : sectionGroupCost.getTaCount()) * budgetView.getBudget().getTaCost();
                Float readerCost = (sectionGroupCost.getReaderCount() == null ? 0.0F: sectionGroupCost.getReaderCount() ) * budgetView.getBudget().getReaderCost();
                Float supportCost = taCost + readerCost;
                Float sectionCost = sectionGroupCost.getCost() == null ? 0.0F : sectionGroupCost.getCost().floatValue();
                scheduleCostSheet = ExcelHelper.writeRowToSheet(
                        scheduleCostSheet,
                        Arrays.asList(
                                budgetView.getWorkgroup().getName(),
                                Term.getRegistrarName(sectionGroupCost.getTermCode()),
                                sectionGroupCost.getSubjectCode(),
                                sectionGroupCost.getCourseNumber(),
                                sectionGroupCost.getTitle(),
                                sectionGroupCost.getUnitsHigh(),
                                sectionGroupCost.getUnitsLow(),
                                sectionGroupCost.getSequencePattern(),
                                sectionGroupCost.getEnrollment(),
                                "",
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
                budgetTerms.addSection(sectionGroupCost);
            }

            budgetSummarySheet = budgetTerms.writeTerms(budgetSummarySheet);

            // Create Funds sheet
            for(LineItem lineItem : budgetView.getLineItems().stream().filter(li -> li.getBudgetScenarioId() == scenarioId).collect(Collectors.toList())){
                List<Object> cellValues = Arrays.asList(
                        budgetView.getWorkgroup().getName(),
                        lineItem.getLineItemCategory().getDescription(),
                        lineItem.getDescription(),
                        lineItem.getAmount());
                fundsSheet = ExcelHelper.writeRowToSheet(fundsSheet, cellValues);
            }

            // Creating Instructor Salaries sheet
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

                BigDecimal instructorCostValue = instructorCost == null ? null : instructorCost.getCost();

                List<Object> cellValues = Arrays.asList(
                        budgetView.getWorkgroup().getName(),
                        instructorName,
                        instructorTypeDescription,
                        instructorCostValue);
                instructorSalariesSheet = ExcelHelper.writeRowToSheet(instructorSalariesSheet, cellValues);
            }

            // Create Instructor Category Costs sheet
            instructorCategoryCostSheet = ExcelHelper.writeRowToSheet(
                    instructorCategoryCostSheet,
                    Arrays.asList(
                            budgetView.getWorkgroup().getName(),
                            "TA",
                            budgetView.getBudget().getTaCost()
                    )
            );
            instructorCategoryCostSheet = ExcelHelper.writeRowToSheet(
                    instructorCategoryCostSheet,
                    Arrays.asList(
                            budgetView.getWorkgroup().getName(),
                            "Reader",
                            budgetView.getBudget().getReaderCost()
                    )
            );
            HashMap<String, Float> instructorTypeCostMap = new HashMap<String, Float>();
            for(InstructorType instructorType : budgetView.getInstructorTypes()){
                instructorTypeCostMap.put(
                        instructorType.getDescription(),
                        null
                );
            }
            for(InstructorTypeCost instructorTypeCost : budgetView.getInstructorTypeCosts()){
                Float currentCost = instructorTypeCostMap.get(instructorTypeCost.getInstructorType().getDescription());
                if(currentCost == null){
                    currentCost = 0.0F;
                }
                instructorTypeCostMap.put(
                        instructorTypeCost.getInstructorType().getDescription(),
                        currentCost + instructorTypeCost.getCost()
                );
            }
            List<String> instructorCategoryTypes = new ArrayList<String>(instructorTypeCostMap.keySet());
            Collections.sort(instructorCategoryTypes);
            for(String instructorCategory : instructorCategoryTypes){
                instructorCategoryCostSheet = ExcelHelper.writeRowToSheet(
                        instructorCategoryCostSheet,
                        Arrays.asList(
                                budgetView.getWorkgroup().getName(),
                                instructorCategory,
                                instructorTypeCostMap.get(instructorCategory)
                        )
                );
            }

        }

        // Expand columns to length of largest value
        workbook = ExcelHelper.expandHeaders(workbook);
        workbook = ExcelHelper.ignoreErrors(workbook, Arrays.asList(IgnoredErrorType.NUMBER_STORED_AS_TEXT));
    }

}
