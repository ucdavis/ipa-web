package edu.ucdavis.dss.ipa.diff.mapper;

import java.util.function.Function;
import java.util.stream.Collectors;

import edu.ucdavis.dss.ipa.diff.entities.DiffSection;
import edu.ucdavis.dss.ipa.entities.Section;

/**
 * Functional class used by Stream::map() for converting IPA sections to
 * DiffSections for comparing sections using JaVers. Used by JpaSectionGroupMapper to
 * convert sections. Uses JpaMeetingMapper to convert meetings. Does not currently
 * convert instructors.
 * 
 * @author Eric Lin
 */
public class JpaSectionMapper implements Function<Section, DiffSection> {
	private String parentId;

	public JpaSectionMapper(String parentId) {
		this.parentId = parentId;
	}

	@Override
	public DiffSection apply(Section section) {
		DiffSection.Builder dsBuilder = new DiffSection.Builder(parentId, section.getSequenceNumber());
		String sectionId = dsBuilder.build().javersId();

		return dsBuilder.crnRestricted(section.isCrnRestricted())
				.visible(section.isVisible())
				.seats(section.getSeats())
				.meetings(section.getActivities() != null ?
						section.getActivities().stream()
							.map(new JpaMeetingMapper(sectionId))
							.filter(m -> m != null)
							.collect(Collectors.toSet())
						: null)
				.build();
	}

}
