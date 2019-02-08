package edu.ucdavis.dss.ipa.entities.validation;

import edu.ucdavis.dss.ipa.entities.SectionGroupCost;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidSectionGroupCostValidator implements ConstraintValidator<ValidSectionGroupCost, SectionGroupCost> {

    @Override
    public void initialize(ValidSectionGroupCost constraintAnnotation) {}

    @Override
    public boolean isValid(SectionGroupCost sectionGroupCost, ConstraintValidatorContext context) {

        if (sectionGroupCost.getInstructor() != null && sectionGroupCost.getInstructorType() == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("SectionGroupCost save failed: If Instructor is set, InstructorType must also be set.").addConstraintViolation();

            return false; }

        return true;
    }
}
