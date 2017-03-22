package edu.ucdavis.dss.ipa.entities.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import edu.ucdavis.dss.ipa.entities.Section;
import edu.ucdavis.dss.ipa.entities.SectionGroup;

public class ValidSectionValidator implements ConstraintValidator<ValidSection, Section> {

	@Override
	public void initialize(ValidSection constraintAnnotation) {
	}
	
	@Override
	public boolean isValid(Section section, ConstraintValidatorContext context) {

		if (section.getSequenceNumber() == null || section.getSequenceNumber().length() == 0) {
			return false;
		}

		for (SectionGroup slotSectionGroup : section.getSectionGroup().getCourse().getSectionGroups()) {
			for (Section slotSection : slotSectionGroup.getSections()) {
				// Ensure that if the sequencePattern/termCode match, that its just the same section comparing against itself
				if (section.getId() != slotSection.getId()
					&& section.getSequenceNumber().equals(slotSection.getSequenceNumber())
					&& section.getSectionGroup().getTermCode().equals(slotSection.getSectionGroup().getTermCode())) {
					return false;
				}
			}
		}
		return true;
	}
}
