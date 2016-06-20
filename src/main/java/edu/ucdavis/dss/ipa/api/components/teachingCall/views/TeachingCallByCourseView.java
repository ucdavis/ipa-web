package edu.ucdavis.dss.ipa.api.components.teachingCall.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.entities.TeachingAssignment;
import edu.ucdavis.dss.ipa.entities.Term;

public class TeachingCallByCourseView {
	private long id;
	private String description;
	private HashMap<String,TeachingCallSectionGroupView> sectionGroups = new HashMap<String,TeachingCallSectionGroupView>();
	private HashMap<String,List<TeachingCallTeachingAssignmentView>> teachingPreferences = new HashMap<String,List<TeachingCallTeachingAssignmentView>>();

	public TeachingCallByCourseView(Course course, List<TeachingAssignment> teachingAssignments) {
		setId(course.getId());
		setDescription(course.getTitle());
		setSectionGroups(course.getSectionGroups());
		setTeachingPreferences(teachingAssignments);
	}

	public Long getId() {
		return this.id;
	}

	private void setId(Long courseOfferingGroupId) {
		this.id = courseOfferingGroupId;
	}

	public String getDescription() {
		return this.description;
	}

	private void setDescription(String description) {
		this.description = description;
	}

	public HashMap<String,List<TeachingCallTeachingAssignmentView>> getTeachingPreferences() {
		return teachingPreferences;
	}

	public HashMap<String,TeachingCallSectionGroupView> getSectionGroups() {
		return sectionGroups;
	}

	public void setSectionGroups(List<SectionGroup> sectionGroups) {
		for (SectionGroup sectionGroup: sectionGroups) {
			// Get the 2 digit termCode
			String term = Term.getTwoDigitTermCode(sectionGroup.getTermCode());

			this.sectionGroups.put(term, new TeachingCallSectionGroupView(sectionGroup));
		}
	}

	public void setTeachingPreferences(List<TeachingAssignment> teachingAssignments) {
		for (TeachingAssignment teachingAssignment: teachingAssignments) {
			// Get the 2 digit termCode
			String term = Term.getTwoDigitTermCode(teachingAssignment.getTermCode());

			// Initialize teachingPreferences for the term if null, then append to it
			if (this.teachingPreferences.get(term) == null) {
				this.teachingPreferences.put(term, new ArrayList<TeachingCallTeachingAssignmentView>());
			}
			List<TeachingCallTeachingAssignmentView> termTeachingPreferences = this.teachingPreferences.get(term);
			termTeachingPreferences.add(new TeachingCallTeachingAssignmentView(teachingAssignment));
		}
	}

}
