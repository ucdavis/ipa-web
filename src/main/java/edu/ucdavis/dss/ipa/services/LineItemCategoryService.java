package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.Budget;
import edu.ucdavis.dss.ipa.entities.BudgetScenario;
import edu.ucdavis.dss.ipa.entities.LineItem;
import edu.ucdavis.dss.ipa.entities.LineItemCategory;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface LineItemCategoryService {
    List<LineItemCategory> findAll();
}
