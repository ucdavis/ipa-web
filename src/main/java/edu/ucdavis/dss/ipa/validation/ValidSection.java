package edu.ucdavis.dss.ipa.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { ValidSectionValidator.class })
@Documented
public @interface ValidSection {

    String message() default "{edu.ucdavis.dss.ipa.web.validation.ValidSequencePattern.message}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}