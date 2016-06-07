package edu.ucdavis.dss.ipa.repositories;

import org.springframework.data.repository.CrudRepository;

import edu.ucdavis.dss.ipa.entities.Workgroup;

public interface WorkgroupRepository extends CrudRepository<Workgroup, Long> {
	Workgroup findOneByCode(String code);
	
	Workgroup findOneById(long workgroupId);
}
