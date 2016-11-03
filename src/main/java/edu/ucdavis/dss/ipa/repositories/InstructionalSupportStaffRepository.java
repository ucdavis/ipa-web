package edu.ucdavis.dss.ipa.repositories;

import org.springframework.data.repository.CrudRepository;

import edu.ucdavis.dss.ipa.entities.InstructionalSupportStaff;

public interface InstructionalSupportStaffRepository extends CrudRepository<InstructionalSupportStaff, Long> {

    InstructionalSupportStaff findById(Long id);

    InstructionalSupportStaff findByLoginIdIgnoreCase(String loginId);
}