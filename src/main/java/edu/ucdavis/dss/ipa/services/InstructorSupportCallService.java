package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.InstructorSupportCall;

import java.util.List;

public interface InstructorSupportCallService {
    InstructorSupportCall findOneById(long instructorInstructionalSupportCallId);

    InstructorSupportCall findOrCreate(InstructorSupportCall instructorSupportCall);

    List<InstructorSupportCall> findByScheduleId(long scheduleId);

    void delete(long instructorInstructionalSupportCallId);

    List<InstructorSupportCall> findByScheduleIdAndInstructorId(long scheduleId, long instructorId);

}