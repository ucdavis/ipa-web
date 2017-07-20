package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.repositories.SectionGroupCostRepository;
import edu.ucdavis.dss.ipa.services.SectionGroupCostService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

@Service
public class JpaSectionGroupCostService implements SectionGroupCostService {
    @Inject SectionGroupCostRepository sectionGroupCostRepository;

    @Override
    public List<SectionGroupCost> findByBudgetId(Long budgetId) {
        return sectionGroupCostRepository.findByBudgetId(budgetId);
    }

    @Override
    public SectionGroupCost createFrom(SectionGroupCost originalSectionGroupCost, BudgetScenario budgetScenario) {
        SectionGroupCost sectionGroupCost = new SectionGroupCost();

        sectionGroupCost.setBudgetScenario(budgetScenario);
        sectionGroupCost.setInstructor(originalSectionGroupCost.getInstructor());
        sectionGroupCost.setSectionGroup(originalSectionGroupCost.getSectionGroup());
        sectionGroupCost.setEnrollment(originalSectionGroupCost.getEnrollment());
        sectionGroupCost.setOriginalInstructor(originalSectionGroupCost.getOriginalInstructor());
        sectionGroupCost.setReaderCount(originalSectionGroupCost.getReaderCount());
        sectionGroupCost.setTaCount(originalSectionGroupCost.getTaCount());
        sectionGroupCost.setSectionCount(originalSectionGroupCost.getSectionCount());
        sectionGroupCost.setInstructorCost(originalSectionGroupCost.getInstructorCost());
        sectionGroupCost.setSubjectCode(originalSectionGroupCost.getSubjectCode());
        sectionGroupCost.setCourseNumber(originalSectionGroupCost.getCourseNumber());
        sectionGroupCost.setTitle(originalSectionGroupCost.getTitle());
        sectionGroupCost.setEffectiveTermCode(originalSectionGroupCost.getEffectiveTermCode());
        sectionGroupCost.setUnitsHigh(originalSectionGroupCost.getUnitsHigh());
        sectionGroupCost.setUnitsLow(originalSectionGroupCost.getUnitsLow());
        sectionGroupCost.setTermCode(originalSectionGroupCost.getTermCode());
        sectionGroupCost.setSequencePattern(originalSectionGroupCost.getSequencePattern());
        sectionGroupCost.setTermCode(originalSectionGroupCost.getTermCode());

        return this.sectionGroupCostRepository.save(sectionGroupCost);
    }

    @Override
    public SectionGroupCost createFromSectionGroup(SectionGroup sectionGroup, BudgetScenario budgetScenario) {
        SectionGroupCost sectionGroupCost = new SectionGroupCost();

        sectionGroupCost.setBudgetScenario(budgetScenario);
        sectionGroupCost.setSectionGroup(sectionGroup);

        // Set instructor
        Instructor instructor = null;
        for (TeachingAssignment teachingAssignment : sectionGroup.getTeachingAssignments()) {
            Instructor instructorDTO = teachingAssignment.getInstructor();
            if (instructorDTO != null) {
                instructor = instructorDTO;
            }
        }

        sectionGroupCost.setInstructor(instructor);

        // Set course meta data
        sectionGroupCost.setCourseNumber(sectionGroup.getCourse().getCourseNumber());
        sectionGroupCost.setSubjectCode(sectionGroup.getCourse().getSubjectCode());
        sectionGroupCost.setEffectiveTermCode(sectionGroup.getCourse().getEffectiveTermCode());
        sectionGroupCost.setTitle(sectionGroup.getCourse().getTitle());
        sectionGroupCost.setUnitsHigh(sectionGroup.getCourse().getUnitsHigh());
        sectionGroupCost.setUnitsLow(sectionGroup.getCourse().getUnitsHigh());
        sectionGroupCost.setTermCode(sectionGroup.getTermCode());
        sectionGroupCost.setSequencePattern(sectionGroup.getCourse().getSequencePattern());

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

        // Set reader and ta count
        Long readerCount = 0L;
        Long taCount = 0L;

        for (SupportAssignment supportAssignment : sectionGroup.getSupportAssignments()) {
            if (supportAssignment.getAppointmentType().equals("teachingAssistant")) {
                Long taAmount = supportAssignment.getAppointmentPercentage() / 50;
                taCount += taAmount;
            }
            if (supportAssignment.getAppointmentType().equals("reader")) {
                Long readerAmount = supportAssignment.getAppointmentPercentage() / 50;
                readerCount += readerAmount;
            }
        }

        sectionGroupCost.setReaderCount(readerCount);
        sectionGroupCost.setTaCount(taCount);

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

        return this.sectionGroupCostRepository.save(originalSectionGroupCost);
    }
}
