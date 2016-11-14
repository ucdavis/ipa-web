package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.InstructionalSupportStaff;
import edu.ucdavis.dss.ipa.entities.UserRole;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.repositories.InstructionalSupportStaffRepository;
import edu.ucdavis.dss.ipa.services.InstructionalSupportStaffService;
import edu.ucdavis.dss.ipa.services.UserRoleService;
import edu.ucdavis.dss.ipa.services.WorkgroupService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Service
public class JpaInstructionalSupportStaffService implements InstructionalSupportStaffService {

    @Inject InstructionalSupportStaffRepository instructionalSupportStaffRepository;
    @Inject UserRoleService userRoleService;

    public InstructionalSupportStaff save(InstructionalSupportStaff instructionalSupportStaff) {
        return this.instructionalSupportStaffRepository.save(instructionalSupportStaff);
    }

    @Override
    public InstructionalSupportStaff findOneById(long instructionalSupportStaffId) {
        return this.instructionalSupportStaffRepository.findById(instructionalSupportStaffId);
    }

    @Override
    public InstructionalSupportStaff findOrCreate(String firstName, String lastName, String email, String loginId) {
        InstructionalSupportStaff instructionalSupportStaff = instructionalSupportStaffRepository.findByLoginIdIgnoreCase(loginId);

        // Check to see if instructionalSupportStaff already exists
        if (instructionalSupportStaff != null) {
            return instructionalSupportStaff;
        }

        instructionalSupportStaff = new InstructionalSupportStaff();

        // Create an instructionalSupportStaff
        instructionalSupportStaff.setFirstName(firstName);
        instructionalSupportStaff.setLastName(lastName);
        instructionalSupportStaff.setLoginId(loginId);
        instructionalSupportStaff.setEmail(email);

        return this.save(instructionalSupportStaff);
    }

    /**
     * Returns a list of InstructionalSupportStaff entities that are tied to userRoles for masters/phd/instructionalSupport roles.
     * These 3 populations will all have instructionalSupportStaff entities, and represent people
     * an academic planner might want to assign to an instructionalSupportAssignment.
     * @param workgroupId
     * @return
     */
    @Override
    public List<InstructionalSupportStaff> findActiveByWorkgroupId(long workgroupId) {
        List<UserRole> activeUserRoles = new ArrayList<UserRole>();

        List<UserRole> mastersStudents = userRoleService.findByWorkgroupIdAndRoleToken(workgroupId, "studentMasters");
        List<UserRole> phdStudents = userRoleService.findByWorkgroupIdAndRoleToken(workgroupId, "studentPhd");
        List<UserRole> instructionalSupportStaffList = userRoleService.findByWorkgroupIdAndRoleToken(workgroupId, "instructionalSupport");

        activeUserRoles.addAll(mastersStudents);
        activeUserRoles.addAll(phdStudents);
        activeUserRoles.addAll(instructionalSupportStaffList);

        List<InstructionalSupportStaff> activeInstructionalSupportStaffList = new ArrayList<InstructionalSupportStaff>();

        for (UserRole userRole : activeUserRoles) {
            InstructionalSupportStaff instructionalSupportStaff = this.findOrCreate(userRole.getUser().getFirstName(), userRole.getUser().getLastName(), userRole.getUser().getEmail(), userRole.getUser().getLoginId());
            activeInstructionalSupportStaffList.add(instructionalSupportStaff);
        }

        return activeInstructionalSupportStaffList;
    }

    @Override
    public List<InstructionalSupportStaff> findActiveByWorkgroupIdAndRoleToken(long workgroupId, String roleToken) {
        List<UserRole> instructionalSupportStaffUserRoles = userRoleService.findByWorkgroupIdAndRoleToken(workgroupId, roleToken);

        List<InstructionalSupportStaff> activeInstructionalSupportStaffList = new ArrayList<InstructionalSupportStaff>();

        for (UserRole userRole : instructionalSupportStaffUserRoles) {
            InstructionalSupportStaff instructionalSupportStaff = this.findOrCreate(userRole.getUser().getFirstName(), userRole.getUser().getLastName(), userRole.getUser().getEmail(), userRole.getUser().getLoginId());
            activeInstructionalSupportStaffList.add(instructionalSupportStaff);
        }

        return activeInstructionalSupportStaffList;
    }
}
