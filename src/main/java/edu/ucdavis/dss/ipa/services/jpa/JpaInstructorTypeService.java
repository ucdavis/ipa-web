package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.InstructorType;
import edu.ucdavis.dss.ipa.entities.InstructorTypeCost;
import edu.ucdavis.dss.ipa.repositories.InstructorTypeCostRepository;
import edu.ucdavis.dss.ipa.repositories.InstructorTypeRepository;
import edu.ucdavis.dss.ipa.services.InstructorCostService;
import edu.ucdavis.dss.ipa.services.InstructorTypeCostService;
import edu.ucdavis.dss.ipa.services.InstructorTypeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;

@Service
public class JpaInstructorTypeService implements InstructorTypeService {
    @Inject InstructorTypeRepository instructorTypeRepository;

    @Override
    public InstructorType findById(Long instructorTypeId) {
        return instructorTypeRepository.findById(instructorTypeId);
    }
}
