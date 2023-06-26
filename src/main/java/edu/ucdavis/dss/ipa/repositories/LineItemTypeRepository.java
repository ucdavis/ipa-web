package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.LineItemType;
import org.springframework.data.repository.CrudRepository;

public interface LineItemTypeRepository extends CrudRepository<LineItemType, Long> {
    LineItemType findById(long LineItemTypeId);
}
