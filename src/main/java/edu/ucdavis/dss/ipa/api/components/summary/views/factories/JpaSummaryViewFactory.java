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
    @Inject TeachingAssignmentService teachingAssignmentService;
    @Inject ScheduleService scheduleService;
    @Inject TermService termService;
    @Inject InstructorService instructorService;
    @Inject SupportStaffService supportStaffService;
    @Inject StudentSupportCallResponseService studentSupportCallResponseService;
    @Inject InstructorSupportCallResponseService instructorSupportCallResponseService;
    @Inject SupportAssignmentService supportAssignmentService;

    @Override
    public SummaryView createSummaryView(long workgroupId, long year, long userId, long instructorId, long supportStaffId) {
        Schedule schedule = scheduleService.findOrCreateByWorkgroupIdAndYear(workgroupId, year);

        List<TeachingAssignment> teachingAssignments = new ArrayList<TeachingAssignment>();
        List<Course> courses = new ArrayList<Course>();
        List<SectionGroup> sectionGroups = new ArrayList<SectionGroup>();
        List<Section> sections = new ArrayList<Section>();
        List<Activity> activities = new ArrayList<Activity>();

        List<TeachingAssignment> teachingAssignmentsToAdd = new ArrayList<TeachingAssignment>();
        List<InstructorSupportCallResponse> instructorSupportCallResponses = new ArrayList<>();
        List<SupportAssignment> supportAssignments = new ArrayList<>();
        List<SupportStaff> supportStaffList = new ArrayList<>();

        if (schedule != null && instructorId > 0) {
            teachingAssignments = teachingAssignmentService.findByScheduleIdAndInstructorId(schedule.getId(), instructorId);

            // Get assignments data
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

            // Get instructor support call data
            instructorSupportCallResponses = instructorSupportCallResponseService.findByScheduleIdAndInstructorId(schedule.getId(), instructorId);

            // Get student assignment data
            supportAssignments = supportAssignmentService.findVisibleByScheduleAndInstructorId(schedule, instructorId);

            // Get supportStaff
            supportStaffList = supportStaffService.findBySupportAssignments(supportAssignments);
        }

        List<Term> terms = termService.findByYear(year);

        // Grab teachingCallReceipts
        List<TeachingCallReceipt> teachingCallReceipts = new ArrayList<>();
        Instructor instructor = instructorService.getOneById(instructorId);
        if (instructor != null) {
            teachingCallReceipts = instructor.getTeachingCallReceipts();
        }

        // Get support staff support Calls
        List<StudentSupportCallResponse> studentSupportCallResponses = new ArrayList<>();

        if (schedule != null && supportStaffId > 0) {
            studentSupportCallResponses = studentSupportCallResponseService.findByScheduleIdAndSupportStaffId(schedule.getId(), supportStaffId);
        }

        return new SummaryView(schedule, courses, sectionGroups, sections, activities, teachingAssignmentsToAdd, teachingCallReceipts, terms, studentSupportCallResponses, instructorSupportCallResponses, supportAssignments, supportStaffList);
    }
}
