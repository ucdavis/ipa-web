package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.SchedulingNote;
import org.springframework.validation.annotation.Validated;

@Validated
public interface SchedulingNoteService {
    SchedulingNote create(SchedulingNote schedulingNoteDTO);
}
