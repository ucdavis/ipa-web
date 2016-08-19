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
		for (SectionGroup slotSectionGroup : section.getSectionGroup().getCourse().getSectionGroups()) {
			for (Section slotSection : slotSectionGroup.getSections()) {
				if(section.getId() != slotSection.getId()
						&& section.getSequenceNumber().equals(slotSection.getSequenceNumber())
						&& section.getSectionGroup().getTermCode().equals(slotSection.getSectionGroup().getTermCode())) {
					return false;
				}
			}
		}
		return true;
	}
}
