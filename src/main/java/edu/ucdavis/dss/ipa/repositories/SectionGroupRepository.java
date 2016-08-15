package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.SectionGroup;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SectionGroupRepository extends CrudRepository<SectionGroup, Long> {

	public SectionGroup findById(Long id);

	List<SectionGroup> findByCourseScheduleWorkgroupIdAndCourseScheduleYear(long workgroupId, long year);

    List<SectionGroup> findByCourseScheduleWorkgroupIdAndCourseScheduleYearAndTermCode(long workgroupId, long year, String termCode);
}
