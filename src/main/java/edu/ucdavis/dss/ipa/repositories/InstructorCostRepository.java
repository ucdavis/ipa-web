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
    @Query("UPDATE InstructorCost ic SET ic.instructorTypeCost = NULL WHERE ic.instructorTypeCost.id = :instructorTypeCostId")
    void removeAssociationByInstructorTypeId(@Param("instructorTypeCost") long instructorTypeCostId);

    @Query( " SELECT DISTINCT ic" +
    " FROM Schedule s, Workgroup w, Budget b, InstructorCost ic" +
    " WHERE ic.budget = b" +
    " AND b.schedule = s" +
    " AND s.workgroup = w" +
    " AND w.id = :workgroupId" +
    " AND s.year = :year")
    List<InstructorCost> findByWorkgroupIdAndYear(@Param("workgroupId") long workgroupId, @Param("year") long year);
}
