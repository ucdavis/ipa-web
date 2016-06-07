package edu.ucdavis.dss.ipa.diff.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Id;

/**
 * Section entity for comparing differences in sections between DW/Banner and
 * IPA. Id is scoped off DiffSectionGroup, so changes in any part of
 * DiffSectionGroup's id means JaVers detects Sections (and all associated
 * meetings and instructors) as created/removed.
 * 
 * Helps compare differences in:
 * <ul>
 *   <li>Sequence Number</li>
 *   <li>Seats</li>
 *   <li>Visibility/CRN Restriction</li>
 *   <li>Meetings</li>
 *   <li>Instructors</li>
 * </ul>
 * 
 * Required fields are: sequenceNumber, parentId
 * 
 * @author Eric Lin
 */
public class DiffSection implements DiffEntity {
	@Id
	private String javersId;

	private String sequenceNumber, parentId;
	private long seats;
	private boolean visible, crnRestricted;
	private Set<DiffMeeting> meetings;
	
	public DiffSection(Builder builder) {
		sequenceNumber = builder.sequenceNumber;
		parentId = builder.parentId;
		javersId = parentId + "/" + sequenceNumber;

		seats = builder.seats;
		visible = builder.visible;
		crnRestricted = builder.crnRestricted;

		meetings = builder.meetings;
		
		if (meetings == null)
			meetings = Collections.emptySet();
	}

	public String javersId() {
		return javersId;
	}

	@Override
	public void syncJaversIds(DiffEntity entity) {
		DiffSection otherSection = (DiffSection) entity;

		otherSection.javersId = this.javersId;

		otherSection.meetings.forEach(mtg -> mtg.syncJaversParentIds(this.javersId));
	}
	
	@Override
	public void syncJaversParentIds(String parentId) {
		this.parentId = parentId;
		javersId = parentId + "/" + sequenceNumber;
		
		meetings.forEach(mtg -> mtg.syncJaversParentIds(javersId));
	}

	@Override
	public double uncheckedCalculateDifferences(Object o) {
		double differences = 0;
		DiffSection compare = (DiffSection) o;
		
		if ((sequenceNumber != null && (! sequenceNumber.equals(compare.sequenceNumber))) ||
				(sequenceNumber == null ^ compare.sequenceNumber == null) )
			differences++;
		if (seats != compare.seats)
			differences++;
		if (visible != compare.visible)
			differences++;
		if (crnRestricted != compare.crnRestricted)
			differences++;

		differences += calculateSetDifferences(meetings, compare.meetings);

		return differences / (4 +
				// Difference calculation result should be the same regardless of the
				// direction in which the comparison is made, so use the biggest of the
				// two set sizes (in case they're different)
				Math.max(meetings.size(), compare.meetings.size()));
	}

	/**
	 * Builder class for DiffSection. Encourages DiffSection to be immutable.
	 * 
	 * Required values are: Parent DiffSectionGroup's JaVers id and sequenceNumber
	 */
	public static class Builder {
		private String sequenceNumber, parentId;
		private long seats;
		private boolean visible, crnRestricted;
		private Set<DiffMeeting> meetings;
		
		public Builder(String parentId, String sequenceNumber) {
			this.parentId = parentId;
			this.sequenceNumber = sequenceNumber;
		}

		public Builder seats(long value) {
			seats = value;
			return this;
		}

		public Builder visible(boolean value) {
			visible = value;
			return this;
		}

		public Builder crnRestricted(boolean value) {
			crnRestricted = value;
			return this;
		}

		public Builder meetings(Set<DiffMeeting> values) {
			meetings = values;
			return this;
		}

		public DiffSection build() {
			return new DiffSection(this);
		}
	}
}
