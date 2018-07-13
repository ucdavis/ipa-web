package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.LineItem;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LineItemRepository extends CrudRepository<LineItem, Long> {
    @Query( " SELECT DISTINCT li" +
            " FROM Budget b, BudgetScenario bs, LineItem li" +
            " WHERE li.budgetScenario = bs" +
            " AND bs.budget = b" +
            " AND b.id = :budgetId ")
    List<LineItem> findByBudgetId(@Param("budgetId") Long budgetId);

    LineItem findById(Long lineItemId);

    void deleteById(long lineItemId);

    @Modifying
    @Query("delete from LineItem l where l.id in ?1")
    void deleteLineItemsWithIds(List<Long> ids);

    @Query( " SELECT DISTINCT li" +
            " FROM Schedule s, Workgroup w, Budget b, BudgetScenario bs, LineItem li" +
            " WHERE li.budgetScenario = bs" +
            " AND bs.budget = b" +
            " AND b.schedule = s" +
            " AND s.workgroup = w" +
            " AND w.id = :workgroupId" +
            " AND s.year = :year")
    List<LineItem> findbyWorkgroupIdAndYear(@Param("workgroupId") long workgroupId, @Param("year") long year);
}
