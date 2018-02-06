package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.InstructorCost;
import edu.ucdavis.dss.ipa.entities.InstructorType;
import edu.ucdavis.dss.ipa.repositories.InstructorTypeRepository;
import edu.ucdavis.dss.ipa.services.InstructorCostService;
import edu.ucdavis.dss.ipa.services.InstructorTypeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;

@Service
public class JpaInstructorTypeService implements InstructorTypeService {
    @Inject InstructorTypeRepository instructorTypeRepository;
    @Inject InstructorCostService instructorCostService;

    @Override
    public List<InstructorType> findByBudgetId(Long budgetId) {
        return instructorTypeRepository.findByBudgetId(budgetId);
    }

    @Override
    public InstructorType findById(Long instructorTypeId) {
        return instructorTypeRepository.findById(instructorTypeId);
    }

    @Override
    @Transactional
    public void deleteById(long instructorTypeId) {
        InstructorType instructorType = this.findById(instructorTypeId);

        for (InstructorCost instructorCost : instructorType.getInstructorCosts()) {
            instructorCost.setInstructorType(null);
            instructorCostService.update(instructorCost);
        }

        instructorTypeRepository.deleteById(instructorTypeId);
    }

    @Override
    public InstructorType update(InstructorType newInstructorType) {
        InstructorType originalInstructorType = this.findById(newInstructorType.getId());

        if(originalInstructorType == null) {
            return null;
        }

        originalInstructorType.setCost(newInstructorType.getCost());
        originalInstructorType.setDescription(newInstructorType.getDescription());

        return this.instructorTypeRepository.save(originalInstructorType);
    }

    @Override
    public InstructorType findOrCreate(InstructorType instructorTypeDTO) {
        InstructorType existingInstructorType = this.instructorTypeRepository.findByDescriptionAndBudgetId(instructorTypeDTO.getDescription(), instructorTypeDTO.getBudget().getId());

        if (existingInstructorType != null) {
            return existingInstructorType;
        }

        InstructorType instructorType = new InstructorType();

        instructorType.setBudget(instructorTypeDTO.getBudget());
        instructorType.setDescription(instructorTypeDTO.getDescription());
        instructorType.setCost(instructorTypeDTO.getCost());

        return this.instructorTypeRepository.save(instructorType);
    }
}
