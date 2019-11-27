package edu.ucdavis.dss.ipa.services.jpa;

// import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

// import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.repositories.WorkgroupCourseRepository;
import edu.ucdavis.dss.ipa.services.*;
// import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import edu.ucdavis.dss.ipa.entities.WorkgroupCourse;
// import org.springframework.transaction.annotation.Transactional;

@Service
public class JpaWorkgroupCourseService implements WorkgroupCourseService {
	@Inject WorkgroupCourseRepository workgroupCourseRepository;


	@Override
	public List<WorkgroupCourse> getAllWorkgroupCourses() {
		return (List<WorkgroupCourse>) this.workgroupCourseRepository.findAll();
	}

}
