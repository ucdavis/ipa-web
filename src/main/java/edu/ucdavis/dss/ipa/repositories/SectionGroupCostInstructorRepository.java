package edu.ucdavis.dss.ipa.repositories;

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

    @Query( " SELECT DISTINCT sgci" +
            " FROM SectionGroupCostInstructor sgci, SectionGroupCost sgc, BudgetScenario bs" +
            " WHERE bs.id = :budgetScenarioId" +
            " AND sgci.sectionGroupCost = sgc" +
            " AND sgc.budgetScenario = bs"
    )
    List<SectionGroupCostInstructor> findByBudgetScenarioId(
            @Param("budgetScenarioId") long budgetScenarioId);

    @Query( " SELECT DISTINCT sgci" +
            " FROM Schedule s, Workgroup w, Budget b, BudgetScenario bs, SectionGroupCost sgc, SectionGroupCostInstructor sgci" +
            " WHERE sgci.sectionGroupCost = sgc" +
            " AND sgc.budgetScenario = bs" +
            " AND bs.budget = b" +
            " AND b.schedule = s" +
            " AND s.workgroup = w" +
            " AND w.id = :workgroupId" +
            " AND s.year = :year")
    List<SectionGroupCostInstructor> findbyWorkgroupIdAndYear(@Param("workgroupId") long workgroupId, @Param("year") long year);
}