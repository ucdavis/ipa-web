package edu.ucdavis.dss.ipa.entities;

import java.io.Serializable;
import java.sql.Time;
import java.util.Date;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import edu.ucdavis.dss.ipa.enums.ActivityState;
import edu.ucdavis.dss.ipa.web.deserializers.ActivityDeserializer;
import edu.ucdavis.dss.ipa.web.views.SectionGroupViews;

@JsonIgnoreProperties(ignoreUnknown = true)
@SuppressWarnings("serial")
@Entity
@Table(name = "Activities")
@JsonDeserialize(using = ActivityDeserializer.class)
public class Activity implements Serializable {
	private long id;

	private Section section;
	private Building building;
	
	private Date beginDate, endDate;
	private Time startTime, endTime;
	private String room, dayIndicator;
	private ActivityState activityState;
	private int frequency;
	private boolean virtual, shared;
	
	private ActivityType activityTypeCode;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ActivityId", unique = true, nullable = false)
	@JsonProperty
	@JsonView(SectionGroupViews.Detailed.class)
	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	@Basic
	@Column(name = "BeginDate", nullable = true, length = 45)
	@JsonProperty
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="YYYY-MM-DD", timezone="PST")
	@JsonView(SectionGroupViews.Detailed.class)
	public Date getBeginDate() {
		return beginDate;
	}

	public void setBeginDate(Date beginDate) {
		this.beginDate = beginDate;
	}

	@Basic
	@Column(name = "EndDate", nullable = true, length = 45)
	@JsonProperty
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="YYYY-MM-DD", timezone="PST")
	@JsonView(SectionGroupViews.Detailed.class)
	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	@Basic
	@Column(name = "StartTime", nullable = true, length = 45)
	@JsonProperty
	@JsonView(SectionGroupViews.Detailed.class)
	public Time getStartTime() {
		return startTime;
	}

	public void setStartTime(Time startTime) {
		this.startTime = startTime;
	}

	@Basic
	@Column(name = "EndTime", nullable = true, length = 45)
	@JsonProperty
	@JsonView(SectionGroupViews.Detailed.class)
	public Time getEndTime() {
		return endTime;
	}

	public void setEndTime(Time endTime) {
		this.endTime = endTime;
	}

	@Basic
	@Column(name = "Room", nullable = true, length = 45)
	@JsonProperty
	@JsonView(SectionGroupViews.Detailed.class)
	public String getRoom() {
		return room;
	}

	public void setRoom(String room) {
		this.room = room;
	}

	/**
	 * dayIndicator is a 7 digit string with each digit representing a day
	 * of the week, the first digit being Sunday and the last being Saturday
	 * Examples:
	 * '0101010' = Monday/Wednesday/Friday
	 * '0010100' = Tuesday/Thursday
	 */
	@Basic
	@Column(name = "DayIndicator", nullable = false, length = 45)
	@JsonProperty
	@JsonView(SectionGroupViews.Detailed.class)
	public String getDayIndicator() {
		return dayIndicator;
	}

	public void setDayIndicator(String dayIndicator) {
		this.dayIndicator = dayIndicator;
	}

	@Basic
	@Column(name = "ActivityState", unique = false, nullable = false)
	@JsonProperty
	@JsonView(SectionGroupViews.Detailed.class)
	public ActivityState getActivityState() {
		return activityState;
	}

	public void setActivityState(ActivityState activityState) {
		this.activityState = activityState;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Sections_SectionId", nullable = false)
	@NotNull
	@JsonIgnore
	public Section getSection() {
		return section;
	}

	public void setSection(Section section) {
		this.section = section;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Buildings_BuildingId", nullable = true)
	@JsonView(SectionGroupViews.Detailed.class)
	public Building getBuilding() {
		return building;
	}

	public void setBuilding(Building building) {
		this.building = building;
	}
	
	@Embedded
	@JsonProperty
	@AttributeOverrides({
		@AttributeOverride(name="ActivityTypeCode", column=@Column(name = "ActivityTypeCode", nullable = true))
	})
	@JsonView(SectionGroupViews.Detailed.class)
	public ActivityType getActivityTypeCode() {
		return activityTypeCode;
	}
	
	public void setActivityTypeCode(ActivityType activityTypeCode) {
		this.activityTypeCode = activityTypeCode;
	}

	/**
	 * Frequency is a simple integer that defaults to 1 indicating a weekly repetition,
	 * could be 2 for every 2 weeks, 3 for every 3 weeks...etc
	 */
	@Basic
	@Column(name = "Frequency", nullable = true)
	@JsonProperty
	@JsonView(SectionGroupViews.Detailed.class)
	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	@Transient
	@JsonView(SectionGroupViews.Detailed.class)
	public long getSectionGroupId() {
		if (this.getSection() == null || this.getSection().getSectionGroup() == null) {
			return 0L;
		}
		return this.getSection().getSectionGroup().getId();
	}

	@Basic
	@Column(name = "IsVirtual", nullable = false)
	@JsonProperty("virtual")
	@JsonView(SectionGroupViews.Detailed.class)
	public boolean isVirtual() {
		return virtual;
	}

	public void setVirtual(boolean virtual) {
		this.virtual = virtual;
	}

	@Transient
	public boolean isDuplicate(Activity activity) {
		// Activity is itself
		if (this.getId() == activity.getId()) {
			return false;
		}

		if (this.getActivityTypeCode().getActivityTypeCode() != activity.getActivityTypeCode().getActivityTypeCode()) {
			return false;
		}
		// Two duplicate activities must match on all of the following properties to be a duplicate
		// However all/none/some of them can be null for any given activity
		if (this.getStartTime() != null) {
			if (this.getStartTime().equals(activity.getStartTime()) == false) {
				return false;
			}
		} else if (activity.getStartTime() != null) {
			if (activity.getStartTime().equals(this.getStartTime()) == false) {
				return false;
			}
		}

		if (this.getEndTime() != null) {
			if (this.getEndTime().equals(activity.getEndTime()) == false) {
				return false;
			}
		} else if (activity.getEndTime() != null) {
			if (activity.getEndTime().equals(this.getEndTime()) == false) {
				return false;
			}
		}

		if (this.getDayIndicator() != null) {
			if (this.getDayIndicator().equals(activity.getDayIndicator()) == false) {
				return false;
			}
		} else if (activity.getDayIndicator() != null) {
			if (activity.getDayIndicator().equals(this.getDayIndicator()) == false) {
				return false;
			}
		}

		if (this.getRoom() != null) {
			if (this.getRoom().equals(activity.getRoom()) == false) {
				return false;
			}
		} else if (activity.getRoom() != null) {
			if (activity.getRoom().equals(this.getRoom()) == false) {
				return false;
			}
		}

		if (this.getBuilding() != null) {
			if (this.getBuilding().equals(activity.getBuilding()) == false) {
				return false;
			}
		} else if (activity.getBuilding() != null) {
			if (activity.getBuilding().equals(this.getBuilding()) == false) {
				return false;
			}
		}

		return true;
	}

	@Basic
	@Column(name = "Shared", nullable = false)
	@JsonProperty("shared")
	@JsonView(SectionGroupViews.Detailed.class)
	public boolean isShared() {
		return shared;
	}

	public void setShared(boolean shared) {
		this.shared = shared;
	}
}