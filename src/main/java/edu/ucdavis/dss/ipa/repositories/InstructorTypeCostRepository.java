package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.InstructorTypeCost;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface InstructorTypeCostRepository extends CrudRepository<InstructorTypeCost, Long> {
    InstructorTypeCost findById(Long instructorTypeId);

    void deleteById(long instructorTypeCostId);

    List<InstructorTypeCost> findByBudgetId(Long budgetId);

    InstructorTypeCost findByDescriptionAndBudgetId(String description, long budgetId);
}
