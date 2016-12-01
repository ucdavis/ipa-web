package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.InstructorInstructionalSupportCall;

import java.util.List;

public interface InstructorInstructionalSupportCallService {
    InstructorInstructionalSupportCall findOneById(long studentInstructionalSupportCallId);

    InstructorInstructionalSupportCall findOrCreate(InstructorInstructionalSupportCall studentInstructionalSupportCall);

    List<InstructorInstructionalSupportCall> findByScheduleId(long scheduleId);

    void delete(long studentInstructionalSupportCallId);
}