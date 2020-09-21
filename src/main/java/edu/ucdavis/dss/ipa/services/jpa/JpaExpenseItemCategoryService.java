package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.ExpenseItemCategory;
import edu.ucdavis.dss.ipa.repositories.ExpenseItemCategoryRepository;
import edu.ucdavis.dss.ipa.services.ExpenseItemCategoryService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

@Service
public class JpaExpenseItemCategoryService implements ExpenseItemCategoryService {
    @Inject
    ExpenseItemCategoryRepository expenseItemCategoryRepository;

    @Override
    public List<ExpenseItemCategory> findAll() {
        return (List<ExpenseItemCategory>) expenseItemCategoryRepository.findAll();
    }

    @Override
    public ExpenseItemCategory findById(long expenseItemCategoryId) {
        return expenseItemCategoryRepository.findById(expenseItemCategoryId);
    }

    @Override
    public ExpenseItemCategory findByDescription(String description) {
        return expenseItemCategoryRepository.findByDescription(description);
    }
}
