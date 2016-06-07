package edu.ucdavis.dss.ipa.diff.entities;

import java.sql.Time;
import java.time.LocalDateTime;

import javax.persistence.Id;

/**
 * Meeting entity for comparing meetings/activities between DW/Banner and IPA.
 * Associated with DiffSection through DiffSection's meetings variable. The
 * JaVers id for this class is a combination of the DiffSection id, building code,
 * room code, and day indicator, so changes in any of those fields mean
 * JaVers detects (at least) three changes.
 * 
 * Helps compare differences in:
 * <ul>
 *   <li>Building and Room</li>
 *   <li>Meeting Days</li>
 *   <li>Meeting Frequency</li>
 *   <li>Begin and End Times</li>
 *   <li>Begin and End Dates</li>
 * </ul>
 * 
 * @author Eric Lin
 */
public class DiffMeeting implements DiffEntity {
	@Id
	private String javersId;

	private String parentId, buildingCode, roomCode, dayIndicator;
	private char activityCode;
	private int frequency;
	private Time beginTime, endTime;
	private LocalDateTime beginDate, endDate;
	
	private DiffMeeting(Builder builder) {
		parentId = builder.parentId;
		buildingCode = builder.buildingCode;
		roomCode = builder.roomCode;
		dayIndicator = builder.dayIndicator;
		activityCode = builder.activityCode;
		javersId = generateJaversId();

		frequency = builder.frequency;
		beginTime = builder.beginTime;
		endTime = builder.endTime;
		beginDate = builder.beginDate;
		endDate = builder.endDate;
	}
	
	public String javersId() {
		return javersId;
	}

	@Override
	public void syncJaversIds(DiffEntity entity) {
		DiffMeeting otherMtg = (DiffMeeting) entity;
		otherMtg.javersId = this.javersId;
	}

	@Override
	public void syncJaversParentIds(String parentId) {
		this.parentId = parentId;
		javersId = generateJaversId();
	}

	// Don't need to check parentId, because objects passed to this method
	// should be from a SetChange and therefore have the same parent
	@Override
	public double uncheckedCalculateDifferences(Object o) {
		double differences = 9;

		DiffMeeting compare = (DiffMeeting) o;

		if ( (buildingCode != null && buildingCode.equals(compare.buildingCode)) ||
				(buildingCode == null && compare.buildingCode == null) )
			differences--;
		if ( (roomCode != null && roomCode.equals(compare.roomCode)) ||
				(roomCode == null && compare.roomCode == null) )
			differences--;
		if ( (dayIndicator != null && dayIndicator.equals(compare.dayIndicator)) ||
				(dayIndicator == null && compare.dayIndicator == null) )
			differences--;
		if (activityCode == compare.activityCode)
			differences--;
		if (frequency == compare.frequency)
			differences--;
		if ( (beginTime != null && beginTime.equals(compare.beginTime)) ||
				(beginTime == null && compare.beginTime == null) )
			differences--;
		if ( (endTime != null && endTime.equals(compare.endTime)) ||
				(endTime == null && compare.endTime == null) )
			differences--;
		if ( (beginDate != null && beginDate.equals(compare.beginDate)) ||
				(beginDate == null && compare.beginDate == null) )
			differences--;
		if ( (endDate != null && endDate.equals(compare.endDate)) ||
				(endDate == null && compare.endDate == null) )
			differences--;
		
		return differences / 9;
	}
	
	private String generateJaversId() {
		return parentId + "/" + buildingCode + "," + roomCode + "," + dayIndicator + "," + activityCode;
	}

	/**
	 * Builder class for DiffMeeting. Encourages DiffMeeting to be immutable.
	 * 
	 * Required values for constructing a DiffMeeting:
	 * <ul>
	 *   <li>Parent DiffSection's JaVers id</li>
	 *   <li>Building Code</li>
	 *   <li>Room Code</li>
	 *   <li>Meeting Days</li>
	 * </ul>
	 * 
	 * All other values are optional.
	 */
	public static class Builder {
		private String parentId, buildingCode, roomCode, dayIndicator;
		private char activityCode;

		private int frequency;
		private Time beginTime, endTime;
		private LocalDateTime beginDate, endDate;

		public Builder(String parentId, String buildingCode, String roomCode, String dayIndicator, char activityCode) {
			this.parentId = parentId;
			this.buildingCode = buildingCode;
			this.roomCode = roomCode;
			this.dayIndicator = dayIndicator;
			this.activityCode = activityCode;
		}

		public Builder frequency(int value) {
			frequency = value;
			return this;
		}

		public Builder beginTime(Time time) {
			beginTime = time;
			return this;
		}

		public Builder endTime(Time time) {
			endTime = time;
			return this;
		}

		public Builder beginDate(LocalDateTime date) {
			beginDate = date;
			return this;
		}

		public Builder endDate(LocalDateTime date) {
			endDate = date;
			return this;
		}

		public DiffMeeting build() {
			return new DiffMeeting(this);
		}
	}
}