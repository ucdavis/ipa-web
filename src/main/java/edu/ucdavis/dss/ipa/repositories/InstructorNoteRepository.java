package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.InstructorNote;
import org.springframework.data.repository.CrudRepository;

public interface InstructorNoteRepository extends CrudRepository<InstructorNote, Long> {
  InstructorNote findById(Long id);

  InstructorNote findByScheduleIdAndInstructorId(long scheduleId, long instructorId);
}
