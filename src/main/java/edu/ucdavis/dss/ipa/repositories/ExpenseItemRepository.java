package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.ExpenseItem;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExpenseItemRepository extends CrudRepository<ExpenseItem, Long> {
    @Query( " SELECT DISTINCT e" +
            " FROM Budget b, BudgetScenario bs, ExpenseItem e" +
            " WHERE e.budgetScenario = bs" +
            " AND bs.budget = b" +
            " AND b.id = :budgetId ")
    List<ExpenseItem> findByBudgetId(@Param("budgetId") Long budgetId);

    ExpenseItem findById(Long expenseItemId);

    void deleteById(long expenseItemId);

    @Modifying
    @Query("delete from ExpenseItem e where e.id in ?1")
    void deleteExpenseItemsWithIds(List<Long> ids);

    @Query( " SELECT DISTINCT e" +
            " FROM Schedule s, Workgroup w, Budget b, BudgetScenario bs, ExpenseItem e" +
            " WHERE e.budgetScenario = bs" +
            " AND bs.budget = b" +
            " AND b.schedule = s" +
            " AND s.workgroup = w" +
            " AND w.id = :workgroupId" +
            " AND s.year = :year")
    List<ExpenseItem> findbyWorkgroupIdAndYear(@Param("workgroupId") long workgroupId, @Param("year") long year);
}
