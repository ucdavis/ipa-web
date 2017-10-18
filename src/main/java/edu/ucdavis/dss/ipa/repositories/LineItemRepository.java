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
    void deleteLineItemsWithIds(List<Integer> ids);

}
