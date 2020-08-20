package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.SectionGroupCostInstructor;
import org.springframework.validation.annotation.Validated;

@Validated
public interface SectionGroupCostInstructorService {
    SectionGroupCostInstructor create(SectionGroupCostInstructor sectionGroupCostInstructorDTO);

    SectionGroupCostInstructor findById(long sectionGroupCostInstructorId);

    SectionGroupCostInstructor update(SectionGroupCostInstructor sectionGroupCostInstructorDTO);
}