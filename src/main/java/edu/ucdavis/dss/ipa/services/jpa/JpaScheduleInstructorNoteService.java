package edu.ucdavis.dss.ipa.services.jpa;

import javax.inject.Inject;

import edu.ucdavis.dss.ipa.services.ScheduleService;
import org.springframework.stereotype.Service;

import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.ScheduleInstructorNote;
import edu.ucdavis.dss.ipa.repositories.ScheduleInstructorNoteRepository;
import edu.ucdavis.dss.ipa.services.ScheduleInstructorNoteService;

import java.util.List;

@Service
public class JpaScheduleInstructorNoteService implements ScheduleInstructorNoteService {
	@Inject ScheduleInstructorNoteRepository scheduleInstructorNoteRepository;
	@Inject ScheduleService scheduleService;

	@Override
	public ScheduleInstructorNote saveScheduleInstructorNote(ScheduleInstructorNote isn) {
		return scheduleInstructorNoteRepository.save(isn);
	}

	@Override
	public ScheduleInstructorNote findById(Long id) {
		return scheduleInstructorNoteRepository.findOne(id);
	}

	@Override
	public ScheduleInstructorNote findOrCreateOneByInstructorAndSchedule(Instructor instructor, Schedule schedule) {
		if (instructor == null || schedule == null) { return null; }

		ScheduleInstructorNote isn = scheduleInstructorNoteRepository.
				findFirstByInstructorIdAndScheduleId(instructor.getId(), schedule.getId());

		if (isn == null) {
			isn = new ScheduleInstructorNote();
			isn.setInstructor(instructor);
			isn.setSchedule(schedule);
			saveScheduleInstructorNote(isn);
		}

		return isn;
	}

	@Override
	public ScheduleInstructorNote findOneByInstructorIdAndScheduleId(long instructorId, long scheduleId) {
		return scheduleInstructorNoteRepository.findFirstByInstructorIdAndScheduleId(instructorId, scheduleId);
	}

	@Override
	public List<ScheduleInstructorNote> findByScheduleId(long id) {
		return scheduleInstructorNoteRepository.findByScheduleId(id);
	}

}
