package edu.ucdavis.dss.ipa.diff.mapper;

import java.util.function.Function;
import java.util.stream.Collectors;

import edu.ucdavis.dss.ipa.diff.entities.DiffSectionGroup;
import edu.ucdavis.dss.ipa.entities.SectionGroup;

/**
 * Functional class used by Stream::map() for converting IPA section groups to
 * DiffSectionGroups for comparing section groups with JaVers. Uses JpaSectionMapper
 * to convert sections within given SectionGroup.
 * 
 * @author Eric Lin
 */
public class JpaSectionGroupMapper implements Function<SectionGroup, DiffSectionGroup> {

	@Override
	public DiffSectionGroup apply(SectionGroup sg) {
		DiffSectionGroup.Builder dsgBuilder = new DiffSectionGroup.Builder(
				sg.getTermCode(),
				sg.getCourse().getCourseNumber(),
				sg.getCourse().getTitle(),
				sg.getCourse().getSequencePattern());

		String sectionGroupId = dsgBuilder.build().javersId();

		return dsgBuilder.unitsHigh((int) sg.getCourse().getUnitsHigh())
				.unitsLow((int) sg.getCourse().getUnitsLow())
				.sections(sg.getSections().stream()
						.map(new JpaSectionMapper(sectionGroupId))
						.filter(s -> s != null)
						.collect(Collectors.toSet()))
				.instructors(sg.getTeachingAssignments() != null ?
						sg.getTeachingAssignments().stream().map(i -> i.getInstructor())
							.map(new JpaInstructorMapper(sectionGroupId))
							.filter(i -> i != null)
							.collect(Collectors.toSet())
						: null)
				.build();
			
	}

}
