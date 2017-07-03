package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.LineItem;
import edu.ucdavis.dss.ipa.entities.LineItemCategory;
import edu.ucdavis.dss.ipa.repositories.LineItemCategoryRepository;
import edu.ucdavis.dss.ipa.repositories.LineItemRepository;
import edu.ucdavis.dss.ipa.services.LineItemCategoryService;
import edu.ucdavis.dss.ipa.services.LineItemService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

@Service
public class JpaLineItemCategoryService implements LineItemCategoryService {
    @Inject LineItemCategoryRepository lineItemCategoryRepository;

    @Override
    public List<LineItemCategory> findAll() {
        return (List<LineItemCategory>) lineItemCategoryRepository.findAll();
    }
}
