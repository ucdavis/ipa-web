package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.SupportStaff;
import edu.ucdavis.dss.ipa.entities.UserRole;
import edu.ucdavis.dss.ipa.repositories.SupportStaffRepository;
import edu.ucdavis.dss.ipa.services.SupportStaffService;
import edu.ucdavis.dss.ipa.services.UserRoleService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Service
public class JpaSupportStaffService implements SupportStaffService {

    @Inject
    SupportStaffRepository supportStaffRepository;
    @Inject UserRoleService userRoleService;

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

    /**
     * Returns a list of SupportStaff entities that are tied to userRoles for masters/phd/instructionalSupport roles.
     * These 3 populations will all have instructionalSupportStaff entities, and represent people
     * an academic planner might want to assign to an instructionalSupportAssignment.
     * @param workgroupId
     * @return
     */
    @Override
    public List<SupportStaff> findActiveByWorkgroupId(long workgroupId) {
        List<UserRole> activeUserRoles = new ArrayList<UserRole>();

        List<UserRole> mastersStudents = userRoleService.findByWorkgroupIdAndRoleToken(workgroupId, "studentMasters");
        List<UserRole> phdStudents = userRoleService.findByWorkgroupIdAndRoleToken(workgroupId, "studentPhd");
        List<UserRole> instructionalSupportStaffList = userRoleService.findByWorkgroupIdAndRoleToken(workgroupId, "instructionalSupport");

        activeUserRoles.addAll(mastersStudents);
        activeUserRoles.addAll(phdStudents);
        activeUserRoles.addAll(instructionalSupportStaffList);

        List<SupportStaff> activeSupportStaffList = new ArrayList<SupportStaff>();

        for (UserRole userRole : activeUserRoles) {
            SupportStaff supportStaff = this.findOrCreate(userRole.getUser().getFirstName(), userRole.getUser().getLastName(), userRole.getUser().getEmail(), userRole.getUser().getLoginId());
            activeSupportStaffList.add(supportStaff);
        }

        return activeSupportStaffList;
    }

    @Override
    public List<SupportStaff> findActiveByWorkgroupIdAndRoleToken(long workgroupId, String roleToken) {
        List<UserRole> instructionalSupportStaffUserRoles = userRoleService.findByWorkgroupIdAndRoleToken(workgroupId, roleToken);

        List<SupportStaff> activeSupportStaffList = new ArrayList<SupportStaff>();

        for (UserRole userRole : instructionalSupportStaffUserRoles) {
            SupportStaff supportStaff = this.findOrCreate(userRole.getUser().getFirstName(), userRole.getUser().getLastName(), userRole.getUser().getEmail(), userRole.getUser().getLoginId());
            activeSupportStaffList.add(supportStaff);
        }

        return activeSupportStaffList;
    }

    @Override
    public SupportStaff findByLoginId(String loginId) {
        return this.supportStaffRepository.findByLoginIdIgnoreCase(loginId);
    }
}
