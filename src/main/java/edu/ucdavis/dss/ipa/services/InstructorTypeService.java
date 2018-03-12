package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.InstructorType;
import edu.ucdavis.dss.ipa.entities.InstructorTypeCost;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface InstructorTypeService {
    InstructorType findById(Long instructorTypeId);

    List<InstructorType> getAllInstructorTypes();
}
