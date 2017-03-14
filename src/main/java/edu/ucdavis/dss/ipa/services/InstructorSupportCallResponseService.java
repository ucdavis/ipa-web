package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.*;

import java.util.List;

public interface InstructorSupportCallResponseService {
    InstructorSupportCallResponse findOneById(long instructorInstructionalSupportCallResponseId);

    List<InstructorSupportCallResponse> findByScheduleId(long scheduleId);

    InstructorSupportCallResponse create(InstructorSupportCallResponse instructorSupportCallResponse);

    void delete(long instructorInstructionalSupportCallResponseId);

    InstructorSupportCallResponse update(InstructorSupportCallResponse instructorSupportCallResponse);

    List<InstructorSupportCallResponse> findByScheduleIdAndInstructorId(long scheduleId, long instructorId);

    void sendNotificationsByWorkgroupId(Long workgroupId);
}