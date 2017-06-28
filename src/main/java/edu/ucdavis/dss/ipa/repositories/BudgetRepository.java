package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.Budget;
import org.springframework.data.repository.CrudRepository;

import edu.ucdavis.dss.ipa.entities.Instructor;

public interface BudgetRepository extends CrudRepository<Instructor, Long> {

    Budget findById(Long id);

    Budget findByScheduleId(Long scheduleId);
}