package edu.ucdavis.dss.ipa.repositories;

import org.springframework.data.repository.CrudRepository;

import edu.ucdavis.dss.ipa.entities.ScheduleInstructorNote;

import java.util.List;

public interface ScheduleInstructorNoteRepository extends CrudRepository<ScheduleInstructorNote, Long> {

	ScheduleInstructorNote findFirstByInstructorIdAndScheduleId(long instructorId, long scheduleId);

	List<ScheduleInstructorNote> findByScheduleId(long scheduleId);
}
