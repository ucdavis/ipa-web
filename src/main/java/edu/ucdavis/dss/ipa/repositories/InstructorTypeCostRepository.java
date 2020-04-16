package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.InstructorTypeCost;
import edu.ucdavis.dss.ipa.entities.LineItem;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InstructorTypeCostRepository extends CrudRepository<InstructorTypeCost, Long> {
    void deleteById(long instructorTypeCostId);

    InstructorTypeCost findByInstructorTypeIdAndBudgetId(long instructorTypeId, long budgetId);

    List<InstructorTypeCost> findByBudgetId(Long budgetId);

    @Query( " SELECT DISTINCT itc" +
        " FROM Schedule s, Workgroup w, Budget b, InstructorTypeCost itc" +
        " WHERE itc.budget = b" +
        " AND b.schedule = s" +
        " AND s.workgroup = w" +
        " AND w.id = :workgroupId" +
        " AND s.year = :year")
    List<InstructorTypeCost> findbyWorkgroupIdAndYear(@Param("workgroupId") long workgroupId, @Param("year") long year);
}
