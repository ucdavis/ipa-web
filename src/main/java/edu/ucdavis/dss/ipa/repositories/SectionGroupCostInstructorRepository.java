package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.SectionGroupCostInstructor;
import org.springframework.data.repository.CrudRepository;

public interface SectionGroupCostInstructorRepository extends CrudRepository<SectionGroupCostInstructor, Long> {
    SectionGroupCostInstructor findById(long sectionGroupCostInstructorId);
}