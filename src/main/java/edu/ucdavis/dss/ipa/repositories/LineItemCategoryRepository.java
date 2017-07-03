package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.LineItem;
import edu.ucdavis.dss.ipa.entities.LineItemCategory;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LineItemCategoryRepository extends CrudRepository<LineItemCategory, Long> {

}
