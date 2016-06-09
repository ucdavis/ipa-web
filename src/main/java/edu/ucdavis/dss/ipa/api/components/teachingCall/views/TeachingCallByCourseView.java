package edu.ucdavis.dss.ipa.api.components.teachingCall.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.ucdavis.dss.ipa.entities.CourseOffering;
import edu.ucdavis.dss.ipa.entities.CourseOfferingGroup;
import edu.ucdavis.dss.ipa.entities.TeachingPreference;
import edu.ucdavis.dss.ipa.entities.Term;

public class TeachingCallByCourseView {
	private long id;
	private String description;
	private HashMap<String,TeachingCallCourseOfferingView> courseOfferings = new HashMap<String,TeachingCallCourseOfferingView>();
	private HashMap<String,List<TeachingCallTeachingPreferenceView>> teachingPreferences = new HashMap<String,List<TeachingCallTeachingPreferenceView>>();

	public TeachingCallByCourseView(CourseOfferingGroup cog, List<TeachingPreference> cogTeachingPreferences) {
		setId(cog.getId());
		setDescription(cog.getDescription());
		setCourseOfferings(cog.getCourseOfferings());
		setTeachingPreferences(cogTeachingPreferences);
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

	public HashMap<String,List<TeachingCallTeachingPreferenceView>> getTeachingPreferences() {
		return teachingPreferences;
	}

	public HashMap<String,TeachingCallCourseOfferingView> getCourseOfferings() {
		return courseOfferings;
	}

	public void setCourseOfferings(List<CourseOffering> courseOfferings) {
		for (CourseOffering courseOffering: courseOfferings) {
			// Get the 2 digit termCode
			String term = Term.getTwoDigitTermCode(courseOffering.getTermCode());

			this.courseOfferings.put(term, new TeachingCallCourseOfferingView(courseOffering));
		}
	}

	public void setTeachingPreferences(List<TeachingPreference> cogTeachingPreferences) {
		for (TeachingPreference teachingPreference: cogTeachingPreferences) {
			// Get the 2 digit termCode
			String term = Term.getTwoDigitTermCode(teachingPreference.getTermCode());

			// Initialize teachingPreferences for the term if null, then append to it
			if (this.teachingPreferences.get(term) == null) {
				this.teachingPreferences.put(term, new ArrayList<TeachingCallTeachingPreferenceView>());
			}
			List<TeachingCallTeachingPreferenceView> termTeachingPreferences = this.teachingPreferences.get(term);
			termTeachingPreferences.add(new TeachingCallTeachingPreferenceView(teachingPreference));
		}
	}

}
