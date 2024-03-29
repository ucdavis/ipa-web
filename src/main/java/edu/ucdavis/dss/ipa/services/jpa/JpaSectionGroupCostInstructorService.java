package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.InstructorType;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.entities.SectionGroupCost;
import edu.ucdavis.dss.ipa.entities.SectionGroupCostInstructor;
import edu.ucdavis.dss.ipa.entities.TeachingAssignment;
import edu.ucdavis.dss.ipa.repositories.SectionGroupCostInstructorRepository;
import edu.ucdavis.dss.ipa.services.InstructorService;
import edu.ucdavis.dss.ipa.services.InstructorTypeService;
import edu.ucdavis.dss.ipa.services.SectionGroupCostInstructorService;
import edu.ucdavis.dss.ipa.services.SectionGroupCostService;
import edu.ucdavis.dss.ipa.services.SectionGroupService;
import edu.ucdavis.dss.ipa.services.TeachingAssignmentService;
import java.util.ArrayList;
import java.util.List;
import jakarta.inject.Inject;
import org.springframework.stereotype.Service;

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
    public SectionGroupCostInstructor findOrCreate(SectionGroupCostInstructor sectionGroupCostInstructorDTO) {
        if(sectionGroupCostInstructorDTO.getInstructor() != null){
            SectionGroupCostInstructor existingSectionGroupCostInstructor =
                    sectionGroupCostInstructorRepository
                            .findByInstructorIdAndSectionGroupCostIdAndTeachingAssignmentId(
                                    sectionGroupCostInstructorDTO.getInstructor().getId(),
                                    sectionGroupCostInstructorDTO.getSectionGroupCost() != null ? sectionGroupCostInstructorDTO.getSectionGroupCost().getId() : null,
                                    sectionGroupCostInstructorDTO.getTeachingAssignment() != null ? sectionGroupCostInstructorDTO.getTeachingAssignment().getId() : null
                            );

            if (existingSectionGroupCostInstructor != null) {
                return existingSectionGroupCostInstructor;
            }
        }

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
        return this.sectionGroupCostInstructorRepository.findById(sectionGroupCostInstructorId).orElse(null);
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
        this.sectionGroupCostInstructorRepository.deleteById(sectionGroupCostInstructorId);
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

    @Override
    public List<SectionGroupCostInstructor> findbyWorkgroupIdAndYear(long workgroupId, long year) {
        return this.sectionGroupCostInstructorRepository.findbyWorkgroupIdAndYear(workgroupId, year);
    }
}
