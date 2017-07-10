package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.LineItem;
import edu.ucdavis.dss.ipa.repositories.LineItemRepository;
import edu.ucdavis.dss.ipa.services.LineItemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;

@Service
public class JpaLineItemService implements LineItemService {
    @Inject LineItemRepository lineItemRepository;

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
        lineItem.setNotes(lineItemDTO.getNotes());
        lineItem.setDescription(lineItemDTO.getDescription());
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
        originalLineItem.setNotes(lineItem.getNotes());

        return this.lineItemRepository.save(originalLineItem);
    }
}
