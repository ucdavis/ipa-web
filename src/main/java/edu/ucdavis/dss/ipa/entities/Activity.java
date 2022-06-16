package edu.ucdavis.dss.ipa.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import edu.ucdavis.dss.ipa.api.deserializers.ActivityDeserializer;
import edu.ucdavis.dss.ipa.entities.enums.ActivityState;
import edu.ucdavis.dss.ipa.entities.validation.ValidActivity;

import java.time.format.DateTimeFormatter;
import javax.persistence.*;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.sql.Time;
import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
@SuppressWarnings("serial")
@Entity
@Table(name = "Activities")
@JsonDeserialize(using = ActivityDeserializer.class)
@ValidActivity
public class Activity extends BaseEntity {
    private long id;

    private Section section;
    private SectionGroup sectionGroup;

    private Date beginDate;
    private Date endDate;
    private Time startTime, endTime;
    private String dayIndicator, bannerLocation;
    private ActivityState activityState;
    private int frequency;
    private Long category;
    private boolean virtual;
    private boolean syncLocation;
    private Location location;
    private ActivityType activityTypeCode;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", unique = true, nullable = false)
    @JsonProperty
    public long getId() {
    	return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }
	
    @Basic
    @Column(name = "BeginDate", nullable = true, length = 45)
    @JsonProperty
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "YYYY-MM-DD", timezone = "PST")
    public Date getBeginDate() {
		return beginDate;
	}

	public void setBeginDate(Date beginDate) {
		this.beginDate = beginDate;
	}

	@Basic
	@Column(name = "EndDate", nullable = true, length = 45)
	@JsonProperty
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern = "YYYY-MM-DD", timezone = "PST")
	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	@Basic
	@Column(name = "StartTime", nullable = true, length = 45)
	@JsonProperty
	public Time getStartTime() {
		return startTime;
	}

	public void setStartTime(Time startTime) {
		this.startTime = startTime;
	}

	@Basic
	@Column(name = "EndTime", nullable = true, length = 45)
	@JsonProperty
	public Time getEndTime() {
		return endTime;
	}

	public void setEndTime(Time endTime) {
		this.endTime = endTime;
	}

	@Transient
	public String getTimeDescription() {
		if (this.startTime != null && this.endTime != null) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a");
			return this.startTime.toLocalTime().format(formatter) + " - " + this.endTime.toLocalTime().format(formatter);
		} else {
			return null;
		}
	}

	@Basic
	@Column(name = "category", nullable = true)
	@JsonProperty
	public Long getCategory() {
		return this.category;
	}

	public void setCategory(Long category) {
		this.category = category;
	}

	@Transient
	@JsonProperty
	public boolean isPrimary() {
		if (this.category == null) {
			return false;
		}

		return (this.category == 1);
	}

	@Transient
	public boolean isLecture() {
		if (this.activityTypeCode.getActivityTypeCode() == '0' ||
			this.activityTypeCode.getActivityTypeCode() == 'A' ||
			this.activityTypeCode.getActivityTypeCode() == 'B' ||
			this.activityTypeCode.getActivityTypeCode() == 'L') {
			return true;
		}
		return false;
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
	public String getDayIndicator() {
		return dayIndicator;
	}

	public void setDayIndicator(String dayIndicator) {
		this.dayIndicator = dayIndicator;
	}

	@JsonIgnore
	@Transient
	public String getDayIndicatorDescription() {

		return Activity.getDayIndicatorDescription(this.dayIndicator);
	}

	@Transient
	public static String getDayIndicatorDescription(String dayIndicator) {

		String description = "";

		for (int i = 0; i < dayIndicator.length(); i++) {
			int intValue = dayIndicator.charAt(i) - '0';

			if (1 == intValue) {
				switch(i) {
					case 0:
						description += "U";
						break;
					case 1:
						description += "M";
						break;
					case 2:
						description += "T";
						break;
					case 3:
						description += "W";
						break;
					case 4:
						description += "R";
						break;
					case 5:
						description += "F";
						break;
					case 6:
						description += "S";
						break;
				}
			}
		}

		return description;
	}

	@Basic
	@Column(name = "ActivityState", unique = false, nullable = false)
	@JsonProperty
	public ActivityState getActivityState() {
		return activityState;
	}

	public void setActivityState(ActivityState activityState) {
		this.activityState = activityState;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SectionId", nullable = true)
	@JsonIgnore
	public Section getSection() {
		return section;
	}

	public void setSection(Section section) {
		this.section = section;
	}
	
	public String getBannerLocation() {
		return bannerLocation;
	}

	public void setBannerLocation(String bannerLocation) {
		this.bannerLocation = bannerLocation;
	}

	@Embedded
	@JsonProperty
	@AttributeOverrides({
		@AttributeOverride(name="ActivityTypeCode", column=@Column(name = "ActivityTypeCode", nullable = true))
	})
	public ActivityType getActivityTypeCode() {
		return activityTypeCode;
	}
	
	public void setActivityTypeCode(ActivityType activityTypeCode) {
		this.activityTypeCode = activityTypeCode;
	}
	public void setActivityTypeCode(char code) {
		this.activityTypeCode = new ActivityType(code);
	}

	/**
	 * Frequency is a simple integer that defaults to 1 indicating a weekly repetition,
	 * could be 2 for every 2 weeks, 3 for every 3 weeks...etc
	 */
	@Basic
	@Column(name = "Frequency", nullable = true)
	@JsonProperty
	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	@Transient
	@JsonProperty("sectionId")
	public long getSectionIdentification() {
		if (this.getSection() == null) {
			return 0L;
		}
		return this.getSection().getId();
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SectionGroupId", nullable = true)
	@JsonIgnore
	public SectionGroup getSectionGroup() {
		return sectionGroup;
	}

	public void setSectionGroup(SectionGroup sectionGroup) {
		this.sectionGroup = sectionGroup;
	}

	@Transient
	@JsonProperty("sectionGroupId")
	public long getSectionGroupIdentification() {
		if (this.getSectionGroup() != null) {
			return this.getSectionGroup().getId();
		} else if (this.getSection() != null && this.getSection().getSectionGroup() != null) {
			return this.getSection().getSectionGroup().getId();
		} else {
			return 0L;
		}
	}

	@Basic
	@Column(name = "IsVirtual", nullable = false)
	@JsonProperty("virtual")
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

		if (this.getBannerLocation() != null) {
			if (this.getBannerLocation().equals(activity.getBannerLocation()) == false) {
				return false;
			}
		} else if (activity.getBannerLocation() != null) {
			if (activity.getBannerLocation().equals(this.getBannerLocation()) == false) {
				return false;
			}
		}

		// TODO: Check also for Location
		return true;
	}

	@Transient
	@JsonProperty("shared")
	public boolean isShared() {
		return this.getSectionGroup() != null;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "LocationId", nullable = false)
	@JsonIgnore
	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	@Transient
	@JsonProperty("locationId")
	public long getLocationIdentification() {
		if (this.getLocation() == null) {
			return 0L;
		}
		return this.getLocation().getId();
	}

	@Transient
	@JsonProperty("locationDescription")
	public String getLocationDescription() {
		if (this.bannerLocation != null && this.bannerLocation.length() > 0 && (this.bannerLocation.equals("null null") == false)) {
			return this.bannerLocation;
		} else if (this.location != null) {
			return this.location.getDescription();
		}

		return null;
	}

	/**
	 * Flag defaults to true, will allow banner location data to override local data.
	 * If a user sets a location manually, the flag should be set to false.
	 * If a user in the banner diff view accepts a banner location value, the flag should be set to true.
	 * @return
     */
	@JsonIgnore
	public boolean isSyncLocation() {
		return syncLocation;
	}

	public void setSyncLocation(boolean syncLocation) {
		this.syncLocation = syncLocation;
	}

	@JsonIgnore
	@Transient
	public String getActivityTypeCodeDescription() {
		return this.activityTypeCode.GetDescription();
	}
}
