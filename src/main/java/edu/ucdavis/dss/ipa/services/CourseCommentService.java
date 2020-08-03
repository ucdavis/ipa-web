package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.CourseComment;
import org.springframework.validation.annotation.Validated;

@Validated
public interface CourseCommentService {
    CourseComment create(CourseComment courseCommentDTO);
}