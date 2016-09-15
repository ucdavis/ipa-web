package edu.ucdavis.dss.ipa.api.components.summary.views.factories;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import edu.ucdavis.dss.dw.dto.DwTerm;
import edu.ucdavis.dss.ipa.api.components.assignment.views.AssignmentView;
import edu.ucdavis.dss.ipa.api.components.summary.views.SummaryView;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.repositories.DataWarehouseRepository;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.stereotype.Service;

@Service
public class JpaSummaryViewFactory implements SummaryViewFactory {
    @Inject WorkgroupService workgroupService;
    @Inject TeachingAssignmentService teachingAssignmentService;
    @Inject ScheduleService scheduleService;
    @Inject CourseService courseService;
    @Inject TeachingCallService teachingCallService;
    @Inject DataWarehouseRepository dwRepository;

    @Override
    public SummaryView createSummaryView(long workgroupId, long year, long userId, long instructorId) {
        Schedule schedule = scheduleService.findByWorkgroupIdAndYear(workgroupId, year);

        List<TeachingAssignment> teachingAssignments = new ArrayList<TeachingAssignment>();
        List<Course> courses = new ArrayList<Course>();
        List<SectionGroup> sectionGroups = new ArrayList<SectionGroup>();
        List<Section> sections = new ArrayList<Section>();
        List<Activity> activities = new ArrayList<Activity>();

        List<TeachingAssignment> teachingAssignmentsToAdd = new ArrayList<TeachingAssignment>();

        if (instructorId > 0) {
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

        // Grab teaching calls
        // TODO: What roles can view the teaching calls?
        List<TeachingCall> teachingCallsToAdd = teachingCallService.findByScheduleId(schedule.getId());

        // Grab terms info from DW
        List<DwTerm> dwTerms = dwRepository.getTerms();

        return new SummaryView(courses, sectionGroups, sections, activities, teachingAssignmentsToAdd, teachingCallsToAdd, dwTerms);
    }
}
