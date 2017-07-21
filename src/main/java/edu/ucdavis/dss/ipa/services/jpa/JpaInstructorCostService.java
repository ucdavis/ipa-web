package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.InstructorCost;
import edu.ucdavis.dss.ipa.repositories.InstructorCostRepository;
import edu.ucdavis.dss.ipa.services.InstructorCostService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;

@Service
public class JpaInstructorCostService implements InstructorCostService {
    @Inject InstructorCostRepository instructorCostRepository;

    @Override
    public List<InstructorCost> findByBudgetId(Long budgetId) {
        return instructorCostRepository.findByBudgetId(budgetId);
    }

    @Override
    public InstructorCost findById(Long lineItemId) {
        return instructorCostRepository.findById(lineItemId);
    }

    @Override
    public InstructorCost findOrCreate(InstructorCost instructorCostDto) {
        InstructorCost instructorCost = this.instructorCostRepository.findByInstructorIdAndBudgetId(instructorCostDto.getInstructor().getId(), instructorCostDto.getBudget().getId());

        if (instructorCost != null) {
            return instructorCost;
        }

        instructorCost = new InstructorCost();

        instructorCost.setBudget(instructorCost.getBudget());
        instructorCost.setCost(instructorCost.getCost());
        instructorCost.setInstructor(instructorCostDto.getInstructor());
        instructorCost.setLecturer(instructorCostDto.getLecturer());

        return this.instructorCostRepository.save(instructorCost);
    }

    @Override
    @Transactional
    public void deleteById(long instructorCostId) {
        instructorCostRepository.deleteById(instructorCostId);
    }

    @Override
    public InstructorCost update(InstructorCost instructorCostDTO) {
        InstructorCost originalInstructorCost = this.findById(instructorCostDTO.getId());

        if(originalInstructorCost == null) {
            return null;
        }

        originalInstructorCost.setBudget(instructorCostDTO.getBudget());
        originalInstructorCost.setCost(instructorCostDTO.getCost());
        originalInstructorCost.setInstructor(instructorCostDTO.getInstructor());
        originalInstructorCost.setLecturer(instructorCostDTO.getLecturer());

        return this.instructorCostRepository.save(originalInstructorCost);
    }
}
