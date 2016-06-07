package edu.ucdavis.dss.ipa.repositories;

import org.springframework.data.repository.CrudRepository;

import edu.ucdavis.dss.ipa.entities.GraduateStudent;

public interface GraduateStudentRepository extends CrudRepository<GraduateStudent, Long> {

	GraduateStudent findById(Long id);

	GraduateStudent findByLoginId(String loginId);
}