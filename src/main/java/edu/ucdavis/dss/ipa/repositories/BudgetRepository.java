package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.Budget;
import org.springframework.data.repository.CrudRepository;

public interface BudgetRepository extends CrudRepository<Budget, Long> {
    Budget findByScheduleId(Long scheduleId);
}