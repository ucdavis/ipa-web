package edu.ucdavis.dss.ipa.services;

import org.springframework.validation.annotation.Validated;

import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.Workgroup;

@Validated
public interface InstructorWorkgroupRelationshipService {

	InstructorWorkgroupRelationship saveInstructorWorkgroupRelationship(InstructorWorkgroupRelationship idr);

	boolean deleteById(Long id);

	InstructorWorkgroupRelationship findById(Long id);

	InstructorWorkgroupRelationship findOrCreateOneByWorkgroupAndInstructor(Workgroup workgroup,
			Instructor instructor);

	InstructorWorkgroupRelationship createOneByWorkgroupAndInstructor(Workgroup workgroup, Instructor instructor);
	
	boolean existsByWorkgroupIdAndInstructorId(long workgroupId, long instructorId);
}
