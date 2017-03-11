package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.InstructorSupportCall;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface InstructorInstructionalSupportCallRepository extends CrudRepository<InstructorSupportCall, Long> {

    List<InstructorSupportCall> findByScheduleId(long scheduleId);

    InstructorSupportCall findById(long instructorInstructionalSupportCallId);
}
