package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.ExpenseItemType;
import org.springframework.data.repository.CrudRepository;

public interface ExpenseItemTypeRepository extends CrudRepository<ExpenseItemType, Long> {
    ExpenseItemType findById(long ExpenseItemTypeId);

    ExpenseItemType findByDescription(String description);
}