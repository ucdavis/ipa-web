package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.StudentSupportCall;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface StudentInstructionalSupportCallRepository extends CrudRepository<StudentSupportCall, Long> {
    List<StudentSupportCall> findByScheduleId (long scheduleId);

    StudentSupportCall findById (long studentInstructionalSupportCallId);
}
