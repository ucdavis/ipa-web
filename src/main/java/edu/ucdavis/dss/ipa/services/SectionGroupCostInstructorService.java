package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.SectionGroupCost;
import edu.ucdavis.dss.ipa.entities.SectionGroupCostInstructor;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface SectionGroupCostInstructorService {
    SectionGroupCostInstructor create(SectionGroupCostInstructor sectionGroupCostInstructorDTO);

    SectionGroupCostInstructor findById(long sectionGroupCostInstructorId);

    SectionGroupCostInstructor update(SectionGroupCostInstructor sectionGroupCostInstructorDTO);

    void delete(long sectionGroupCostInstructorId);

    List<SectionGroupCostInstructor> copyInstructors(long workgroupId, SectionGroupCost originalSectionGroupCost, SectionGroupCost newSectionGroupCost);


    List<SectionGroupCostInstructor> findBySectionGroupCosts(List<SectionGroupCost> sectionGroupCosts);

    List<SectionGroupCostInstructor> findByBudgetId(long budgetId);

    List<SectionGroupCostInstructor> findByBudgetScenarioId(long budgetScenarioId);

    List<SectionGroupCostInstructor> findbyWorkgroupIdAndYear(long workgroupId, long year);
}