package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.StudentSupportCallCrn;
import edu.ucdavis.dss.ipa.repositories.StudentSupportCallCrnRepository;
import edu.ucdavis.dss.ipa.services.StudentSupportCallCrnService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class JpaStudentSupportCallCrnService implements StudentSupportCallCrnService {
    @Inject
    StudentSupportCallCrnRepository studentSupportCallResponseRepository;

    @Override
    public void delete(Long studentSupportCallCrnId) {
        studentSupportCallResponseRepository.delete(studentSupportCallCrnId);
    }

    @Override
    public StudentSupportCallCrn create (StudentSupportCallCrn studentSupportCallCrn) {
        return studentSupportCallResponseRepository.save(studentSupportCallCrn);
    }
}
