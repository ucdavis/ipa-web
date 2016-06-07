package edu.ucdavis.dss.ipa.services;

import java.util.List;

import javax.validation.Valid;

import org.springframework.validation.annotation.Validated;

import edu.ucdavis.dss.ipa.entities.CensusSnapshot;
import edu.ucdavis.dss.ipa.entities.Section;

@Validated
public interface SectionService {

	Section saveSection(@Valid Section section);

	Section getSectionById(Long id);

	boolean deleteSectionById(Long id);

	void saveCensusSnapshot(CensusSnapshot censusSnapshot);

	Section updateSection(Section section);

	Section addSectionToCourseOfferingGroup(Long cogId, String termCode, @Valid Section section);

	boolean deleteSectionsBySequence(Long courseOfferingGroupId, String sequence);

	boolean updateSectionSequences(Long courseOfferingGroupId, String oldSequence, String newSequence);

	Section getSectionByCrnAndTerm(String crn, String termCode);

	CensusSnapshot getCensusSnapshotBySectionIdAndSnapshotCode(long id, String snapshotCode);

	List<CensusSnapshot> getCensusSnapshotsBySectionId(long sectionId);

}