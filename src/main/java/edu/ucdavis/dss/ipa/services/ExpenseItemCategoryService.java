package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.ExpenseItemCategory;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface ExpenseItemCategoryService {
    List<ExpenseItemCategory> findAll();

    ExpenseItemCategory findById(long expenseItemCategoryId);

    ExpenseItemCategory findByDescription(String description);
}
