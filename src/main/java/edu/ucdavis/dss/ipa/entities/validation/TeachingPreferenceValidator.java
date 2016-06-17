package edu.ucdavis.dss.ipa.entities.validation;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class TeachingPreferenceValidator implements Validator {
	@SuppressWarnings("rawtypes")

	public boolean supports(Class clazz) {
		return TeachingPreference.class.equals(clazz);
	}

	public void validate(Object target, Errors errors) {
		TeachingPreference teachingPreference = (TeachingPreference) target;
		int fieldsSetCount = 0;

		// Ensure one of the following fields have a value
		if (teachingPreference.getCourse() != null) {
			fieldsSetCount++;
		}
		if (teachingPreference.getCourseOffering() != null && teachingPreference.getCourseOffering().getId() > 0) {
			fieldsSetCount++;
		}
		if (teachingPreference.getIsBuyout() != false) {
			fieldsSetCount++;
		}
		if (teachingPreference.getIsSabbatical() != false) {
			fieldsSetCount++;
		}
		if (teachingPreference.getIsCourseRelease() != false) {
			fieldsSetCount++;
		}

		if (fieldsSetCount > 1) {
			errors.rejectValue("course", "more than one of the following have been set: course, courseOffering, sabbatical, buyout or courseRelease");
		}
		if (fieldsSetCount < 1) {
			errors.rejectValue("course", "at least one of the following must be set: course, courseOffering, sabbatical, buyout or courseRelease");
		}
	}
}