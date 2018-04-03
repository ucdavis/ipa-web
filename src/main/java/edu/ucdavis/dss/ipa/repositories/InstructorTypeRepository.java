package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.InstructorType;
import org.springframework.data.repository.CrudRepository;

public interface InstructorTypeRepository extends CrudRepository<InstructorType, Long> {
    InstructorType findById(Long instructorTypeId);
}
