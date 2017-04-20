package edu.ucdavis.dss.ipa.services;

import java.util.List;

import javax.validation.Valid;

import org.springframework.validation.annotation.Validated;

import edu.ucdavis.dss.ipa.entities.Section;

@Validated
public interface SectionService {

	Section save(@Valid Section section);

	Section getOneById(Long id);

	boolean delete(Long id);

	Section updateSequenceNumber(Long sectionid, String newSequence);

	List<Section> findVisibleByWorkgroupIdAndYearAndTermCode(long workgroupId, long year, String termCode);

	Section findOrCreateBySectionGroupIdAndSequenceNumber(long id, String sequenceNumber);
}