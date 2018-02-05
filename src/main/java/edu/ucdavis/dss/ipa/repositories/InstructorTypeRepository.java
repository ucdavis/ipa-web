package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.InstructorCost;
import edu.ucdavis.dss.ipa.entities.InstructorType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InstructorTypeRepository extends CrudRepository<InstructorType, Long> {
    InstructorType findById(Long instructorTypeId);

    void deleteById(long instructorTypeId);

    List<InstructorType> findByBudgetId(Long budgetId);

    InstructorType findByDescriptionAndBudgetId(String description, long id);
}
