package edu.ucdavis.dss.ipa.entities.validation;

import edu.ucdavis.dss.ipa.entities.Course;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Created by okadri on 8/5/16.
 */
@Component
public class CourseValidator implements Validator {

    @SuppressWarnings("rawtypes")
    public boolean supports(Class clazz) {
        return Course.class.equals(clazz);
    }

    public void validate(Object target, Errors errors) {
        Course course = (Course)target;

//        if(course.getTitle() != null) {
//            errors.rejectValue("title", "The course has a title");
//        }

    }
}
