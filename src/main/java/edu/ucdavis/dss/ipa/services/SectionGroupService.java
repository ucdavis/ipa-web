package edu.ucdavis.dss.ipa.services;

import java.util.List;

import org.springframework.validation.annotation.Validated;

import edu.ucdavis.dss.ipa.entities.Section;
import edu.ucdavis.dss.ipa.entities.SectionGroup;

@Validated
public interface SectionGroupService {

	SectionGroup getOneById(Long id);

	SectionGroup save(SectionGroup sectionGroup);

	void delete(Long id);

	List<SectionGroup> findByScheduleIdAndTermCode(long scheduleId, String termCode);

	Section addSection(Long sectionGroupId, Section section);

	List<SectionGroup> findByWorkgroupIdAndYear(long workgroupId, long year);

    List<SectionGroup> findVisibleByWorkgroupIdAndYearAndTermCode(long workgroupId, long year, String termCode);
}