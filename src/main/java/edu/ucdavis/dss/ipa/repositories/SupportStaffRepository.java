package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.SupportStaff;
import org.springframework.data.repository.CrudRepository;

public interface SupportStaffRepository extends CrudRepository<SupportStaff, Long> {

    SupportStaff findById(Long id);

    SupportStaff findByLoginIdIgnoreCase(String loginId);
}