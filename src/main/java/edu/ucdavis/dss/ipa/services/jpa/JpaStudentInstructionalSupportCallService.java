package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.StudentInstructionalSupportCall;
import edu.ucdavis.dss.ipa.repositories.StudentInstructionalSupportCallRepository;
import edu.ucdavis.dss.ipa.services.StudentInstructionalSupportCallService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

@Service
public class JpaStudentInstructionalSupportCallService implements StudentInstructionalSupportCallService {

    @Inject StudentInstructionalSupportCallRepository studentInstructionalSupportCallRepository;

    @Override
    public StudentInstructionalSupportCall findOneById(long studentInstructionalSupportCallId) {
        return studentInstructionalSupportCallRepository.findById(studentInstructionalSupportCallId);
    }

    @Override
    public StudentInstructionalSupportCall findOrCreate(StudentInstructionalSupportCall studentInstructionalSupportCall) {
        return this.create(studentInstructionalSupportCall);
    }

    @Override
    public List<StudentInstructionalSupportCall> findByScheduleId(long scheduleId) {
        return studentInstructionalSupportCallRepository.findByScheduleId(scheduleId);
    }

    @Override
    public void delete(long studentInstructionalSupportCallId) {
        studentInstructionalSupportCallRepository.delete(studentInstructionalSupportCallId);
    }

    private StudentInstructionalSupportCall create (StudentInstructionalSupportCall studentInstructionalSupportCall) {
        return studentInstructionalSupportCallRepository.save(studentInstructionalSupportCall);
    }
}
