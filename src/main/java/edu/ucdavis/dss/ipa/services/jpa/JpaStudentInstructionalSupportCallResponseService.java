package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.InstructionalSupportStaff;
import edu.ucdavis.dss.ipa.entities.StudentInstructionalSupportCall;
import edu.ucdavis.dss.ipa.entities.StudentInstructionalSupportCallResponse;
import edu.ucdavis.dss.ipa.repositories.StudentInstructionalSupportCallResponseRepository;
import edu.ucdavis.dss.ipa.services.StudentInstructionalSupportCallResponseService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

@Service
public class JpaStudentInstructionalSupportCallResponseService implements StudentInstructionalSupportCallResponseService {

    @Inject StudentInstructionalSupportCallResponseService studentInstructionalSupportCallResponseService;
    @Inject StudentInstructionalSupportCallResponseRepository studentInstructionalSupportCallResponseRepository;

    @Override
    public StudentInstructionalSupportCallResponse findOneById(long studentInstructionalSupportCallResponseId) {
        return studentInstructionalSupportCallResponseRepository.findById(studentInstructionalSupportCallResponseId);
    }

    @Override
    public List<StudentInstructionalSupportCallResponse> findByScheduleId(long studentInstructionalSupportCallId) {
        return studentInstructionalSupportCallResponseRepository.findByStudentInstructionalSupportCallId(studentInstructionalSupportCallId);
    }

    @Override
    public void delete(long studentInstructionalSupportCallResponseId) {
        studentInstructionalSupportCallResponseRepository.delete(studentInstructionalSupportCallResponseId);
    }

    @Override
    public StudentInstructionalSupportCallResponse create (StudentInstructionalSupportCall studentInstructionalSupportCall, InstructionalSupportStaff instructionalSupportStaff) {
        StudentInstructionalSupportCallResponse studentInstructionalSupportCallResponse = new StudentInstructionalSupportCallResponse();

        studentInstructionalSupportCallResponse.setStudentInstructionalSupportCall(studentInstructionalSupportCall);
        studentInstructionalSupportCallResponse.setInstructionalSupportStaff(instructionalSupportStaff);

        return studentInstructionalSupportCallResponseRepository.save(studentInstructionalSupportCallResponse);
    }
}
