package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.Budget;
import edu.ucdavis.dss.ipa.entities.InstructorCost;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.repositories.BudgetRepository;
import edu.ucdavis.dss.ipa.services.BudgetService;
import edu.ucdavis.dss.ipa.services.InstructorCostService;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;

@Service
public class JpaBudgetService implements BudgetService {
    @Inject BudgetRepository budgetRepository;
    @Inject ScheduleService scheduleService;
    @Inject InstructorCostService instructorCostService;

    /**
     * Helper method to handle schedule checking. Intentionally private.
     * @param schedule
     * @return
     */
    private Budget findOrCreateBySchedule(Schedule schedule) {
        Budget budget = budgetRepository.findByScheduleId(schedule.getId());

        if (budget != null) {
            return budget;
        }

        return this.createBudgetBySchedule(schedule);
    }

    /**
     * If budget does not exist, this method will ensure schedule exists.
     * @param workgroupId
     * @param year
     * @return
     */
    @Override
    public Budget findOrCreateByWorkgroupIdAndYear(long workgroupId, long year) {
        Schedule schedule = scheduleService.findOrCreateByWorkgroupIdAndYear(workgroupId, year);
        Budget budget = this.findOrCreateBySchedule(schedule);

        return budget;
    }

    @Override
    public Budget findById(long budgetId) {
        return budgetRepository.findById(budgetId);
    }

    @Override
    public Budget update(Budget budgetDTO) {
        Budget originalBudget = this.findById(budgetDTO.getId());

        if(originalBudget == null) {
            return null;
        }

        originalBudget.setTaCost(budgetDTO.getTaCost());
        originalBudget.setReaderCost(budgetDTO.getReaderCost());
        originalBudget.setLecturerCost(budgetDTO.getLecturerCost());

        return this.budgetRepository.save(originalBudget);
    }

    @Transactional
    private Budget createBudgetBySchedule(Schedule schedule) {
        Budget budget = new Budget();
        budget.setSchedule(schedule);
        budget = budgetRepository.save(budget);

        instructorCostService.findOrCreateManyFromBudget(budget);

        return budget;
    }
}
