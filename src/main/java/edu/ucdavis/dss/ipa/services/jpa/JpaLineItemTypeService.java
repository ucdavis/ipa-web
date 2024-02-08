package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.LineItemType;
import edu.ucdavis.dss.ipa.repositories.LineItemTypeRepository;
import edu.ucdavis.dss.ipa.services.LineItemTypeService;
import java.util.List;
import jakarta.inject.Inject;
import org.springframework.stereotype.Service;

@Service
public class JpaLineItemTypeService implements LineItemTypeService {
    @Inject
    LineItemTypeRepository LineItemTypeRepository;

    @Override
    public List<LineItemType> findAll() {
        return (List<LineItemType>) LineItemTypeRepository.findAll();
    }

    @Override
    public LineItemType findById(long lineItemTypeId) {
        return LineItemTypeRepository.findById(lineItemTypeId).orElse(null);
    }
}
