package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.LineItemCategory;
import edu.ucdavis.dss.ipa.repositories.LineItemCategoryRepository;
import edu.ucdavis.dss.ipa.services.LineItemCategoryService;
import org.springframework.stereotype.Service;

import jakarta.inject.Inject;
import java.util.List;

@Service
public class JpaLineItemCategoryService implements LineItemCategoryService {
    @Inject LineItemCategoryRepository lineItemCategoryRepository;

    @Override
    public List<LineItemCategory> findAll() {
        return (List<LineItemCategory>) lineItemCategoryRepository.findAll();
    }

    @Override
    public LineItemCategory findById(long lineItemCategoryId) {
        return lineItemCategoryRepository.findById(lineItemCategoryId).orElse(null);
    }

    @Override
    public LineItemCategory findByDescription(String description) {
        return lineItemCategoryRepository.findByDescription(description);
    }
}
