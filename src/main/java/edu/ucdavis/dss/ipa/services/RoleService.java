package edu.ucdavis.dss.ipa.services;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

import edu.ucdavis.dss.ipa.entities.Role;

@Validated
public interface RoleService {

	void saveRole(@NotNull @Valid Role role);

	Role findOneById(Long id);
	
	Role findOneByName(String name);

	List<Role> getAllRoles();
}
