package edu.ucdavis.dss.ipa.entities.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NotBlankValidator implements ConstraintValidator<NotBlank, CharSequence> {
	@Override
	public void initialize(NotBlank annotation)
	{

	}

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext context)
	{
		if(value instanceof String)
			return ((String) value).trim().length() > 0;
			return value.toString().trim().length() > 0;
	}
}
