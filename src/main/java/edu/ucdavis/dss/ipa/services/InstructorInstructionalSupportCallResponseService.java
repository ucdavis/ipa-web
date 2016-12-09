package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.*;

import java.util.List;

public interface InstructorInstructionalSupportCallResponseService {
    InstructorInstructionalSupportCallResponse findOneById(long instructorInstructionalSupportCallResponseId);

    List<InstructorInstructionalSupportCallResponse> findByScheduleId(long scheduleId);

    InstructorInstructionalSupportCallResponse create(InstructorInstructionalSupportCall instructorInstructionalSupportCall, Instructor instructor);

    void delete(long instructorInstructionalSupportCallResponseId);

    InstructorInstructionalSupportCallResponse update(InstructorInstructionalSupportCallResponse instructorInstructionalSupportCallResponse);

    List<InstructorInstructionalSupportCallResponse> findByScheduleIdAndInstructorId(long scheduleId, long instructorId);
}