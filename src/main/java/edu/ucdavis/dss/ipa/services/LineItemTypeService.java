package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.LineItemType;
import java.util.List;
import org.springframework.validation.annotation.Validated;

@Validated
public interface LineItemTypeService {
    List<LineItemType> findAll();

    LineItemType findById(long LineItemTypeId);
}
