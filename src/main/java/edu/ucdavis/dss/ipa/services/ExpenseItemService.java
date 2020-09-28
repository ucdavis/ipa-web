package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.BudgetScenario;
import edu.ucdavis.dss.ipa.entities.ExpenseItem;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface ExpenseItemService {
    List<ExpenseItem> findByBudgetId(Long budgetId);

    List<ExpenseItem> findByBudgetScenarioId(Long budgetScenarioId);

    ExpenseItem findById(Long expenseItemId);

    void deleteById(long expenseItemId);

    ExpenseItem findOrCreate(ExpenseItem expenseItemDTO);

    ExpenseItem update(ExpenseItem expenseItem);

    ExpenseItem createDuplicate(ExpenseItem originalExpenseItem, BudgetScenario budgetScenario);

    void deleteMany(List<Long> expenseItemIds);

    List<ExpenseItem> findbyWorkgroupIdAndYear(long workgroupId, long year);

    List<ExpenseItem> duplicateFunds(BudgetScenario budgetScenario, BudgetScenario originalBudgetScenario);
}
