package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.SectionGroup;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SectionGroupRepository extends CrudRepository<SectionGroup, Long> {

	public SectionGroup findById(Long id);
}
