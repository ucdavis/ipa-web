package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.SupportStaff;
import edu.ucdavis.dss.ipa.entities.StudentInstructionalSupportCall;
import edu.ucdavis.dss.ipa.entities.StudentInstructionalSupportCallResponse;

import java.util.List;

public interface StudentInstructionalSupportCallResponseService {
    StudentInstructionalSupportCallResponse findOneById(long studentInstructionalSupportCallResponseId);

    List<StudentInstructionalSupportCallResponse> findByScheduleId(long scheduleId);

    StudentInstructionalSupportCallResponse create(StudentInstructionalSupportCall studentInstructionalSupportCall, SupportStaff supportStaff);

    void delete(long studentInstructionalSupportCallResponseId);

    StudentInstructionalSupportCallResponse update(StudentInstructionalSupportCallResponse studentSupportCallResponse);

    List<StudentInstructionalSupportCallResponse> findByScheduleIdAndSupportStaffId(long scheduleId, long supportStaffId);

    List<StudentInstructionalSupportCallResponse> findByScheduleIdAndTermCode(long id, String termCode);

    void sendNotificationsByWorkgroupId(Long workgroupId);
}