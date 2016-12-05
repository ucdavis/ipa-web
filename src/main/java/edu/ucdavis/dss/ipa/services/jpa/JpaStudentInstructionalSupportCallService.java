package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.InstructionalSupportStaff;
import edu.ucdavis.dss.ipa.entities.StudentInstructionalSupportCall;
import edu.ucdavis.dss.ipa.entities.StudentInstructionalSupportCallResponse;
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
    public StudentInstructionalSupportCall findOneById(long studentInstructionalSupportCallId) {
        return studentInstructionalSupportCallRepository.findById(studentInstructionalSupportCallId);
    }

    @Override
    public StudentInstructionalSupportCall findOrCreate(StudentInstructionalSupportCall studentInstructionalSupportCallDTO) {

        // Find the support call based on matching participants? needs discussion.

        // Extract the supportStaffIds
        List<Long> supportStaffIds = new ArrayList<>();

        for (StudentInstructionalSupportCallResponse studentInstructionalSupportCallResponse : studentInstructionalSupportCallDTO.getStudentInstructionalSupportCallResponses()) {
            Long supportStaffId = studentInstructionalSupportCallResponse.getInstructionalSupportStaff().getId();

            supportStaffIds.add(supportStaffId);
        }

        // Make the supportCall
        StudentInstructionalSupportCall studentInstructionalSupportCall = this.create(studentInstructionalSupportCallDTO);

        // Make supportCallResponses
        List<StudentInstructionalSupportCallResponse> supportCallResponses = new ArrayList<>();

        for (Long supportStaffId : supportStaffIds) {
            InstructionalSupportStaff instructionalSupportStaff = instructionalSupportStaffService.findOneById(supportStaffId);
            StudentInstructionalSupportCallResponse supportCallResponse = studentInstructionalSupportCallResponseService.create(studentInstructionalSupportCall, instructionalSupportStaff);

            supportCallResponses.add(supportCallResponse);
        }

        // Tie the supportCallResponses to the new SupportCall
        studentInstructionalSupportCall.setStudentInstructionalSupportCallResponses(supportCallResponses);

        return studentInstructionalSupportCall;
    }

    @Override
    public List<StudentInstructionalSupportCall> findByScheduleId(long scheduleId) {
        return studentInstructionalSupportCallRepository.findByScheduleId(scheduleId);
    }

    @Override
    public void delete(long studentInstructionalSupportCallId) {
        StudentInstructionalSupportCall studentInstructionalSupportCall = this.findOneById(studentInstructionalSupportCallId);

        studentInstructionalSupportCallRepository.delete(studentInstructionalSupportCallId);
    }

    private StudentInstructionalSupportCall create (StudentInstructionalSupportCall studentInstructionalSupportCall) {

        // Create StartDate
        java.util.Calendar cal = java.util.Calendar.getInstance();
        java.util.Date utilDate = cal.getTime();
        java.sql.Date sqlDate = new Date(utilDate.getTime());

        studentInstructionalSupportCall.setStartDate(sqlDate);
        studentInstructionalSupportCall.setStudentInstructionalSupportCallResponses(null);

        return studentInstructionalSupportCallRepository.save(studentInstructionalSupportCall);
    }
}
