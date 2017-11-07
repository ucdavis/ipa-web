package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.repositories.SupportAssignmentRepository;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Collection;

@Service
public class JpaSupportAssignmentService implements SupportAssignmentService {

    @Inject SupportAssignmentRepository supportAssignmentRepository;
    @Inject SectionGroupService sectionGroupService;
    @Inject SupportStaffService supportStaffService;
    @Inject ScheduleService scheduleService;
    @Inject TeachingAssignmentService teachingAssignmentService;
    @Inject InstructorService instructorService;

    @Override
    public SupportAssignment save(SupportAssignment supportAssignment) {
        supportAssignment = this.supportAssignmentRepository.save(supportAssignment);

        // If assigning an associate instructor,
        // Create a teaching assignment and instructor as well
        if ("associateInstructor".equals(supportAssignment.getAppointmentType()) && supportAssignment.getSupportStaff() != null) {
            String firstName = supportAssignment.getSupportStaff().getFirstName();
            String lastName = supportAssignment.getSupportStaff().getLastName();
            String email = supportAssignment.getSupportStaff().getEmail();
            String loginId = supportAssignment.getSupportStaff().getLoginId();
            Long workgroupId = supportAssignment.getSectionGroup().getCourse().getSchedule().getWorkgroup().getId();

            Instructor instructor = instructorService.findOrCreate(firstName, lastName, email, loginId, workgroupId);
            TeachingAssignment teachingAssignment = teachingAssignmentService.findOrCreateOneBySectionGroupAndInstructor(supportAssignment.getSectionGroup(), instructor);

            teachingAssignment.setApproved(true);
            teachingAssignmentService.save(teachingAssignment);
        }

        return supportAssignment;
    }

    @Override
    public List<SupportAssignment> findBySectionGroups(List<SectionGroup> sectionGroups) {
        List<SupportAssignment> supportAssignments = new ArrayList<>();

        for (SectionGroup sectionGroup : sectionGroups) {
            supportAssignments.addAll(sectionGroup.getSupportAssignments());
        }

        return supportAssignments;
    }

    /**
     * Will return any supportAssignments that should be visible to the specified instructor, for the given schedule.
     * Visibility to supportAssignments is controlled by the schedule.instructorSupportCallReviewOpen term 'blob'
     * And only supportAssignments made for sectionGroups that the instructor is teaching.
     * @param schedule
     * @param instructorId
     * @return
     */
    @Override
    public List<SupportAssignment> findVisibleByScheduleAndInstructorId(Schedule schedule, long instructorId) {
        List<SupportAssignment> supportAssignments = new ArrayList<>();

        // Find all supportAssignments associated to sectionGroups the instructor is teaching
        List<TeachingAssignment> teachingAssignments = teachingAssignmentService.findByScheduleIdAndInstructorId(schedule.getId(), instructorId);

        if (teachingAssignments == null || teachingAssignments.size() == 0) {
            return supportAssignments;
        }

        // Filter out supportAssignments based on what has been made visible via 'open for review'
        String supportCallReviewTermBlob = schedule.getInstructorSupportCallReviewOpen();

        List<SupportAssignment> instructorSupportAssignments = new ArrayList<>();

        for (TeachingAssignment teachingAssignment: teachingAssignments) {
            if (teachingAssignment.isApproved() == true && teachingAssignment.getSectionGroup() != null) {
                instructorSupportAssignments.addAll(teachingAssignment.getSectionGroup().getSupportAssignments());
            }
        }

        for (SupportAssignment supportAssignment : instructorSupportAssignments) {
            if (supportAssignment.isOpenToReview()) {
                supportAssignments.add(supportAssignment);
            }
        }

        return supportAssignments;
    }

    @Override
    public SupportAssignment findOneById(Long instructionalSupportAssignmentId) {
        return this.supportAssignmentRepository.findById(instructionalSupportAssignmentId);
    }

    @Override
    public void delete(Long instructionalSupportAssignmentId) {
        this.supportAssignmentRepository.deleteById(instructionalSupportAssignmentId);
    }

    @Override
    public SupportAssignment create(long sectionGroupId, String type, long appointmentPercentage) {

        SectionGroup sectionGroup = sectionGroupService.getOneById(sectionGroupId);

        SupportAssignment supportAssignment = new SupportAssignment();

        supportAssignment.setSectionGroup(sectionGroup);
        supportAssignment.setAppointmentPercentage(appointmentPercentage);
        supportAssignment.setAppointmentType(type);

        supportAssignment = this.save(supportAssignment);

        return supportAssignment;
    }

    @Override
    public List<SupportAssignment> createMultiple(long sectionGroupId, String type, long appointmentPercentage, long numberToCreate) {

        List<SupportAssignment> supportAssignments = new ArrayList<SupportAssignment>();

        for (int i = 0; i < numberToCreate; i++) {
            SupportAssignment supportAssignment = this.create(sectionGroupId, type, appointmentPercentage);
            supportAssignments.add(supportAssignment);
        }

        return supportAssignments;
    }

    @Override
    public SupportAssignment assignInstructionalSupportStaff(long instructionalSupportStaffId, long instructionalSupportAssignmentId) {

        SupportStaff supportStaff = supportStaffService.findOneById(instructionalSupportStaffId);
        SupportAssignment supportAssignment = this.findOneById(instructionalSupportAssignmentId);

        supportAssignment.setSupportStaff(supportStaff);

        return this.save(supportAssignment);
    }

    @Override
    public SupportAssignment unassignInstructionalSupportStaff(long instructionalSupportAssignmentId) {
        SupportAssignment supportAssignment = this.findOneById(instructionalSupportAssignmentId);

        supportAssignment.setSupportStaff(null);

        return this.save(supportAssignment);
    }

    @Override
    public List<SupportAssignment> findByScheduleIdAndTermCode(long scheduleId, String termCode) {
        Schedule schedule = this.scheduleService.findById(scheduleId);
        List<SupportAssignment> supportAssignments = new ArrayList<SupportAssignment>();

        // Find all sectionGroups for the specified schedule and termCode
        List<SectionGroup> sectionGroups = schedule.getCourses()
                .stream()
                .map(course -> course.getSectionGroups().stream()
                        .filter(sectionGroup -> termCode == null || termCode.trim().isEmpty() || sectionGroup.getTermCode().equals(termCode.trim()))
                        .collect(Collectors.toList()))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        // Find all supportAssignments associated to the sectionGroups.
        for (SectionGroup sectionGroup : sectionGroups) {
            supportAssignments.addAll(sectionGroup.getSupportAssignments());
        }

        return supportAssignments;
    }

    /**
     * Returns support assignments assigned to the specified support staff.
     * @param scheduleId
     * @param termCode
     * @param supportStaffId
     * @return
     */
    @Override
    public List<SupportAssignment> findByScheduleIdAndTermCodeAndSupportStaffId(long scheduleId, String termCode, long supportStaffId) {
        List<SupportAssignment> allSupportAssignments = this.findByScheduleIdAndTermCode(scheduleId, termCode);
        List<SupportAssignment> supportStaffAssignments = new ArrayList<>();

        for (SupportAssignment slotAssignment : allSupportAssignments) {
            if (slotAssignment.getSupportStaff() != null
            && slotAssignment.getSupportStaff().getId() == supportStaffId) {
                supportStaffAssignments.add(slotAssignment);
            }
        }

        return supportStaffAssignments;
    }
}