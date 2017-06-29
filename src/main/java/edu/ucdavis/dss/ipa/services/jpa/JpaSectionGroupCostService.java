package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.SectionGroupCost;
import edu.ucdavis.dss.ipa.repositories.SectionGroupCostRepository;
import edu.ucdavis.dss.ipa.services.SectionGroupCostService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

@Service
public class JpaSectionGroupCostService implements SectionGroupCostService {
    @Inject SectionGroupCostRepository sectionGroupCostRepository;

    @Override
    public List<SectionGroupCost> findByBudgetId(Long budgetId) {
        return sectionGroupCostRepository.findByBudgetId(budgetId);
    }
}
