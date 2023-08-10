package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.dw.dto.DwCensus;
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
import edu.ucdavis.dss.ipa.entities.WorkloadAssignment;
import edu.ucdavis.dss.ipa.entities.WorkloadSnapshot;
import edu.ucdavis.dss.ipa.repositories.DataWarehouseRepository;
import edu.ucdavis.dss.ipa.repositories.WorkloadAssignmentRepository;
import edu.ucdavis.dss.ipa.services.CourseService;
import edu.ucdavis.dss.ipa.services.InstructorService;
import edu.ucdavis.dss.ipa.services.InstructorTypeService;
import edu.ucdavis.dss.ipa.services.ScheduleInstructorNoteService;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.SectionGroupService;
import edu.ucdavis.dss.ipa.services.SectionService;
import edu.ucdavis.dss.ipa.services.TeachingAssignmentService;
import edu.ucdavis.dss.ipa.services.UserRoleService;
import edu.ucdavis.dss.ipa.services.WorkloadAssignmentService;
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
import org.springframework.stereotype.Service;

@Service
public class JpaWorkloadAssignmentService implements WorkloadAssignmentService {
    @Inject
    WorkloadAssignmentRepository workloadAssignmentRepository;
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

    public List<WorkloadAssignment> findByWorkloadSnapshotId(long workloadSnapshotId) {
        return workloadAssignmentRepository.findByWorkloadSnapshotId(workloadSnapshotId);
    }

    public List<WorkloadAssignment> saveAll(List<WorkloadAssignment> workloadAssignments) {
        List<WorkloadAssignment> results = new ArrayList<>();
        for (WorkloadAssignment workloadAssignment : workloadAssignments) {
            results.add(workloadAssignmentRepository.save(workloadAssignment));
        }
        return results;
    }

    public List<WorkloadAssignment> generateWorkloadAssignments(long workgroupId, long year) {
        return generateWorkloadAssignments(workgroupId, year, false);
    }

    public List<WorkloadAssignment> generateWorkloadAssignments(long workgroupId, long year, boolean includeUnassigned) {
        List<WorkloadAssignment> workloadAssignments = new ArrayList<>();

        // Gathering phase
        Schedule schedule = scheduleService.findByWorkgroupIdAndYear(workgroupId, year);
        List<Course> courses = schedule.getCourses();
        List<SectionGroup> sectionGroups = sectionGroupService.findByScheduleId(schedule.getId());
        List<Section> sections = sectionService.findVisibleByWorkgroupIdAndYear(workgroupId, year);
        List<ScheduleInstructorNote> scheduleInstructorNotes =
            scheduleInstructorNoteService.findByScheduleId(schedule.getId());
        List<TeachingAssignment> teachingAssignments = teachingAssignmentService.findApprovedByWorkgroupIdAndYear(workgroupId, year);
        List<InstructorType> instructorTypes = instructorTypeService.getAllInstructorTypes();

        if (schedule == null) {
            return null;
        }

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
            if (!courseMap.containsKey(c.getSubjectCode())) {
                courseMap.put(c.getSubjectCode(), new ArrayList<>());
            }

            courseMap.get(c.getSubjectCode()).add(c.getCourseNumber());
        }

        List<CompletableFuture<List<DwCensus>>> termCodeCensusFutures = new ArrayList<>();
        List<CompletableFuture<List<DwCensus>>> courseCensusFutures = new ArrayList<>();

        for (String subjectCode : courseMap.keySet()) {
            termCodeCensusFutures.addAll(termCodes.stream().map(termCode -> CompletableFuture.supplyAsync(
                    () -> dwRepository.getCensusBySubjectCodeAndTermCode(subjectCode, termCode)))
                .collect(Collectors.toList()));

            courseCensusFutures.addAll(courseMap.get(subjectCode).stream().distinct().map(
                    courseNumber -> CompletableFuture.supplyAsync(
                        () -> dwRepository.getCensusBySubjectCodeAndCourseNumber(subjectCode, courseNumber)))
                .collect(Collectors.toList()));
        }

        List<DwCensus> termCodeCensus =
            termCodeCensusFutures.stream().map(CompletableFuture::join).flatMap(Collection::stream)
                .filter(c -> "CURRENT".equals(c.getSnapshotCode())).collect(Collectors.toList());
        List<DwCensus> courseCensus =
            courseCensusFutures.stream().map(CompletableFuture::join).flatMap(Collection::stream)
                .filter(c -> "CURRENT".equals(c.getSnapshotCode())).collect(Collectors.toList());

        Map<String, Map<String, Long>> termCodeCensusMap = generateCensusMap(termCodeCensus);
        Map<String, Map<String, Long>> courseCensusMap = generateCensusMap(courseCensus);

        courseCensus.stream().filter(c -> "009A".equals(c.getCourseNumber()) && "202301".equals(c.getTermCode())).collect(Collectors.toList());
        // Transform phase
        String department = schedule.getWorkgroup().getName();

        for (Instructor instructor : instructors) {
            Long instructorTypeId = getInstructorTypeId(instructor, teachingAssignments, workgroupId);

            if (instructorTypeId == null) {
                continue;
            }

            String instructorTypeDescription = instructorTypeService.findById(instructorTypeId).getDescription();

            List<TeachingAssignment> scheduleAssignments = teachingAssignments.stream().filter(ta -> ta.getInstructor() != null && ta.getInstructor().getId() == instructor.getId()).collect(Collectors.toList());

            String instructorNote = scheduleInstructorNotes.stream()
                .filter(note -> note.getInstructor().getId() == instructor.getId())
                .map(note -> note.getInstructorComment()).findAny().orElse("");

            if (scheduleAssignments.size() == 0) {
                WorkloadAssignment wa = new WorkloadAssignment();
                wa.setYear(year);
                wa.setDepartment(department);
                wa.setInstructorType(instructorTypeDescription);
                wa.setName(instructor.getInvertedName());
                wa.setInstructorNote(instructorNote);
                workloadAssignments.add(wa);
            } else {
                for (TeachingAssignment assignment : scheduleAssignments) {
                    String termCode = null, previousYearTermCode = null, courseDescription = null, offering = null, lastOfferedCensus = null, unit = null;
                    Integer plannedSeats = null;
                    Long censusCount = null;
                    Long previousYearCensus = null;
                    Float studentCreditHour = null;

                    String courseType = getCourseType(assignment);
                    if (assignment.getSectionGroup() != null) {
                        SectionGroup sectionGroup = assignment.getSectionGroup();
                        Course course = sectionGroup.getCourse();

                        termCode = sectionGroup.getTermCode();
                        previousYearTermCode = Term.getPreviousYearTermCode(termCode);
                        courseDescription = course.getSubjectCode() + " " + course.getCourseNumber();
                        offering = course.getSequencePattern();
                        unit = sectionGroup.getDisplayUnits();
                        plannedSeats = sectionGroup.getPlannedSeats();
                        censusCount = 0L;

                        if (termCodeCensus.size() > 0) {
                            String courseKey = course.getSubjectCode() + "-" + course.getCourseNumber() + "-" +
                                course.getSequencePattern();

                            if (courseCensusMap.containsKey(termCode) &&
                                courseCensusMap.get(termCode).containsKey(courseKey)) {
                                censusCount = courseCensusMap.get(termCode).get(courseKey);

                                studentCreditHour = calculateStudentCreditHours(censusCount, course, sectionGroup);
//                                plannedSeats = sectionGroup.getPlannedSeats();
                            }

                            if (courseCensusMap.containsKey(previousYearTermCode) &&
                                courseCensusMap.get(previousYearTermCode).containsKey(courseKey)) {
                                previousYearCensus = courseCensusMap.get(previousYearTermCode).get(courseKey);
                            }

                            // find last offering term
                            List<String> offeredTermCodes =
                                courseCensusMap.keySet().stream().sorted(Comparator.reverseOrder())
                                    .collect(Collectors.toList());

                            String lastOfferedTermCode = null;
                            String lastOfferedCourseKey =
                                course.getSubjectCode() + "-" + course.getCourseNumber() + "-" +
                                    course.getSequencePattern();
                            if (offeredTermCodes.size() > 2) {
                                for (String offeredTermCode : offeredTermCodes.subList(offeredTermCodes.indexOf(termCode) + 1, offeredTermCodes.size())) {
                                    if (courseCensusMap.get(offeredTermCode).containsKey(lastOfferedCourseKey) && courseCensusMap.get(offeredTermCode).get(lastOfferedCourseKey) != 0) {
                                        lastOfferedTermCode = offeredTermCode;
                                        break;
                                    }
                                }
                                if (lastOfferedTermCode != null) {
                                    lastOfferedCensus =
                                        courseCensusMap.get(lastOfferedTermCode).get(lastOfferedCourseKey) + " (" +
                                            Term.getShortDescription(lastOfferedTermCode) + ")";
                                }
                            } else {
                                lastOfferedCensus = "";
                            }
                        }
                    } else {
                        termCode = assignment.getTermCode();
                        courseDescription = assignment.getDescription();
                    }

                    WorkloadAssignment wa = new WorkloadAssignment();
                    wa.setYear(year);
                    wa.setDepartment(department);
                    wa.setInstructorType(instructorTypeDescription);
                    wa.setName(instructor.getInvertedName());
                    wa.setTermCode(termCode);
                    wa.setCourseType(courseType);
                    wa.setDescription(courseDescription);
                    wa.setOffering(offering);
                    wa.setCensus(censusCount);
                    wa.setPlannedSeats(plannedSeats);
                    wa.setPreviousYearCensus(previousYearCensus);
                    wa.setLastOfferedCensus(lastOfferedCensus);
                    wa.setUnits(unit);
                    wa.setStudentCreditHours(studentCreditHour);
                    wa.setInstructorNote(instructorNote);

                    workloadAssignments.add(wa);
                }
            }
        }

        // fill in TBD instructor assignments
        List<TeachingAssignment> unnamedAssignments =
            teachingAssignments.stream().filter(teachingAssignment -> teachingAssignment.getInstructor() == null)
                .collect(Collectors.toList());
        for (TeachingAssignment teachingAssignment : unnamedAssignments) {
            SectionGroup sectionGroup = teachingAssignment.getSectionGroup();
            Course course = sectionGroup.getCourse();
            String termCode = sectionGroup.getTermCode();
                    long censusCount = 0;
                    long previousYearCensus = 0;
                    Float studentCreditHour = null;
                                        String previousYearTermCode =
                        Integer.parseInt(termCode.substring(0, 4)) - 1 + termCode.substring(4, 6);
                                        String lastOfferedCensus = null;

                                    if (termCodeCensus.size() > 0) {
                            String courseKey = course.getSubjectCode() + "-" + course.getCourseNumber() + "-" +
                                course.getSequencePattern();

                            if (courseCensusMap.containsKey(termCode) &&
                                courseCensusMap.get(termCode).containsKey(courseKey)) {
                                censusCount = courseCensusMap.get(termCode).get(courseKey);

                                studentCreditHour = calculateStudentCreditHours(censusCount, course, sectionGroup);
//                                plannedSeats = sectionGroup.getPlannedSeats();
                            }

                            if (courseCensusMap.containsKey(previousYearTermCode) &&
                                courseCensusMap.get(previousYearTermCode).containsKey(courseKey)) {
                                previousYearCensus = courseCensusMap.get(previousYearTermCode).get(courseKey);
                            }

                            // find last offering term
                            List<String> offeredTermCodes =
                                courseCensusMap.keySet().stream().sorted(Comparator.reverseOrder())
                                    .collect(Collectors.toList());

                            String lastOfferedTermCode = null;
                            String lastOfferedCourseKey =
                                course.getSubjectCode() + "-" + course.getCourseNumber() + "-" +
                                    course.getSequencePattern();
                            if (offeredTermCodes.size() > 2) {
                                for (String offeredTermCode : offeredTermCodes.subList(offeredTermCodes.indexOf(termCode) + 1, offeredTermCodes.size())) {
                                    if (courseCensusMap.get(offeredTermCode).containsKey(lastOfferedCourseKey) && courseCensusMap.get(offeredTermCode).get(lastOfferedCourseKey) != 0) {
                                        lastOfferedTermCode = offeredTermCode;
                                        break;
                                    }
                                }

                                if (lastOfferedTermCode != null) {
                                    lastOfferedCensus =
                                        courseCensusMap.get(lastOfferedTermCode).get(lastOfferedCourseKey) + " (" +
                                            Term.getShortDescription(lastOfferedTermCode) + ")";
                                }
                            } else {
                                lastOfferedCensus = "";
                            }
                        }

            WorkloadAssignment wa = new WorkloadAssignment();
            wa.setYear(year);
            wa.setDepartment(department);
            wa.setInstructorType(
                instructorTypeService.findById(teachingAssignment.getInstructorTypeIdentification()).getDescription());
            wa.setName("TBD");
            wa.setTermCode(sectionGroup.getTermCode());
            wa.setCourseType(getCourseType(teachingAssignment));
            wa.setDescription(course.getSubjectCode() + " " + course.getCourseNumber());
            wa.setOffering(course.getSequencePattern());
            wa.setCensus(censusCount);
            wa.setPlannedSeats(sectionGroup.getPlannedSeats());
            wa.setPreviousYearCensus(previousYearCensus);
            wa.setLastOfferedCensus(lastOfferedCensus);
            wa.setUnits(sectionGroup.getDisplayUnits());
            workloadAssignments.add(wa);
        }

        // fill in unassigned data for snapshots
        if (includeUnassigned) {
            List<SectionGroup> unassignedSectionGroups = sectionGroups.stream().filter(sg ->
                sg.getTeachingAssignments().stream().filter(ta -> ta.isApproved()).collect(Collectors.toList())
                    .size() == 0).collect(Collectors.toList());

            for (SectionGroup sectionGroup : unassignedSectionGroups) {
                Course course = sectionGroup.getCourse();
                String termCode = sectionGroup.getTermCode();
                long censusCount = 0;
                long previousYearCensus = 0;
                Float studentCreditHour = null;
                String previousYearTermCode =
                    Integer.parseInt(termCode.substring(0, 4)) - 1 + termCode.substring(4, 6);
                String lastOfferedCensus = null;

                if (termCodeCensus.size() > 0) {
                    String courseKey = course.getSubjectCode() + "-" + course.getCourseNumber() + "-" +
                        course.getSequencePattern();

                    if (courseCensusMap.containsKey(termCode) &&
                        courseCensusMap.get(termCode).containsKey(courseKey)) {
                        censusCount = courseCensusMap.get(termCode).get(courseKey);

                        studentCreditHour = calculateStudentCreditHours(censusCount, course, sectionGroup);
//                                plannedSeats = sectionGroup.getPlannedSeats();
                    }

                    if (courseCensusMap.containsKey(previousYearTermCode) &&
                        courseCensusMap.get(previousYearTermCode).containsKey(courseKey)) {
                        previousYearCensus = courseCensusMap.get(previousYearTermCode).get(courseKey);
                    }

                    // find last offering term
                    List<String> offeredTermCodes =
                        courseCensusMap.keySet().stream().sorted(Comparator.reverseOrder())
                            .collect(Collectors.toList());

                    String lastOfferedTermCode = null;
                    String lastOfferedCourseKey =
                        course.getSubjectCode() + "-" + course.getCourseNumber() + "-" +
                            course.getSequencePattern();
                    if (offeredTermCodes.size() > 2) {
                        // walk through map to check for course key
                        for (String offeredTermCode : offeredTermCodes.subList(offeredTermCodes.indexOf(termCode) + 1,
                            offeredTermCodes.size())) {
                            if (courseCensusMap.get(offeredTermCode).containsKey(lastOfferedCourseKey) &&
                                courseCensusMap.get(offeredTermCode).get(lastOfferedCourseKey) != 0) {
                                lastOfferedTermCode = offeredTermCode;
                                break;
                            }
                        }
//                                lastOfferedTermCode = offeredTermCodes.get(offeredTermCodes.size() - 2);

                        if (lastOfferedTermCode != null) {
                            lastOfferedCensus =
                                courseCensusMap.get(lastOfferedTermCode).get(lastOfferedCourseKey) + " (" +
                                    Term.getShortDescription(lastOfferedTermCode) + ")";
                        }
                    } else {
                        lastOfferedCensus = "";
                    }
                }

                WorkloadAssignment wa = new WorkloadAssignment();
                wa.setYear(year);
                wa.setDepartment(department);
                wa.setInstructorType("Unassigned");
                wa.setName("");
                wa.setTermCode(sectionGroup.getTermCode());
                wa.setCourseType(getCourseType(sectionGroup));
                wa.setDescription(course.getSubjectCode() + " " + course.getCourseNumber());
                wa.setOffering(course.getSequencePattern());
                wa.setCensus(censusCount);
                wa.setPlannedSeats(sectionGroup.getPlannedSeats());
                wa.setPreviousYearCensus(previousYearCensus);
                wa.setLastOfferedCensus(lastOfferedCensus);
                wa.setUnits(sectionGroup.getDisplayUnits());
                workloadAssignments.add(wa);
            }
        }

        return workloadAssignments;
    }

    public List<WorkloadAssignment> generateWorkloadAssignments(long workgroupId, long year, WorkloadSnapshot workloadSnapshot) {
        List<WorkloadAssignment> assignments = generateWorkloadAssignments(workgroupId, year, true);

        for (WorkloadAssignment a : assignments) {
            a.setWorkloadSnapshot(workloadSnapshot);
        }

        return (List<WorkloadAssignment>) workloadAssignmentRepository.save(assignments);
    }

    private String calculateCourseType(Course course) {
        int courseNumbers = Integer.parseInt(course.getCourseNumber().replaceAll("[^\\d.]", ""));

        if (courseNumbers < 100) {
            return "Lower";
        } else if (courseNumbers >= 200) {
            return "Grad";
        } else {
            return "Upper";
        }
    }

    private String getCourseType(TeachingAssignment teachingAssignment) {
        if (teachingAssignment.getSectionGroup() == null) {
            // non-Course Assignment
            return teachingAssignment.getDescription();
        } else {
            return calculateCourseType(teachingAssignment.getSectionGroup().getCourse());
        }
    }

    private String getCourseType(SectionGroup sectionGroup) {
        return calculateCourseType(sectionGroup.getCourse());
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
