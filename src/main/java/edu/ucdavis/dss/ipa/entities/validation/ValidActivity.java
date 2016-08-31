package edu.ucdavis.dss.ipa.entities.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { ValidActivityValidator.class })
@Documented
public @interface ValidActivity {

    String message() default "Invalid Activity";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}