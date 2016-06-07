package edu.ucdavis.dss.ipa.services;

import java.util.List;

import org.springframework.validation.annotation.Validated;

import edu.ucdavis.dss.dw.dto.DwSectionGroup;
import edu.ucdavis.dss.ipa.entities.Section;
import edu.ucdavis.dss.ipa.entities.SectionGroup;

@Validated
public interface SectionGroupService {

	SectionGroup getSectionGroupById(Long id);

	List<SectionGroup> getSectionGroups();

	SectionGroup saveSectionGroup(SectionGroup sectionGroup);

	void deleteSectionGroupById(Long id);

	SectionGroup createSectionGroup(SectionGroup co);

	SectionGroup createSectionGroup(SectionGroup co, boolean initSection);

	Section addAutoIncrementSection(long sectionGroupId);

	String getSectionGroupSequence(long sectionGroupId);

	List<SectionGroup> getSectionGroupsByScheduleIdAndTermCode(long scheduleId, String termCode);

	List<DwSectionGroup> getSectionGroupsByCourseId(Long courseId, String termCode);

	SectionGroup findOneById(Long courseId);
	
	List<SectionGroup> getSectionGroupsByScheduleIdAndTermCodeAndInstructorId(long scheduleId, String termCode, Long instructorId);

	List<SectionGroup> getSectionGroupsByCourseOfferingId(long courseOfferingId);

	List<SectionGroup> findAllEager();
}