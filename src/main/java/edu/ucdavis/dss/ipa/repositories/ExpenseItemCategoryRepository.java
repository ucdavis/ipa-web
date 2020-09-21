package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.ExpenseItemCategory;
import org.springframework.data.repository.CrudRepository;

public interface ExpenseItemCategoryRepository extends CrudRepository<ExpenseItemCategory, Long> {
    ExpenseItemCategory findById(long expenseItemCategoryId);

    ExpenseItemCategory findByDescription(String description);
}