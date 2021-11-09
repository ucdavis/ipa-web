package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.SchedulingNote;
import org.springframework.data.repository.CrudRepository;

public interface SchedulingNoteRepository extends CrudRepository<SchedulingNote, Long> {
}
