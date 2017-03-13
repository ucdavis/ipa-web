package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.StudentSupportCall;

import java.util.List;

public interface StudentSupportCallService {
    StudentSupportCall findOneById(long studentSupportCallId);

    StudentSupportCall findOrCreate(StudentSupportCall studentSupportCall);

    List<StudentSupportCall> findByScheduleId(long scheduleId);

    void delete(long studentSupportCallId);

    List<StudentSupportCall> findByScheduleIdAndSupportStaffId(long scheduleId, long supportStaffId);
}