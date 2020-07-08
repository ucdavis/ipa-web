package edu.ucdavis.dss.ipa.api.components.budget.views;

import edu.ucdavis.dss.ipa.api.helpers.SpringContext;
import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.InstructorCost;
import edu.ucdavis.dss.ipa.entities.InstructorType;
import edu.ucdavis.dss.ipa.entities.InstructorTypeCost;
import edu.ucdavis.dss.ipa.entities.LineItem;
import edu.ucdavis.dss.ipa.entities.SectionGroupCost;
import edu.ucdavis.dss.ipa.entities.TeachingAssignment;
import edu.ucdavis.dss.ipa.entities.Term;
import edu.ucdavis.dss.ipa.entities.User;
import edu.ucdavis.dss.ipa.entities.UserRole;
import edu.ucdavis.dss.ipa.services.InstructorCostService;
import edu.ucdavis.dss.ipa.services.InstructorTypeCostService;
import edu.ucdavis.dss.ipa.services.UserService;
import edu.ucdavis.dss.ipa.utilities.ExcelHelper;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
import org.springframework.web.servlet.view.document.AbstractXlsxView;

public class BudgetExcelView extends AbstractXlsxView {
    private InstructorCostService getInstructorCostService() {
        return SpringContext.getBean(InstructorCostService.class);
    }

    private UserService getUserService() {
        return SpringContext.getBean(UserService.class);
    }

    private InstructorTypeCostService getInstructorTypeCostService() {
        return SpringContext.getBean(InstructorTypeCostService.class);
    }

    private List<BudgetScenarioExcelView> budgetScenarioExcelViews = null;
    public BudgetExcelView ( List<BudgetScenarioExcelView> budgetScenarioExcelViews) {
        this.budgetScenarioExcelViews = budgetScenarioExcelViews;
    }

    public class BudgetSummaryTerms {

        public class BudgetSummaryTerm{
            private double taCount;
            private double readerCount;
            private double emeritiCost = 0;
            private double visitingProfessorCost = 0;
            private double associateInstructorCost = 0;
            private double unit18LecturerCost = 0;
            private double continuingLecturerCost = 0;
            private double ladderFacultyCost = 0;
            private double instructorCost = 0;
            private double lecturerSOECost = 0;
            private double lowerDivUnits;
            private double upperDivUnits;

//            public double calculateInstructorCost(BudgetScenarioExcelView budgetScenarioExcelView, SectionGroupCost sectionGroupCost){
//                if(sectionGroupCost.getCost() == null){
//                    Long budgetId = sectionGroupCost.getBudgetScenario().getId();
//                    if(sectionGroupCost.getInstructor() != null){
//                        Long instructorId = sectionGroupCost.getInstructor().getId();
////                        InstructorCost instructorCost = getInstructorCostService().findByInstructorIdAndBudgetId(instructorId, budgetId);
//                        InstructorCost instructorCost = budgetScenarioExcelView.getInstructorCosts().stream().filter(ic -> ic.getInstructorTypeIdentification() == 3 && ic.getInstructor().getId() == sectionGroupCost.getInstructor().getId()).findFirst().orElse(null);
//                        if(instructorCost == null){
//                            return 0.0;
//                        } else{
//                            double cost = instructorCost.getCost().doubleValue();
//                            System.err.println("Found cost, cost was " + cost + " Id is " + instructorCost.getId() + " Section group cost ID was " + sectionGroupCost.getId());
//                            return cost;
//                        }
//                    } else{
//                        return 0.0;
//                    }
//                } else{
//                    return sectionGroupCost.getCost().doubleValue();
//                }
//            }

            public BudgetSummaryTerm(BudgetScenarioExcelView budgetScenarioExcelView, SectionGroupCost sectionGroupCost){
                this.taCount = (double) (sectionGroupCost.getTaCount() == null ? 0.0F : sectionGroupCost.getTaCount());
                this.readerCount = (double) (sectionGroupCost.getReaderCount() == null ? 0.0F: sectionGroupCost.getReaderCount() );
                this.lowerDivUnits = (double) (sectionGroupCost.getUnitsLow() == null ? 0.0F : sectionGroupCost.getUnitsLow());
                this.upperDivUnits = (double) (sectionGroupCost.getUnitsHigh() == null ? 0.0F : sectionGroupCost.getUnitsHigh());
                long instructorTypeId = 0;
                double instructorCostAmount = 0.0;
                if(sectionGroupCost.getInstructor() != null){
                    UserRole instructorRole = getUserService().getOneByLoginId(sectionGroupCost.getInstructor().getLoginId()).getUserRoles().stream().filter(ur -> (ur.getRole().getId() == 15 && budgetScenarioExcelView.getWorkgroup().getId() == ur.getWorkgroup().getId())).findFirst().orElse(null);
                    if(instructorRole != null){
                        System.err.println("We found their role! " + instructorRole.getInstructorType().getId() + " " + instructorRole.getInstructorType().getDescription());
                        instructorTypeId = instructorRole.getInstructorType().getId();
                    }
                }else{
                    //System.err.println("We did not find their role :/");
                    if(sectionGroupCost.getInstructorTypeIdentification() != null){
                        instructorTypeId = sectionGroupCost.getInstructorTypeIdentification();
                    }
                }
                if(sectionGroupCost.getCost() != null){
                    instructorCostAmount = sectionGroupCost.getCost().doubleValue();
                }else{
                    if(sectionGroupCost.getInstructor() != null){
                        InstructorCost instructorCost = getInstructorCostService().findByInstructorIdAndBudgetId(sectionGroupCost.getInstructor().getId(), budgetScenarioExcelView.getBudget().getId());
                        instructorCostAmount = (instructorCost == null ? 0.0F : instructorCost.getCost().doubleValue());
                    } else if (instructorTypeId > 0){
                        final long instructorTypeIdFinal = instructorTypeId;
                        InstructorTypeCost instructorTypeCost = getInstructorTypeCostService().findByBudgetId(budgetScenarioExcelView.getBudget().getId()).stream().filter(itc -> itc.getInstructorTypeIdIfExists() == instructorTypeIdFinal).findFirst().orElse(null);
                        if(instructorTypeCost != null){
                            instructorCostAmount = instructorTypeCost.getCost();
                        }
                    }
                }
                if(instructorTypeId == 1){
                    this.emeritiCost = instructorCostAmount;
                } else if (instructorTypeId == 2){
                    this.visitingProfessorCost = instructorCostAmount;
                } else if (instructorTypeId == 3){
                    this.associateInstructorCost = instructorCostAmount;
                } else if (instructorTypeId == 4){
                    this.unit18LecturerCost = instructorCostAmount;
                } else if (instructorTypeId == 5){
                    this.continuingLecturerCost = instructorCostAmount;
                } else if (instructorTypeId == 6){
                    this.ladderFacultyCost = instructorCostAmount;
                } else if (instructorTypeId == 7){
                    this.instructorCost = instructorCostAmount;
                } else if (instructorTypeId == 8){
                    this.lecturerSOECost = instructorCostAmount;
                }

            }

            public void add(BudgetScenarioExcelView budgetScenarioExcelView, SectionGroupCost sectionGroupCost){
                this.taCount += (int) (sectionGroupCost.getTaCount() == null ? 0.0F : sectionGroupCost.getTaCount());
                this.readerCount += (int) (sectionGroupCost.getReaderCount() == null ? 0.0F: sectionGroupCost.getReaderCount() );
                this.lowerDivUnits += (double) (sectionGroupCost.getUnitsLow() == null ? 0.0F : sectionGroupCost.getUnitsLow());
                this.upperDivUnits += (double) (sectionGroupCost.getUnitsHigh() == null ? 0.0F : sectionGroupCost.getUnitsHigh());
                this.associateInstructorCost += 0.0;
                this.continuingLecturerCost += 0.0;
                long instructorTypeId = 0;
                double instructorCostAmount = 0.0;
                if(sectionGroupCost.getInstructor() != null){
                    UserRole instructorRole = getUserService().getOneByLoginId(sectionGroupCost.getInstructor().getLoginId()).getUserRoles().stream().filter(ur -> (ur.getRole().getId() == 15 && budgetScenarioExcelView.getWorkgroup().getId() == ur.getWorkgroup().getId())).findFirst().orElse(null);
                    if(instructorRole != null){
                        System.err.println("We found their role! " + instructorRole.getInstructorType().getId() + " " + instructorRole.getInstructorType().getDescription());
                        instructorTypeId = instructorRole.getInstructorType().getId();
                    }
                }else{
                    //System.err.println("We did not find their role :/");
                    if(sectionGroupCost.getInstructorTypeIdentification() != null){
                        instructorTypeId = sectionGroupCost.getInstructorTypeIdentification();
                    }
                }
                if(sectionGroupCost.getCost() != null){
                   instructorCostAmount = sectionGroupCost.getCost().doubleValue();
                }else{
                    if(sectionGroupCost.getInstructor() != null){
                        InstructorCost instructorCost = getInstructorCostService().findByInstructorIdAndBudgetId(sectionGroupCost.getInstructor().getId(), budgetScenarioExcelView.getBudget().getId());
                        instructorCostAmount = (instructorCost == null ? 0.0F : instructorCost.getCost().doubleValue());
                    } else if (instructorTypeId > 0){
                        final long instructorTypeIdFinal = instructorTypeId;
                        InstructorTypeCost instructorTypeCost = getInstructorTypeCostService().findByBudgetId(budgetScenarioExcelView.getBudget().getId()).stream().filter(itc -> itc.getInstructorTypeIdIfExists() == instructorTypeIdFinal).findFirst().orElse(null);
                        if(instructorTypeCost != null){
                            instructorCostAmount = instructorTypeCost.getCost();
                        }
                    }
                }
                if(instructorTypeId == 1){
                    this.emeritiCost += instructorCostAmount;
                } else if (instructorTypeId == 2){
                    this.visitingProfessorCost += instructorCostAmount;
                } else if (instructorTypeId == 3){
                    this.associateInstructorCost += instructorCostAmount;
                } else if (instructorTypeId == 4){
                    this.unit18LecturerCost += instructorCostAmount;
                } else if (instructorTypeId == 5){
                    this.continuingLecturerCost += instructorCostAmount;
                } else if (instructorTypeId == 6){
                    this.ladderFacultyCost += instructorCostAmount;
                } else if (instructorTypeId == 7){
                    this.instructorCost += instructorCostAmount;
                } else if (instructorTypeId == 8){
                    this.lecturerSOECost += instructorCostAmount;
                }
            }
        }

        private HashMap<String, BudgetSummaryTerm> terms = new HashMap<String, BudgetSummaryTerm>();
        private double taCost;
        private double readerCost;
        private String department;
        private BudgetScenarioExcelView budgetScenarioExcelView;

        public BudgetSummaryTerms(BudgetScenarioExcelView budgetScenarioExcelView, String department, float taCost, float readerCost){
            this.department = department;
            this.taCost = (double) taCost;
            this.readerCost = (double) readerCost;
            this.budgetScenarioExcelView = budgetScenarioExcelView;
        }
        public void addSection(SectionGroupCost sectionGroupCost){
            if(this.terms.get(sectionGroupCost.getTermCode()) == null){
                terms.put(sectionGroupCost.getTermCode(), new BudgetSummaryTerm(budgetScenarioExcelView, sectionGroupCost));
            } else{
                terms.get(sectionGroupCost.getTermCode()).add(budgetScenarioExcelView, sectionGroupCost);
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
                BigDecimal bd = new BigDecimal(terms.get(termCode).associateInstructorCost).setScale(2, RoundingMode.HALF_UP);
                return bd.doubleValue();
            } else{
                return 0.0;
            }
        }

        private double getContinuingLecturerCost(String termCode){
            if(terms.get(termCode) != null){
                BigDecimal bd = new BigDecimal(terms.get(termCode).continuingLecturerCost).setScale(2, RoundingMode.HALF_UP);
                return bd.doubleValue();
            } else{
                return 0.0;
            }
        }

        private double getEmeritiCost(String termCode){
            if(terms.get(termCode) != null){
                BigDecimal bd = new BigDecimal(terms.get(termCode).emeritiCost).setScale(2, RoundingMode.HALF_UP);
                return bd.doubleValue();
            } else{
                return 0.0;
            }
        }

        private double getVisitingProfessorCost(String termCode){
            if(terms.get(termCode) != null){
                BigDecimal bd = new BigDecimal(terms.get(termCode).visitingProfessorCost).setScale(2, RoundingMode.HALF_UP);
                return bd.doubleValue();
            } else{
                return 0.0;
            }
        }

        private double getUnit18LecturerCost(String termCode){
            if(terms.get(termCode) != null){
                BigDecimal bd = new BigDecimal(terms.get(termCode).unit18LecturerCost).setScale(2, RoundingMode.HALF_UP);
                return bd.doubleValue();
            } else{
                return 0.0;
            }
        }

        private double getLadderFacultyCost(String termCode){
            if(terms.get(termCode) != null){
                BigDecimal bd = new BigDecimal(terms.get(termCode).ladderFacultyCost).setScale(2, RoundingMode.HALF_UP);
                return bd.doubleValue();
            } else{
                return 0.0;
            }
        }

        private double getInstructorCost(String termCode){
            if(terms.get(termCode) != null){
                BigDecimal bd = new BigDecimal(terms.get(termCode).instructorCost).setScale(2, RoundingMode.HALF_UP);
                return bd.doubleValue();
            } else{
                return 0.0;
            }
        }

        private double getLecturerSOECost(String termCode){
            if(terms.get(termCode) != null){
                BigDecimal bd = new BigDecimal(terms.get(termCode).lecturerSOECost).setScale(2, RoundingMode.HALF_UP);
                return bd.doubleValue();
            } else{
                return 0.0;
            }
        }

        private double getReplacementCost(String termCode){
            return getEmeritiCost(termCode) + getVisitingProfessorCost(termCode) + getAssociateInstructorCost(termCode) +
                    getUnit18LecturerCost(termCode) + getContinuingLecturerCost(termCode) + getLadderFacultyCost(termCode) +
                    getInstructorCost(termCode) + getLecturerSOECost(termCode);
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
            } else if (field == "Continuing Lecturer"){
                for(String termCode: termCodes){
                    data.add(getContinuingLecturerCost(termCode));
                }
            } else if (field == "Emeriti - Recalled"){
                for(String termCode: termCodes){
                    data.add(getEmeritiCost(termCode));
                }
            } else if (field == "Instructor"){
                for(String termCode: termCodes){
                    data.add(getInstructorCost(termCode));
                }
            } else if (field == "Ladder Faculty"){
                for(String termCode: termCodes){
                    data.add(getLadderFacultyCost(termCode));
                }
            } else if (field == "Lecturer SOE"){
                for(String termCode: termCodes){
                    data.add(getLecturerSOECost(termCode));
                }
            } else if (field == "Unit 18 Pre-Six Lecturer"){
                for(String termCode: termCodes){
                    data.add(getUnit18LecturerCost(termCode));
                }
            } else if (field == "Visiting Professor"){
                for(String termCode: termCodes){
                    data.add(getVisitingProfessorCost(termCode));
                }
            } else if (field == "Replacement Cost"){
                for(String termCode: termCodes){
                    data.add(getReplacementCost(termCode));
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
                    "Associate Instructor",
                    "Continuing Lecturer",
                    "Emeriti - Recalled",
                    "Instructor",
                    "Ladder Faculty",
                    "Lecturer SOE",
                    "Unit 18 Pre-Six Lecturer",
                    "Visiting Professor",
                    "Replacement Cost"

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

        for (BudgetScenarioExcelView budgetScenarioExcelView : budgetScenarioExcelViews) {
            Long scenarioId = budgetScenarioExcelView.getBudgetScenario().getId();

            BudgetSummaryTerms budgetTerms = new BudgetSummaryTerms(
                    budgetScenarioExcelView,
                    budgetScenarioExcelView.getWorkgroup().getName(),
                    budgetScenarioExcelView.getBudget().getTaCost(),
                    budgetScenarioExcelView.getBudget().getReaderCost());

            // Create Schedule Cost sheet
            for(SectionGroupCost sectionGroupCost : budgetScenarioExcelView.getSectionGroupCosts().stream().filter(sgc -> sgc.getBudgetScenario().getId() == scenarioId).sorted(Comparator.comparing(SectionGroupCost::getTermCode).thenComparing(SectionGroupCost::getSubjectCode).thenComparing(SectionGroupCost::getCourseNumber)).collect(Collectors.toList()) ){
                Float taCost = (sectionGroupCost.getTaCount() == null ? 0.0F : sectionGroupCost.getTaCount()) * budgetScenarioExcelView.getBudget().getTaCost();
                Float readerCost = (sectionGroupCost.getReaderCount() == null ? 0.0F: sectionGroupCost.getReaderCount() ) * budgetScenarioExcelView.getBudget().getReaderCost();
                Float supportCost = taCost + readerCost;
                Float sectionCost = sectionGroupCost.getCost() == null ? 0.0F : sectionGroupCost.getCost().floatValue();
                scheduleCostSheet = ExcelHelper.writeRowToSheet(
                        scheduleCostSheet,
                        Arrays.asList(
                                budgetScenarioExcelView.getWorkgroup().getName(),
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
            for(LineItem lineItem : budgetScenarioExcelView.getLineItems().stream().filter(li -> li.getBudgetScenarioId() == scenarioId).collect(Collectors.toList())){
                List<Object> cellValues = Arrays.asList(
                        budgetScenarioExcelView.getWorkgroup().getName(),
                        lineItem.getLineItemCategory().getDescription(),
                        lineItem.getDescription(),
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

                User user = budgetScenarioExcelView.users.stream().filter(u -> u.getLoginId().equals(instructor.getLoginId())).findFirst().orElse(null);
                UserRole userRole = user.getUserRoles().stream().filter(ur -> (ur.getRole().getId() == 15 && budgetScenarioExcelView.getWorkgroup().getId() == ur.getWorkgroup().getId())).findFirst().orElse(null);
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
            HashMap<String, Float> instructorTypeCostMap = new HashMap<String, Float>();
            for(InstructorType instructorType : budgetScenarioExcelView.getInstructorTypes()){
                instructorTypeCostMap.put(
                        instructorType.getDescription(),
                        null
                );
            }
            for(InstructorTypeCost instructorTypeCost : budgetScenarioExcelView.getInstructorTypeCosts()){
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
    }

}
