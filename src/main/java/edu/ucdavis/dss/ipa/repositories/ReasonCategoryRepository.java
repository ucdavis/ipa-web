package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.ReasonCategory;
import org.springframework.data.repository.CrudRepository;

public interface ReasonCategoryRepository extends CrudRepository<ReasonCategory, Long> {
}