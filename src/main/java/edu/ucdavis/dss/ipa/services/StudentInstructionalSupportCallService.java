package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.StudentInstructionalSupportCall;
import org.springframework.stereotype.Service;

import java.util.List;

public interface StudentInstructionalSupportCallService {
    StudentInstructionalSupportCall findOneById(long studentInstructionalSupportCallId);

    StudentInstructionalSupportCall findOrCreate(StudentInstructionalSupportCall studentInstructionalSupportCall);

    List<StudentInstructionalSupportCall> findByScheduleId(long scheduleId);

    void delete(long studentInstructionalSupportCallId);
}