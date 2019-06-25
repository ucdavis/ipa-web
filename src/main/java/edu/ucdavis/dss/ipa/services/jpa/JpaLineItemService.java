package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.BudgetScenario;
import edu.ucdavis.dss.ipa.entities.LineItem;
import edu.ucdavis.dss.ipa.entities.TeachingAssignment;
import edu.ucdavis.dss.ipa.entities.Term;
import edu.ucdavis.dss.ipa.repositories.LineItemRepository;
import edu.ucdavis.dss.ipa.services.LineItemCategoryService;
import edu.ucdavis.dss.ipa.services.LineItemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class JpaLineItemService implements LineItemService {
    @Inject LineItemRepository lineItemRepository;
    @Inject LineItemCategoryService lineItemCategoryService;

    @Override
    public List<LineItem> findByBudgetId(Long budgetId) {
        return lineItemRepository.findByBudgetId(budgetId);
    }

    @Override
    public LineItem findById(Long lineItemId) {
        return lineItemRepository.findById(lineItemId);
    }

    @Override
    @Transactional
    public void deleteById(long lineItemId) {
        lineItemRepository.deleteById(lineItemId);
    }

    @Override
    public LineItem findOrCreate(LineItem lineItemDTO) {
        LineItem lineItem = this.findById(lineItemDTO.getId());

        if (lineItem != null) {
            return lineItem;
        }

        lineItem = new LineItem();
        lineItem.setBudgetScenario(lineItemDTO.getBudgetScenario());
        lineItem.setLineItemCategory(lineItemDTO.getLineItemCategory());
        lineItem.setAmount(lineItemDTO.getAmount());
        lineItem.setAccountNumber((lineItemDTO.getAccountNumber()));
        lineItem.setNotes(lineItemDTO.getNotes());
        lineItem.setDescription(lineItemDTO.getDescription());
        lineItem.setTeachingAssignment(lineItemDTO.getTeachingAssignment());
        lineItem.setHidden(lineItemDTO.getHidden());

        return lineItemRepository.save(lineItem);
    }

    @Override
    public LineItem update(LineItem lineItem) {
        LineItem originalLineItem = this.findById(lineItem.getId());

        if(originalLineItem == null) {
            return null;
        }

        originalLineItem.setDescription(lineItem.getDescription());
        originalLineItem.setAmount(lineItem.getAmount());
        originalLineItem.setAccountNumber(lineItem.getAccountNumber());
        originalLineItem.setNotes(lineItem.getNotes());
        originalLineItem.setLineItemCategory(lineItem.getLineItemCategory());
        originalLineItem.setHidden(lineItem.getHidden());

        return this.lineItemRepository.save(originalLineItem);
    }

    @Override
    public LineItem createDuplicate(LineItem originalLineItem, BudgetScenario budgetScenario) {
        LineItem lineItem = new LineItem();

        lineItem.setBudgetScenario(budgetScenario);

        lineItem.setNotes(originalLineItem.getNotes());
        lineItem.setAmount(originalLineItem.getAmount());
        lineItem.setAccountNumber(originalLineItem.getAccountNumber());
        lineItem.setDescription(originalLineItem.getDescription());
        lineItem.setLineItemCategory(originalLineItem.getLineItemCategory());
        lineItem.setHidden(originalLineItem.getHidden());
        lineItem.setTeachingAssignment(originalLineItem.getTeachingAssignment());

        return this.lineItemRepository.save(lineItem);
    }

    @Transactional
    @Override
    public void deleteMany(List<Long> lineItemIds) {
        for (Long lineItemId : lineItemIds) {
            this.deleteById(lineItemId);
        }
    }

    @Override
    public List<LineItem> findbyWorkgroupIdAndYear(long workgroupId, long year) {
        return lineItemRepository.findbyWorkgroupIdAndYear(workgroupId, year);
    }

    @Override
    public List<LineItem> duplicateFunds(BudgetScenario budgetScenario, BudgetScenario originalBudgetScenario) {
        List<LineItem> lineItems = new ArrayList<>();

        for (LineItem originalLineItem : originalBudgetScenario.getLineItems()) {
            LineItem lineItem = this.createDuplicate(originalLineItem, budgetScenario);
            lineItems.add(lineItem);
        }

        return lineItems;
    }
}
