package edu.ucdavis.dss.ipa.services;

import java.util.List;

import edu.ucdavis.dss.ipa.entities.Course;
import org.springframework.validation.annotation.Validated;

import edu.ucdavis.dss.ipa.entities.Section;
import edu.ucdavis.dss.ipa.entities.SectionGroup;

@Validated
public interface SectionGroupService {
	SectionGroup getOneById(Long id);

	SectionGroup save(SectionGroup sectionGroup);

	void delete(Long id);

	List<SectionGroup> findEmpty();

	List<SectionGroup> findByScheduleIdAndTermCode(long scheduleId, String termCode);

	List<SectionGroup> findByWorkgroupIdAndYear(long workgroupId, long year);

	List<SectionGroup> findByWorkgroupIdAndYearAndTermCode(long workgroupId, long year, String termCode);

    List<SectionGroup> findVisibleByWorkgroupIdAndYearAndTermCode(long workgroupId, long year, String termCode);

	List<SectionGroup> findByCourses(List<Course> courses);

	List<SectionGroup> findByScheduleIdAndTermCodeAndStudentSupportCallResponseId(long scheduleId, String termCode, long studentSupportCallResponseId);

	SectionGroup findOrCreateByCourseIdAndTermCode(Long courseId, String termCode);

	SectionGroup identifyAndCondenseSharedActivities(SectionGroup sectionGroup);

	List<SectionGroup> findByScheduleId(long scheduleId);
}
