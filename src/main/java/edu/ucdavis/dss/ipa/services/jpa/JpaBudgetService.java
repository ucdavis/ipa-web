package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.Budget;
import edu.ucdavis.dss.ipa.repositories.BudgetRepository;
import edu.ucdavis.dss.ipa.services.BudgetService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class JpaBudgetService implements BudgetService {
    @Inject BudgetRepository budgetRepository;

    @Override
    public Budget findOrCreateByScheduleId(Long scheduleId) {
        Budget budget = budgetRepository.findByScheduleId(scheduleId);

        if (budget != null) {
            return budget;
        }

        return this.createBudgetByScheduleId(scheduleId);
    }

    private Budget createBudgetByScheduleId(Long scheduleId) {
        return null;
    }
}
