package edu.ucdavis.dss.ipa.repositories;

import org.springframework.data.repository.CrudRepository;

import edu.ucdavis.dss.ipa.entities.ScheduleInstructorNote;

public interface ScheduleInstructorNoteRepository extends CrudRepository<ScheduleInstructorNote, Long> {

	ScheduleInstructorNote findFirstByInstructorIdAndScheduleId(long instructorId, long scheduleId);

}
