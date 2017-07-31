package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.repositories.InstructorCostRepository;
import edu.ucdavis.dss.ipa.services.BudgetService;
import edu.ucdavis.dss.ipa.services.InstructorCostService;
import edu.ucdavis.dss.ipa.services.InstructorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Service
public class JpaInstructorCostService implements InstructorCostService {
    @Inject InstructorCostRepository instructorCostRepository;
    @Inject InstructorService instructorService;
    @Inject BudgetService budgetService;

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

        instructorCost.setBudget(instructorCostDto.getBudget());
        instructorCost.setCost(instructorCostDto.getCost());
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

        originalInstructorCost.setCost(instructorCostDTO.getCost());

        return this.instructorCostRepository.save(originalInstructorCost);
    }

    /**
     * Will find or create instructorCosts for all instructors currently active in the workgroup,
     * and any instructors of record for the schedule in the specified year.
     * @param workgroup
     * @param year
     * @return
     */
    @Override
    public List<InstructorCost> findOrCreateManyFromWorkgroupAndYear(Workgroup workgroup, long year) {
        Budget budget = budgetService.findOrCreateByWorkgroupIdAndYear(workgroup.getId(), year);

        if (budget == null) {
            return null;
        }

        List<InstructorCost> instructorCosts = new ArrayList<>();
        instructorCosts.addAll(this.findOrCreateFromWorkgroup(workgroup, budget));

        return instructorCosts;
    }

    /**
     * Find all instructors based on workgroup userRoles, and find or create instructorCosts for each.
     * @param workgroup
     * @return
     */
    private List<InstructorCost> findOrCreateFromWorkgroup(Workgroup workgroup, Budget budget) {
        List<InstructorCost> instructorCosts = new ArrayList<>();
        List<Long> instructorCostIdsHash = new ArrayList<>();

        // Find existing instructorCosts
        List<InstructorCost> existingInstructorCosts = budget.getInstructorCosts();

        // Only add unique occurrences of the instructorCost
        for (InstructorCost instructorCost : existingInstructorCosts) {
            if (instructorCostIdsHash.indexOf(instructorCost.getId()) == -1) {
                instructorCosts.add(instructorCost);
                instructorCostIdsHash.add(instructorCost.getId());
            }
        }

        // Add instructorCosts from lecturers
        List<Instructor> lecturers = instructorService.findActiveByWorkgroupIdAndLecturer(workgroup.getId(), true);
        List<InstructorCost> lecturerInstructorCosts = this.findOrCreateFromInstructors(lecturers, budget, true);

        for (InstructorCost instructorCost : lecturerInstructorCosts) {
            if (instructorCostIdsHash.indexOf(instructorCost.getId()) == -1) {
                instructorCosts.add(instructorCost);
                instructorCostIdsHash.add(instructorCost.getId());
            }
        }

        // Add non lecturer instructorCosts
        List<Instructor> nonLecturers = instructorService.findActiveByWorkgroupIdAndLecturer(workgroup.getId(), false);
        List<InstructorCost> nonLecturerInstructorCosts = this.findOrCreateFromInstructors(nonLecturers, budget, false);

        for (InstructorCost instructorCost : nonLecturerInstructorCosts) {
            if (instructorCostIdsHash.indexOf(instructorCost.getId()) == -1) {
                instructorCosts.add(instructorCost);
                instructorCostIdsHash.add(instructorCost.getId());
            }
        }

        return instructorCosts;
    }

    /**
     * Finds or creates instructor costs for each instructor
     * @param instructors
     * @return
     */
    private List<InstructorCost> findOrCreateFromInstructors(List<Instructor> instructors, Budget budget, boolean isLecturer) {
        List<InstructorCost> instructorCosts = new ArrayList<>();

        for (Instructor instructor : instructors) {
            InstructorCost instructorCost = new InstructorCost();
            instructorCost.setInstructor(instructor);
            instructorCost.setBudget(budget);
            instructorCost.setLecturer(isLecturer);

            instructorCost = this.findOrCreate(instructorCost);
            instructorCosts.add(instructorCost);
        }

        return instructorCosts;
    }
}
