package edu.ucdavis.dss.ipa.services;

import org.springframework.validation.annotation.Validated;

import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.ScheduleInstructorNote;

import java.util.List;

@Validated
public interface ScheduleInstructorNoteService {

	ScheduleInstructorNote saveScheduleInstructorNote(ScheduleInstructorNote isn);

	ScheduleInstructorNote findById(Long id);

	ScheduleInstructorNote findOrCreateOneByInstructorAndSchedule(Instructor instructor, Schedule schedule);

	ScheduleInstructorNote findOneByInstructorIdAndScheduleId(long instructorId, long scheduleId);

	List<ScheduleInstructorNote> findByScheduleId(long id);
}