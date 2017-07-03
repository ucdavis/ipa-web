package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.LineItem;
import edu.ucdavis.dss.ipa.repositories.LineItemRepository;
import edu.ucdavis.dss.ipa.services.LineItemService;
import org.springframework.stereotype.Service;

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

        return lineItemRepository.save(lineItem);
    }
}
