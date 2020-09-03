package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.repositories.SectionGroupCostInstructorRepository;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

@Service
public class JpaSectionGroupCostInstructorService implements SectionGroupCostInstructorService {
    @Inject
    SectionGroupCostInstructorRepository sectionGroupCostInstructorRepository;
    @Inject
    InstructorService instructorService;
    @Inject
    SectionGroupCostService sectionGroupCostService;
    @Inject
    TeachingAssignmentService teachingAssignmentService;
    @Inject
    InstructorTypeService instructorTypeService;

    @Override
    public SectionGroupCostInstructor create(SectionGroupCostInstructor sectionGroupCostInstructorDTO) {
        SectionGroupCostInstructor sectionGroupCostInstructor = new SectionGroupCostInstructor();

        SectionGroupCost sectionGroupCost = sectionGroupCostService.findById(sectionGroupCostInstructorDTO.getSectionGroupCost().getId());
        InstructorType instructorType = instructorTypeService.findById(sectionGroupCostInstructorDTO.getInstructorType().getId());
        if(sectionGroupCostInstructorDTO.getTeachingAssignment() != null){
            TeachingAssignment teachingAssignment = teachingAssignmentService.findOneById(sectionGroupCostInstructorDTO.getTeachingAssignment().getId());
            sectionGroupCostInstructor.setTeachingAssignment(teachingAssignment);
        }

        if(sectionGroupCostInstructorDTO.getInstructor() != null){
            Instructor instructor = instructorService.getOneById(sectionGroupCostInstructorDTO.getInstructor().getId());
            sectionGroupCostInstructor.setInstructor(instructor);
        }

        sectionGroupCostInstructor.setSectionGroupCost(sectionGroupCost);
        sectionGroupCostInstructor.setInstructorType(instructorType);
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
        originalSectionGroupCostInstructor.setInstructorType(sectionGroupCostInstructorDTO.getInstructorType());
        originalSectionGroupCostInstructor.setCost(sectionGroupCostInstructorDTO.getCost());
        originalSectionGroupCostInstructor.setReason(sectionGroupCostInstructorDTO.getReason());

        return this.save(originalSectionGroupCostInstructor);
    }

    private SectionGroupCostInstructor save(SectionGroupCostInstructor sectionGroupCostInstructor) {
        return this.sectionGroupCostInstructorRepository.save(sectionGroupCostInstructor);
    }

    @Override
    public void delete(long sectionGroupCostInstructorId){
        this.sectionGroupCostInstructorRepository.delete(sectionGroupCostInstructorId);
    }

    @Override
    public List<SectionGroupCostInstructor> copyInstructors(SectionGroupCost originalSectionGroupCost, SectionGroupCost newSectionGroupCost){
        List<SectionGroupCostInstructor> originalSectionGroupCostInstructors = originalSectionGroupCost.getSectionGroupCostInstructors();
        List<SectionGroupCostInstructor> newSectionGroupCostInstructors = newSectionGroupCost.getSectionGroupCostInstructors();

        for(SectionGroupCostInstructor originalSectionGroupCostInstructor : originalSectionGroupCostInstructors){
            SectionGroupCostInstructor newSectionGroupCostInstructor = new SectionGroupCostInstructor();
            newSectionGroupCostInstructor.setSectionGroupCost(newSectionGroupCost);
            newSectionGroupCostInstructor.setCost(originalSectionGroupCostInstructor.getCost());
            newSectionGroupCostInstructor.setReason(originalSectionGroupCostInstructor.getReason());
            newSectionGroupCostInstructor.setInstructorType(originalSectionGroupCostInstructor.getInstructorType());

            if(originalSectionGroupCostInstructor.getInstructor() != null){
                newSectionGroupCostInstructor.setInstructor(originalSectionGroupCostInstructor.getInstructor());
            }

            newSectionGroupCostInstructors.add(sectionGroupCostInstructorRepository.save(newSectionGroupCostInstructor));
        }
        return newSectionGroupCostInstructors;
    }
}
