package edu.ucdavis.dss.ipa.diff.mapper;

import java.util.Set;
import java.util.function.BinaryOperator;

import edu.ucdavis.dss.ipa.diff.entities.DiffInstructor;
import edu.ucdavis.dss.ipa.diff.entities.DiffSection;
import edu.ucdavis.dss.ipa.diff.entities.DiffSectionGroup;

/**
 * Functional class used by Collectors::reducing() for combining sections in
 * different DiffSectionGroups that should be the same DiffSectionGroup into
 * one DiffSectionGroup.
 * 
 * @author Eric Lin
 */
public class SectionGroupGrouper implements BinaryOperator<DiffSectionGroup> {

	@Override
	public DiffSectionGroup apply(DiffSectionGroup dsgOne, DiffSectionGroup dsgTwo) {
		if (dsgOne == null)
			return dsgTwo;

		// Combine sections from the two DiffSectionGroups
		Set<DiffSection> sections = dsgOne.getSections();
		sections.addAll(dsgTwo.getSections());

		// Combine instructors from the two DiffSectionGroups
		Set<DiffInstructor> instructors = dsgOne.getInstructors();
		instructors.addAll(dsgTwo.getInstructors());

		// Copy the term code, course number, etc. from one DiffSectionGroup and combines the
		// sections from the two DiffSectionGroups to create a new DiffSectionGroup.
		return new DiffSectionGroup.Builder(dsgOne.getTermCode(), dsgOne.getCourseNumber(), dsgOne.getTitle(), dsgOne.getSequencePattern())
				.unitsHigh(dsgOne.getUnitsHigh())
				.unitsLow(dsgOne.getUnitsLow())
				.sections(sections)
				.instructors(instructors)
				.build();
	}

}
