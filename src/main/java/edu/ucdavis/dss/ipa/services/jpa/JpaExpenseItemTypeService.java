package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.ExpenseItemType;
import edu.ucdavis.dss.ipa.repositories.ExpenseItemTypeRepository;
import edu.ucdavis.dss.ipa.services.ExpenseItemTypeService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

@Service
public class JpaExpenseItemTypeService implements ExpenseItemTypeService {
    @Inject
    ExpenseItemTypeRepository ExpenseItemTypeRepository;

    @Override
    public List<ExpenseItemType> findAll() {
        return (List<ExpenseItemType>) ExpenseItemTypeRepository.findAll();
    }

    @Override
    public ExpenseItemType findById(long ExpenseItemTypeId) {
        return ExpenseItemTypeRepository.findById(ExpenseItemTypeId).orElse(null);
    }

    @Override
    public ExpenseItemType findByDescription(String description) {
        return ExpenseItemTypeRepository.findByDescription(description);
    }
}
