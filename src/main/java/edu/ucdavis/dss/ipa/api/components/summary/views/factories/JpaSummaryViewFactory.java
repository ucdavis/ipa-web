package edu.ucdavis.dss.ipa.api.components.summary.views.factories;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import edu.ucdavis.dss.ipa.api.components.assignment.views.AssignmentView;
import edu.ucdavis.dss.ipa.api.components.summary.views.SummaryView;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.stereotype.Service;

@Service
public class JpaSummaryViewFactory implements SummaryViewFactory {
    @Inject
    WorkgroupService workgroupService;
    @Inject TeachingAssignmentService teachingAssignmentService;
    @Inject ScheduleService scheduleService;
    @Inject CourseService courseService;

    @Override
    public SummaryView createSummaryView(long workgroupId, long year, long userId, long instructorId) {
        Workgroup workgroup = workgroupService.findOneById(workgroupId);
        Schedule schedule = scheduleService.findByWorkgroupAndYear(workgroup, year);

        List<TeachingAssignment> teachingAssignments = new ArrayList<TeachingAssignment>();
        List<Course> courses = new ArrayList<Course>();
        List<SectionGroup> sectionGroups = new ArrayList<SectionGroup>();
        List<Section> sections = new ArrayList<Section>();
        List<Activity> activities = new ArrayList<Activity>();

        if (instructorId > 0) {
            teachingAssignments = teachingAssignmentService.findByScheduleIdAndInstructorId(schedule.getId(), instructorId);

            for (TeachingAssignment teachingAssignment : teachingAssignments) {

                if (sectionGroups.contains(teachingAssignment.getSectionGroup()) == false) {
                    sectionGroups.add(teachingAssignment.getSectionGroup());
                }

                if ( (teachingAssignment.getSectionGroup() != null) && courses.contains(teachingAssignment.getSectionGroup().getCourse()) == false) {
                    courses.add(teachingAssignment.getSectionGroup().getCourse());
                }

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

        return new SummaryView(courses, sectionGroups, sections, activities, teachingAssignments);
    }
}
