package edu.ucdavis.dss.ipa.repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import edu.ucdavis.dss.ipa.entities.UserRole;

public interface UserRoleRepository extends CrudRepository<UserRole, Long> {

	List<UserRole> findByWorkgroupId(long id);

}
