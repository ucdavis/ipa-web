package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.repositories.SectionGroupCostInstructorRepository;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
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
    @Inject
    SectionGroupService sectionGroupService;

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
    public List<SectionGroupCostInstructor> copyInstructors(long workgroupId, SectionGroupCost originalSectionGroupCost, SectionGroupCost newSectionGroupCost){
        List<SectionGroupCostInstructor> originalSectionGroupCostInstructors = originalSectionGroupCost.getSectionGroupCostInstructors();
        List<SectionGroupCostInstructor> newSectionGroupCostInstructors = newSectionGroupCost.getSectionGroupCostInstructors();
        List<Long> teachingAssingmentIds = new ArrayList<>();

        for(SectionGroupCostInstructor originalSectionGroupCostInstructor : originalSectionGroupCostInstructors){
            SectionGroupCostInstructor newSectionGroupCostInstructor = new SectionGroupCostInstructor();
            newSectionGroupCostInstructor.setSectionGroupCost(newSectionGroupCost);
            newSectionGroupCostInstructor.setCost(originalSectionGroupCostInstructor.getCost());
            newSectionGroupCostInstructor.setReason(originalSectionGroupCostInstructor.getReason());
            newSectionGroupCostInstructor.setInstructorType(originalSectionGroupCostInstructor.getInstructorType());

            if(originalSectionGroupCostInstructor.getInstructor() != null){
                newSectionGroupCostInstructor.setInstructor(originalSectionGroupCostInstructor.getInstructor());
            }
            if(originalSectionGroupCostInstructor.getTeachingAssignment() != null){
                teachingAssingmentIds.add(originalSectionGroupCostInstructor.getTeachingAssignment().getId());
            }

            newSectionGroupCostInstructors.add(sectionGroupCostInstructorRepository.save(newSectionGroupCostInstructor));
        }

        if(originalSectionGroupCost.getBudgetScenario().getFromLiveData()){
            SectionGroup sectionGroup = sectionGroupService.findBySectionGroupCostDetails(
                    workgroupId,
                    originalSectionGroupCost.getCourseNumber(),
                    originalSectionGroupCost.getSequencePattern(),
                    originalSectionGroupCost.getTermCode(),
                    originalSectionGroupCost.getSubjectCode());
            if(sectionGroup != null){
                List<TeachingAssignment> teachingAssignments = sectionGroup.getTeachingAssignments();
                for(TeachingAssignment teachingAssignment : teachingAssignments){
                    if(!teachingAssingmentIds.contains(teachingAssignment.getId()) && teachingAssignment.isApproved()){
                        SectionGroupCostInstructor newSectionGroupCostInstructor = new SectionGroupCostInstructor();
                        newSectionGroupCostInstructor.setSectionGroupCost(newSectionGroupCost);
                        if(teachingAssignment.getInstructor() != null){
                            newSectionGroupCostInstructor.setInstructor(teachingAssignment.getInstructor());
                        }
                        if(teachingAssignment.getInstructorType() != null){
                            newSectionGroupCostInstructor.setInstructorType(teachingAssignment.getInstructorType());
                        }
                        newSectionGroupCostInstructors.add(newSectionGroupCostInstructor);
                    }
                }

            }
        }
        return newSectionGroupCostInstructors;
    }

    @Override
    public List<SectionGroupCostInstructor> findBySectionGroupCosts(List<SectionGroupCost> sectionGroupCosts) {
        List<SectionGroupCostInstructor> sectionGroupCostInstructors = new ArrayList<>();

        for (SectionGroupCost sectionGroupCost : sectionGroupCosts) {
            sectionGroupCostInstructors.addAll(sectionGroupCost.getSectionGroupCostInstructors());
        }

        return sectionGroupCostInstructors;
    }

    @Override
    public List<SectionGroupCostInstructor> findByBudgetId(long budgetId) {
        return sectionGroupCostInstructorRepository.findByBudgetId(budgetId);
    }

    @Override
    public List<SectionGroupCostInstructor> findByBudgetScenarioId(long budgetScenarioId) {
        return sectionGroupCostInstructorRepository.findByBudgetScenarioId(budgetScenarioId);
    }
}
