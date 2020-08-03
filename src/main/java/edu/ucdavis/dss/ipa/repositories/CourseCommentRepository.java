package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.CourseComment;
import org.springframework.data.repository.CrudRepository;

public interface CourseCommentRepository extends CrudRepository<CourseComment, Long> {

}