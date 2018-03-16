package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.InstructorType;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.UserRole;
import edu.ucdavis.dss.ipa.repositories.InstructorTypeRepository;
import edu.ucdavis.dss.ipa.services.InstructorTypeService;
import edu.ucdavis.dss.ipa.services.UserRoleService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

@Service
public class JpaInstructorTypeService implements InstructorTypeService {
    @Inject InstructorTypeRepository instructorTypeRepository;
    @Inject UserRoleService userRoleService;

    @Override
    public InstructorType findById(Long instructorTypeId) {
        return instructorTypeRepository.findById(instructorTypeId);
    }

    @Override
    public List<InstructorType> getAllInstructorTypes() {
        return (List<InstructorType>) instructorTypeRepository.findAll();
    }

    /**
     * Attempt to find the relevant instructorType for the instructor based on userRoles. Will return 'instructor' as a default type if none are found.
     * @param instructor
     * @param schedule
     * @return
     */
    @Override
    public InstructorType findByInstructorAndSchedule(Instructor instructor, Schedule schedule) {
        List<UserRole> userRoles = userRoleService.findByLoginIdAndWorkgroup(instructor.getLoginId(), schedule.getWorkgroup());

        InstructorType instructorType = null;

        for (UserRole userRole : userRoles) {
            if (userRole.getRoleToken().equals("instructor")) {
                instructorType = userRole.getInstructorType();
            }
        }

        if (instructorType == null) {
            // Default to instructorType of '7', the generic 'instructor' type.
            instructorType = this.findById(7L);
        }

        return instructorType;
    }
}
