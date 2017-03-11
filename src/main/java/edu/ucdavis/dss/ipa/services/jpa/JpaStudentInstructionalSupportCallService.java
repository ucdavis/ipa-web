package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.SupportStaff;
import edu.ucdavis.dss.ipa.entities.StudentSupportCall;
import edu.ucdavis.dss.ipa.entities.StudentSupportCallResponse;
import edu.ucdavis.dss.ipa.repositories.StudentInstructionalSupportCallRepository;
import edu.ucdavis.dss.ipa.services.InstructionalSupportStaffService;
import edu.ucdavis.dss.ipa.services.StudentInstructionalSupportCallResponseService;
import edu.ucdavis.dss.ipa.services.StudentInstructionalSupportCallService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Service
public class JpaStudentInstructionalSupportCallService implements StudentInstructionalSupportCallService {

    @Inject StudentInstructionalSupportCallRepository studentInstructionalSupportCallRepository;
    @Inject StudentInstructionalSupportCallResponseService studentInstructionalSupportCallResponseService;
    @Inject InstructionalSupportStaffService instructionalSupportStaffService;

    @Override
    public StudentSupportCall findOneById(long studentInstructionalSupportCallId) {
        return studentInstructionalSupportCallRepository.findById(studentInstructionalSupportCallId);
    }

    @Override
    public StudentSupportCall findOrCreate(StudentSupportCall studentSupportCallDTO) {

        // Find the support call based on matching participants? needs discussion.

        // Extract the supportStaffIds
        List<Long> supportStaffIds = new ArrayList<>();

        for (StudentSupportCallResponse studentSupportCallResponse : studentSupportCallDTO.getStudentSupportCallResponses()) {
            Long supportStaffId = studentSupportCallResponse.getSupportStaff().getId();

            supportStaffIds.add(supportStaffId);
        }

        // Make the supportCall
        StudentSupportCall studentSupportCall = this.create(studentSupportCallDTO);

        // Make supportCallResponses
        List<StudentSupportCallResponse> supportCallResponses = new ArrayList<>();

        for (Long supportStaffId : supportStaffIds) {
            SupportStaff supportStaff = instructionalSupportStaffService.findOneById(supportStaffId);
            StudentSupportCallResponse supportCallResponse = studentInstructionalSupportCallResponseService.create(studentSupportCall, supportStaff);

            supportCallResponses.add(supportCallResponse);
        }

        // Tie the supportCallResponses to the new SupportCall
        studentSupportCall.setStudentSupportCallResponses(supportCallResponses);

        return studentSupportCall;
    }

    @Override
    public List<StudentSupportCall> findByScheduleId(long scheduleId) {
        return studentInstructionalSupportCallRepository.findByScheduleId(scheduleId);
    }

    @Override
    public void delete(long studentInstructionalSupportCallId) {
        StudentSupportCall studentSupportCall = this.findOneById(studentInstructionalSupportCallId);

        studentInstructionalSupportCallRepository.delete(studentInstructionalSupportCallId);
    }

    @Override
    public List<StudentSupportCall> findByScheduleIdAndSupportStaffId(long scheduleId, long supportStaffId) {
        List<StudentSupportCall> scheduleSupportCalls = this.findByScheduleId(scheduleId);
        List<StudentSupportCall> filteredSupportCalls = new ArrayList<>();

        for (StudentSupportCall studentSupportCall : scheduleSupportCalls) {
            for (StudentSupportCallResponse studentSupportCallResponse : studentSupportCall.getStudentSupportCallResponses()) {
                if (studentSupportCallResponse.getInstructionalSupportStaffIdentification() == supportStaffId) {
                    filteredSupportCalls.add(studentSupportCall);
                    break;
                }
            }
        }

        return filteredSupportCalls;
    }

    private StudentSupportCall create (StudentSupportCall studentSupportCall) {

        // Create StartDate
        java.util.Calendar cal = java.util.Calendar.getInstance();
        java.util.Date utilDate = cal.getTime();
        java.sql.Date sqlDate = new Date(utilDate.getTime());

        studentSupportCall.setStartDate(sqlDate);
        studentSupportCall.setStudentSupportCallResponses(null);

        return studentInstructionalSupportCallRepository.save(studentSupportCall);
    }
}
