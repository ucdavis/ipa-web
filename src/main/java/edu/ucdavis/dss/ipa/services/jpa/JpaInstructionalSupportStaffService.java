package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.InstructionalSupportStaff;
import edu.ucdavis.dss.ipa.repositories.InstructionalSupportStaffRepository;
import edu.ucdavis.dss.ipa.services.InstructionalSupportStaffService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class JpaInstructionalSupportStaffService implements InstructionalSupportStaffService {

    @Inject InstructionalSupportStaffRepository instructionalSupportStaffRepository;

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

        // Create an instructionalSupportStaff
        instructionalSupportStaff.setFirstName(firstName);
        instructionalSupportStaff.setLastName(lastName);
        instructionalSupportStaff.setLoginId(loginId);
        instructionalSupportStaff.setEmail(email);

        return this.save(instructionalSupportStaff);
    }
}
