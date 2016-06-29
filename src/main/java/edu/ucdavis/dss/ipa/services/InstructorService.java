package edu.ucdavis.dss.ipa.services;

import org.springframework.validation.annotation.Validated;

import edu.ucdavis.dss.ipa.entities.Instructor;

import java.util.List;

@Validated
public interface InstructorService {

	Instructor save(Instructor instructor);

	Instructor getOneById(Long instructorId);

	Instructor getOneByUcdStudentSID(String ucdStudentSID);

	/**
	 * Finds instructor by case-insensitive 'loginId', if exists.
	 * 
	 * @param loginId
	 * @return
	 */
	Instructor getOneByLoginId(String loginId);

	Instructor findOrCreate(String firstName, String lastName, String email, String loginId, Long workgroupId);

	Instructor findOrCreate(String firstName, String lastName, String email, String loginId, Long workgroupId, String employeeId);

	void removeOrphanedByLoginId(String loginId);

}