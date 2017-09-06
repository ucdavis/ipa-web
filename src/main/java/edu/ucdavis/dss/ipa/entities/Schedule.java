package edu.ucdavis.dss.ipa.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import edu.ucdavis.dss.ipa.api.views.ScheduleViews;
import edu.ucdavis.dss.ipa.api.views.WorkgroupViews;

@SuppressWarnings("serial")
@Entity
@Table(
		name = "Schedules",
		uniqueConstraints = {@UniqueConstraint(columnNames={"WorkgroupId", "Year"})}
)
@JsonAutoDetect(creatorVisibility = JsonAutoDetect.Visibility.NONE,
	fieldVisibility = JsonAutoDetect.Visibility.NONE,
	getterVisibility = JsonAutoDetect.Visibility.NONE,
	isGetterVisibility = JsonAutoDetect.Visibility.NONE,
	setterVisibility = JsonAutoDetect.Visibility.NONE)
public class Schedule implements Serializable {
	private long id;
	private long year;
	private boolean importing;
	private String secretToken;
	private Workgroup workgroup;
	private List<Course> courses = new ArrayList<Course>();
	private List<TeachingCallResponse> teachingCallResponses = new ArrayList<TeachingCallResponse>();
	private List<StudentSupportCallResponse> studentSupportCallResponses = new ArrayList<StudentSupportCallResponse>();
	private List<InstructorSupportCallResponse> instructorSupportCallResponses = new ArrayList<InstructorSupportCallResponse>();

	private List<TeachingCallReceipt> teachingCallReceipts = new ArrayList<TeachingCallReceipt>();
	private List<TeachingAssignment> teachingAssignments = new ArrayList<>();
	private List<SupportAssignment> supportAssignments = new ArrayList<>();

	private String supportStaffSupportCallReviewOpen;
	private String instructorSupportCallReviewOpen;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "Id", unique = true, nullable = false)
	@JsonProperty("id")
	@JsonView({WorkgroupViews.Summary.class,ScheduleViews.Summary.class})
	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	/**
	 * Returns the academic year of the schedule as the first year, e.g. 2016-17 is "2016".
	 * 
	 * @return
	 */
	@Column(name = "Year", unique = false, nullable = false)
	@JsonProperty("year")
	@JsonView({WorkgroupViews.Summary.class,ScheduleViews.Summary.class})
	public long getYear() {
		return this.year;
	}

	public void setYear(long year) {
		this.year = year;
	}

	@Column(name = "Importing", unique = false, nullable = false)
	@JsonProperty("isImporting")
	@JsonView({WorkgroupViews.Summary.class,ScheduleViews.Summary.class})
	public boolean isImporting() {
		return importing;
	}

	public void setImporting(boolean importing) {
		this.importing = importing;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "WorkgroupId", nullable = false)
	@NotNull
	@JsonIgnore
	public Workgroup getWorkgroup() {
		return this.workgroup;
	}

	public void setWorkgroup(Workgroup workgroup)
	{
		this.workgroup = workgroup;
		if (!workgroup.getSchedules().contains(this)) {
			workgroup.getSchedules().add(this);
		}
	}

	public String getSecretToken() {
		return secretToken;
	}

	public void setSecretToken(String secretToken) {
		this.secretToken = secretToken;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "schedule", cascade = {CascadeType.ALL})
	@JsonIgnore
	public List<Course> getCourses() {
		return courses;
	}

	public void setCourses(List<Course> courses) {
		this.courses = courses;
	}

	@JsonIgnore
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
	public List<TeachingCallResponse> getTeachingCallResponses() {
		return teachingCallResponses;
	}

	public void setTeachingCallResponses(List<TeachingCallResponse> teachingCallResponses) {
		this.teachingCallResponses = teachingCallResponses;
	}

	@JsonIgnore
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
	public List<TeachingCallReceipt> getTeachingCallReceipts() {
		return teachingCallReceipts;
	}

	public void setTeachingCallReceipts(List<TeachingCallReceipt> teachingCallReceipts) {
		this.teachingCallReceipts = teachingCallReceipts;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "schedule")
	@JsonIgnore
	public List<TeachingAssignment> getTeachingAssignments() {
		return teachingAssignments;
	}

	public void setTeachingAssignments(List<TeachingAssignment> teachingAssignments) {
		this.teachingAssignments = teachingAssignments;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "schedule")
	@JsonIgnore
	public List<SupportAssignment> getSupportAssignments() {
		return supportAssignments;
	}

	public void setSupportAssignments(List<SupportAssignment> supportAssignments) {
		this.supportAssignments = supportAssignments;
	}

	/**
	 * Terms are expected to be sorted ['01','02','03','04','05','06','07','08','09','10']
	 */
	@JsonProperty
	public String getSupportStaffSupportCallReviewOpen() {
		return supportStaffSupportCallReviewOpen;
	}

	public void setSupportStaffSupportCallReviewOpen(String supportStaffSupportCallReviewOpen) {
		this.supportStaffSupportCallReviewOpen = supportStaffSupportCallReviewOpen;
	}

	/**
	 * Terms are expected to be sorted ['01','02','03','04','05','06','07','08','09','10']
	 */
	@JsonProperty
	public String getInstructorSupportCallReviewOpen() {
		return instructorSupportCallReviewOpen;
	}

	public void setInstructorSupportCallReviewOpen(String instructorSupportCallReviewOpen) {
		this.instructorSupportCallReviewOpen = instructorSupportCallReviewOpen;
	}

	@JsonIgnore
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
	public List<StudentSupportCallResponse> getStudentSupportCallResponses() {
		return studentSupportCallResponses;
	}

	public void setStudentSupportCallResponses(List<StudentSupportCallResponse> studentSupportCallResponses) {
		this.studentSupportCallResponses = studentSupportCallResponses;
	}

	@JsonIgnore
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
	public List<InstructorSupportCallResponse> getInstructorSupportCallResponses() {
		return instructorSupportCallResponses;
	}

	public void setInstructorSupportCallResponses(List<InstructorSupportCallResponse> instructorSupportCallResponses) {
		this.instructorSupportCallResponses = instructorSupportCallResponses;
	}

	/**
	 * Given a term, toggle the relevant flag in the termsBlob
	 * @param term
     */
	public void toggleSupportStaffSupportCallReview(String term) {
		int index = Integer.parseInt(term) - 1;

		String currentTermsBlob = this.getSupportStaffSupportCallReviewOpen();
		long currentTermValue = Integer.parseInt(String.valueOf(currentTermsBlob.charAt(index)));
		long newTermValue = 1;

		if (currentTermValue == 1) {
			newTermValue = 0;
		}

		String beforeIndex = currentTermsBlob.substring(0,index);
		String afterIndex = currentTermsBlob.substring(index + 1);
		String newTermsBlob = beforeIndex + newTermValue + afterIndex;

		this.setSupportStaffSupportCallReviewOpen(newTermsBlob);
	}

	public void toggleInstructorSupportCallReview(String term) {
		int index = Integer.parseInt(term) - 1;

		String currentTermsBlob = this.getInstructorSupportCallReviewOpen();

		long currentTermValue = Integer.parseInt(String.valueOf(currentTermsBlob.charAt(index)));
		long newTermValue = 1;

		if (currentTermValue == 1) {
			newTermValue = 0;
		}

		String beforeIndex = currentTermsBlob.substring(0,index);
		String afterIndex = currentTermsBlob.substring(index + 1);
		String newTermsBlob = beforeIndex + newTermValue + afterIndex;

		this.setInstructorSupportCallReviewOpen(newTermsBlob);
	}
}