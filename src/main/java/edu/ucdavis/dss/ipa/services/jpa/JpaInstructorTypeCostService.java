package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.BudgetScenario;
import edu.ucdavis.dss.ipa.entities.InstructorTypeCost;
import edu.ucdavis.dss.ipa.repositories.InstructorTypeCostRepository;
import edu.ucdavis.dss.ipa.services.InstructorCostService;
import edu.ucdavis.dss.ipa.services.InstructorTypeCostService;
import java.util.ArrayList;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;

@Service
public class JpaInstructorTypeCostService implements InstructorTypeCostService {
    @Inject InstructorTypeCostRepository instructorTypeCostRepository;
    @Inject InstructorCostService instructorCostService;

    @Override
    public List<InstructorTypeCost> findByBudgetId(Long budgetId) {
        return instructorTypeCostRepository.findByBudgetId(budgetId);
    }

    @Override
    public InstructorTypeCost findByInstructorTypeIdAndBudgetId(long instructorTypeId, long budgetId) {
        return instructorTypeCostRepository.findByInstructorTypeIdAndBudgetId(instructorTypeId, budgetId);
    }

    @Override
    public InstructorTypeCost findById(Long instructorTypeId) {
        return instructorTypeCostRepository.findById(instructorTypeId);
    }

    @Override
    @Transactional
    public void deleteById(long instructorTypeId) {
        instructorCostService.removeAssociationByInstructorTypeId(instructorTypeId);

        instructorTypeCostRepository.deleteById(instructorTypeId);
    }

    /**
     * Will return null if newInstructorTypeCost does not already exist in the database.
     * Will only update the 'Cost' value on the instructorTypeCost.
     * @param newInstructorTypeCost
     * @return
     */
    @Override
    public InstructorTypeCost update(InstructorTypeCost newInstructorTypeCost) {
        InstructorTypeCost originalInstructorType = this.findById(newInstructorTypeCost.getId());

        if(originalInstructorType == null) {
            return null;
        }

        if (null != originalInstructorType.getBudgetScenario() && originalInstructorType.getBudgetScenario().getIsSnapshot()) {
            return null;
        }

        originalInstructorType.setCost(newInstructorTypeCost.getCost());

        return this.instructorTypeCostRepository.save(originalInstructorType);
    }

    @Override
    public InstructorTypeCost findOrCreate(InstructorTypeCost instructorTypeCostDTO) {
        if (instructorTypeCostDTO == null || instructorTypeCostDTO.getBudget() == null) {
            return null;
        }

        InstructorTypeCost existingInstructorTypeCost = this.findByInstructorTypeIdAndBudgetId(instructorTypeCostDTO.getInstructorType().getId(), instructorTypeCostDTO.getBudget().getId());

        if (existingInstructorTypeCost != null) {
            return existingInstructorTypeCost;
        }

        return this.instructorTypeCostRepository.save(instructorTypeCostDTO);
    }

    @Override
    public List<InstructorTypeCost> findbyWorkgroupIdAndYear(long workgroupId, long year) {
        return this.instructorTypeCostRepository.findbyWorkgroupIdAndYear(workgroupId, year);
    }

    @Override
    public List<InstructorTypeCost> snapshotInstructorTypeCosts(BudgetScenario snapshotBudgetScenario, BudgetScenario originalBudgetScenario) {
        List<InstructorTypeCost> originalInstructorTypeCostList = originalBudgetScenario.getBudget().getInstructorTypeCosts();
        List<InstructorTypeCost> snapshotInstructorTypeCostList = new ArrayList<>();

        for (InstructorTypeCost originalInstructorTypeCost : originalInstructorTypeCostList) {
            if (originalInstructorTypeCost.getBudgetScenario() != null) { continue; }

            InstructorTypeCost instructorTypeCost = new InstructorTypeCost();
            instructorTypeCost.setCost(originalInstructorTypeCost.getCost());
            instructorTypeCost.setInstructorType(originalInstructorTypeCost.getInstructorType());
            instructorTypeCost.setBudgetScenario(snapshotBudgetScenario);

            InstructorTypeCost snapshotInstructorCost = instructorTypeCostRepository.save(instructorTypeCost);

            snapshotInstructorTypeCostList.add(snapshotInstructorCost);
        }

        return snapshotInstructorTypeCostList;
    }

    @Override
    public InstructorTypeCost findByInstructorTypeIdAndBudgetScenarioId(long instructorTypeCostId, long budgetScenarioId) {
        return instructorTypeCostRepository.findByInstructorTypeIdAndBudgetScenarioId(instructorTypeCostId, budgetScenarioId);
    }

    @Override
    public List<InstructorTypeCost> findByBudgetScenarioId(Long budgetScenarioIdentification) {
        return instructorTypeCostRepository.findByBudgetScenarioId(budgetScenarioIdentification);
    }
}
