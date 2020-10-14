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

        /*// Budget view entities to be audited.
        HashMap<String, HashMap<String, Boolean>> budgetView = new HashMap<String, HashMap<String, Boolean>>();

        // Fields to audit in budget view for line item (funds)
        HashMap<String, Boolean> budgetViewSection = new HashMap<String, Boolean>();
        budgetViewSection.put("documentNumber", true);
        budgetViewSection.put("amount", true);
        budgetView.put("LineItem", budgetViewSection);
        temp.put("budgetViewController", budgetView);*/

        auditProps = temp;
    }

    public static String getFormattedModule(String moduleNameRaw){
        System.err.println("Raw Module is " + moduleNameRaw);
        switch (moduleNameRaw) {
            case "courseViewController":
                return "Courses";
            case "budgetViewController":
                return "Budget";
            default:
                return "";
        }
    }

    public static String getFormattedEntityDescription(Object obj){
        String simpleName = obj.getClass().getSimpleName();
        System.err.println("Object is of class " + obj.getClass().getSimpleName());
        switch (simpleName){
            case "Course":
                Course course = (Course) obj;
                return course.getSubjectCode() + " " + course.getCourseNumber() + " - " + course.getSequencePattern();
            case "Section":
                Section section = (Section) obj;
                Course sectionCourse = section.getSectionGroup().getCourse();
                return sectionCourse.getSubjectCode() + " " + sectionCourse.getCourseNumber() + " - " + section.getSequenceNumber();
            case "SectionGroup":
                SectionGroup sectionGroup = (SectionGroup) obj;
                Course sectionGroupCourse = sectionGroup.getCourse();
                return sectionGroupCourse.getSubjectCode() + " " + sectionGroupCourse.getCourseNumber() + " - " + sectionGroupCourse.getSequencePattern();
            case "LineItem":
                LineItem lineItem = (LineItem) obj;
                return lineItem.getDescription();
            default:
                return simpleName;
        }

    }

    public static String getFormattedPropName(String prop){
        switch (prop){
            case "termCode":
                return "Term";
            case "plannedSeats":
                return "planned seats";
            default:
                return prop;
        }
    }

    public static String getFormattedPropValue(String propName, Object obj){
        if(obj instanceof Instructor){
            Instructor instructor = (Instructor) obj;
            return instructor.getFullName();
        } else if (propName == "termCode"){
            return Term.getRegistrarName(obj.toString());
        } else {
            if(obj == null){
                return "";
            }else {
                return obj.toString();
            }
        }
    }

    public static String getTermCode(Object obj){
        if(obj instanceof Section){
            Section section = (Section) obj;
            return Term.getRegistrarName(section.getSectionGroup().getTermCode());
        } else if(obj instanceof SectionGroup){
            SectionGroup sectionGroup = (SectionGroup) obj;
            return Term.getRegistrarName(sectionGroup.getTermCode());
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
        } else{
            return "";
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
        } else{
            return "";
        }
    }

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
        }else{
            return 0;
        }
    }

    // For updates
    public static Boolean isAudited(String module, String entity, String field){
        if(auditProps.containsKey(module) && auditProps.get(module).containsKey(entity) && auditProps.get(module).get(entity).containsKey(field)){
            return auditProps.get(module).get(entity).get(field);
        }
        return false;
    }

    // For inserts
    public static Boolean isAudited(String module, String entity){
        if(auditProps.containsKey(module) && auditProps.get(module).containsKey(entity)){
            return true;
        }
        return false;
    }


}
