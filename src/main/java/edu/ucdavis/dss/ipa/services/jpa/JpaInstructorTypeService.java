package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.InstructorType;
import edu.ucdavis.dss.ipa.entities.UserRole;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.repositories.InstructorTypeRepository;
import edu.ucdavis.dss.ipa.services.InstructorTypeService;
import edu.ucdavis.dss.ipa.services.UserRoleService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

@Service
public class JpaInstructorTypeService implements InstructorTypeService {
    static long INSTRUCTOR_TYPE = 7L;

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
     * @param workgroup
     * @return
     */
    @Override
    public InstructorType findByInstructorAndWorkgroup(Instructor instructor, Workgroup workgroup) {
        InstructorType instructorType = null;
        List<UserRole> userRoles = userRoleService.findByLoginIdAndWorkgroup(instructor.getLoginId(), workgroup);

        if (userRoles != null) {
          for (UserRole userRole : userRoles) {
              if (userRole.getRoleToken().equals("instructor")) {
                  instructorType = userRole.getInstructorType();
                  break;
              }
          }
        }

        if (instructorType == null) {
            // Default to instructorType of '7', the generic 'instructor' type.
            instructorType = this.findById(INSTRUCTOR_TYPE);
        }

        return instructorType;
    }
}
