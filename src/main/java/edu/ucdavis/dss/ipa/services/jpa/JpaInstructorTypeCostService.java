package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.InstructorTypeCost;
import edu.ucdavis.dss.ipa.repositories.InstructorTypeCostRepository;
import edu.ucdavis.dss.ipa.services.InstructorCostService;
import edu.ucdavis.dss.ipa.services.InstructorTypeCostService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;

@Service
public class JpaInstructorTypeCostService implements InstructorTypeCostService {
    @Inject
    InstructorTypeCostRepository instructorTypeCostRepository;
    @Inject InstructorCostService instructorCostService;

    @Override
    public List<InstructorTypeCost> findByBudgetId(Long budgetId) {
        return instructorTypeCostRepository.findByBudgetId(budgetId);
    }

    @Override
    public InstructorTypeCost findById(Long instructorTypeId) {
        return instructorTypeCostRepository.findById(instructorTypeId);
    }

    @Override
    @Transactional
    public void deleteById(long instructorTypeId) {
        InstructorTypeCost instructorTypeCost = this.findById(instructorTypeId);

        instructorCostService.removeAssociationByInstructorTypeId(instructorTypeId);

        instructorTypeCostRepository.deleteById(instructorTypeId);
    }

    @Override
    public InstructorTypeCost update(InstructorTypeCost newInstructorType) {
        InstructorTypeCost originalInstructorType = this.findById(newInstructorType.getId());

        if(originalInstructorType == null) {
            return null;
        }

        originalInstructorType.setCost(newInstructorType.getCost());

        return this.instructorTypeCostRepository.save(originalInstructorType);
    }

    @Override
    public InstructorTypeCost findOrCreate(InstructorTypeCost instructorTypeCostDTO) {
        if (instructorTypeCostDTO == null || instructorTypeCostDTO.getBudget() == null) {
            return null;
        }

        InstructorTypeCost existingInstructorTypeCost = this.instructorTypeCostRepository.findByInstructorTypeIdAndBudgetId(instructorTypeCostDTO.getInstructorType().getId(), instructorTypeCostDTO.getBudget().getId());

        if (existingInstructorTypeCost != null) {
            return existingInstructorTypeCost;
        }

        InstructorTypeCost instructorTypeCost = new InstructorTypeCost();

        instructorTypeCost.setBudget(instructorTypeCostDTO.getBudget());
        instructorTypeCost.setCost(instructorTypeCostDTO.getCost());

        return this.instructorTypeCostRepository.save(instructorTypeCost);
    }
}
