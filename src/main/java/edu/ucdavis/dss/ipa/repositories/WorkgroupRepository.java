package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.Term;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import edu.ucdavis.dss.ipa.entities.Workgroup;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface WorkgroupRepository extends CrudRepository<Workgroup, Long> {
	Workgroup findOneByCode(String code);
	
	Workgroup findOneById(long workgroupId);

	@Query("SELECT id from Workgroup")
    List<Long> findAllIds();
}
