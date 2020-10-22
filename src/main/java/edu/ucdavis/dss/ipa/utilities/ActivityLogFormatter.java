package edu.ucdavis.dss.ipa.utilities;
import edu.ucdavis.dss.ipa.entities.*;

import java.time.temporal.Temporal;
import java.util.HashMap;
import java.util.List;

public final class ActivityLogFormatter {
    private static final HashMap<String, HashMap<String, HashMap<String, Boolean>>> auditProps;
    static{
        HashMap<String, HashMap<String, HashMap<String, Boolean>>> temp =
                new HashMap<String, HashMap<String, HashMap<String, Boolean>>>();

        // Course view entities to be audited.
        HashMap<String, HashMap<String, Boolean>> courseView = new HashMap<String, HashMap<String, Boolean>>();

        // Fields to audit in course view for course
        HashMap<String, Boolean> courseViewCourse = new HashMap<String, Boolean>();
        courseView.put("Course", courseViewCourse);

        // Fields to audit in course view for section
        HashMap<String, Boolean> courseViewSection = new HashMap<String, Boolean>();
        courseViewSection.put("seats", true);
        courseView.put("Section", courseViewSection);

        // Fields to audit in course view for sectionGroup
        HashMap<String, Boolean> courseViewSectionGroup = new HashMap<String, Boolean>();
        courseViewSectionGroup.put("termCode", true);
        courseViewSectionGroup.put("plannedSeats", true);
        courseView.put("SectionGroup", courseViewSectionGroup);
        temp.put("courseViewController", courseView);

        // Assignment view entities to be audited.
        HashMap<String, HashMap<String, Boolean>> assignmentView = new HashMap<String, HashMap<String, Boolean>>();

        // Fields to audit in assignment view for teachingAssignment
        HashMap<String, Boolean> teachingAssignmentViewTeachingAssignment = new HashMap<String, Boolean>();
        assignmentView.put("TeachingAssignment", teachingAssignmentViewTeachingAssignment);
        temp.put("assignmentViewTeachingAssignmentController", assignmentView);

        // Budget view entities to be audited
        HashMap<String, HashMap<String, Boolean>> budgetView = new HashMap<String, HashMap<String, Boolean>>();

        // Fields to audit in budget view for budgetScenario
        HashMap<String, Boolean> budgetViewBudgetScenario = new HashMap<String, Boolean>();
        budgetView.put("BudgetScenario", budgetViewBudgetScenario);

        // Fields to audit in budget view for lineItem
        HashMap<String, Boolean> budgetViewLineItem = new HashMap<>();
        budgetViewLineItem.put("amount", true);
        budgetViewLineItem.put("documentNumber", true);
        budgetView.put("LineItem", budgetViewLineItem);

        // Fields to audit in budget view for instructorCost
        HashMap<String, Boolean> budgetViewInstructorCost = new HashMap<>();
        budgetViewInstructorCost.put("cost", true);
        budgetView.put("InstructorCost", budgetViewInstructorCost);

        // Fields to audit in budget view for instructorTypeCost
        HashMap<String, Boolean> budgetViewInstructorTypeCost = new HashMap<>();
        budgetViewInstructorTypeCost.put("cost", true);
        budgetView.put("InstructorTypeCost", budgetViewInstructorTypeCost);

        // Fields to audit in budget view for budget
        // Special case because TA/Reader live under
        // Category Costs (InstructorTypeCosts) in UI
        HashMap<String, Boolean> budgetViewBudget = new HashMap<>();
        budgetViewBudget.put("taCost", true);
        budgetViewBudget.put("readerCost", true);
        budgetView.put("Budget", budgetViewBudget);

        temp.put("budgetViewController", budgetView);

        auditProps = temp;
    }

    // Get workgroup CRUD operations is tied to.
    // Required for all entities
    public static long getWorkgroupId(Object obj){
        if(obj instanceof  Course){
            Course course = (Course) obj;
            return course.getSchedule().getWorkgroup().getId();
        } else if(obj instanceof Section){
            Section section = (Section) obj;
            return section.getSectionGroup().getCourse().getSchedule().getWorkgroup().getId();
        } else if(obj instanceof SectionGroup){
            SectionGroup sectionGroup = (SectionGroup) obj;
            return sectionGroup.getCourse().getSchedule().getWorkgroup().getId();
        } else if(obj instanceof Tag){
            Tag tag = (Tag) obj;
            return tag.getWorkgroup().getId();
        } else if(obj instanceof LineItem){
            LineItem lineItem = (LineItem) obj;
            return lineItem.getBudgetScenario().getBudget().getSchedule().getWorkgroup().getId();
        } else if(obj instanceof TeachingAssignment){
            TeachingAssignment teachingAssignment = (TeachingAssignment) obj;
            return teachingAssignment.getSchedule().getWorkgroup().getId();
        } else if(obj instanceof BudgetScenario){
            BudgetScenario budgetScenario = (BudgetScenario) obj;
            return budgetScenario.getBudget().getSchedule().getWorkgroup().getId();
        } else if (obj instanceof InstructorCost){
            InstructorCost instructorCost = (InstructorCost) obj;
            return instructorCost.getBudget().getSchedule().getWorkgroup().getId();
        } else if (obj instanceof InstructorTypeCost){
            InstructorTypeCost instructorTypeCost = (InstructorTypeCost) obj;
            return instructorTypeCost.getBudget().getSchedule().getWorkgroup().getId();
        } else if (obj instanceof Budget){
            Budget budget = (Budget) obj;
            return budget.getSchedule().getWorkgroup().getId();
        } else {
            return 0;
        }
    }

    // Get user friendly module that originated change.
    // Ex. "courseViewController" becomes "Courses"
    public static String getFormattedModule(String moduleNameRaw){
        switch (moduleNameRaw) {
            case "courseViewController":
                return "Courses";
            case "budgetViewController":
                return "Budget";
            case "assignmentViewTeachingAssignmentController":
                return "Assign Instructors";
            default:
                return moduleNameRaw;
        }
    }

    // Get user friendly description of record.
    public static String getFormattedEntityDescription(Object obj){
        String simpleName = obj.getClass().getSimpleName();
        switch (simpleName){
            case "Course":
                Course course = (Course) obj;
                return "Course: " + course.getSubjectCode() + " " + course.getCourseNumber() + " - " + course.getSequencePattern();
            case "Section":
                Section section = (Section) obj;
                Course sectionCourse = section.getSectionGroup().getCourse();
                return "Section: " + sectionCourse.getSubjectCode() + " " + sectionCourse.getCourseNumber() + " - " + section.getSequenceNumber();
            case "SectionGroup":
                SectionGroup sectionGroup = (SectionGroup) obj;
                Course sectionGroupCourse = sectionGroup.getCourse();
                return "Section Group: " + sectionGroupCourse.getSubjectCode() + " " + sectionGroupCourse.getCourseNumber() + " - " + sectionGroupCourse.getSequencePattern();
            case "LineItem":
                LineItem lineItem = (LineItem) obj;
                return "Fund: " + lineItem.getLineItemCategory().getDescription() +
                        " - " + lineItem.getDescription() + " on " +
                        "Scenario: " + lineItem.getBudgetScenario().getName();
            case "TeachingAssignment":
                TeachingAssignment teachingAssignment = (TeachingAssignment) obj;
                Course teachingAssignmentCourse = teachingAssignment.getSectionGroup().getCourse();
                String instructorName = "";
                if (teachingAssignment.getInstructor() != null){
                    instructorName = teachingAssignment.getInstructor().getFullName();
                } else {
                    instructorName = teachingAssignment.getInstructorType().getDescription();
                }
                return "Assignment: " + instructorName + " on "
                        + teachingAssignmentCourse.getSubjectCode() + " " +
                        teachingAssignmentCourse.getCourseNumber() + " - " +
                        teachingAssignmentCourse.getSequencePattern();
            case "BudgetScenario":
                BudgetScenario budgetScenario = (BudgetScenario) obj;
                return "Budget Scenario: " + budgetScenario.getName();
            case "InstructorCost":
                InstructorCost instructorCost = (InstructorCost) obj;
                return "Salary: " + instructorCost.getInstructor().getFullName();
            case "InstructorTypeCost":
                InstructorTypeCost instructorTypeCost = (InstructorTypeCost) obj;
                return "Category Cost: " + instructorTypeCost.getInstructorType().getDescription();
            case "Budget":
                return "Category Cost:";
            default:
                return simpleName;
        }

    }

    // Get term code if applicable for entity
    public static String getTermCode(Object obj){
        if(obj instanceof Section){
            Section section = (Section) obj;
            return Term.getRegistrarName(section.getSectionGroup().getTermCode());
        } else if(obj instanceof SectionGroup){
            SectionGroup sectionGroup = (SectionGroup) obj;
            return Term.getRegistrarName(sectionGroup.getTermCode());
        } else if (obj instanceof TeachingAssignment){
            TeachingAssignment teachingAssignment = (TeachingAssignment) obj;
            return Term.getRegistrarName(teachingAssignment.getTermCode());
        } else {
            return "";
        }
    }

    // Returns year for use with DB entries
    public static String getYear(Object obj) {
        if(obj instanceof  Course){
            Course course = (Course) obj;
            return String.valueOf(course.getYear());
        } else if(obj instanceof Section){
            Section section = (Section) obj;
            return String.valueOf(section.getSectionGroup().getCourse().getYear());
        } else if (obj instanceof SectionGroup){
            SectionGroup sectionGroup = (SectionGroup) obj;
            return String.valueOf(sectionGroup.getCourse().getYear());
        } else if(obj instanceof LineItem){
            LineItem lineItem = (LineItem) obj;
            return String.valueOf(lineItem.getBudgetScenario().getBudget().getSchedule().getYear());
        } else if(obj instanceof TeachingAssignment){
            TeachingAssignment teachingAssignment = (TeachingAssignment) obj;
            return String.valueOf(teachingAssignment.getSchedule().getYear());
        } else if(obj instanceof BudgetScenario){
            BudgetScenario budgetScenario = (BudgetScenario) obj;
            return String.valueOf(budgetScenario.getBudget().getSchedule().getYear());
        } else if (obj instanceof InstructorCost){
            InstructorCost instructorCost = (InstructorCost) obj;
            return String.valueOf(instructorCost.getBudget().getSchedule().getYear());
        } else if (obj instanceof InstructorTypeCost){
            InstructorTypeCost instructorTypeCost = (InstructorTypeCost) obj;
            return String.valueOf(instructorTypeCost.getBudget().getSchedule().getYear());
        } else if (obj instanceof Budget){
            Budget budget = (Budget) obj;
            return String.valueOf(budget.getSchedule().getYear());
        } else {
            return "0";
        }
    }

    // Returns years ex. 2020-2021, for use with customer facing fields
    public static String getYears(Object obj) {
        if(obj instanceof  Course){
            Course course = (Course) obj;
            return course.getYear() + "-" + (course.getYear() + 1);
        } else if(obj instanceof Section){
            Section section = (Section) obj;
            return Term.getAcademicYear(section.getSectionGroup().getTermCode());
        } else if (obj instanceof SectionGroup){
            SectionGroup sectionGroup = (SectionGroup) obj;
            return Term.getAcademicYear(sectionGroup.getTermCode());
        } else if(obj instanceof LineItem){
            LineItem lineItem = (LineItem) obj;
            return String.valueOf(lineItem.getBudgetScenario().getBudget().getSchedule().getYear());
        } else if(obj instanceof TeachingAssignment){
            TeachingAssignment teachingAssignment = (TeachingAssignment) obj;
            return Term.getAcademicYear(teachingAssignment.getTermCode());
        } else if(obj instanceof BudgetScenario){
            BudgetScenario budgetScenario = (BudgetScenario) obj;
            return budgetScenario.getBudget().getSchedule().getYear() + "-" + (budgetScenario.getBudget().getSchedule().getYear()+1);
        } else if (obj instanceof InstructorCost){
            InstructorCost instructorCost = (InstructorCost) obj;
            return instructorCost.getBudget().getSchedule().getYear() + "-" +  (instructorCost.getBudget().getSchedule().getYear()+1);
        } else if (obj instanceof InstructorTypeCost){
            InstructorTypeCost instructorTypeCost = (InstructorTypeCost) obj;
            return instructorTypeCost.getBudget().getSchedule().getYear() + "-" +  (instructorTypeCost.getBudget().getSchedule().getYear()+1);
        } else if (obj instanceof Budget){
            Budget budget = (Budget) obj;
            return budget.getSchedule().getYear() + "-" + (budget.getSchedule().getYear()+1);
        }else {
            return "";
        }
    }

    // Returns user friendly property name.  Ex. "termCode" becomes "term"
    // Applicable only to update
    public static String getFormattedPropName(String prop){
        switch (prop){
            case "termCode":
                return "term";
            case "plannedSeats":
                return "planned seats";
            case "taCost":
                return "TA";
            case "readerCost":
                return "Reader";
            default:
                return prop;
        }
    }

    // Return user friendly prop value.  Ex. Instructor reference becomes "Edgar Perez"
    // Applicable only to update
    public static String getFormattedPropValue(String propName, Object obj){
        if(obj instanceof Instructor){
            Instructor instructor = (Instructor) obj;
            return instructor.getFullName();
        } else if (propName == "termCode"){
            return Term.getRegistrarName(obj.toString());
        } else {
            if(obj != null){
                return obj.toString();
            }else{
                return null;
            }
        }
    }

    // Check field level audit
    public static Boolean isAudited(String module, String entity, String field){
        if(auditProps.containsKey(module) && auditProps.get(module).containsKey(entity) && auditProps.get(module).get(entity).containsKey(field)){
            return auditProps.get(module).get(entity).get(field);
        }
        return false;
    }

    // Check entity level audit
    public static Boolean isAudited(String module, String entity){
        if(auditProps.containsKey(module) && auditProps.get(module).containsKey(entity)){
            return true;
        }
        return false;
    }


}
