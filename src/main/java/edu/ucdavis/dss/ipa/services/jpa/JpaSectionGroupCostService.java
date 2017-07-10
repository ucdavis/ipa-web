package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.BudgetScenario;
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

    @Override
    public SectionGroupCost createFrom(SectionGroupCost originalSectionGroupCost, BudgetScenario budgetScenario) {
        SectionGroupCost sectionGroupCost = new SectionGroupCost();

        sectionGroupCost.setBudgetScenario(budgetScenario);
        sectionGroupCost.setInstructor(originalSectionGroupCost.getInstructor());
        sectionGroupCost.setSectionGroup(originalSectionGroupCost.getSectionGroup());
        sectionGroupCost.setEnrollment(originalSectionGroupCost.getEnrollment());
        sectionGroupCost.setOriginalInstructor(originalSectionGroupCost.getOriginalInstructor());
        sectionGroupCost.setReaderCount(originalSectionGroupCost.getReaderCount());
        sectionGroupCost.setTaCount(originalSectionGroupCost.getTaCount());
        sectionGroupCost.setSectionCount(originalSectionGroupCost.getSectionCount());
        sectionGroupCost.setInstructorCost(originalSectionGroupCost.getInstructorCost());

        return this.sectionGroupCostRepository.save(sectionGroupCost);
    }
}
