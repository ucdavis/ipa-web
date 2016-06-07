package edu.ucdavis.dss.ipa.entities;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import edu.ucdavis.dss.dw.dto.DwCensusSnapshot;
import edu.ucdavis.dss.ipa.web.views.CourseOfferingGroupViews;
import edu.ucdavis.dss.ipa.web.views.ScheduleViews;

@SuppressWarnings("serial")
@Entity
@Table(name = "CensusSnapshots")
@JsonAutoDetect(creatorVisibility = JsonAutoDetect.Visibility.NONE,
fieldVisibility = JsonAutoDetect.Visibility.NONE,
getterVisibility = JsonAutoDetect.Visibility.NONE,
isGetterVisibility = JsonAutoDetect.Visibility.NONE,
setterVisibility = JsonAutoDetect.Visibility.NONE)
public class CensusSnapshot implements Serializable {
	private long id;
	private Section section;
	private String snapshotCode; // e.g. "REGULAR_DROP"
	private Integer currentAvailableSeatCount;
	private Integer currentEnrollmentCount, maxEnrollmentCount, studentCount;
	private Integer waitCount, waitCapacityCount;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "CensusSnapshotId", unique = true, nullable = false)
	@JsonProperty
	@JsonView(CourseOfferingGroupViews.Detailed.class)
	public long getId()
	{
		return this.id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	@Basic(optional = false)
	@Column(name = "CurrentAvailableSeatCount", nullable = false)
	@NotNull
	@JsonProperty
	@JsonView(CourseOfferingGroupViews.Detailed.class)
	public Integer getCurrentAvailableSeatCount() {
		return currentAvailableSeatCount;
	}

	public void setCurrentAvailableSeatCount(Integer currentAvailableSeatCount) {
		this.currentAvailableSeatCount = currentAvailableSeatCount;
	}

	@Basic(optional = false)
	@Column(name = "SnapshotCode", nullable = false, length = 12)
	@NotNull
	@JsonProperty
	@JsonView({CourseOfferingGroupViews.Detailed.class,ScheduleViews.Detailed.class})
	public String getSnapshotCode()
	{
		return this.snapshotCode;
	}

	public void setSnapshotCode(String snapshotCode)
	{
		this.snapshotCode = snapshotCode;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SectionId", nullable = false)
	@NotNull
	public Section getSection() {
		return this.section;
	}

	public void setSection(Section section) {
		this.section = section;
	}

	@Basic(optional = false)
	@Column(name = "CurrentEnrollmentCount", nullable = false)
	@NotNull
	@JsonProperty
	@JsonView(CourseOfferingGroupViews.Detailed.class)
	public Integer getCurrentEnrollmentCount() {
		return currentEnrollmentCount;
	}

	public void setCurrentEnrollmentCount(Integer currentEnrollmentCount) {
		this.currentEnrollmentCount = currentEnrollmentCount;
	}

	@Basic(optional = false)
	@Column(name = "MaxEnrollmentCount", nullable = false)
	@NotNull
	@JsonProperty
	@JsonView(CourseOfferingGroupViews.Detailed.class)
	public Integer getMaxEnrollmentCount() {
		return maxEnrollmentCount;
	}

	public void setMaxEnrollmentCount(Integer maxEnrollmentCount) {
		this.maxEnrollmentCount = maxEnrollmentCount;
	}

	@Basic(optional = false)
	@Column(name = "StudentCount", nullable = false)
	@NotNull
	@JsonProperty
	@JsonView(CourseOfferingGroupViews.Detailed.class)
	public Integer getStudentCount() {
		return studentCount;
	}

	public void setStudentCount(Integer studentCount) {
		this.studentCount = studentCount;
	}

	@Basic(optional = false)
	@Column(name = "WaitCount", nullable = false)
	@NotNull
	@JsonProperty
	@JsonView(CourseOfferingGroupViews.Detailed.class)
	public Integer getWaitCount() {
		return waitCount;
	}

	public void setWaitCount(Integer waitCount) {
		this.waitCount = waitCount;
	}

	@Basic(optional = false)
	@Column(name = "WaitCapacityCount", nullable = false)
	@NotNull
	@JsonProperty
	@JsonView(CourseOfferingGroupViews.Detailed.class)
	public Integer getWaitCapacityCount() {
		return waitCapacityCount;
	}

	public void setWaitCapacityCount(Integer waitCapacityCount) {
		this.waitCapacityCount = waitCapacityCount;
	}

	@Override
	public String toString() {
		return String.format("CensusSnapshot[id=%d]");
	}

	/**
	 * Support comparing against DwCensusSnapshot
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) { return false; }
		
		// DwCensusSnapshot support (CRN and SnapshotCode must match. Ignore all other values.)
		if(obj.getClass() == DwCensusSnapshot.class) {
			DwCensusSnapshot dwSnapshot = (DwCensusSnapshot)obj;
			
			if(dwSnapshot.getCrn() == null) {
				try {
					throw new Exception("DwSnapshot cannot have NULL CRN for equals().");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(dwSnapshot.getCrn() != this.getSection().getCrn()) { return false; }
			if(dwSnapshot.getSnapshotCode() != this.getSnapshotCode()) { return false; }

			return true;
		}

		// Revert to default 'Object'-based behavior
		return ((Object)this).equals(obj);
	}
}
