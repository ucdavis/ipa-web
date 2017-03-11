package edu.ucdavis.dss.ipa.api.components.summary.views.factories;

import edu.ucdavis.dss.ipa.api.components.summary.views.SummaryView;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Service
public class JpaSummaryViewFactory implements SummaryViewFactory {
    @Inject WorkgroupService workgroupService;
    @Inject TeachingAssignmentService teachingAssignmentService;
    @Inject ScheduleService scheduleService;
    @Inject CourseService courseService;
    @Inject TermService termService;
    @Inject InstructorService instructorService;

    @Inject InstructionalSupportStaffService instructionalSupportStaffService;
    @Inject StudentInstructionalSupportCallService studentInstructionalSupportCallService;
    @Inject StudentInstructionalSupportCallResponseService studentInstructionalSupportCallResponseService;

    @Inject InstructorInstructionalSupportCallService instructorInstructionalSupportCallService;
    @Inject InstructorInstructionalSupportCallResponseService instructorInstructionalSupportCallResponseService;
    @Override
    public SummaryView createSummaryView(long workgroupId, long year, long userId, long instructorId, long supportStaffId) {
        Schedule schedule = scheduleService.findByWorkgroupIdAndYear(workgroupId, year);

        List<TeachingAssignment> teachingAssignments = new ArrayList<TeachingAssignment>();
        List<Course> courses = new ArrayList<Course>();
        List<SectionGroup> sectionGroups = new ArrayList<SectionGroup>();
        List<Section> sections = new ArrayList<Section>();
        List<Activity> activities = new ArrayList<Activity>();

        List<TeachingAssignment> teachingAssignmentsToAdd = new ArrayList<TeachingAssignment>();

        if (schedule != null && instructorId > 0) {
            teachingAssignments = teachingAssignmentService.findByScheduleIdAndInstructorId(schedule.getId(), instructorId);

            for (TeachingAssignment teachingAssignment : teachingAssignments) {
                if (teachingAssignment.isApproved() == false) {
                    continue;
                }

                teachingAssignmentsToAdd.add(teachingAssignment);

                if (sectionGroups.contains(teachingAssignment.getSectionGroup()) == false) {
                    sectionGroups.add(teachingAssignment.getSectionGroup());
                }

                if ( (teachingAssignment.getSectionGroup() != null) && courses.contains(teachingAssignment.getSectionGroup().getCourse()) == false) {
                    courses.add(teachingAssignment.getSectionGroup().getCourse());
                }

                if (teachingAssignment.getSectionGroup() != null) {
                    // Get activities from SectionGroup (Shared Activities)
                    for (Activity activity: teachingAssignment.getSectionGroup().getActivities()) {
                        if (activities.contains(activity) == false) {
                            activities.add(activity);
                        }
                    }
                    // Get activities from Sections
                    for (Section section : teachingAssignment.getSectionGroup().getSections()) {

                        if (sections.contains(section) == false) {
                            sections.add(section);
                        }

                        for (Activity activity : section.getActivities()) {
                            if (activities.contains(activity) == false) {
                                activities.add(activity);
                            }
                        }
                    }
                }
            }
        }


        // Grab terms info from DW
        long currentYear = Calendar.getInstance().get(Calendar.YEAR);
        List<Term> terms = termService.findByYear(currentYear);

        // Grab teachingCallReceipts
        List<TeachingCallReceipt> teachingCallReceipts = new ArrayList<>();
        Instructor instructor = instructorService.getOneById(instructorId);
        if (instructor != null) {
            teachingCallReceipts = instructor.getTeachingCallReceipts();
        }

        // Get student support Calls
        List<StudentSupportCall> studentSupportCalls = studentInstructionalSupportCallService.findByScheduleIdAndSupportStaffId(schedule.getId(), supportStaffId);
        List<StudentSupportCallResponse> studentSupportCallResponses = studentInstructionalSupportCallResponseService.findByScheduleIdAndSupportStaffId(schedule.getId(), supportStaffId);

        // Get instructor support Calls
        List<InstructorSupportCall> instructorSupportCalls = instructorInstructionalSupportCallService.findByScheduleIdAndInstructorId(schedule.getId(), instructorId);
        List<InstructorSupportCallResponse> instructorSupportCallResponses = instructorInstructionalSupportCallResponseService.findByScheduleIdAndInstructorId(schedule.getId(), instructorId);

        return new SummaryView(courses, sectionGroups, sections, activities, teachingAssignmentsToAdd, teachingCallReceipts, terms, studentSupportCalls, instructorSupportCalls, studentSupportCallResponses, instructorSupportCallResponses);
    }
}
