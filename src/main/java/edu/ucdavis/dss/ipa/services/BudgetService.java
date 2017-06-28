package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.Budget;
import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.Tag;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface BudgetService {
    Budget findOrCreateByScheduleId(Long scheduleId);
}
