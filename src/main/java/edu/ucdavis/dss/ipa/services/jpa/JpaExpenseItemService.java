package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.BudgetScenario;
import edu.ucdavis.dss.ipa.entities.ExpenseItem;
import edu.ucdavis.dss.ipa.repositories.ExpenseItemRepository;
import edu.ucdavis.dss.ipa.services.ExpenseItemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Service
public class JpaExpenseItemService implements ExpenseItemService {
    @Inject ExpenseItemRepository expenseItemRepository;

    @Override
    public List<ExpenseItem> findByBudgetId(Long budgetId) {
        return expenseItemRepository.findByBudgetId(budgetId);
    }

    public List<ExpenseItem> findByBudgetScenarioId(Long budgetScenarioId) {
        return expenseItemRepository.findByBudgetScenarioId(budgetScenarioId);
    }

    @Override
    public ExpenseItem findById(Long expenseItemId) {
        return expenseItemRepository.findById(expenseItemId).orElse(null);
    }

    @Override
    @Transactional
    public void deleteById(long expenseItemId) {
        expenseItemRepository.deleteById(expenseItemId);
    }

    @Override
    public ExpenseItem findOrCreate(ExpenseItem expenseItemDTO) {
        ExpenseItem expenseItem = this.findById(expenseItemDTO.getId());

        if (expenseItem != null) {
            return expenseItem;
        }

        expenseItem = new ExpenseItem();
        expenseItem.setBudgetScenario(expenseItemDTO.getBudgetScenario());
        expenseItem.setExpenseItemType(expenseItemDTO.getExpenseItemType());
        expenseItem.setAmount(expenseItemDTO.getAmount());
        expenseItem.setDescription(expenseItemDTO.getDescription());
        expenseItem.setTermCode(expenseItemDTO.getTermCode());

        return expenseItemRepository.save(expenseItem);
    }

    @Override
    public ExpenseItem update(ExpenseItem expenseItem) {
        ExpenseItem originalExpenseItem = this.findById(expenseItem.getId());

        if(originalExpenseItem == null) {
            return null;
        }

        if (originalExpenseItem.getBudgetScenario().getIsBudgetRequest()) {
            return null;
        }

        originalExpenseItem.setDescription(expenseItem.getDescription());
        originalExpenseItem.setAmount(expenseItem.getAmount());
        originalExpenseItem.setExpenseItemType(expenseItem.getExpenseItemType());
        originalExpenseItem.setTermCode(expenseItem.getTermCode());

        return this.expenseItemRepository.save(originalExpenseItem);
    }

    @Override
    public ExpenseItem createDuplicate(ExpenseItem originalExpenseItem, BudgetScenario budgetScenario) {
        ExpenseItem expenseItem = new ExpenseItem();

        expenseItem.setBudgetScenario(budgetScenario);

        expenseItem.setAmount(originalExpenseItem.getAmount());
        expenseItem.setDescription(originalExpenseItem.getDescription());
        expenseItem.setExpenseItemType(originalExpenseItem.getExpenseItemType());
        expenseItem.setTermCode(originalExpenseItem.getTermCode());

        return this.expenseItemRepository.save(expenseItem);
    }

    @Transactional
    @Override
    public void deleteMany(List<Long> expenseItemIds) {
        for (Long expenseItemId : expenseItemIds) {
            this.deleteById(expenseItemId);
        }
    }

    @Override
    public List<ExpenseItem> findbyWorkgroupIdAndYear(long workgroupId, long year) {
        return expenseItemRepository.findbyWorkgroupIdAndYear(workgroupId, year);
    }

    @Override
    public List<ExpenseItem> duplicateExpenses(BudgetScenario budgetScenario, BudgetScenario originalBudgetScenario) {
        List<ExpenseItem> expenseItems = new ArrayList<>();

        for (ExpenseItem originalExpenseItem : originalBudgetScenario.getExpenseItems()) {
            ExpenseItem expenseItem = this.createDuplicate(originalExpenseItem, budgetScenario);
            expenseItems.add(expenseItem);
        }

        return expenseItems;
    }
}
