package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.*;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface InstructorCostService {
    List<InstructorCost> findByBudgetId(Long budgetId);

    InstructorCost findById(Long instructorCostId);

    InstructorCost findOrCreate(InstructorCost instructorCostDto);

    void deleteById(long instructorCostId);

    InstructorCost update(InstructorCost instructorCost);

    List<InstructorCost> findOrCreateManyFromWorkgroupAndYear(Workgroup workgroup, long year);
}
