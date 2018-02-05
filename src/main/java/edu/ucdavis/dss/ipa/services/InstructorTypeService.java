package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.InstructorType;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface InstructorTypeService {
    List<InstructorType> findByBudgetId(Long budgetId);

    InstructorType findById(Long instructorTypeId);

    void deleteById(long instructorTypeId);

    InstructorType update(InstructorType instructorType);

    InstructorType findOrCreate(InstructorType instructorTypeDTO);
}
