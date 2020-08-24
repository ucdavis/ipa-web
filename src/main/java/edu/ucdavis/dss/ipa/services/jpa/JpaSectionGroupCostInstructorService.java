package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.repositories.SectionGroupCostInstructorRepository;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class JpaSectionGroupCostInstructorService implements SectionGroupCostInstructorService {
    @Inject
    SectionGroupCostInstructorRepository sectionGroupCostInstructorRepository;
    @Inject
    InstructorService instructorService;
    @Inject
    SectionGroupCostService sectionGroupCostService;

    @Override
    public SectionGroupCostInstructor create(SectionGroupCostInstructor sectionGroupCostInstructorDTO) {
        SectionGroupCostInstructor sectionGroupCostInstructor = new SectionGroupCostInstructor();

        Instructor instructor = instructorService.getOneById(sectionGroupCostInstructorDTO.getInstructor().getId());
        SectionGroupCost sectionGroupCost = sectionGroupCostService.findById(sectionGroupCostInstructorDTO.getSectionGroupCost().getId());
        // TODO do we need to use service?

        sectionGroupCostInstructor.setSectionGroupCost(sectionGroupCost);
        sectionGroupCostInstructor.setInstructor(instructor);
        sectionGroupCostInstructor.setCost(sectionGroupCostInstructorDTO.getCost());
        sectionGroupCostInstructor.setReason(sectionGroupCostInstructorDTO.getReason());

        sectionGroupCostInstructor = this.sectionGroupCostInstructorRepository.save(sectionGroupCostInstructor);

        return sectionGroupCostInstructor;
    }

    @Override
    public SectionGroupCostInstructor findById(long sectionGroupCostInstructorId) {
        return this.sectionGroupCostInstructorRepository.findById(sectionGroupCostInstructorId);
    }

    @Override
    public SectionGroupCostInstructor update(SectionGroupCostInstructor sectionGroupCostInstructorDTO) {
        SectionGroupCostInstructor originalSectionGroupCostInstructor = this.findById(sectionGroupCostInstructorDTO.getId());

        if(originalSectionGroupCostInstructor == null) {
            return null;
        }

        originalSectionGroupCostInstructor.setSectionGroupCost(sectionGroupCostInstructorDTO.getSectionGroupCost());
        originalSectionGroupCostInstructor.setInstructor(sectionGroupCostInstructorDTO.getInstructor());
        originalSectionGroupCostInstructor.setCost(sectionGroupCostInstructorDTO.getCost());
        originalSectionGroupCostInstructor.setReason(sectionGroupCostInstructorDTO.getReason());
        return this.save(originalSectionGroupCostInstructor);
    }

    private SectionGroupCostInstructor save(SectionGroupCostInstructor sectionGroupCostInstructor) {
        return this.sectionGroupCostInstructorRepository.save(sectionGroupCostInstructor);
    }
}
