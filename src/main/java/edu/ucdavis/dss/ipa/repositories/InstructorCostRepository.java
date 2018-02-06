package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.InstructorCost;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InstructorCostRepository extends CrudRepository<InstructorCost, Long> {
    InstructorCost findById(Long lineItemId);

    void deleteById(long lineItemId);

    List<InstructorCost> findByBudgetId(Long budgetId);

    InstructorCost findByInstructorIdAndBudgetId(long id, long id1);

    @Modifying
    @Query("UPDATE InstructorCost ic SET ic.instructorType = NULL WHERE ic.instructorType.id = :instructorTypeId")
    void removeAssociationByInstructorTypeId(@Param("instructorTypeId") long instructorTypeId);
}
