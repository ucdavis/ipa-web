package edu.ucdavis.dss.ipa.api.components.budget.views;

import static edu.ucdavis.dss.ipa.entities.enums.BudgetSummary.*;
import static edu.ucdavis.dss.ipa.entities.enums.InstructorDescription.*;

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

    private List<BudgetScenarioExcelView> budgetScenarioExcelViews;
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
            private double unassignedCost = 0;
            private double enrollment = 0;
            private double unitsLow = 0;
            private double unitsHigh = 0;
            private double unitsOffered = 0;
            private double lowerDivOfferings = 0;
            private double upperDivOfferings = 0;
            private double graduateOfferings = 0;
            private double studentCreditHoursUndergrad = 0;
            private double studentCreditHoursGrad = 0;


            public BudgetSummaryTerm(BudgetScenarioExcelView budgetScenarioExcelView, SectionGroupCost sectionGroupCost){
                this.taCount = (double) (sectionGroupCost.getTaCount() == null ? 0.0F : sectionGroupCost.getTaCount());
                this.readerCount = (double) (sectionGroupCost.getReaderCount() == null ? 0.0F: sectionGroupCost.getReaderCount() );
                this.unitsLow = (double) (sectionGroupCost.getUnitsLow() == null ? 0.0F : sectionGroupCost.getUnitsLow());
                this.unitsHigh = (double) (sectionGroupCost.getUnitsHigh() == null ? 0.0F : sectionGroupCost.getUnitsHigh());
                if((sectionGroupCost.getUnitsHigh() == null ? 0.0F : sectionGroupCost.getUnitsHigh()) > 0.0F){
                    unitsOffered = (double) (sectionGroupCost.getUnitsVariable() == null ? 0.0F : sectionGroupCost.getUnitsVariable());
                } else{
                    unitsOffered = (double) (sectionGroupCost.getUnitsLow() == null ? 0.0F : sectionGroupCost.getUnitsLow());
                }
                boolean isGrad = Integer.parseInt(sectionGroupCost.getCourseNumber().replaceAll("[^\\d.]", "")) >= 200;
                if(isGrad){
                    this.graduateOfferings = 1;
                } else if(Integer.parseInt(sectionGroupCost.getCourseNumber().replaceAll("[^\\d.]", "")) > 99){
                    this.upperDivOfferings = 1;
                } else{
                    lowerDivOfferings = 1;
                }

                double creditHours = 0.0;
                if((sectionGroupCost.getUnitsVariable() == null ? 0.0F : sectionGroupCost.getUnitsVariable()) > 0.0F){
                    creditHours = (sectionGroupCost.getEnrollment() == null ? 0.0F : sectionGroupCost.getEnrollment() * sectionGroupCost.getUnitsVariable());
                } else if((sectionGroupCost.getUnitsHigh() == null ? 0.0F : sectionGroupCost.getUnitsHigh()) > 0.0F){
                    creditHours = 0;
                } else{
                    creditHours = (sectionGroupCost.getEnrollment() == null ? 0.0F : sectionGroupCost.getEnrollment() * sectionGroupCost.getUnitsLow());
                }
                if(isGrad){
                    this.studentCreditHoursGrad = creditHours;
                } else{
                    this.studentCreditHoursUndergrad = creditHours;
                }

                long instructorTypeId = 0;
                double instructorCostAmount = 0.0;

                if(sectionGroupCost.getInstructor() != null){
                    if(sectionGroupCost.getInstructorTypeIdentification() != null){
                        instructorTypeId = sectionGroupCost.getInstructorTypeIdentification();
                    } else{
                        UserRole instructorRole = getUserService().getOneByLoginId(sectionGroupCost.getInstructor().getLoginId()).getUserRoles().stream().filter(ur -> (ur.getRole().getId() == 15 && budgetScenarioExcelView.getWorkgroup().getId() == ur.getWorkgroup().getId())).findFirst().orElse(null);
                        if(instructorRole != null){
                            instructorTypeId = instructorRole.getInstructorType().getId();
                        }
                    }
                }else{
                    if(sectionGroupCost.getInstructorTypeIdentification() != null){
                        instructorTypeId = sectionGroupCost.getInstructorTypeIdentification();
                    }
                }
                if(sectionGroupCost.getCost() != null){
                    instructorCostAmount = sectionGroupCost.getCost().doubleValue();
                }else{
                    if(sectionGroupCost.getInstructor() != null){
                        InstructorCost instructorCost = getInstructorCostService().findByInstructorIdAndBudgetId(sectionGroupCost.getInstructor().getId(), budgetScenarioExcelView.getBudget().getId());

                        if(instructorCost != null && instructorCost.getCost() != null){
                            instructorCostAmount = (instructorCost.getCost() == null ? 0.0F : instructorCost.getCost().doubleValue());
                        }else{
                            final long instructorTypeIdFinal = instructorTypeId;
                            InstructorTypeCost instructorTypeCost = getInstructorTypeCostService().findByBudgetId(budgetScenarioExcelView.getBudget().getId()).stream().filter(itc -> itc.getInstructorTypeIdIfExists() == instructorTypeIdFinal).findFirst().orElse(null);
                            if(instructorTypeCost != null){
                                instructorCostAmount = instructorTypeCost.getCost();
                            }
                        }
                    } else if (instructorTypeId > 0){
                        final long instructorTypeIdFinal = instructorTypeId;
                        InstructorTypeCost instructorTypeCost = getInstructorTypeCostService().findByBudgetId(budgetScenarioExcelView.getBudget().getId()).stream().filter(itc -> itc.getInstructorTypeIdIfExists() == instructorTypeIdFinal).findFirst().orElse(null);
                        if(instructorTypeCost != null){
                            instructorCostAmount = instructorTypeCost.getCost();
                        }
                    }
                }
                if(instructorTypeId == EMERITI.typeId()){
                    this.emeritiCost = instructorCostAmount;
                } else if (instructorTypeId == VISITING_PROFESSOR.typeId()){
                    this.visitingProfessorCost = instructorCostAmount;
                } else if (instructorTypeId == ASSOCIATE_PROFESSOR.typeId()){
                    this.associateInstructorCost = instructorCostAmount;
                } else if (instructorTypeId == UNIT18_LECTURER.typeId()){
                    this.unit18LecturerCost = instructorCostAmount;
                } else if (instructorTypeId == CONTINUING_LECTURER.typeId()){
                    this.continuingLecturerCost = instructorCostAmount;
                } else if (instructorTypeId == LADDER_FACULTY.typeId()){
                    this.ladderFacultyCost = instructorCostAmount;
                } else if (instructorTypeId == INSTRUCTOR.typeId()){
                    this.instructorCost = instructorCostAmount;
                } else if (instructorTypeId == LECTURER_SOE.typeId()){
                    this.lecturerSOECost = instructorCostAmount;
                } else{
                    this.unassignedCost = instructorCostAmount;
                }
                enrollment = sectionGroupCost.getEnrollment();
            }

            public void add(BudgetScenarioExcelView budgetScenarioExcelView, SectionGroupCost sectionGroupCost){
                this.taCount += (double) (sectionGroupCost.getTaCount() == null ? 0.0F : sectionGroupCost.getTaCount());
                this.readerCount += (double) (sectionGroupCost.getReaderCount() == null ? 0.0F: sectionGroupCost.getReaderCount() );
                this.unitsLow += (double) (sectionGroupCost.getUnitsLow() == null ? 0.0F : sectionGroupCost.getUnitsLow());
                this.unitsHigh += (double) (sectionGroupCost.getUnitsHigh() == null ? 0.0F : sectionGroupCost.getUnitsHigh());

                if((sectionGroupCost.getUnitsHigh() == null ? 0.0F : sectionGroupCost.getUnitsHigh()) > 0.0F){
                    unitsOffered += (double) (sectionGroupCost.getUnitsVariable() == null ? 0.0F : sectionGroupCost.getUnitsVariable());
                } else{
                    unitsOffered += (double) (sectionGroupCost.getUnitsLow() == null ? 0.0F : sectionGroupCost.getUnitsLow());
                }
                boolean isGrad = Integer.parseInt(sectionGroupCost.getCourseNumber().replaceAll("[^\\d.]", "")) >= 200;
                if(isGrad){
                    this.graduateOfferings += 1;
                } else if(Integer.parseInt(sectionGroupCost.getCourseNumber().replaceAll("[^\\d.]", "")) > 99){
                    this.upperDivOfferings += 1;
                } else{
                    lowerDivOfferings += 1;
                }

                double creditHours = 0.0;
                if((sectionGroupCost.getUnitsVariable() == null ? 0.0F : sectionGroupCost.getUnitsVariable()) > 0.0F){
                    creditHours = (sectionGroupCost.getEnrollment() == null ? 0.0F : sectionGroupCost.getEnrollment() * sectionGroupCost.getUnitsVariable());
                } else if((sectionGroupCost.getUnitsHigh() == null ? 0.0F : sectionGroupCost.getUnitsHigh()) > 0.0F){
                    creditHours = 0;
                } else{
                    creditHours = (sectionGroupCost.getEnrollment() == null ? 0.0F : sectionGroupCost.getEnrollment() * sectionGroupCost.getUnitsLow());
                }
                if(isGrad){
                    this.studentCreditHoursGrad += creditHours;
                } else{
                    this.studentCreditHoursUndergrad += creditHours;
                }

                long instructorTypeId = 0;
                double instructorCostAmount = 0.0;
                if(sectionGroupCost.getInstructor() != null){
                    if(sectionGroupCost.getInstructorTypeIdentification() != null){
                        instructorTypeId = sectionGroupCost.getInstructorTypeIdentification();
                    } else{
                        UserRole instructorRole = getUserService().getOneByLoginId(sectionGroupCost.getInstructor().getLoginId()).getUserRoles().stream().filter(ur -> (ur.getRole().getId() == 15 && budgetScenarioExcelView.getWorkgroup().getId() == ur.getWorkgroup().getId())).findFirst().orElse(null);
                        if(instructorRole != null){
                            instructorTypeId = instructorRole.getInstructorType().getId();
                        }
                    }
                }else{
                    if(sectionGroupCost.getInstructorTypeIdentification() != null){
                        instructorTypeId = sectionGroupCost.getInstructorTypeIdentification();
                    }
                }
                if(sectionGroupCost.getCost() != null){
                   instructorCostAmount = sectionGroupCost.getCost().doubleValue();
                }else{
                    if(sectionGroupCost.getInstructor() != null){
                        InstructorCost instructorCost = getInstructorCostService().findByInstructorIdAndBudgetId(sectionGroupCost.getInstructor().getId(), budgetScenarioExcelView.getBudget().getId());

                        if(instructorCost != null && instructorCost.getCost() != null){
                            instructorCostAmount = (instructorCost.getCost() == null ? 0.0F : instructorCost.getCost().doubleValue());
                        }else{
                            final long instructorTypeIdFinal = instructorTypeId;
                            InstructorTypeCost instructorTypeCost = getInstructorTypeCostService().findByBudgetId(budgetScenarioExcelView.getBudget().getId()).stream().filter(itc -> itc.getInstructorTypeIdIfExists() == instructorTypeIdFinal).findFirst().orElse(null);
                            if(instructorTypeCost != null){
                                instructorCostAmount = instructorTypeCost.getCost();
                            }
                        }
                    } else if (instructorTypeId > 0){
                        final long instructorTypeIdFinal = instructorTypeId;
                        InstructorTypeCost instructorTypeCost = getInstructorTypeCostService().findByBudgetId(budgetScenarioExcelView.getBudget().getId()).stream().filter(itc -> itc.getInstructorTypeIdIfExists() == instructorTypeIdFinal).findFirst().orElse(null);
                        if(instructorTypeCost != null){
                            instructorCostAmount = instructorTypeCost.getCost();
                        }
                    }
                }
                if(instructorTypeId == EMERITI.typeId()){
                    this.emeritiCost += instructorCostAmount;
                } else if (instructorTypeId == VISITING_PROFESSOR.typeId()){
                    this.visitingProfessorCost += instructorCostAmount;
                } else if (instructorTypeId == ASSOCIATE_PROFESSOR.typeId()){
                    this.associateInstructorCost += instructorCostAmount;
                } else if (instructorTypeId == UNIT18_LECTURER.typeId()){
                    this.unit18LecturerCost += instructorCostAmount;
                } else if (instructorTypeId == CONTINUING_LECTURER.typeId()){
                    this.continuingLecturerCost += instructorCostAmount;
                } else if (instructorTypeId == LADDER_FACULTY.typeId()){
                    this.ladderFacultyCost += instructorCostAmount;
                } else if (instructorTypeId == INSTRUCTOR.typeId()){
                    this.instructorCost += instructorCostAmount;
                } else if (instructorTypeId == LECTURER_SOE.typeId()){
                    this.lecturerSOECost += instructorCostAmount;
                } else {
                    this.unassignedCost += instructorCostAmount;
                }
                enrollment += sectionGroupCost.getEnrollment();
            }
        }

        private HashMap<String, BudgetSummaryTerm> terms = new HashMap<String, BudgetSummaryTerm>();
        private double taCost;
        private double readerCost;
        private String department;
        private BudgetScenarioExcelView budgetScenarioExcelView;
        private List<String> termCodes;

        public BudgetSummaryTerms(BudgetScenarioExcelView budgetScenarioExcelView, String department, float taCost, float readerCost){
            this.department = department;
            this.taCost = (double) taCost;
            this.readerCost = (double) readerCost;
            this.budgetScenarioExcelView = budgetScenarioExcelView;
            this.termCodes = budgetScenarioExcelView.getTermCodes();
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

        private double getUnassignedCost(String termCode){
            if(terms.get(termCode) != null){
                BigDecimal bd = new BigDecimal(terms.get(termCode).unassignedCost).setScale(2, RoundingMode.HALF_UP);
                return bd.doubleValue();
            } else{
                return 0.0;
            }
        }

        private double getReplacementCost(String termCode){
            return getEmeritiCost(termCode) + getVisitingProfessorCost(termCode) + getAssociateInstructorCost(termCode) +
                    getUnit18LecturerCost(termCode) + getContinuingLecturerCost(termCode) + getLadderFacultyCost(termCode) +
                    getInstructorCost(termCode) + getLecturerSOECost(termCode) + getUnassignedCost(termCode);
        }

        private double getTotalTeachingCost(String termCode){
            return getSupportCost(termCode) + getReplacementCost(termCode);
        }

        private double getTotalFunds(){
            BigDecimal totalFunds = new BigDecimal(0).setScale(2);
            for(LineItem lineItem : budgetScenarioExcelView.getLineItems()){
                totalFunds = totalFunds.add(lineItem.getAmount());
            }
            return totalFunds.doubleValue();
        }

        private double getBalance(){
            double value = getTotalFunds();
            for(String termCode : termCodes){
                value -= getTotalTeachingCost(termCode);
            }
            return value;
        }

        private double getUnits(String termCode){
            if(terms.get(termCode) != null){
                BigDecimal bd = new BigDecimal(terms.get(termCode).unitsOffered).setScale(2, RoundingMode.HALF_UP);
                return bd.doubleValue();
            } else{
                return 0.0;
            }
        }

        private double getEnrollment(String termCode){
            if(terms.get(termCode) != null){
                BigDecimal bd = new BigDecimal(terms.get(termCode).enrollment).setScale(2, RoundingMode.HALF_UP);
                return bd.doubleValue();
            } else{
                return 0.0;
            }
        }

        private double getStudentCreditHoursUndergrad(String termCode){
            if(terms.get(termCode) != null){
                BigDecimal bd = new BigDecimal(terms.get(termCode).studentCreditHoursUndergrad).setScale(2, RoundingMode.HALF_UP);
                return bd.doubleValue();
            } else{
                return 0.0;
            }
        }

        private double getStudentCreditHoursGrad(String termCode){
            if(terms.get(termCode) != null){
                BigDecimal bd = new BigDecimal(terms.get(termCode).studentCreditHoursGrad).setScale(2, RoundingMode.HALF_UP);
                return bd.doubleValue();
            } else{
                return 0.0;
            }
        }

        private double getStudentCreditHours(String termCode){
            return getStudentCreditHoursGrad(termCode) + getStudentCreditHoursUndergrad(termCode);
        }


        private double getLowerDivOfferings(String termCode){
            if(terms.get(termCode) != null){
                BigDecimal bd = new BigDecimal(terms.get(termCode).lowerDivOfferings).setScale(2, RoundingMode.HALF_UP);
                return bd.doubleValue();
            } else{
                return 0.0;
            }
        }

        private double getUpperDivOfferings(String termCode){
            if(terms.get(termCode) != null){
                BigDecimal bd = new BigDecimal(terms.get(termCode).upperDivOfferings).setScale(2, RoundingMode.HALF_UP);
                return bd.doubleValue();
            } else{
                return 0.0;
            }
        }

        private double getGraduateOfferings(String termCode){
            if(terms.get(termCode) != null){
                BigDecimal bd = new BigDecimal(terms.get(termCode).graduateOfferings).setScale(2, RoundingMode.HALF_UP);
                return bd.doubleValue();
            } else{
                return 0.0;
            }
        }

        private double getTotalOfferings(String termCode){
            return getLowerDivOfferings(termCode) + getUpperDivOfferings(termCode) + getGraduateOfferings(termCode);
        }

        private List<Object> rowData(String field){
            List<Object> data = new ArrayList<Object>();

            data.add(department);
            data.add(budgetScenarioExcelView.budgetScenario.getName());
            data.add(field);

            if (field == "Funds Cost"){
                for(String termCode: termCodes){
                    data.add("");
                }
                double totalFunds = getTotalFunds();
                data.add(totalFunds);
                return data;
            } else if (field == "Balance"){
                for(String termCode: termCodes){
                    data.add("");
                }
                double totalBalance = getBalance();
                data.add(totalBalance);
                return data;
            }

            double totalValue = 0;
            double value = 0;
            for(String termCode: termCodes){
                switch(field){
                    case "TA Count":
                        value = getTaCount(termCode);
                        totalValue += value;
                        data.add(value);
                        break;
                    case "TA Cost":
                        value = getTaCost(termCode);
                        totalValue += value;
                        data.add(value);
                        break;
                    case "Reader Count":
                        value = getReaderCount(termCode);
                        totalValue += value;
                        data.add(value);
                        break;
                    case "Reader Cost":
                        value = getReaderCost(termCode);
                        totalValue += value;
                        data.add(value);
                        break;
                    case "Support Cost":
                        value = getSupportCost(termCode);
                        totalValue += value;
                        data.add(value);
                        break;
                    case "Associate Instructor":
                        value = getAssociateInstructorCost(termCode);
                        totalValue += value;
                        data.add(value);
                        break;
                    case "Continuing Lecturer":
                        value = getContinuingLecturerCost(termCode);
                        totalValue += value;
                        data.add(value);
                        break;
                    case "Emeriti - Recalled":
                        value = getEmeritiCost(termCode);
                        totalValue += value;
                        data.add(value);
                        break;
                    case "Instructor":
                        value = getInstructorCost(termCode);
                        totalValue += value;
                        data.add(value);
                        break;
                    case "Ladder Faculty":
                        value = getLadderFacultyCost(termCode);
                        totalValue += value;
                        data.add(value);
                        break;
                    case "Lecturer SOE":
                        value = getLecturerSOECost(termCode);
                        totalValue += value;
                        data.add(value);
                        break;
                    case "Unit 18 Pre-Six Lecturer":
                        value = getUnit18LecturerCost(termCode);
                        totalValue += value;
                        data.add(value);
                        break;
                    case "Visiting Professor":
                        value = getVisitingProfessorCost(termCode);
                        totalValue += value;
                        data.add(value);
                        break;
                    case "Unassigned":
                        value = getUnassignedCost(termCode);
                        totalValue += value;
                        data.add(value);
                        break;
                    case "Replacement Cost":
                        value = getReplacementCost(termCode);
                        totalValue += value;
                        data.add(value);
                        break;
                    case "Total Teaching Costs":
                        value = getTotalTeachingCost(termCode);
                        totalValue += value;
                        data.add(value);
                        break;
                    case "Units Offered":
                        value = getUnits(termCode);
                        totalValue += value;
                        data.add(value);
                        break;
                    case "Enrollment":
                        value = getEnrollment(termCode);
                        totalValue += value;
                        data.add(value);
                        break;
                    case "Student Credit Hours (Undergrad)":
                        value = getStudentCreditHoursUndergrad(termCode);
                        totalValue += value;
                        data.add(value);
                        break;
                    case "Student Credit Hours (Graduate)":
                        value = getStudentCreditHoursGrad(termCode);
                        totalValue += value;
                        data.add(value);
                        break;
                    case "Student Credit Hours":
                        value = getStudentCreditHours(termCode);
                        totalValue += value;
                        data.add(value);
                        break;
                    case "Lower Div Offerings":
                        value = getLowerDivOfferings(termCode);
                        totalValue += value;
                        data.add(value);
                        break;
                    case "Upper Div Offerings":
                        value = getUpperDivOfferings(termCode);
                        totalValue += value;
                        BigDecimal test = budgetScenarioExcelView.termTotals.get(termCode).get(UPPER_DIV_OFFERINGS);
                        data.add(value);
                        break;
                    case "Graduate Offerings":
                        value = getGraduateOfferings(termCode);
                        totalValue += value;
                        data.add(value);
                        break;
                    case "Total Offerings":
                        value = getTotalOfferings(termCode);
                        totalValue += value;
                        data.add(value);
                        break;
                }

            }
            data.add(totalValue);
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
                            rowData(row)
                    );
                }
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
        fundsSheet = ExcelHelper.setSheetHeader(fundsSheet, Arrays.asList("Department", "Scenario Name", "Type", "Description", "Amount"));

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
                budgetTerms.addSection(sectionGroupCost);
            }

            budgetSummarySheet = budgetTerms.writeTerms(budgetSummarySheet);

            // Create Funds sheet
            for(LineItem lineItem : budgetScenarioExcelView.getLineItems()){
                List<Object> cellValues = Arrays.asList(
                        budgetScenarioExcelView.getWorkgroup().getName(),
                        budgetScenarioExcelView.getBudgetScenario().getName(),
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
