package edu.ucdavis.dss.ipa.repositories;

import org.springframework.data.repository.CrudRepository;

import edu.ucdavis.dss.ipa.entities.Instructor;

public interface InstructorRepository extends CrudRepository<Instructor, Long> {
	Instructor findByUcdStudentSID(String ucdStudentSID);

	Instructor findByLoginIdIgnoreCase(String loginId);

}