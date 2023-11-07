package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.SectionGroupCost;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SectionGroupCostRepository extends CrudRepository<SectionGroupCost, Long> {
    @Query( " SELECT DISTINCT sgc" +
            " FROM Budget b, BudgetScenario bs, SectionGroupCost sgc" +
            " WHERE sgc.budgetScenario = bs" +
            " AND bs.budget = b" +
            " AND b.id = :budgetId ")
    List<SectionGroupCost> findByBudgetId(@Param("budgetId") Long budgetId);

    @Query( " SELECT DISTINCT sgc" +
        " FROM Schedule s, Workgroup w, Budget b, BudgetScenario bs, SectionGroupCost sgc" +
        " WHERE sgc.budgetScenario = bs" +
        " AND bs.budget = b" +
        " AND b.schedule = s" +
        " AND s.workgroup = w" +
        " AND w.id = :workgroupId" +
        " AND s.year = :year")
    List<SectionGroupCost> findbyWorkgroupIdAndYear(@Param("workgroupId") long workgroupId, @Param("year") long year);

    SectionGroupCost findBySubjectCodeAndCourseNumberAndSequencePatternAndBudgetScenarioIdAndTermCode(String subjectCode, String courseNumber, String sequencePattern, long id, String termCode);
}
