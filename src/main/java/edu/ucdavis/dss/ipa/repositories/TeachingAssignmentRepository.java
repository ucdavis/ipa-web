package edu.ucdavis.dss.ipa.repositories;

import org.springframework.data.repository.CrudRepository;

import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.entities.TeachingAssignment;

public interface TeachingAssignmentRepository extends CrudRepository<TeachingAssignment, Long> {

	TeachingAssignment findById(Long id);

	TeachingAssignment findOneBySectionGroupAndInstructor(SectionGroup sectionGroup, Instructor instructor);
}