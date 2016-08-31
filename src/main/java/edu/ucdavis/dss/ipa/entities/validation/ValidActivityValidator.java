package edu.ucdavis.dss.ipa.entities.validation;

import edu.ucdavis.dss.ipa.entities.Activity;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidActivityValidator implements ConstraintValidator<ValidActivity, Activity> {

	@Override
	public void initialize(ValidActivity constraintAnnotation) {
	}

	@Override
	public boolean isValid(Activity activity, ConstraintValidatorContext context) {
		boolean sectionIsNull = (activity.getSection() == null);
		boolean sectionGroupIsNull = (activity.getSectionGroup() == null);

		if (sectionGroupIsNull == sectionIsNull) {
			return false;
		}

		return true;
	}
}
