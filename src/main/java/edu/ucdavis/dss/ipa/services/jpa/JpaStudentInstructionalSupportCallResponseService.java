package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.InstructionalSupportStaff;
import edu.ucdavis.dss.ipa.entities.StudentInstructionalSupportCall;
import edu.ucdavis.dss.ipa.entities.StudentInstructionalSupportCallResponse;
import edu.ucdavis.dss.ipa.entities.StudentInstructionalSupportPreference;
import edu.ucdavis.dss.ipa.repositories.StudentInstructionalSupportCallResponseRepository;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.StudentInstructionalSupportCallResponseService;
import edu.ucdavis.dss.ipa.services.StudentInstructionalSupportCallService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Service
public class JpaStudentInstructionalSupportCallResponseService implements StudentInstructionalSupportCallResponseService {

    @Inject StudentInstructionalSupportCallResponseService studentInstructionalSupportCallResponseService;
    @Inject StudentInstructionalSupportCallResponseRepository studentInstructionalSupportCallResponseRepository;
    @Inject StudentInstructionalSupportCallService studentInstructionalSupportCallService;

    @Override
    public StudentInstructionalSupportCallResponse findOneById(long studentInstructionalSupportCallResponseId) {
        return studentInstructionalSupportCallResponseRepository.findById(studentInstructionalSupportCallResponseId);
    }

    @Override
    public List<StudentInstructionalSupportCallResponse> findByScheduleId(long scheduleId) {
        List<StudentInstructionalSupportCall> studentSupportCalls = studentInstructionalSupportCallService.findByScheduleId(scheduleId);
        List<StudentInstructionalSupportCallResponse> studentSupportCallResponses = new ArrayList<>();

        for (StudentInstructionalSupportCall studentSupportCall : studentSupportCalls) {
            studentSupportCallResponses.addAll(studentSupportCall.getStudentInstructionalSupportCallResponses());
        }

        return studentSupportCallResponses;
    }

    @Override
    public void delete(long studentInstructionalSupportCallResponseId) {
        studentInstructionalSupportCallResponseRepository.delete(studentInstructionalSupportCallResponseId);
    }

    @Override
    public StudentInstructionalSupportCallResponse update(StudentInstructionalSupportCallResponse studentSupportCallResponse) {
        return studentInstructionalSupportCallResponseRepository.save(studentSupportCallResponse);
    }

    @Override
    public StudentInstructionalSupportCallResponse findByScheduleIdAndSupportStaffId(long scheduleId, long supportStaffId) {
        List<StudentInstructionalSupportCallResponse> scheduleSupportCallResponses = this.findByScheduleId(scheduleId);

        for (StudentInstructionalSupportCallResponse supportCallResponse : scheduleSupportCallResponses) {
            if (supportCallResponse.getInstructionalSupportStaffIdentification() == supportStaffId) {
                return supportCallResponse;
            }
        }

        return null;
    }

    @Override
    public StudentInstructionalSupportCallResponse create (StudentInstructionalSupportCall studentInstructionalSupportCall, InstructionalSupportStaff instructionalSupportStaff) {
        StudentInstructionalSupportCallResponse studentInstructionalSupportCallResponse = new StudentInstructionalSupportCallResponse();

        studentInstructionalSupportCallResponse.setStudentInstructionalSupportCall(studentInstructionalSupportCall);
        studentInstructionalSupportCallResponse.setInstructionalSupportStaff(instructionalSupportStaff);

        return studentInstructionalSupportCallResponseRepository.save(studentInstructionalSupportCallResponse);
    }
}
