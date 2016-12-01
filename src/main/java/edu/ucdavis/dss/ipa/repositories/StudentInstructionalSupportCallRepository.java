package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.StudentInstructionalSupportCall;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface StudentInstructionalSupportCallRepository extends CrudRepository<StudentInstructionalSupportCall, Long> {
    List<StudentInstructionalSupportCall> findByScheduleId (long scheduleId);

    StudentInstructionalSupportCall findById (long studentInstructionalSupportCallId);
}
