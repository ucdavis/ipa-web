package edu.ucdavis.dss.ipa.api.components.workloadSummaryReport.views.factories;

import edu.ucdavis.dss.dw.dto.DwCensus;
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
import edu.ucdavis.dss.ipa.repositories.DataWarehouseRepository;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.springframework.stereotype.Service;

@Service
public class JpaWorkloadSummaryReportViewFactory implements WorkloadSummaryReportViewFactory {
    @Inject
    DataWarehouseRepository dwRepository;
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

        List<DwCensus> censusList = new ArrayList<>();
        List<String> subjectCodes =
            courses.stream().map(c -> c.getSubjectCode()).distinct().collect(Collectors.toList());
        for (String subjectCode : subjectCodes) {
            for (String termCode : Term.getTermCodesByYear(year)) {
                censusList.addAll(dwRepository.getCensusBySubjectCodeAndTermCode(subjectCode, termCode).stream()
                    .filter(c -> "CURRENT".equals(c.getSnapshotCode())).collect(Collectors.toList()));
            }
        }

        Map<String, Map<String, Map<String, Long>>> censusByTermCode = new HashMap<>(new HashMap<>());

        for (DwCensus census : censusList) {
            String termCode = census.getTermCode();
            String sequencePattern = census.getSequencePattern();
            String courseIdentifier = census.getSubjectCode() + census.getCourseNumber();

            if (censusByTermCode.get(termCode) == null) {
                censusByTermCode.put(termCode, new HashMap<>());
            }

            if (censusByTermCode.get(termCode).get(courseIdentifier) == null) {
                censusByTermCode.get(termCode).put(courseIdentifier, new HashMap<>());
            }

            if (censusByTermCode.get(termCode).get(courseIdentifier).get(sequencePattern) == null) {
                censusByTermCode.get(termCode).get(courseIdentifier)
                    .put(sequencePattern, census.getCurrentEnrolledCount());
            } else {
                censusByTermCode.get(termCode).get(courseIdentifier).put(sequencePattern,
                    censusByTermCode.get(termCode).get(courseIdentifier).get(sequencePattern) +
                        census.getCurrentEnrolledCount());
            }
        }

        return new WorkloadSummaryReportView(year, schedule, courses, instructors, instructorTypes, teachingAssignments,
            scheduleInstructorNotes, sectionGroups, sections, censusByTermCode);
    }

    @Override
    public WorkloadSummaryReportExcelView createWorkloadSummaryReportExcelView(long[] workgroupIds, long year) {
        List<WorkloadInstructorDTO> workloadInstructors = new ArrayList<>();

        for (long workgroupId : workgroupIds) {
            WorkloadSummaryReportView workloadSummaryReportView = createWorkloadSummaryReportView(workgroupId, year);

            String department = workloadSummaryReportView.getSchedule().getWorkgroup().getName();

            for (Instructor instructor : workloadSummaryReportView.getInstructors()) {
                Long instructorTypeId =
                    getInstructorTypeId(instructor, workloadSummaryReportView.getTeachingAssignment(), workgroupId);

                if (instructorTypeId == null) {
                    continue;
                }
                // use enum instead?
                String instructorTypeDescription = instructorTypeService.findById(instructorTypeId).getDescription();

                List<TeachingAssignment> scheduleAssignments =
                    teachingAssignmentService.findByScheduleIdAndInstructorId(
                        workloadSummaryReportView.getSchedule().getId(), instructor.getId());

                // TODO: add Previous Enrollment

                if (scheduleAssignments.size() == 0) {
                    workloadInstructors.add(
                        new WorkloadInstructorDTO(department, instructorTypeDescription, instructor.getFullName(), null,
                            null, null, null, null, null, null, null, null));
                } else {
                    for (TeachingAssignment assignment : scheduleAssignments) {
                        String termCode = assignment.getTermCode();

                        String courseDescription = null, offering = null, instructorNote =
                            null, unit = null;
                        int plannedSeats = 0;
                        long censusCount = 0;
                        Float studentCreditHour = null;

                        String courseType = getCourseType(assignment);
                        if (assignment.getSectionGroup() != null) {
                            SectionGroup sectionGroup = assignment.getSectionGroup();
                            Course course = sectionGroup.getCourse();

                            courseDescription = course.getSubjectCode() + " " + course.getCourseNumber();
                            offering = course.getSequencePattern();
                            unit = sectionGroup.getDisplayUnits();

                            instructorNote = workloadSummaryReportView.getScheduleInstructorNotes().stream()
                                .filter(n -> n.getInstructor().getId() == assignment.getInstructor().getId())
                                .map(s -> s.getInstructorComment()).findFirst().orElse("");

                            if (workloadSummaryReportView.getCensusByTermCode().size() > 0) {
                                String courseKey = course.getSubjectCode() + course.getCourseNumber();
                                if (workloadSummaryReportView.getCensusByTermCode().get(termCode).get(courseKey) !=
                                    null) {
                                    if (workloadSummaryReportView.getCensusByTermCode().get(termCode).get(courseKey)
                                        .get(course.getSequencePattern()) != null) {
                                        censusCount =
                                            workloadSummaryReportView.getCensusByTermCode().get(termCode).get(
                                                    courseKey)
                                                .get(course.getSequencePattern());
                                    }

                                    studentCreditHour = calculateStudentCreditHours(censusCount, course, sectionGroup);
                                    plannedSeats = sectionGroup.getPlannedSeats();
                                }
                            }

                        } else {
                            courseDescription = assignment.getDescription();
                        }

                        workloadInstructors.add(new WorkloadInstructorDTO(department, instructorTypeDescription,
                            instructor.getLastName() + ", " + instructor.getFirstName(),
                            Term.getRegistrarName(termCode) + " " + Term.getYear(termCode), courseType,
                            courseDescription,
                            offering, censusCount, plannedSeats, unit, studentCreditHour, instructorNote));
                    }
                }
            }

            // fill in TBD instructor assignments
            List<TeachingAssignment> unnamedAssignments = workloadSummaryReportView.getTeachingAssignment().stream()
                .filter(teachingAssignment -> teachingAssignment.getInstructor() == null).collect(
                    Collectors.toList());
            for (TeachingAssignment teachingAssignment : unnamedAssignments) {
                workloadInstructors.add(
                    new WorkloadInstructorDTO(department,
                        instructorTypeService.findById(teachingAssignment.getInstructorTypeIdentification())
                            .getDescription(), "TBD",
                        Term.getRegistrarName(teachingAssignment.getTermCode()), getCourseType(teachingAssignment),
                        teachingAssignment.getDescription(),
                        teachingAssignment.getSectionGroup().getCourse().getSequencePattern(),
                        null, null, null, null, null));
            }
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

    private Float calculateStudentCreditHours(Long students, Course course, SectionGroup sectionGroup) {
        Float units = 0.0f;

        if (sectionGroup.getUnitsVariable() != null) {
            units = sectionGroup.getUnitsVariable();
        } else if (course.getUnitsLow() != null && course.getUnitsLow() > 0) {
            units = course.getUnitsLow();
        }

        return students * units;
    }
}
