package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.SupportStaff;
import edu.ucdavis.dss.ipa.entities.StudentSupportCall;
import edu.ucdavis.dss.ipa.entities.StudentSupportCallResponse;
import edu.ucdavis.dss.ipa.repositories.StudentInstructionalSupportCallResponseRepository;
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
    public StudentSupportCallResponse findOneById(long studentInstructionalSupportCallResponseId) {
        return studentInstructionalSupportCallResponseRepository.findById(studentInstructionalSupportCallResponseId);
    }

    @Override
    public List<StudentSupportCallResponse> findByScheduleId(long scheduleId) {
        List<StudentSupportCall> studentSupportCalls = studentInstructionalSupportCallService.findByScheduleId(scheduleId);
        List<StudentSupportCallResponse> studentSupportCallResponses = new ArrayList<>();

        for (StudentSupportCall studentSupportCall : studentSupportCalls) {
            studentSupportCallResponses.addAll(studentSupportCall.getStudentSupportCallResponses());
        }

        return studentSupportCallResponses;
    }

    @Override
    public void delete(long studentInstructionalSupportCallResponseId) {
        studentInstructionalSupportCallResponseRepository.delete(studentInstructionalSupportCallResponseId);
    }

    @Override
    public StudentSupportCallResponse update(StudentSupportCallResponse studentSupportCallResponse) {
        return studentInstructionalSupportCallResponseRepository.save(studentSupportCallResponse);
    }

    @Override
    public List<StudentSupportCallResponse> findByScheduleIdAndSupportStaffId(long scheduleId, long supportStaffId) {
        List<StudentSupportCallResponse> scheduleSupportCallResponses = this.findByScheduleId(scheduleId);
        List<StudentSupportCallResponse> filteredSupportCallResponses = new ArrayList<>();

        for (StudentSupportCallResponse supportCallResponse : scheduleSupportCallResponses) {
            if (supportCallResponse.getInstructionalSupportStaffIdentification() == supportStaffId) {
                filteredSupportCallResponses.add(supportCallResponse);
            }
        }

        return filteredSupportCallResponses;
    }

    @Override
    public List<StudentSupportCallResponse> findByScheduleIdAndTermCode(long scheduleId, String termCode) {
        List<StudentSupportCallResponse> scheduleSupportCallResponses = this.findByScheduleId(scheduleId);
        List<StudentSupportCallResponse> filteredSupportCallResponses = new ArrayList<>();

        for (StudentSupportCallResponse supportCallResponse : scheduleSupportCallResponses) {
            if (supportCallResponse.getStudentSupportCall().getTermCode().equals(termCode)) {
                filteredSupportCallResponses.add(supportCallResponse);
            }
        }

        return filteredSupportCallResponses;
    }

    @Override
    public void sendNotificationsByWorkgroupId(Long workgroupId) {
        
    }

    @Override
    public StudentSupportCallResponse create (StudentSupportCall studentSupportCall, SupportStaff supportStaff) {
        StudentSupportCallResponse studentSupportCallResponse = new StudentSupportCallResponse();

        studentSupportCallResponse.setStudentSupportCall(studentSupportCall);
        studentSupportCallResponse.setSupportStaff(supportStaff);

        return studentInstructionalSupportCallResponseRepository.save(studentSupportCallResponse);
    }
}
