package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.SupportStaff;
import edu.ucdavis.dss.ipa.entities.StudentSupportCallResponse;
import edu.ucdavis.dss.ipa.repositories.StudentSupportCallResponseRepository;
import edu.ucdavis.dss.ipa.services.StudentSupportCallResponseService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Service
public class JpaStudentSupportCallResponseService implements StudentSupportCallResponseService {

    @Inject StudentSupportCallResponseRepository studentSupportCallResponseRepository;

    @Override
    public StudentSupportCallResponse findOneById(long studentInstructionalSupportCallResponseId) {
        return studentSupportCallResponseRepository.findById(studentInstructionalSupportCallResponseId);
    }

    @Override
    public List<StudentSupportCallResponse> findByScheduleId(long scheduleId) {
        List<StudentSupportCallResponse> studentSupportCallResponses = studentSupportCallResponseRepository.findByScheduleId(scheduleId);

        return studentSupportCallResponses;
    }

    @Override
    public void delete(long studentInstructionalSupportCallResponseId) {
        studentSupportCallResponseRepository.delete(studentInstructionalSupportCallResponseId);
    }

    @Override
    public StudentSupportCallResponse update(StudentSupportCallResponse studentSupportCallResponse) {
        return studentSupportCallResponseRepository.save(studentSupportCallResponse);
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
            if (supportCallResponse.getTermCode().equals(termCode)) {
                filteredSupportCallResponses.add(supportCallResponse);
            }
        }

        return filteredSupportCallResponses;
    }

    @Override
    public void sendNotificationsByWorkgroupId(Long workgroupId) {
        
    }

    @Override
    public StudentSupportCallResponse findByScheduleIdAndSupportStaffIdAndTermCode(long scheduleId, long supportStaffId, String termCode) {
        return studentSupportCallResponseRepository.findByScheduleIdAndSupportStaffIdAndTermCode(scheduleId, supportStaffId, termCode);
    }

    @Override
    public StudentSupportCallResponse create (StudentSupportCallResponse studentSupportCallResponseDTO) {
        return studentSupportCallResponseRepository.save(studentSupportCallResponseDTO);
    }
}
