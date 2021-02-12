package edu.ucdavis.dss.ipa.utilities;
import edu.ucdavis.dss.ipa.entities.*;

import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;

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
        courseViewSectionGroup.put("readerAppointments", true);
        courseViewSectionGroup.put("teachingAssistantAppointments", true);
        courseView.put("SectionGroup", courseViewSectionGroup);

        HashMap<String, Boolean> courseViewTeachingAssignment = new HashMap<String, Boolean>();
        courseView.put("TeachingAssignment", courseViewTeachingAssignment);
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
        budgetViewBudgetScenario.put("name", true);
        budgetView.put("BudgetScenario", budgetViewBudgetScenario);

        // Fields to audit in budget view for lineItem
        HashMap<String, Boolean> budgetViewLineItem = new HashMap<>();
        budgetViewLineItem.put("amount", true);
        budgetViewLineItem.put("documentNumber", true);
        budgetViewLineItem.put("description", true);
        budgetViewLineItem.put("notes", true);
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

        // Fields to audit in budget view for sectionGroupCost
        HashMap<String, Boolean> budgetViewSectionGroupCost = new HashMap<>();
        budgetViewSectionGroupCost.put("reason", true);
        budgetViewSectionGroupCost.put("reasonCategory", true);
        budgetViewSectionGroupCost.put("originalInstructor", true);
        budgetViewSectionGroupCost.put("enrollment", true);
        budgetViewSectionGroupCost.put("sectionCount", true);
        budgetViewSectionGroupCost.put("taCount", true);
        budgetViewSectionGroupCost.put("readerCount", true);
        budgetViewSectionGroupCost.put("disabled", true);
        budgetView.put("SectionGroupCost", budgetViewSectionGroupCost);

        // Fields to audit in budget view for Section Group Cost Instructor
        HashMap<String, Boolean> budgetViewSectionGroupCostInstructor = new HashMap<>();
        budgetViewSectionGroupCostInstructor.put("cost", true);
        budgetViewSectionGroupCostInstructor.put("instructor", true);
        budgetViewSectionGroupCostInstructor.put("instructorType", true);
        budgetView.put("SectionGroupCostInstructor", budgetViewSectionGroupCostInstructor);

        // Fields to audit in budget view for Expense Items
        HashMap<String, Boolean> budgetViewExpenseItem = new HashMap<>();
        budgetViewExpenseItem.put("amount", true);
        budgetViewExpenseItem.put("description", true);
        budgetViewExpenseItem.put("termCode", true);
        budgetViewExpenseItem.put("expenseItemType", true);
        budgetView.put("ExpenseItem", budgetViewExpenseItem);

        temp.put("budgetViewController", budgetView);

        // Fields to audit for the Section Group Cost Controller
        HashMap<String, HashMap<String, Boolean>> sectionGroupCostController = new HashMap<String, HashMap<String, Boolean>>();
        HashMap<String, Boolean> sectionGroupCostControllerSectionGroupCost = new HashMap<>();
        sectionGroupCostController.put("SectionGroupCost", sectionGroupCostControllerSectionGroupCost);
        temp.put("sectionGroupCostController", sectionGroupCostController);

        // Fields to audit in the scheduling view for Activities
        HashMap<String, HashMap<String, Boolean>> schedulingViewController = new HashMap<String, HashMap<String, Boolean>>();
        HashMap<String, Boolean> schedulingViewControllerActivity = new HashMap<>();
        schedulingViewControllerActivity.put("location", true);
        schedulingViewControllerActivity.put("frequency", true);
        schedulingViewControllerActivity.put("startTime", true);
        schedulingViewControllerActivity.put("endTime", true);
        schedulingViewControllerActivity.put("dayIndicator", true);
        schedulingViewController.put("Activity", schedulingViewControllerActivity);
        temp.put("schedulingViewController", schedulingViewController);

        // Fields to audit for TA's and Readers
        HashMap<String, HashMap<String, Boolean>> instructionalAssignmentsController = new HashMap<>();
        HashMap<String, Boolean> instructionalAssignmentsControllerSupportAssignments = new HashMap<>();
        instructionalAssignmentsController.put("SupportAssignment", instructionalAssignmentsControllerSupportAssignments);
        HashMap<String, Boolean> instructionalAssignmentsControllerSupportAppointments = new HashMap<>();
        instructionalAssignmentsControllerSupportAppointments.put("percentage", true);
        instructionalAssignmentsController.put("SupportAppointment", instructionalAssignmentsControllerSupportAppointments);

        temp.put("instructionalSupportAssignmentsController", instructionalAssignmentsController);

        HashMap<String, HashMap<String, Boolean>> instructionalSupportCallsController = new HashMap<>();
        HashMap<String, Boolean> instructionalSupportCallsControllerSchedule = new HashMap<>();
        instructionalSupportCallsControllerSchedule.put("supportStaffSupportCallReviewOpen", true);
        instructionalSupportCallsControllerSchedule.put("instructorSupportCallReviewOpen", true);
        instructionalSupportCallsController.put("Schedule", instructionalSupportCallsControllerSchedule);

        HashMap<String, Boolean> instructionalSupportCallsControllerInstructorSupportCallResponse = new HashMap<>();
        instructionalSupportCallsControllerInstructorSupportCallResponse.put("nextContactAt", true);
        instructionalSupportCallsController.put("InstructorSupportCallResponse", instructionalSupportCallsControllerInstructorSupportCallResponse);

        temp.put("instructionalSupportCallsController", instructionalSupportCallsController);

        HashMap<String, Boolean> instructionalSupportCallsControllerStudentSupportCall = new HashMap<>();
        instructionalSupportCallsControllerStudentSupportCall.put("nextContactAt", true);
        instructionalSupportCallsController.put("StudentSupportCallResponse", instructionalSupportCallsControllerStudentSupportCall);

        temp.put("instructionalSupportCallsController", instructionalSupportCallsController);

        HashMap<String, HashMap<String, Boolean>> teachingCallStatusViewController =  new HashMap<>();
        HashMap<String, Boolean> teachingCallStatusViewControllerTeachingCallReceipt = new HashMap<>();
        teachingCallStatusViewControllerTeachingCallReceipt.put("nextContactAt", true);
        teachingCallStatusViewController.put("TeachingCallReceipt", teachingCallStatusViewControllerTeachingCallReceipt);
        temp.put("teachingCallStatusViewController", teachingCallStatusViewController);

        HashMap<String, HashMap<String, Boolean>> assignmentViewController = new HashMap<String, HashMap<String, Boolean>>();
        HashMap<String, Boolean> assignmentViewControllerTeachingAssignment= new HashMap<>();
        assignmentViewController.put("TeachingAssignment", assignmentViewControllerTeachingAssignment);
        temp.put("assignmentViewController", assignmentViewController);

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
        } else if (obj instanceof SectionGroupCost){
            SectionGroupCost sectionGroupCost = (SectionGroupCost) obj;
            return sectionGroupCost.getBudgetScenario().getBudget().getSchedule().getWorkgroup().getId();
        } else if (obj instanceof SectionGroupCostInstructor){
            SectionGroupCostInstructor sectionGroupCostInstructor = (SectionGroupCostInstructor) obj;
            return sectionGroupCostInstructor.getSectionGroupCost().getBudgetScenario().getBudget().getSchedule().getWorkgroup().getId();
        } else if (obj instanceof Activity){
            Activity activity = (Activity) obj;
            if(activity.getSectionGroup() != null){
                return activity.getSectionGroup().getCourse().getSchedule().getWorkgroup().getId();
            } else {
                return activity.getSection().getSectionGroup().getCourse().getSchedule().getWorkgroup().getId();
            }
        } else if (obj instanceof ExpenseItem) {
            ExpenseItem expenseItem = (ExpenseItem) obj;
            return expenseItem.getBudgetScenario().getBudget().getSchedule().getWorkgroup().getId();
        } else if(obj instanceof SupportAssignment){
            SupportAssignment supportAssignment = (SupportAssignment) obj;
            if(supportAssignment.getSectionGroup() != null){
                return supportAssignment.getSectionGroup().getCourse().getSchedule().getWorkgroup().getId();
            } else {
                return supportAssignment.getSection().getSectionGroup().getCourse().getSchedule().getWorkgroup().getId();
            }
        } else if (obj instanceof SupportAppointment){
            SupportAppointment supportAppointment = (SupportAppointment) obj;
            return supportAppointment.getSchedule().getWorkgroup().getId();
        } else if (obj instanceof Schedule){
            Schedule schedule = (Schedule) obj;
            return schedule.getWorkgroup().getId();
        } else if (obj instanceof TeachingCallReceipt){
            TeachingCallReceipt teachingCallReceipt = (TeachingCallReceipt) obj;
            return teachingCallReceipt.getWorkgroupId();
        } else if (obj instanceof InstructorSupportCallResponse){
            InstructorSupportCallResponse instructorSupportCallResponse = (InstructorSupportCallResponse) obj;
            return instructorSupportCallResponse.getSchedule().getWorkgroup().getId();
        } else if (obj instanceof StudentSupportCallResponse){
            StudentSupportCallResponse studentSupportCallResponse = (StudentSupportCallResponse) obj;
            return studentSupportCallResponse.getSchedule().getWorkgroup().getId();
        } else {
            return 0;
        }
    }

    // Get DB friendly module that originated change.
    // Ex. "courseViewController" becomes "Courses"
    public static String getFormattedModule(String moduleNameRaw, Object obj){
        switch (moduleNameRaw) {
            case "courseViewController":
                if(obj instanceof TeachingAssignment){
                    return "Assign Instructors";
                } else {
                    return "Courses";
                }
            case "budgetViewController":
                return "Budget";
            case "assignmentViewTeachingAssignmentController":
            case "assignmentViewController":
                return "Assign Instructors";
            case "sectionGroupCostController":
                return "Budget";
            case "schedulingViewController":
                return "Scheduling";
            case "instructionalSupportAssignmentsController":
                return "Support Staff Assignments";
            case "instructionalSupportCallsController":
                return "Support Calls";
            case "teachingCallStatusViewController":
                return "Teaching Calls";
            default:
                return moduleNameRaw;
        }
    }

    // Get DB friendly module that originated change for updates
    // Updates may take the specific field into account
    public static String getFormattedModule(String moduleNameRaw, Object obj, String propName){
        if(obj instanceof SectionGroup && (propName.equals("teachingAssistantAppointments") || propName.equals("readerAppointments"))){
            return "Support Staff Assignments";
        } else if (obj instanceof Schedule && (propName.equals("supportStaffSupportCallReviewOpen") || propName.equals("instructorSupportCallReviewOpen"))) {
            return "Support Staff Assignments";
        } else {
            return ActivityLogFormatter.getFormattedModule(moduleNameRaw, obj);
        }
    }

    // Get user friendly module that originated change.
    // Ex. "courseViewController" becomes "Courses"
    public static String getModuleDisplayName(String moduleNameRaw, Object obj){
        switch (moduleNameRaw) {
            case "courseViewController":
                return "Courses";
            case "budgetViewController":
                if(obj instanceof SectionGroupCost){
                    SectionGroupCost sectionGroupCost = (SectionGroupCost) obj;
                    return "Budget - Schedule Costs - " + sectionGroupCost.getBudgetScenario().getName();
                } else if(obj instanceof SectionGroupCostInstructor){
                    SectionGroupCostInstructor sectionGroupCostInstructor = (SectionGroupCostInstructor) obj;
                    return "Budget - Schedule Costs - " + sectionGroupCostInstructor.getSectionGroupCost().getBudgetScenario().getName();
                } else if (obj instanceof LineItem){
                    LineItem lineItem = (LineItem) obj;
                    return "Budget - Funds - " + lineItem.getBudgetScenario().getName();
                } else if (obj instanceof InstructorCost || obj instanceof InstructorTypeCost){
                    return "Budget - Instructor List";
                } else if (obj instanceof ExpenseItem) {
                    return "Budget - Other Costs";
                } else {
                    return "Budget";
                }
            case "assignmentViewTeachingAssignmentController":
            case "assignmentViewController":
                return "Assign Instructors";
            case "sectionGroupCostController":
                if(obj instanceof SectionGroupCost){
                    SectionGroupCost sectionGroupCost = (SectionGroupCost) obj;
                    return "Budget - Course List - " + sectionGroupCost.getBudgetScenario().getName();
                } else{
                    return "Budget - Course List";
                }
            case "schedulingViewController":
                return "Scheduling";
            case "instructionalSupportAssignmentsController":
                return "Support Staff Assignments";
            case "instructionalSupportCallsController":
                return "Support Calls";
            case "teachingCallStatusViewController":
                return "Teaching Calls";
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
                        " - " + lineItem.getDescription();
            case "TeachingAssignment":
                TeachingAssignment teachingAssignment = (TeachingAssignment) obj;
                String instructorName = "";
                if (teachingAssignment.getInstructor() != null){
                    instructorName = teachingAssignment.getInstructor().getFullName();
                } else {
                    instructorName = teachingAssignment.getInstructorType().getDescription();
                }
                if(teachingAssignment.getSectionGroup() != null){
                    Course teachingAssignmentCourse = teachingAssignment.getSectionGroup().getCourse();
                    return "Assignment: " + instructorName + " on "
                            + teachingAssignmentCourse.getSubjectCode() + " " +
                            teachingAssignmentCourse.getCourseNumber() + " - " +
                            teachingAssignmentCourse.getSequencePattern();
                } else {
                    String teachingAssignmentDisplayName = "Assignment: " + instructorName;
                    if(teachingAssignment.isBuyout()){
                        return "Buyout " +  teachingAssignmentDisplayName;
                    } else if (teachingAssignment.isSabbatical()){
                        return "Sabbatical " +  teachingAssignmentDisplayName;
                    } else if (teachingAssignment.isInResidence()){
                        return "In Residence " +  teachingAssignmentDisplayName;
                    } else if (teachingAssignment.isWorkLifeBalance()){
                        return "Work Life Balance " +  teachingAssignmentDisplayName;
                    } else if (teachingAssignment.isLeaveOfAbsence()){
                        return "Leave of Absence " +  teachingAssignmentDisplayName;
                    } else if (teachingAssignment.isSabbaticalInResidence()){
                        return "Sabbatical in Residence " +  teachingAssignmentDisplayName;
                    } else if (teachingAssignment.isCourseRelease()){
                        return "Course Release " +  teachingAssignmentDisplayName;
                    } else {
                        return  teachingAssignmentDisplayName;
                    }

                }
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
            case "SectionGroupCost":
                SectionGroupCost sectionGroupCost = (SectionGroupCost) obj;
                return "Schedule Cost: " + sectionGroupCost.getSubjectCode() +
                        " " + sectionGroupCost.getCourseNumber() +
                        " - " + sectionGroupCost.getSequencePattern();
            case "SectionGroupCostInstructor":
                SectionGroupCostInstructor sectionGroupCostInstructor = (SectionGroupCostInstructor) obj;
                SectionGroupCost sgc = sectionGroupCostInstructor.getSectionGroupCost();
                String scheduleCostDescription = "Schedule Cost: " +
                        sgc.getSubjectCode() +
                        " " + sgc.getCourseNumber() +
                        " - " + sgc.getSequencePattern();
                if(sectionGroupCostInstructor.getInstructor() != null){
                    return scheduleCostDescription + ", Instructor: " + sectionGroupCostInstructor.getInstructor().getFullName();
                } else {
                    return scheduleCostDescription + ", Instructor: " + sectionGroupCostInstructor.getInstructorTypeDescription();
                }
            case "Activity":
                Activity activity = (Activity) obj;
                if(activity.getSectionGroup() != null){
                    Course activityCourse  = activity.getSectionGroup().getCourse();
                    return "Section " + activityCourse.getSubjectCode() + " " +
                            activityCourse.getCourseNumber() + " - " +
                            activityCourse.getSequencePattern() +
                            ", Activity: " + activity.getActivityTypeCodeDescription();
                } else {
                    Course activityCourse  = activity.getSection().getSectionGroup().getCourse();
                    return "Section " + activityCourse.getSubjectCode() + " " +
                            activityCourse.getCourseNumber() + " - " +
                            activityCourse.getSequencePattern() +
                            ", Activity: " + activity.getActivityTypeCodeDescription();
                }
            case "ExpenseItem":
                ExpenseItem expenseItem = (ExpenseItem) obj;
                return expenseItem.getExpenseItemTypeDescription() + " - " + expenseItem.getDescription();
            case "SupportAssignment":
                SupportAssignment supportAssignment = (SupportAssignment) obj;
                String assignmentType = supportAssignment.getAppointmentType();
                if(assignmentType.equals("teachingAssistant")){
                    assignmentType = "Teaching Assistant";
                } else if (assignmentType.equals("reader")) {
                    assignmentType = "Reader";
                }
                if (supportAssignment.getSectionGroup() != null){
                    Course supportAssignmentCourse  = supportAssignment.getSectionGroup().getCourse();
                    return assignmentType + " Assignment: " + supportAssignment.getSupportStaff().getFullName() +
                            " on Section " + supportAssignmentCourse.getSubjectCode() + " " +
                            supportAssignmentCourse.getCourseNumber() + " - " +
                            supportAssignmentCourse.getSequencePattern();

                } else {
                    Course supportAssignmentCourse  = supportAssignment.getSection().getSectionGroup().getCourse();
                    return assignmentType + " Assignment: " + supportAssignment.getSupportStaff().getFullName() +
                            " on Section " + supportAssignmentCourse.getSubjectCode() + " " +
                            supportAssignmentCourse.getCourseNumber() + " - " +
                            supportAssignmentCourse.getSequencePattern();
                }
            case "SupportAppointment":
                SupportAppointment supportAppointment = (SupportAppointment) obj;
                return "Staff: " + supportAppointment.getSupportStaff().getFullName();
            case "Schedule":
                Schedule schedule = (Schedule) obj;
                return "Schedule: " + schedule.getWorkgroup().getName();
            case "TeachingCallReceipt":
                TeachingCallReceipt teachingCallReceipt = (TeachingCallReceipt) obj;
                return "Teaching Call: " + teachingCallReceipt.getInstructor().getFullName();

            case "InstructorSupportCallResponse":
                InstructorSupportCallResponse instructorSupportCallResponse = (InstructorSupportCallResponse) obj;
                return "Instructor Support Call: " + instructorSupportCallResponse.getInstructor().getFullName();
            case "StudentSupportCallResponse":
                StudentSupportCallResponse studentSupportCallResponse = (StudentSupportCallResponse) obj;
                return "Support Staff Support Call: " + studentSupportCallResponse.getSupportStaff().getFullName();
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
        } else if (obj instanceof SectionGroupCost) {
            SectionGroupCost sectionGroupCost = (SectionGroupCost) obj;
            return Term.getRegistrarName(sectionGroupCost.getTermCode());
        } else if (obj instanceof SectionGroupCostInstructor) {
            SectionGroupCostInstructor sectionGroupCostInstructor = (SectionGroupCostInstructor) obj;
            return Term.getRegistrarName(sectionGroupCostInstructor.getSectionGroupCost().getTermCode());
        } else if (obj instanceof Activity){
            Activity activity = (Activity) obj;
            if(activity.getSectionGroup() != null){
                return Term.getRegistrarName(activity.getSectionGroup().getTermCode());
            } else {
                return Term.getRegistrarName(activity.getSection().getSectionGroup().getTermCode());
            }
        } else if (obj instanceof ExpenseItem){
            ExpenseItem expenseItem = (ExpenseItem) obj;
            return Term.getRegistrarName(expenseItem.getTermCode());
        } else if (obj instanceof SupportAssignment){
            SupportAssignment supportAssignment = (SupportAssignment) obj;
            if(supportAssignment.getSectionGroup() != null){
                return Term.getRegistrarName(supportAssignment.getSectionGroup().getTermCode());
            } else {
                return Term.getRegistrarName(supportAssignment.getSection().getSectionGroup().getTermCode());
            }
        } else if (obj instanceof SupportAppointment) {
            SupportAppointment supportAppointment = (SupportAppointment) obj;
            return Term.getRegistrarName(supportAppointment.getTermCode());
        } else if (obj instanceof InstructorSupportCallResponse){
            InstructorSupportCallResponse instructorSupportCallResponse = (InstructorSupportCallResponse) obj;
            return Term.getRegistrarName(instructorSupportCallResponse.getTermCode());
        } else if (obj instanceof StudentSupportCallResponse){
            StudentSupportCallResponse studentSupportCallResponse = (StudentSupportCallResponse) obj;
            return Term.getRegistrarName(studentSupportCallResponse.getTermCode());
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
        } else if (obj instanceof SectionGroupCost){
            SectionGroupCost sectionGroupCost = (SectionGroupCost) obj;
            return Term.getYear(sectionGroupCost.getTermCode());
        } else if (obj instanceof SectionGroupCostInstructor){
            SectionGroupCostInstructor sectionGroupCostInstructor = (SectionGroupCostInstructor) obj;
            return Term.getYear(sectionGroupCostInstructor.getSectionGroupCost().getTermCode());
        } else if (obj instanceof Activity){
            Activity activity = (Activity) obj;
            if(activity.getSectionGroup() != null){
                return String.valueOf(activity.getSectionGroup().getCourse().getYear());
            } else {
                return String.valueOf(activity.getSection().getSectionGroup().getCourse().getYear());
            }

        } else if (obj instanceof ExpenseItem) {
            ExpenseItem expenseItem = (ExpenseItem) obj;
            return String.valueOf(expenseItem.getBudgetScenario().getBudget().getSchedule().getYear());
        } else if (obj instanceof SupportAssignment){
            SupportAssignment supportAssignment = (SupportAssignment) obj;
            if(supportAssignment.getSectionGroup() != null){
                return String.valueOf(supportAssignment.getSectionGroup().getCourse().getYear());
            } else {
                return String.valueOf(supportAssignment.getSection().getSectionGroup().getCourse().getYear());
            }

        } else if (obj instanceof SupportAppointment) {
            SupportAppointment supportAppointment = (SupportAppointment) obj;
            return String.valueOf(supportAppointment.getSchedule().getYear());
        } else if (obj instanceof Schedule){
            Schedule schedule = (Schedule) obj;
            return String.valueOf(schedule.getYear());
        } else if (obj instanceof TeachingCallReceipt){
            TeachingCallReceipt teachingCallReceipt = (TeachingCallReceipt) obj;
            return String.valueOf(teachingCallReceipt.getAcademicYear());
        } else if (obj instanceof InstructorSupportCallResponse){
            InstructorSupportCallResponse instructorSupportCallResponse = (InstructorSupportCallResponse) obj;
            return String.valueOf(instructorSupportCallResponse.getSchedule().getYear());
        } else if (obj instanceof StudentSupportCallResponse){
            StudentSupportCallResponse studentSupportCallResponse = (StudentSupportCallResponse) obj;
            return String.valueOf(studentSupportCallResponse.getSchedule().getYear());
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
            return lineItem.getBudgetScenario().getBudget().getSchedule().getYear() + "-" + (lineItem.getBudgetScenario().getBudget().getSchedule().getYear()+1);
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
        } else if (obj instanceof SectionGroupCost){
            SectionGroupCost sectionGroupCost = (SectionGroupCost) obj;
            return Term.getAcademicYear(sectionGroupCost.getTermCode());
        } else if (obj instanceof SectionGroupCostInstructor){
            SectionGroupCostInstructor sectionGroupCostInstructor = (SectionGroupCostInstructor) obj;
            return Term.getAcademicYear(sectionGroupCostInstructor.getSectionGroupCost().getTermCode());
        } else if (obj instanceof Activity){
            Activity activity = (Activity) obj;
            if(activity.getSectionGroup() != null){
                return Term.getAcademicYear(activity.getSectionGroup().getTermCode());
            } else {
                return Term.getAcademicYear(activity.getSection().getSectionGroup().getTermCode());
            }
        } else if (obj instanceof ExpenseItem){
            ExpenseItem expenseItem = (ExpenseItem) obj;
            long expenseItemYear = expenseItem.getBudgetScenario().getBudget().getSchedule().getYear();
            return expenseItemYear + "-" + (expenseItemYear+1);
        }  else if (obj instanceof SupportAssignment){
            SupportAssignment supportAssignment = (SupportAssignment) obj;
            if(supportAssignment.getSectionGroup() != null){
                return Term.getAcademicYear(supportAssignment.getSectionGroup().getTermCode());
            } else {
                return Term.getAcademicYear(supportAssignment.getSection().getSectionGroup().getTermCode());
            }
        } else if (obj instanceof SupportAppointment) {
            SupportAppointment supportAppointment = (SupportAppointment) obj;
            return Term.getAcademicYear(supportAppointment.getTermCode());
        } else if (obj instanceof Schedule){
            Schedule schedule = (Schedule) obj;
            return schedule.getYear() + "-" + (schedule.getYear()+1);
        } else if (obj instanceof TeachingCallReceipt){
            TeachingCallReceipt teachingCallReceipt = (TeachingCallReceipt) obj;
            return teachingCallReceipt.getAcademicYear() + "-" + (teachingCallReceipt.getAcademicYear()+1);
        } else if (obj instanceof InstructorSupportCallResponse){
            InstructorSupportCallResponse instructorSupportCallResponse = (InstructorSupportCallResponse) obj;
            return instructorSupportCallResponse.getSchedule().getYear() + "-" + (instructorSupportCallResponse.getSchedule().getYear()+1);
        } else if (obj instanceof StudentSupportCallResponse){
            StudentSupportCallResponse studentSupportCallResponse = (StudentSupportCallResponse) obj;
            return studentSupportCallResponse.getSchedule().getYear() + "-" + (studentSupportCallResponse.getSchedule().getYear()+1);
        } else {
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
                return "reader";
            case "reason":
                return "additional comments";
            case "reasonCategory":
                return "reason category";
            case "enrollment":
                return "enroll";
            case "sectionCount":
                return "sections";
            case "taCount":
                return "TAs";
            case "readerCount":
                return "readers";
            case "instructorType":
                return "instructor type";
            case "originalInstructor":
                return "regular instructor";
            case "frequency":
                return "Repeats every";
            case "dayIndicator":
                return "days";
            case "startTime":
                return "start time";
            case "endTime":
                return "end time";
            case "expenseItemType":
                return "type";
            case "readerAppointments":
                return "Readers";
            case "teachingAssistantAppointments":
                return "TAs";
            case "supportStaffSupportCallReviewOpen":
                return "Student Review";
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
        } else if (propName.equals("termCode")){
            return Term.getRegistrarName(obj.toString());
        } else if (obj instanceof ReasonCategory){
            ReasonCategory reasonCategory = (ReasonCategory) obj;
            return reasonCategory.getDescription();
        } else if (obj instanceof InstructorType){
            InstructorType instructorType = (InstructorType) obj;
            return instructorType.getDescription();
        } else if(obj instanceof Location){
            Location location = (Location) obj;
            return location.getDescription();
        } else if (propName.equals("frequency")){
            return obj.toString() + " week(s)";
        } else if (propName.equals("dayIndicator")){
            return Activity.getDayIndicatorDescription(obj.toString());
        } else if (obj instanceof Time){
            Time time = (Time) obj;
            DateFormat format = new SimpleDateFormat( "h:mm a" );
            return format.format( time.getTime() );
        } else if (obj instanceof ExpenseItemType) {
            ExpenseItemType expenseItemType = (ExpenseItemType) obj;
            return expenseItemType.getDescription();
        } else {
            if(obj != null){
                return obj.toString();
            }else{
                return null;
            }
        }
    }

    // Check field level audit
    public static Boolean isFieldAudited(String module, String entity, String field){
        if(auditProps.containsKey(module) && auditProps.get(module).containsKey(entity) && auditProps.get(module).get(entity).containsKey(field)){
            return auditProps.get(module).get(entity).get(field);
        }
        return false;
    }

    // Check entity level audit
    // We want to make sure the endpoint is actually for the entity in question
    // Otherwise, even if configured skip it.
    public static Boolean isAudited(String module, String entity, String endpoint){

        if(endpoint.toLowerCase().contains(entity.toLowerCase()) && auditProps.containsKey(module) && auditProps.get(module).containsKey(entity)){
            return true;
        } else if (entity.equals("BudgetScenario") && endpoint.equals("budgetRequest")) { // Exception for budget requests endpoint
            return true;
        } else if(entity.equals("SupportAssignment") && endpoint.equals("supportStaff")){ // Exception for support Assignments
            return true;
        } else if (entity.equals("SupportAppointment") && endpoint.equals("schedules")) { // Exception for support Appointments
            return true;
        } else if (entity.equals("Schedule") && (endpoint.equals("toggleSupportStaffSupportCallReview") || endpoint.equals("toggleInstructorSupportCallReview"))){
            return true;
        } else if ((entity.equals("InstructorSupportCallResponse") || entity.equals("TeachingCallReceipt") || entity.equals("StudentSupportCallResponse")) && (endpoint.equals("addInstructors") || endpoint.equals("contactInstructors"))){
            return true;
        } else if (entity.equals("StudentSupportCallResponse" ) && (endpoint.equals("addStudents") || endpoint.equals("contactSupportStaff"))){
            return true;
        } else if (entity.equals("TeachingAssignment") && endpoint.equals("courses" )){
            return true;
        } else if (entity.equals("TeachingAssignment") && endpoint.equals("assignAI")){
            return true;
        } else if (entity.endsWith("y") && (entity.toLowerCase().substring(0, entity.length() - 1) + "ies").equals(endpoint) ) {
            return true;
        }
        return false;
    }

    // Get name of endpoint
    public static String getEndpoint(String uri){
        String endpoint = uri.substring(uri.lastIndexOf('/') + 1);
        if(endpoint.matches("\\d+") || endpoint.length() <= 1){
            endpoint = uri.substring(0, uri.lastIndexOf('/'));
            endpoint = endpoint.substring(endpoint.lastIndexOf('/') + 1);
        }
        return endpoint;
    }

    // Get message that will display to user for updates
    public static String getFormattedUpdateAction(String module, Object entity, String propName, Object oldValue, Object newValue, String userDisplayName){
        StringBuilder sb = new StringBuilder();
        String entityDescription = ActivityLogFormatter.getFormattedEntityDescription(entity);
        String oldVal = ActivityLogFormatter.getFormattedPropValue(propName, oldValue);
        String newVal = ActivityLogFormatter.getFormattedPropValue(propName, newValue);
        String formattedPropName = ActivityLogFormatter.getFormattedPropName(propName);
        String years = ActivityLogFormatter.getYears(entity);

        // Exception for ta cost and reader cost
        // Since unlike other categories they are tracked on the budget scenario instead
        // of the category cost table
        if(entity instanceof Budget && (propName.equals("taCost") || propName.equals("readerCost"))) {
            module += " - Instructor List";
        } else if(entity instanceof SectionGroup && (propName.equals("teachingAssistantAppointments") || propName.equals("readerAppointments"))){
            module = "Support Staff Assignments";
        } else if (entity instanceof Schedule && (propName.equals("supportStaffSupportCallReviewOpen") || propName.equals("instructorSupportCallReviewOpen"))) {
            module = "Support Staff Assignments";
        }

            sb.append("**" + userDisplayName + "**");
        sb.append(" in **" + module + "** - **" + years + "**");
        String termCode = ActivityLogFormatter.getTermCode(entity);

        if(entity instanceof Schedule && (propName.equals("supportStaffSupportCallReviewOpen") || propName.equals("instructorSupportCallReviewOpen"))){ // Term code is stored in field as part of blob
            if(oldVal.length() == newVal.length()){
                for(int i = 0; i< oldVal.length(); i++){
                    if(oldVal.charAt(i) != newVal.charAt(i)){
                        String term = String.valueOf(i + 1);

                        // Zero pad if necessary
                        if (term.length() == 1) {
                            term = "0" + term;
                        }
                        termCode = Term.getRegistrarName(term);
                    }
                }
            }
        }

        if (termCode.length() > 0) {
            sb.append(", **" + termCode + "**");
        }
        sb.append("\n");

        if(entity instanceof SectionGroupCost && propName.equals("disabled")) { // Exceptions to usual update wording
            if (newVal.equals("false")) {
                sb.append("Added ");
            } else {
                sb.append("Removed ");
            }
            sb.append("**" + entityDescription + "**");
        } else if (entity instanceof Schedule && (propName.equals("supportStaffSupportCallReviewOpen") || propName.equals("instructorSupportCallReviewOpen"))){
            if(oldVal.length() == newVal.length()){
                for(int i = 0; i< oldVal.length(); i++){
                    if(oldVal.charAt(i) != newVal.charAt(i)){
                        if(newVal.charAt(i) == '1'){
                            sb.append("Opened ");
                        } else {
                            sb.append("Closed ");
                        }
                        if(propName.equals("supportStaffSupportCallReviewOpen")){
                            sb.append("Student Review");
                        } else {
                            sb.append("Instructor Review");
                        }
                    }
                }
            }
        } else if (entity instanceof TeachingCallReceipt && propName.equals("nextContactAt")){
            sb.append("Scheduled teaching" +
                    "" +
                    " call follow up for **");
            TeachingCallReceipt teachingCallReceipt = (TeachingCallReceipt) entity;
            sb.append(teachingCallReceipt.getInstructor().getFullName());
            sb.append("** on **" + newVal + "**");
        } else if (entity instanceof InstructorSupportCallResponse && propName.equals("nextContactAt")){
            sb.append("Scheduled instructor support call follow up for **");
            InstructorSupportCallResponse instructorSupportCallResponse = (InstructorSupportCallResponse) entity;
            sb.append(instructorSupportCallResponse.getInstructor().getFullName());
            sb.append("** on **" + newVal + "**");
        } else if (entity instanceof StudentSupportCallResponse && propName.equals("nextContactAt")){
            sb.append("Scheduled support staff support call follow up for **");
            StudentSupportCallResponse studentSupportCallResponse = (StudentSupportCallResponse) entity;
            sb.append(studentSupportCallResponse.getSupportStaff().getFullName());
            sb.append("** on **" + newVal + "**");
        } else { // Generic update methodology
            if (oldVal == null) {
                sb.append("Set ");
                sb.append("**" + entityDescription + "**");
                sb.append(" **" + formattedPropName + "** to **" + newVal + "**");
            } else if(newValue == null){
                sb.append("Cleared ");
                sb.append("**" + entityDescription + "**");
                sb.append(" **" + formattedPropName + "**");
            } else {
                sb.append("Changed ");
                sb.append("**" + entityDescription + "**");
                sb.append(" **" + formattedPropName + "** from **" + oldVal + "** to **" + newVal + "**");

            }
        }
        return sb.toString();
    }

    // Get message that will display to the user for inserts
    public static String getFormattedInsertAction(String module, Object entity, String userDisplayName) {
        StringBuilder sb = new StringBuilder();
        String entityDescription = ActivityLogFormatter.getFormattedEntityDescription(entity);
        String years = ActivityLogFormatter.getYears(entity);
        sb.append("**" + userDisplayName + "**");
        sb.append(" in **" + module + "** - **" + years + "**");
        String termCode = ActivityLogFormatter.getTermCode(entity);
        if(termCode.length() > 0){
            sb.append(", **" + termCode + "**");
        }

        if(entity instanceof SectionGroupCostInstructor){
            SectionGroupCostInstructor sectionGroupCostInstructor = (SectionGroupCostInstructor) entity;
            if(sectionGroupCostInstructor.getTeachingAssignment() != null){
                sb.append("\nSet ");
                sb.append("**" + entityDescription + "**");
                sb.append(" **cost** to **" + sectionGroupCostInstructor.getCost() + "**");
                return sb.toString();
            }
        } else if (entity instanceof SupportAppointment){
            SupportAppointment supportAppointment = (SupportAppointment) entity;
            if(supportAppointment.getPercentage() != null){
                sb.append("\nSet ");
                sb.append("**" + entityDescription + "**");
                sb.append(" **percentage** to **" + supportAppointment.getPercentage() + "**");
                return sb.toString();
            }
        }
        sb.append("\nCreated ");
        sb.append("**" + entityDescription + "**");

        return sb.toString();
    }

}
