package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.WorkgroupCourse;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface WorkgroupCourseService {
  List<WorkgroupCourse> getAllWorkgroupCourses();
}
