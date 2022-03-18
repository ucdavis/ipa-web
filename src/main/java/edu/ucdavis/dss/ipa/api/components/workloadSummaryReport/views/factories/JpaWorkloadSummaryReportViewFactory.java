package edu.ucdavis.dss.ipa.api.components.workloadSummaryReport.views.factories;

import edu.ucdavis.dss.ipa.api.components.workloadSummaryReport.views.WorkloadInstructorDTO;
import edu.ucdavis.dss.ipa.api.components.workloadSummaryReport.views.WorkloadSummaryReportExcelView;
import edu.ucdavis.dss.ipa.api.components.workloadSummaryReport.views.WorkloadSummaryReportView;
import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.InstructorType;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.ScheduleInstructorNote;
import edu.ucdavis.dss.ipa.entities.Section;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.entities.TeachingAssignment;
import edu.ucdavis.dss.ipa.entities.Term;
import edu.ucdavis.dss.ipa.entities.UserRole;
import edu.ucdavis.dss.ipa.services.CourseService;
import edu.ucdavis.dss.ipa.services.InstructorService;
import edu.ucdavis.dss.ipa.services.InstructorTypeService;
import edu.ucdavis.dss.ipa.services.ScheduleInstructorNoteService;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.SectionGroupService;
import edu.ucdavis.dss.ipa.services.SectionService;
import edu.ucdavis.dss.ipa.services.TeachingAssignmentService;
import edu.ucdavis.dss.ipa.services.UserRoleService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.springframework.stereotype.Service;

@Service
public class JpaWorkloadSummaryReportViewFactory implements WorkloadSummaryReportViewFactory {
    @Inject
    ScheduleService scheduleService;
    @Inject
    CourseService courseService;
    @Inject
    InstructorTypeService instructorTypeService;
    @Inject
    InstructorService instructorService;
    @Inject
    TeachingAssignmentService teachingAssignmentService;
    @Inject
    ScheduleInstructorNoteService scheduleInstructorNoteService;
    @Inject
    SectionGroupService sectionGroupService;
    @Inject
    SectionService sectionService;
    @Inject
    UserRoleService userRoleService;

    @Override
    public WorkloadSummaryReportView createWorkloadSummaryReportView(long workgroupId, long year) {
        Schedule schedule = scheduleService.findByWorkgroupIdAndYear(workgroupId, year);

        if (schedule == null) {
            return null;
        }

        List<Course> courses = schedule.getCourses();
        List<SectionGroup> sectionGroups = sectionGroupService.findByScheduleId(schedule.getId());
        List<Section> sections = sectionService.findVisibleByWorkgroupIdAndYear(workgroupId, year);
        List<ScheduleInstructorNote> scheduleInstructorNotes =
            scheduleInstructorNoteService.findByScheduleId(schedule.getId());
        List<TeachingAssignment> teachingAssignments = teachingAssignmentService.findByScheduleId(schedule.getId());
        List<InstructorType> instructorTypes = instructorTypeService.getAllInstructorTypes();

        Set<Instructor> instructorSet = new HashSet<>();
        Set<Instructor> activeInstructors =
            new HashSet<>(instructorService.findActiveByWorkgroupId(schedule.getWorkgroup().getId()));
        Set<Instructor> assignedInstructors =
            new HashSet<>(instructorService.findAssignedByScheduleId(schedule.getId()));

        instructorSet.addAll(assignedInstructors);
        instructorSet.addAll(activeInstructors);

        List<Instructor> instructors = new ArrayList<>(instructorSet);

        return new WorkloadSummaryReportView(year, schedule, courses, instructors, instructorTypes, teachingAssignments,
            scheduleInstructorNotes, sectionGroups, sections);
    }

    @Override
    public WorkloadSummaryReportExcelView createWorkloadSummaryReportExcelView(long workgroupId, long year) {
        WorkloadSummaryReportView workloadSummaryReportView = createWorkloadSummaryReportView(workgroupId, year);

        // list every instructor regardless of active assignments
        // "Year", "Department", "Instructor Type", "Name", "Term", "Course Type", "Description", "Offering"
        // instructor has teachingAssignments, filter for current year
        // for each instructor, print teachingAssignments if exists, else just print year, department, type, name
        List<WorkloadInstructorDTO> workloadInstructors = new ArrayList<>();

        String department = workloadSummaryReportView.getSchedule().getWorkgroup().getName();

        for (Instructor instructor : workloadSummaryReportView.getInstructors()) {
            Long instructorTypeId =
                getInstructorTypeId(instructor, workloadSummaryReportView.getTeachingAssignment(), workgroupId);

            if (instructorTypeId == null) {
                continue;
            }
            // use enum instead?
            String instructorTypeDescription = instructorTypeService.findById(instructorTypeId).getDescription();

            List<TeachingAssignment> scheduleAssignments = teachingAssignmentService.findByScheduleIdAndInstructorId(
                workloadSummaryReportView.getSchedule().getId(), instructor.getId());

            // TODO: add enrollment/units/SCH

            if (scheduleAssignments.size() == 0) {
                workloadInstructors.add(
                    new WorkloadInstructorDTO(department, instructorTypeDescription, instructor.getFullName(), null,
                        null, null, null));
            } else {
                for (TeachingAssignment assignment : scheduleAssignments) {
                    String courseType = getCourseType(assignment);

                    workloadInstructors.add(new WorkloadInstructorDTO(department, instructorTypeDescription,
                        instructor.getLastName() + ", " + instructor.getFirstName(),
                        Term.getRegistrarName(assignment.getTermCode()), courseType, assignment.getDescription(),
                        assignment.getSectionGroup().getCourse().getSequencePattern()));
                }
            }


        }

        // fill in TBD instructor assignments
        for (TeachingAssignment teachingAssignment : workloadSummaryReportView.getTeachingAssignment().stream()
            .filter(teachingAssignment -> teachingAssignment.getInstructor() == null).collect(
                Collectors.toList())) {
            workloadInstructors.add(
                new WorkloadInstructorDTO(department,
                    instructorTypeService.findById(teachingAssignment.getInstructorTypeIdentification())
                        .getDescription(), "TBD",
                    Term.getRegistrarName(teachingAssignment.getTermCode()), getCourseType(teachingAssignment),
                    teachingAssignment.getDescription(),
                    teachingAssignment.getSectionGroup().getCourse().getSequencePattern()));
        }

        // write data to excel
        WorkloadSummaryReportExcelView workloadSummaryReportExcelView =
            new WorkloadSummaryReportExcelView(workloadInstructors, year);

        return workloadSummaryReportExcelView;

    }

    private Long getInstructorTypeId(Instructor instructor, List<TeachingAssignment> teachingAssignments,
                                     long workgroupId) {
        // attempt by userRole
        UserRole userRole =
            userRoleService.findOrCreateByLoginIdAndWorkgroupIdAndRoleToken(instructor.getLoginId(), workgroupId,
                "instructor");

        if (userRole != null) {
            return userRole.getInstructorTypeIdentification();
        }
        // attempt by teachingAssignment
        TeachingAssignment teachingAssignment =
            teachingAssignments.stream().filter(ta -> ta.getInstructor().getId() == instructor.getId()).findFirst()
                .get();

        if (teachingAssignment != null) {
            return teachingAssignment.getInstructorType().getId();
        }

        return null;
    }

    private String getCourseType(TeachingAssignment teachingAssignment) {
        SectionGroup sectionGroup = teachingAssignment.getSectionGroup();

        if (sectionGroup == null) {
            // non-Course Assignment
            return teachingAssignment.getDescription();
        } else {
            Course course = teachingAssignment.getSectionGroup().getCourse();
            int courseNumbers = Integer.parseInt(course.getCourseNumber().replaceAll("[^\\d.]", ""));

            if (courseNumbers < 100) {
                return "Lower";
            } else if (courseNumbers >= 200) {
                return "Grad";
            } else {
                return "Upper";
            }
        }
    }
}
