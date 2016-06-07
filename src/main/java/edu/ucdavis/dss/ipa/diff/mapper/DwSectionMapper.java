package edu.ucdavis.dss.ipa.diff.mapper;

import java.util.function.Function;
import java.util.stream.Collectors;

import edu.ucdavis.dss.dw.dto.DwSection;
import edu.ucdavis.dss.ipa.diff.entities.DiffSection;

/**
 * Functional class used by Stream::map() for converting DW/Banner sections to
 * DiffSections for comparing sections using JaVers. Used by DwSectionGroupMapper to
 * convert sections. Uses DwMeetingMapper to convert meetings. Does not currently
 * convert instructors.
 * 
 * @author Eric Lin
 */
public class DwSectionMapper implements Function<DwSection, DiffSection> {
	private String parentId;

	public DwSectionMapper(String parentId) {
		this.parentId = parentId;
	}

	@Override
	public DiffSection apply(DwSection section) {
		DiffSection.Builder dsBuilder = new DiffSection.Builder(parentId, section.getSequenceNumber());
		String sectionId = dsBuilder.build().javersId();

		return dsBuilder.crnRestricted(section.isCrnRestricted())
				.visible(section.isVisible())
				.seats(section.getMaximumEnrollment())
				.meetings(section.getMeetings() != null ?
						section.getMeetings().stream()
							.map(new DwMeetingMapper(sectionId))
							.filter(m -> m != null)
							.collect(Collectors.toSet())
						: null)
				.build();
	}

}
