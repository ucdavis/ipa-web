package edu.ucdavis.dss.ipa.diff.mapper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import edu.ucdavis.dss.dw.dto.DwSectionGroup;
import edu.ucdavis.dss.ipa.diff.entities.DiffSectionGroup;

/**
 * Functional class used by Stream::map() for converting DW/Banner section groups to
 * DiffSectionGroups for comparing section groups with JaVers. Uses DwSectionMapper
 * to convert sections within given DwSectionGroup.
 * 
 * @author Eric Lin
 */
public class DwSectionGroupMapper implements Function<DwSectionGroup, DiffSectionGroup> {

	@Override
	public DiffSectionGroup apply(DwSectionGroup sg) {
		DiffSectionGroup.Builder dsgBuilder = new DiffSectionGroup.Builder(sg.getTermCode(), sg.getCourseNumber(), sg.getTitle(), sg.getSequencePattern());
		String sectionGroupId = dsgBuilder.build().javersId();

		return dsgBuilder.unitsHigh((int) sg.getCreditHoursHigh())
				.unitsLow((int) sg.getCreditHoursLow())
				.sections(sg.getSections().stream()
						.map(new DwSectionMapper(dsgBuilder.build().javersId()))
						.filter(s -> s != null)
						.collect(Collectors.toSet()))
				// See JpaDwScheduleService#addOrUpdateDwSectionGroupToSchedule. Instructors for a sectionGroup
				// are added from the sectionGroup's sections.
				.instructors(sg.getSections().stream().map(s -> s.getInstructors()) // Get the instructors for each section
						.filter(i -> i != null) // Remove nulls
						.map(new DwInstructorMapper(sectionGroupId)) // Convert the List<DwInstructor> for each section to a List<DiffInstructor>
						.flatMap(l -> l.stream()) // Flatten the lists to one list
						.distinct() // Remove duplicates. Uses .equals() method for DiffInstructor object
						.collect(Collectors.toSet()))
				.build();
	}
}
