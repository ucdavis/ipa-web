package edu.ucdavis.dss.ipa.entities.validation;

import edu.ucdavis.dss.ipa.entities.Activity;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

class ValidActivityValidator implements ConstraintValidator<ValidActivity, Activity> {

	@Override
	public void initialize(ValidActivity constraintAnnotation) {

	}

	@Override
	public boolean isValid(Activity activity, ConstraintValidatorContext context) {
		if (activity.getStartTime() == null) {
			return false;
		}
		return true;
	}
}
