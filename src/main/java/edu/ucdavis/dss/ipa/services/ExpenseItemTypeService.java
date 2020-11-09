package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.ExpenseItemType;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface ExpenseItemTypeService {
    List<ExpenseItemType> findAll();

    ExpenseItemType findById(long ExpenseItemTypeId);

    ExpenseItemType findByDescription(String description);
}
