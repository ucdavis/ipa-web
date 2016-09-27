package edu.ucdavis.dss.ipa.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import edu.ucdavis.dss.ipa.entities.Workgroup;

import java.util.List;

public interface WorkgroupRepository extends CrudRepository<Workgroup, Long> {
	Workgroup findOneByCode(String code);
	
	Workgroup findOneById(long workgroupId);

	@Query("SELECT id from Workgroup")
    List<Long> findAllIds();
}
