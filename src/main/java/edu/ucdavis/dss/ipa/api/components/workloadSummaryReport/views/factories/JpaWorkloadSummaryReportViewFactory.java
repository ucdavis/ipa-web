package edu.ucdavis.dss.ipa.api.components.workloadSummaryReport.views.factories;

import edu.ucdavis.dss.dw.dto.DwCensus;
import edu.ucdavis.dss.ipa.api.components.workloadSummaryReport.views.InstructorAssignment;
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
import edu.ucdavis.dss.ipa.utilities.ExcelHelper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        List<TeachingAssignment> teachingAssignments = teachingAssignmentService.findApprovedByWorkgroupIdAndYear(workgroupId, year);
        List<InstructorType> instructorTypes = instructorTypeService.getAllInstructorTypes();

        Set<Instructor> instructorSet = new HashSet<>();
        Set<Instructor> activeInstructors =
            new HashSet<>(instructorService.findActiveByWorkgroupId(schedule.getWorkgroup().getId()));
        Set<Instructor> assignedInstructors =
            new HashSet<>(instructorService.findAssignedByScheduleId(schedule.getId()));

        instructorSet.addAll(assignedInstructors);
        instructorSet.addAll(activeInstructors);

        List<Instructor> instructors = new ArrayList<>(instructorSet);
        List<String> termCodes = new ArrayList<>();

        // TODO: remove termCodes not in Schedule
        termCodes.addAll(Term.getTermCodesByYear(year));
        termCodes.addAll(Term.getTermCodesByYear(year - 1));

        // SubjectCode: [CourseNumbers]
        Map<String, List<String>> courseMap = new HashMap<>();

        for (Course c : courses) {
            if (courseMap.containsKey(c.getSubjectCode()) == false) {
                courseMap.put(c.getSubjectCode(), new ArrayList<>());
            }

            courseMap.get(c.getSubjectCode()).add(c.getCourseNumber());
        }

        List<CompletableFuture<List<DwCensus>>> termCodeCensusFutures = new ArrayList<>();
        List<CompletableFuture<List<DwCensus>>> courseCensusFutures = new ArrayList<>();

        for (String subjectCode : courseMap.keySet()) {
            termCodeCensusFutures.addAll(termCodes.stream().map(termCode -> CompletableFuture.supplyAsync(
                () -> dwRepository.getCensusBySubjectCodeAndTermCode(subjectCode, termCode))).collect(
                Collectors.toList()));

            courseCensusFutures.addAll(courseMap.get(subjectCode).stream().map(
                courseNumber -> CompletableFuture.supplyAsync(
                    () -> dwRepository.getCensusBySubjectCodeAndCourseNumber(subjectCode, courseNumber))).collect(
                Collectors.toList()));
        }

        List<DwCensus> termCodeCensus =
            termCodeCensusFutures.stream().map(CompletableFuture::join).flatMap(Collection::stream)
                .filter(c -> "CURRENT".equals(c.getSnapshotCode())).collect(Collectors.toList());
        List<DwCensus> courseCensus =
            courseCensusFutures.stream().map(CompletableFuture::join).flatMap(Collection::stream)
                .filter(c -> "CURRENT".equals(c.getSnapshotCode())).collect(Collectors.toList());

        Map<String, Map<String, Long>> termCodeCensusMap = generateCensusMap(termCodeCensus);
        Map<String, Map<String, Long>> courseCensusMap = generateCensusMap(courseCensus);

        return new WorkloadSummaryReportView(year, schedule, courses, instructors, instructorTypes, teachingAssignments,
            scheduleInstructorNotes, sectionGroups, sections, termCodeCensusMap, courseCensusMap);
    }

    @Override
    public WorkloadSummaryReportExcelView createWorkloadSummaryReportExcelView(long[] workgroupIds, long year) {
        List<InstructorAssignment> instructorAssignments = new ArrayList<>();

        for (long workgroupId : workgroupIds) {
            instructorAssignments.addAll(generateInstructorData(workgroupId, year));
        }

        // write data to excel
        WorkloadSummaryReportExcelView workloadSummaryReportExcelView =
            new WorkloadSummaryReportExcelView(instructorAssignments, year);

        return workloadSummaryReportExcelView;
    }

    @Override
//    @Async
    @Transactional
    // needed for Async https://stackoverflow.com/questions/17278385/spring-async-generates-lazyinitializationexceptions
    public CompletableFuture<byte[]> createWorkloadSummaryReportBytes(long[] workgroupIds, long year) {
        List<InstructorAssignment> instructorDTOList = new ArrayList<>();
        System.out.println("Generating workload report for " + workgroupIds.length + " departments");

        int count = 0;
        for (long workgroupId : workgroupIds) {
            ++count;
            System.out.println(count + ". Generating for workgroupId: " + workgroupId);

            instructorDTOList.addAll(generateInstructorData(workgroupId, year));
        }

        System.out.println("Finished gathering data, writing to excel");

        XSSFWorkbook workbook = new XSSFWorkbook();
        WorkloadSummaryReportExcelView.buildRawAssignmentsSheet(workbook, instructorDTOList);
        ExcelHelper.expandHeaders(workbook);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            workbook.write(bos);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return CompletableFuture.completedFuture(bos.toByteArray());
    }

    private List<InstructorAssignment> generateInstructorData(long workgroupId, long year) {
        List<InstructorAssignment> instructorAssignments = new ArrayList<>();

        WorkloadSummaryReportView workloadSummaryReportView = createWorkloadSummaryReportView(workgroupId, year);

        String department = workloadSummaryReportView.getSchedule().getWorkgroup().getName();

        for (Instructor instructor : workloadSummaryReportView.getInstructors()) {
            Long instructorTypeId =
                getInstructorTypeId(instructor, workloadSummaryReportView.getTeachingAssignment(), workgroupId);

            if (instructorTypeId == null) {
                continue;
            }

            String instructorTypeDescription = instructorTypeService.findById(instructorTypeId).getDescription();

            List<TeachingAssignment> scheduleAssignments = workloadSummaryReportView.getTeachingAssignment().stream()
                    .filter(ta -> ta.getInstructor() != null && ta.getInstructor().getId() == instructor.getId())
                    .collect(Collectors.toList());

            String instructorNote = workloadSummaryReportView.getScheduleInstructorNotes().stream()
                        .filter(note -> note.getInstructor().getId() == instructor.getId())
                        .map(note -> note.getInstructorComment()).findAny().orElse("");

            if (scheduleAssignments.size() == 0) {
                instructorAssignments.add(
                    new InstructorAssignment(year, department, instructorTypeDescription, instructor.getFullName(), instructorNote));
            } else {
                for (TeachingAssignment assignment : scheduleAssignments) {
                    String termCode = assignment.getTermCode();
                    String previousYearTermCode =
                        Integer.parseInt(termCode.substring(0, 4)) - 1 + termCode.substring(4, 6);

                    String courseDescription = null, offering = null, lastOfferedCensus = null, unit = null;
                    Integer plannedSeats = null;
                    long censusCount = 0;
                    long previousYearCensus = 0;
                    Float studentCreditHour = null;

                    String courseType = getCourseType(assignment);
                    if (assignment.getSectionGroup() != null) {
                        SectionGroup sectionGroup = assignment.getSectionGroup();
                        Course course = sectionGroup.getCourse();

                        courseDescription = course.getSubjectCode() + " " + course.getCourseNumber();
                        offering = course.getSequencePattern();
                        unit = sectionGroup.getDisplayUnits();

                        if (workloadSummaryReportView.getTermCodeCensus().size() > 0) {
                            String courseKey = course.getSubjectCode() + "-" + course.getCourseNumber() + "-" +
                                course.getSequencePattern();
                            Map<String, Map<String, Long>> censusMap =
                                workloadSummaryReportView.getTermCodeCensus();

                            if (censusMap.containsKey(termCode) && censusMap.get(termCode).containsKey(courseKey)) {
                                censusCount = censusMap.get(termCode).get(courseKey);

                                studentCreditHour = calculateStudentCreditHours(censusCount, course, sectionGroup);
                                plannedSeats = sectionGroup.getPlannedSeats();
                            }

                            if (censusMap.containsKey(previousYearTermCode) &&
                                censusMap.get(previousYearTermCode).containsKey(courseKey)) {
                                previousYearCensus = censusMap.get(previousYearTermCode).get(courseKey);
                            }

                            // find last offering term
                            Map<String, Map<String, Long>> courseCensusMap =
                                workloadSummaryReportView.getCourseCensus();

                            List<String> offeredTermCodes = courseCensusMap.keySet().stream().sorted(
                                Comparator.naturalOrder()).collect(
                                Collectors.toList());

                            String lastOfferedTermCode;
                            String lastOfferedCourseKey =
                                course.getSubjectCode() + "-" + course.getCourseNumber() + "-" +
                                    course.getSequencePattern();
                            if (offeredTermCodes.size() > 2) {
                                lastOfferedTermCode = offeredTermCodes.get(offeredTermCodes.size() - 2);

                                if (courseCensusMap.get(lastOfferedTermCode)
                                    .containsKey(lastOfferedCourseKey)) {
                                    lastOfferedCensus =
                                        courseCensusMap.get(lastOfferedTermCode).get(lastOfferedCourseKey) +
                                            " (" +
                                            Term.getShortDescription(lastOfferedTermCode) + ")";
                                }
                            } else {
                                lastOfferedCensus = "";
                            }
                        }
                    } else {
                        courseDescription = assignment.getDescription();
                    }

                    instructorAssignments.add(new InstructorAssignment(year, department, instructorTypeDescription,
                        instructor.getLastName() + ", " + instructor.getFirstName(),
                        Term.getRegistrarName(termCode) + " " + Term.getYear(termCode), courseType,
                        courseDescription, offering, censusCount, plannedSeats, previousYearCensus,
                        lastOfferedCensus, unit, studentCreditHour, instructorNote));
                }
            }
        }

        // fill in TBD instructor assignments
        List<TeachingAssignment> unnamedAssignments = workloadSummaryReportView.getTeachingAssignment().stream()
            .filter(teachingAssignment -> teachingAssignment.getInstructor() == null).collect(
                Collectors.toList());
        for (TeachingAssignment teachingAssignment : unnamedAssignments) {
            instructorAssignments.add(
                new InstructorAssignment(year, department,
                    instructorTypeService.findById(teachingAssignment.getInstructorTypeIdentification())
                        .getDescription(), "TBD",
                    Term.getRegistrarName(teachingAssignment.getTermCode()), getCourseType(teachingAssignment),
                    teachingAssignment.getDescription(),
                    teachingAssignment.getSectionGroup().getCourse().getSequencePattern()));
        }
        return instructorAssignments;
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

    /**
     * @param censuses
     * @return { "termCode": { "course": enrollmentCount } }
     */
    private Map<String, Map<String, Long>> generateCensusMap(List<DwCensus> censuses) {
        Map<String, Map<String, Long>> censusMap = new HashMap<>();
        for (DwCensus census : censuses) {
            String termCode = census.getTermCode();
            String courseKey =
                census.getSubjectCode() + "-" + census.getCourseNumber() + "-" + census.getSequencePattern();

            if (censusMap.get(termCode) == null) {
                censusMap.put(termCode, new HashMap<>());
            }

            if (censusMap.get(termCode).get(courseKey) == null) {
                censusMap.get(termCode).put(courseKey, census.getCurrentEnrolledCount());
            } else {
                censusMap.get(termCode)
                    .put(courseKey, censusMap.get(termCode).get(courseKey) + census.getCurrentEnrolledCount());
            }
        }

        return censusMap;
    }
}
