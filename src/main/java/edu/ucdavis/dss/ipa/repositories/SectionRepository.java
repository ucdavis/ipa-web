package edu.ucdavis.dss.ipa.repositories;

import org.springframework.data.repository.CrudRepository;

import edu.ucdavis.dss.ipa.entities.Section;

public interface SectionRepository extends CrudRepository<Section, Long> {

	Section findById(Long id);

	Section findByCrnAndSectionGroupCourseOfferingTermCode(String crn, String termCode);

}