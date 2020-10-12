package edu.ucdavis.dss.ipa.utilities;
import edu.ucdavis.dss.ipa.entities.*;
import java.util.HashMap;

public final class ActivityLogFormatter {
    private static final HashMap<String, HashMap<String, HashMap<String, Boolean>>> auditProps;
    static{
        HashMap<String, HashMap<String, HashMap<String, Boolean>>> temp =
                new HashMap<String, HashMap<String, HashMap<String, Boolean>>>();

        // Course view entities to be audited.
        HashMap<String, Boolean> courseViewSection = new HashMap<String, Boolean>();

        // Fields to audit in course view for section
        HashMap<String, HashMap<String, Boolean>> courseView = new HashMap<String, HashMap<String, Boolean>>();
        courseViewSection.put("seats", true);
        courseView.put("Section", courseViewSection);
        temp.put("courseViewController", courseView);

        // Budget view entities to be audited.
        HashMap<String, HashMap<String, Boolean>> budgetView = new HashMap<String, HashMap<String, Boolean>>();

        // Fields to audit in budget view for line item (funds)
        HashMap<String, Boolean> budgetViewSection = new HashMap<String, Boolean>();
        budgetViewSection.put("documentNumber", true);
        budgetViewSection.put("amount", true);
        budgetView.put("LineItem", budgetViewSection);
        temp.put("budgetViewController", budgetView);

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
            case "Section":
                Section section = (Section) obj;
                Course sectionCourse = section.getSectionGroup().getCourse();
                return sectionCourse.getSubjectCode() + " " + sectionCourse.getCourseNumber() + " - " + section.getSequenceNumber();
            case "LineItem":
                LineItem lineItem = (LineItem) obj;
                return lineItem.getDescription();
            default:
                return simpleName;
        }

    }

    public static String getEntityDisplayName(Object obj){
        System.err.println("Field being updated is of class " + obj.getClass().getSimpleName() + " is instructor? " + (obj instanceof Instructor));
        if(obj instanceof Instructor){
            Instructor instructor = (Instructor) obj;
            return instructor.getFullName();
        } else{
            return obj.toString();
        }
    }

    public static String getTermCode(Object obj){
        if(obj instanceof Section){
            Section section = (Section) obj;
            return Term.getRegistrarName(section.getSectionGroup().getTermCode());
        } else{
            return "";
        }
    }

    public static String getYear(Object obj) {
        if(obj instanceof Section){
            Section section = (Section) obj;
            return Term.getYear(section.getSectionGroup().getTermCode());
        }else if(obj instanceof LineItem){
            LineItem lineItem = (LineItem) obj;
            return String.valueOf(lineItem.getBudgetScenario().getBudget().getSchedule().getYear());
        } else{
            return "";
        }
    }

    public static long getWorkgroupId(Object obj){
        if(obj instanceof Section){
            Section section = (Section) obj;
            return section.getSectionGroup().getCourse().getSchedule().getWorkgroup().getId();
        } else if(obj instanceof LineItem){
            LineItem lineItem = (LineItem) obj;
            return lineItem.getBudgetScenario().getBudget().getSchedule().getWorkgroup().getId();
        }else{
            return 0;
        }
    }

    public static Boolean isAudited(String module, String entity, String field){
        if(auditProps.containsKey(module) && auditProps.get(module).containsKey(entity) && auditProps.get(module).get(entity).containsKey(field)){
            return auditProps.get(module).get(entity).get(field);
        }
        return false;
    }


}
