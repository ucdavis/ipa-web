package edu.ucdavis.dss.ipa.services;

import org.springframework.validation.annotation.Validated;

import edu.ucdavis.dss.ipa.entities.Instructor;

@Validated
public interface InstructorService {

	Instructor saveInstructor(Instructor instructor);

	Instructor getInstructorById(Long instructorId);

	Instructor getInstructorByEmployeeId(String employeeId);

	/**
	 * Finds instructor by case-insensitive 'loginId', if exists.
	 * 
	 * @param loginId
	 * @return
	 */
	Instructor getInstructorByLoginId(String loginId);

	Instructor findOrCreateInstructor(String firstName, String lastName, String email, String loginId, Long workgroupId);

	Instructor findOrCreateInstructor(String firstName, String lastName, String email, String loginId, Long workgroupId, String employeeId);

	Instructor addInstructorWorkgroupRelationship(Long instructorId, Long workgroupId);

	void removeOrphanedInstructorByLoginId(String loginId);
}