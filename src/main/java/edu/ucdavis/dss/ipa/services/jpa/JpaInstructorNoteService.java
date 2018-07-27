package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.InstructorNote;
import edu.ucdavis.dss.ipa.repositories.InstructorNoteRepository;
import edu.ucdavis.dss.ipa.services.InstructorNoteService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class JpaInstructorNoteService implements InstructorNoteService {
  @Inject InstructorNoteRepository instructorNoteRepository;

  /**
   * Will either create the instructorNote, or find one that matches on schedule and instructor.
   * @param scheduleId
   * @param instructorId
   * @return
   */
  @Override
  public InstructorNote findOrCreateByScheduleIdAndInstructorId(long scheduleId, long instructorId) {
    return instructorNoteRepository.findOrCreateByScheduleIdAndInstructorId(scheduleId, instructorId);
  }

  private InstructorNote findById(long instructorNoteId) {
    return instructorNoteRepository.findById(instructorNoteId);
  }

  @Override
  public InstructorNote update(InstructorNote newInstructorNote) {
    InstructorNote originalInstructorNote = this.findById(newInstructorNote.getId());

    if(originalInstructorNote == null) {
      return null;
    }

    originalInstructorNote.setNote(newInstructorNote.getNote());

    return this.instructorNoteRepository.save(originalInstructorNote);
  }
}
