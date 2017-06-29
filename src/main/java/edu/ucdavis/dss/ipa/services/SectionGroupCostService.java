package edu.ucdavis.dss.ipa.services;

import java.util.List;

import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.SectionGroupCost;
import org.springframework.validation.annotation.Validated;

import edu.ucdavis.dss.ipa.entities.Section;
import edu.ucdavis.dss.ipa.entities.SectionGroup;

@Validated
public interface SectionGroupCostService {
    List<SectionGroupCost> findByBudgetId(Long budgetId);
}