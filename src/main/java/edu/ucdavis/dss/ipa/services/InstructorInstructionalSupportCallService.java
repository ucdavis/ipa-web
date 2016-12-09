package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.InstructorInstructionalSupportCall;

import java.util.List;

public interface InstructorInstructionalSupportCallService {
    InstructorInstructionalSupportCall findOneById(long instructorInstructionalSupportCallId);

    InstructorInstructionalSupportCall findOrCreate(InstructorInstructionalSupportCall instructorInstructionalSupportCall);

    List<InstructorInstructionalSupportCall> findByScheduleId(long scheduleId);

    void delete(long instructorInstructionalSupportCallId);

    List<InstructorInstructionalSupportCall> findByScheduleIdAndInstructorId(long scheduleId, long instructorId);

}