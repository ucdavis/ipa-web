package edu.ucdavis.dss.ipa.repositories;

import org.springframework.data.repository.CrudRepository;

import edu.ucdavis.dss.ipa.entities.Role;

public interface RoleRepository extends CrudRepository<Role, Long> {

	Role findOneByName(String name);

}
