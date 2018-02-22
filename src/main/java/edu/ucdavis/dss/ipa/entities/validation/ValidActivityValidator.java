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

		// An activity can have a section or a sectionGroup, but not both
		if (sectionGroupIsNull == sectionIsNull) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(
					"only section or sectionGroup may be set"
			)
					.addConstraintViolation();
			return false;
		}

		// If start and end times are both set, they cannot be the same value
		if (activity.getStartTime() != null && activity.getEndTime() != null && activity.getStartTime().equals(activity.getEndTime())) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(
					"start time and end time cannot be equal if set"
			)
					.addConstraintViolation();
			return false;
		}

		return true;
	}
}
