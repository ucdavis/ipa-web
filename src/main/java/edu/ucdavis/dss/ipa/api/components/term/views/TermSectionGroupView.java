package edu.ucdavis.dss.ipa.api.components.term.views;

import java.util.ArrayList;
import java.util.List;

import edu.ucdavis.dss.ipa.entities.Activity;
import edu.ucdavis.dss.ipa.entities.Section;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.entities.TeachingAssignment;

public class TermSectionGroupView {
	private long id, courseOfferingId;
	private String sequencePattern;
	private List<TermSectionView> sections = new ArrayList<TermSectionView>();
	private List<TermTeachingAssignmentView> teachingAssignments = new ArrayList<TermTeachingAssignmentView>();
	private List<TermActivityView> sharedActivities = new ArrayList<TermActivityView>();
	private List<TermInstructorView> instructors = new ArrayList<TermInstructorView>();

	public TermSectionGroupView(SectionGroup sectionGroup) {
		if (sectionGroup == null) return;
		setId(sectionGroup.getId());
		setSharedActivities(sectionGroup);
		setSections(sectionGroup.getSections());
		setTeachingAssignments(sectionGroup.getTeachingAssignments());
		setSequencePattern(sectionGroup.getSequencePattern());
		setCourseOfferingId(sectionGroup.getCourseOffering().getId());
		setInstructors(sectionGroup);
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public List<TermSectionView> getSections() {
		return sections;
	}

	public void setSections(List<Section> sections) {
		for (Section section : sections) {
			this.sections.add(new TermSectionView(section));
		}
	}

	public List<TermTeachingAssignmentView> getTeachingAssignments() {
		return teachingAssignments;
	}

	public void setTeachingAssignments(List<TeachingAssignment> teachingAssignments) {
		List<TermTeachingAssignmentView> termTeachingAssignments = new ArrayList<TermTeachingAssignmentView>();
		for (TeachingAssignment teachingAssignment : teachingAssignments) {
			termTeachingAssignments.add(new TermTeachingAssignmentView(teachingAssignment));
		}
		this.teachingAssignments = termTeachingAssignments;
	}

	public String getSequencePattern() {
		return this.sequencePattern;
	}

	public void setSequencePattern(String sequencePattern) {
		this.sequencePattern = sequencePattern;
	}

	public List<TermActivityView> getSharedActivities() {
		return this.sharedActivities;
	}

	public void setSharedActivities(SectionGroup sectionGroup) {
		List<Activity> sharedActivities = new ArrayList<Activity>();

		for (Section section: sectionGroup.getSections()) {
			List<Activity> toBeRemoved = new ArrayList<Activity>();

			for (Activity activity: section.getActivities()) {
				if (activity.isShared()) {
					boolean alreadyAdded = false;
					toBeRemoved.add(activity);

					for (Activity activityTarget: sharedActivities) {
						if (activityTarget.isDuplicate(activity) || activityTarget.getId() == activity.getId()) {
							alreadyAdded = true;
							break;
						}
					}
					if (alreadyAdded == false) {
						sharedActivities.add(activity);
						this.sharedActivities.add(new TermActivityView(activity));
					}
				}
			}
			section.getActivities().removeAll(toBeRemoved);
		}
	}

	public boolean isSharedActivity(SectionGroup sectionGroup, Activity activityTarget) {
		for (Section section: sectionGroup.getSections()) {
			for (Activity activity: section.getActivities()) {
				if (activityTarget.isDuplicate(activity)) {
					return true;
				}
			}
		}
		return false;
	}

	public long getCourseOfferingId() {
		return courseOfferingId;
	}

	public void setCourseOfferingId(long courseOfferingId) {
		this.courseOfferingId = courseOfferingId;
	}

	public List<TermInstructorView> getInstructors() {
		return instructors;
	}

	public void setInstructors(SectionGroup sectionGroup) {
		boolean IS_ASSIGNED = true;
		for (TeachingAssignment ta: sectionGroup.getTeachingAssignments()) {
			TermInstructorView taInstructor = new TermInstructorView(ta.getInstructor(), IS_ASSIGNED);
			if (this.instructors.contains(taInstructor) == false) {
				this.instructors.add(taInstructor);
			}
		}
	}
}
