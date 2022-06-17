package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.Course;
import java.util.List;
import java.util.Map;

public interface DataWarehouseService {
    Map<String, Map<String, Long>> generateCourseCensusMap(List<Course> courses);
}
