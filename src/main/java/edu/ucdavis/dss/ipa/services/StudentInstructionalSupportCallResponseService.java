package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.SupportStaff;
import edu.ucdavis.dss.ipa.entities.StudentSupportCall;
import edu.ucdavis.dss.ipa.entities.StudentSupportCallResponse;

import java.util.List;

public interface StudentInstructionalSupportCallResponseService {
    StudentSupportCallResponse findOneById(long studentInstructionalSupportCallResponseId);

    List<StudentSupportCallResponse> findByScheduleId(long scheduleId);

    StudentSupportCallResponse create(StudentSupportCall studentSupportCall, SupportStaff supportStaff);

    void delete(long studentInstructionalSupportCallResponseId);

    StudentSupportCallResponse update(StudentSupportCallResponse studentSupportCallResponse);

    List<StudentSupportCallResponse> findByScheduleIdAndSupportStaffId(long scheduleId, long supportStaffId);

    List<StudentSupportCallResponse> findByScheduleIdAndTermCode(long id, String termCode);

    void sendNotificationsByWorkgroupId(Long workgroupId);
}