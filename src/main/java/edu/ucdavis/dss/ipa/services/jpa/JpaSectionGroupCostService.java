package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.repositories.InstructorRepository;
import edu.ucdavis.dss.ipa.repositories.InstructorTypeRepository;
import edu.ucdavis.dss.ipa.repositories.SectionGroupCostRepository;
import edu.ucdavis.dss.ipa.services.SectionGroupCostService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

@Service
public class JpaSectionGroupCostService implements SectionGroupCostService {
    @Inject SectionGroupCostRepository sectionGroupCostRepository;
    @Inject InstructorRepository instructorRepository;
    @Inject InstructorTypeRepository instructorTypeRepository;

    @Override
    public List<SectionGroupCost> findByBudgetId(Long budgetId) {
        return sectionGroupCostRepository.findByBudgetId(budgetId);
    }

    @Override
    public SectionGroupCost createOrUpdateFrom(SectionGroupCost originalSectionGroupCost, BudgetScenario budgetScenario) {
        if (originalSectionGroupCost == null || budgetScenario == null) {
            return null;
        }

        SectionGroupCost sectionGroupCost = sectionGroupCostRepository.findBySubjectCodeAndCourseNumberAndSequencePatternAndBudgetScenarioIdAndTermCode(originalSectionGroupCost.getSubjectCode(), originalSectionGroupCost.getCourseNumber(), originalSectionGroupCost.getSequencePattern(), budgetScenario.getId(), originalSectionGroupCost.getTermCode());

        if (sectionGroupCost == null) {
            sectionGroupCost = new SectionGroupCost();
            sectionGroupCost.setUnitsHigh(originalSectionGroupCost.getUnitsHigh());
            sectionGroupCost.setUnitsLow(originalSectionGroupCost.getUnitsLow());
            sectionGroupCost.setEffectiveTermCode(originalSectionGroupCost.getEffectiveTermCode());
            sectionGroupCost.setTermCode(originalSectionGroupCost.getTermCode());
            sectionGroupCost.setTitle(originalSectionGroupCost.getTitle());
            sectionGroupCost.setSubjectCode(originalSectionGroupCost.getSubjectCode());
            sectionGroupCost.setCourseNumber(originalSectionGroupCost.getCourseNumber());
            sectionGroupCost.setBudgetScenario(budgetScenario);
            sectionGroupCost.setSequencePattern(originalSectionGroupCost.getSequencePattern());
        }

        if (originalSectionGroupCost.getInstructorType() != null) {
            InstructorType instructorType = instructorTypeRepository.findById(originalSectionGroupCost.getInstructorType().getId());
            originalSectionGroupCost.setInstructorType(instructorType);
        }

        if (originalSectionGroupCost.getInstructor() != null) {
            Instructor instructor = instructorRepository.findById(originalSectionGroupCost.getInstructor().getId());
            sectionGroupCost.setInstructor(instructor);
        }

        sectionGroupCost.setBudgetScenario(budgetScenario);
        sectionGroupCost.setInstructorType(originalSectionGroupCost.getInstructorType());
        sectionGroupCost.setEnrollment(originalSectionGroupCost.getEnrollment());
        sectionGroupCost.setOriginalInstructor(originalSectionGroupCost.getOriginalInstructor());
        sectionGroupCost.setReaderCount(originalSectionGroupCost.getReaderCount());
        sectionGroupCost.setTaCount(originalSectionGroupCost.getTaCount());
        sectionGroupCost.setReason(originalSectionGroupCost.getReason());
        sectionGroupCost.setSectionCount(originalSectionGroupCost.getSectionCount());
        sectionGroupCost.setCost(originalSectionGroupCost.getCost());
        sectionGroupCost.setDisabled(originalSectionGroupCost.isDisabled());

        return this.sectionGroupCostRepository.save(sectionGroupCost);
    }

    @Override
    public SectionGroupCost createFromSectionGroup(SectionGroup sectionGroup, BudgetScenario budgetScenario) {
        SectionGroupCost sectionGroupCost = new SectionGroupCost();

        sectionGroupCost.setBudgetScenario(budgetScenario);
        sectionGroupCost.setTermCode(sectionGroup.getTermCode());
        sectionGroupCost.setCourseNumber(sectionGroup.getCourse().getCourseNumber());
        sectionGroupCost.setTitle(sectionGroup.getCourse().getTitle());
        sectionGroupCost.setSubjectCode(sectionGroup.getCourse().getSubjectCode());
        sectionGroupCost.setSequencePattern(sectionGroup.getCourse().getSequencePattern());
        sectionGroupCost.setEffectiveTermCode(sectionGroup.getCourse().getEffectiveTermCode());
        sectionGroupCost.setUnitsHigh(sectionGroup.getCourse().getUnitsHigh());
        sectionGroupCost.setUnitsLow(sectionGroup.getCourse().getUnitsLow());
        sectionGroupCost.setTermCode(sectionGroup.getTermCode());
        sectionGroupCost.setTaCount(sectionGroup.getTeachingAssistantAppointments());
        sectionGroupCost.setReaderCount(sectionGroup.getReaderAppointments());

        // Set instructor
        Instructor instructor = null;
        InstructorType instructorType = null;

        for (TeachingAssignment teachingAssignment : sectionGroup.getTeachingAssignments()) {
            Instructor instructorDTO = teachingAssignment.getInstructor();
            InstructorType instructorTypeDTO = teachingAssignment.getInstructorType();
            if (instructorDTO != null) {
                instructor = instructorDTO;
                instructorType = instructorTypeDTO;
            }
        }

        sectionGroupCost.setInstructor(instructor);
        sectionGroupCost.setInstructorType(instructorType);

        // Set sectionCount
        Integer sectionCount = sectionGroup.getSections().size();
        sectionGroupCost.setSectionCount(sectionCount);

        // Set enrollment
        Long enrollment = 0L;

        for (Section section : sectionGroup.getSections()) {
            if (section.getSeats() != null) {
                enrollment += section.getSeats();
            }
        }

        sectionGroupCost.setEnrollment(enrollment);

        return sectionGroupCostRepository.save(sectionGroupCost);
    }

    @Override
    public SectionGroupCost findById(long sectionGroupCostId) {
        return this.sectionGroupCostRepository.findById(sectionGroupCostId);
    }

    @Override
    public SectionGroupCost update(SectionGroupCost sectionGroupCostDTO) {
        SectionGroupCost originalSectionGroupCost = this.findById(sectionGroupCostDTO.getId());

        if(originalSectionGroupCost == null) {
            return null;
        }

        originalSectionGroupCost.setTaCount(sectionGroupCostDTO.getTaCount());
        originalSectionGroupCost.setReaderCount(sectionGroupCostDTO.getReaderCount());
        originalSectionGroupCost.setEnrollment(sectionGroupCostDTO.getEnrollment());
        originalSectionGroupCost.setSectionCount(sectionGroupCostDTO.getSectionCount());
        originalSectionGroupCost.setCost(sectionGroupCostDTO.getCost());
        originalSectionGroupCost.setReason(sectionGroupCostDTO.getReason());
        originalSectionGroupCost.setDisabled(sectionGroupCostDTO.isDisabled());

        originalSectionGroupCost.setInstructor(instructorRepository.findById(sectionGroupCostDTO.getInstructorIdentification()));
        originalSectionGroupCost.setOriginalInstructor(instructorRepository.findById(sectionGroupCostDTO.getOriginalInstructorIdentification()));

        if (sectionGroupCostDTO.getInstructorType() != null) {
            originalSectionGroupCost.setInstructorType(instructorTypeRepository.findById(sectionGroupCostDTO.getInstructorType().getId()));
        }

        return this.sectionGroupCostRepository.save(originalSectionGroupCost);
    }

    @Override
    public List<SectionGroupCost> findbyWorkgroupIdAndYear(long workgroupId, long year) {
        return this.sectionGroupCostRepository.findbyWorkgroupIdAndYear(workgroupId, year);
    }
}
