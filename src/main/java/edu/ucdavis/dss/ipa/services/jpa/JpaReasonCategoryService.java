package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.ReasonCategory;
import edu.ucdavis.dss.ipa.repositories.ReasonCategoryRepository;
import edu.ucdavis.dss.ipa.services.ReasonCategoryService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

@Service
public class JpaReasonCategoryService implements ReasonCategoryService {
    @Inject ReasonCategoryRepository reasonCategoryRepository;

    @Override
    public List<ReasonCategory> findAll() {
        return (List<ReasonCategory>) reasonCategoryRepository.findAll();
    }
    
}
