package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.InstructionalSupportStaff;
import edu.ucdavis.dss.ipa.entities.StudentInstructionalSupportCall;
import edu.ucdavis.dss.ipa.entities.StudentInstructionalSupportCallResponse;
import edu.ucdavis.dss.ipa.entities.StudentInstructionalSupportPreference;

import java.util.List;

public interface StudentInstructionalSupportCallResponseService {
    StudentInstructionalSupportCallResponse findOneById(long studentInstructionalSupportCallResponseId);

    List<StudentInstructionalSupportCallResponse> findByScheduleId(long scheduleId);

    StudentInstructionalSupportCallResponse create(StudentInstructionalSupportCall studentInstructionalSupportCall, InstructionalSupportStaff instructionalSupportStaff);

    void delete(long studentInstructionalSupportCallResponseId);

    StudentInstructionalSupportCallResponse update(StudentInstructionalSupportCallResponse studentSupportCallResponse);

    List<StudentInstructionalSupportCallResponse> findByScheduleIdAndSupportStaffId(long scheduleId, long supportStaffId);
}