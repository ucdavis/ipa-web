package edu.ucdavis.dss.ipa.services.jpa;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import edu.ucdavis.dss.ipa.entities.Role;
import edu.ucdavis.dss.ipa.services.RoleService;
import edu.ucdavis.dss.ipa.repositories.RoleRepository;

@Service
public class JpaRoleService implements RoleService {
	@Inject RoleRepository roleRepository;
	
	@Override
	@Transactional
	public void saveRole(Role role)
	{
		this.roleRepository.save(role);
	}

	@Override
	public Role findOneById(Long id) {
		return this.roleRepository.findById(id).orElse(null);
	}

	@Override
	public List<Role> getAllRoles() {
		return (List<Role>) this.roleRepository.findAll();
	}

	@Override
	public Role findOneByName(String name) {
		return this.roleRepository.findOneByName(name);
	}

}
