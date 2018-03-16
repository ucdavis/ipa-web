package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.repositories.SupportStaffRepository;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.SupportStaffService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Service
public class JpaSupportStaffService implements SupportStaffService {

    @Inject SupportStaffRepository supportStaffRepository;
    @Inject ScheduleService scheduleService;

    public SupportStaff save(SupportStaff supportStaff) {
        return this.supportStaffRepository.save(supportStaff);
    }

    @Override
    public SupportStaff findOneById(long instructionalSupportStaffId) {
        return this.supportStaffRepository.findById(instructionalSupportStaffId);
    }

    @Override
    public SupportStaff findOrCreate(String firstName, String lastName, String email, String loginId) {
        SupportStaff supportStaff = supportStaffRepository.findByLoginIdIgnoreCase(loginId);

        // Check to see if supportStaff already exists
        if (supportStaff != null) {
            return supportStaff;
        }

        supportStaff = new SupportStaff();

        // Create an supportStaff
        supportStaff.setFirstName(firstName);
        supportStaff.setLastName(lastName);
        supportStaff.setLoginId(loginId);
        supportStaff.setEmail(email);

        return this.save(supportStaff);
    }

    @Override
    public SupportStaff findByLoginId(String loginId) {
        return this.supportStaffRepository.findByLoginIdIgnoreCase(loginId);
    }

    /**
     * Returns a List of SupportStaff who have been assigned to sectionGroups in the specified schedule.
     * @param scheduleId
     * @return
     */
    @Override
    public List<SupportStaff> findByScheduleId(long scheduleId) {
        List<SupportStaff> supportStaffList = new ArrayList<>();

        Schedule schedule = this.scheduleService.findById(scheduleId);

        for (Course course : schedule.getCourses()) {
            for (SectionGroup sectionGroup : course.getSectionGroups()) {
                for (SupportAssignment supportAssignment : sectionGroup.getSupportAssignments()) {
                    SupportStaff supportStaff = supportAssignment.getSupportStaff();
                    if (supportStaff != null && supportStaffList.contains(supportStaff) == false) {
                        supportStaffList.add(supportStaff);
                    }
                }

                for (StudentSupportPreference preference : sectionGroup.getStudentInstructionalSupportCallPreferences()) {
                    SupportStaff supportStaff = preference.getSupportStaff();
                    if (supportStaff != null && supportStaffList.contains(supportStaff) == false) {
                        supportStaffList.add(supportStaff);
                    }
                }
            }
        }

        return supportStaffList;
    }

    @Override
    public List<SupportStaff> findBySupportAssignments(List<SupportAssignment> supportAssignments) {
        List<SupportStaff> supportStaffList = new ArrayList<>();

        for (SupportAssignment supportAssignment : supportAssignments) {
            if (supportAssignment.getSupportStaff() != null) {
                supportStaffList.add(supportAssignment.getSupportStaff());
            }
        }

        return supportStaffList;
    }
}
