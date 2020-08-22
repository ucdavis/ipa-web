package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.BudgetScenario;
import edu.ucdavis.dss.ipa.entities.InstructorTypeCost;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface InstructorTypeCostService {
    List<InstructorTypeCost> findByBudgetId(Long budgetId);

    InstructorTypeCost findById(Long instructorTypeId);

    void deleteById(long instructorTypeId);

    InstructorTypeCost update(InstructorTypeCost instructorTypeCost);

    InstructorTypeCost findOrCreate(InstructorTypeCost instructorTypeCostDTO);

    List<InstructorTypeCost> findbyWorkgroupIdAndYear(long workgroupId, long year);

    InstructorTypeCost findByInstructorTypeIdAndBudgetId(long instructorTypeId, long budgetId);

    List<InstructorTypeCost> copyInstructorTypeCosts(BudgetScenario snapshotScenario, BudgetScenario originalScenario);

    InstructorTypeCost findByInstructorTypeIdAndBudgetScenarioId(long instructorTypeId, long budgetScenarioId);

    List<InstructorTypeCost> findByBudgetScenarioId(Long budgetScenarioIdentification);
}
