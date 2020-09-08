package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.entities.SectionGroupCostInstructor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SectionGroupCostInstructorRepository extends CrudRepository<SectionGroupCostInstructor, Long> {
    SectionGroupCostInstructor findById(long sectionGroupCostInstructorId);

    @Query( " SELECT DISTINCT sgci" +
            " FROM SectionGroupCostInstructor sgci, SectionGroupCost sgc, BudgetScenario bs, Budget b" +
            " WHERE b.id = :budgetId" +
            " AND sgci.sectionGroupCost = sgc" +
            " AND sgc.budgetScenario = bs" +
            " AND bs.budget = b"
    )
    List<SectionGroupCostInstructor> findByBudgetId(
            @Param("budgetId") long budgetId);
}