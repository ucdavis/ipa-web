package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.InstructorNote;
import org.springframework.validation.annotation.Validated;

@Validated
public interface InstructorNoteService {
  InstructorNote findOrCreateByScheduleIdAndInstructorId(long scheduleId, long instructorId);

  InstructorNote update(InstructorNote instructorNote);
}
