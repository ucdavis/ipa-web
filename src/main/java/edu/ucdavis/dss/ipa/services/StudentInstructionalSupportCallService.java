package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.StudentSupportCall;

import java.util.List;

public interface StudentInstructionalSupportCallService {
    StudentSupportCall findOneById(long studentInstructionalSupportCallId);

    StudentSupportCall findOrCreate(StudentSupportCall studentSupportCall);

    List<StudentSupportCall> findByScheduleId(long scheduleId);

    void delete(long studentInstructionalSupportCallId);

    List<StudentSupportCall> findByScheduleIdAndSupportStaffId(long scheduleId, long supportStaffId);
}