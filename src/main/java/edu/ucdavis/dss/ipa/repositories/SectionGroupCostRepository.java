package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.SectionGroupCost;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SectionGroupCostRepository extends CrudRepository<SectionGroupCost, Long> {

    @Query( " SELECT DISTINCT sgc" +
            " FROM Budget b, BudgetScenaro bs, SectionGroupCost sgc" +
            " WHERE sgc.budgetScenario = bs" +
            " AND bs.budget = b" +
            " AND b.id = :budgetId ")
    List<SectionGroupCost> findByBudgetId(@Param("budgetId") Long budgetId);

}
