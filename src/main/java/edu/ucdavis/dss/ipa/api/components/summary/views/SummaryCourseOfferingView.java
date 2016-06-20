package edu.ucdavis.dss.ipa.api.components.summary.views;

import java.util.ArrayList;
import java.util.List;

import edu.ucdavis.dss.ipa.entities.Activity;
import edu.ucdavis.dss.ipa.entities.Section;
import edu.ucdavis.dss.ipa.entities.SectionGroup;

public class SummaryCourseOfferingView {
	private String title, subjectCode, termCode, courseNumber, sequencePattern;
	private long id, enrollment, maxEnrollment;
	private List<Activity> activities = new ArrayList<Activity>();

	public SummaryCourseOfferingView(SectionGroup sectionGroup) {
		setId(sectionGroup);
		setTitle(sectionGroup.getCourse().getTitle());
		setSubjectCode(sectionGroup.getCourse().getSubjectCode());
		setTermCode(sectionGroup.getTermCode());
		setCourseNumber(sectionGroup.getCourse().getCourseNumber());
		setActivities(sectionGroup);
		setEnrollment(sectionGroup);
		setMaxEnrollment(sectionGroup);
		setSequencePattern(sectionGroup);
	}

	public long getId() {
		return this.id;
	}

	public void setId(SectionGroup sectionGroup) {
		this.id = sectionGroup.getId();
	}

	public void setEnrollment(SectionGroup sectionGroup) {
		long enrollment = 0;

		//TODO: Source?
//		for (Section section : sectionGroup.getSections()) {
//			for (CensusSnapshot censusSnapshot : section.getCensusSnapshots()) {
//				if ("CURRENT".equals(censusSnapshot.getSnapshotCode())) {
//					enrollment += censusSnapshot.getCurrentEnrollmentCount();
//				}
//			}
//		}
		this.enrollment = enrollment;
	}

	public long getEnrollment() {
		return this.enrollment;
	}

	public void setMaxEnrollment(SectionGroup sectionGroup) {
		long maxEnrollment = 0;

		for (Section section : sectionGroup.getSections()) {
			maxEnrollment += section.getSeats();
		}
		this.maxEnrollment = maxEnrollment;
	}

	public long getMaxEnrollment() {
		return this.maxEnrollment;
	}

	public String getTitle() {
		return this.title;
	}
	
	private void setTitle(String title) {
		this.title = title;
	}

	public String getTermCode() {
		return this.termCode;
	}
	
	private void setTermCode(String termCode) {
		this.termCode = termCode;
	}

	public String getCourseNumber() {
		return this.courseNumber;
	}
	
	private void setCourseNumber(String courseNumber) {
		this.courseNumber = courseNumber;
	}

	public String getSubjectCode() {
		return this.subjectCode;
	}
	
	private void setSubjectCode(String subjectCode) {
		this.subjectCode = subjectCode;
	}

	public List<Activity> getActivities() {
		return this.activities;
	}

	private void setActivities(SectionGroup sectionGroup) {
		List<Activity> activities = new ArrayList<Activity>();

		// Whitelist of activity types to serialize
		List<Character> activityTypeFilter = new ArrayList<Character>();
		activityTypeFilter.add('A');

		for (Section section : sectionGroup.getSections() ) {
			for (Activity activity : section.getActivities() ) {

				// Ensure activity is of a type that we are interested in
				if (activityTypeFilter.contains(activity.getActivityTypeCode().getActivityTypeCode()) ) {
					boolean activityAlreadyExists = false;

					for (Activity addedActivity : activities) {

						// Ensure only one instance of that activityTypeCode from the sectionGroup has been added
						if (addedActivity.getSectionGroupId() == activity.getSectionGroupId() &&
							addedActivity.getActivityTypeCode().getActivityTypeCode() == activity.getActivityTypeCode().getActivityTypeCode()) {
							activityAlreadyExists = true;
						}
					}

					if (!activityAlreadyExists) {
						activities.add(activity);
					}
				}
			}
		}
		
		this.activities = activities;
	}

	public String getSequencePattern() {
		return this.sequencePattern;
	}

	private void setSequencePattern(SectionGroup sectionGroup) {
		this.sequencePattern = sectionGroup.getCourse().getSequencePattern();
	}
}