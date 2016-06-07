package edu.ucdavis.dss.ipa.web.components.summary.views;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import edu.ucdavis.dss.ipa.config.SettingsConfiguration;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.TeachingCall;

public class SummaryActivitiesScheduleView {
	private long id, year;
	private List<SummaryActivitiesTermView> summaryActivitiesTermViews = new ArrayList<SummaryActivitiesTermView>();
	private List<SummaryActivitiesActivityView> activities = new ArrayList<SummaryActivitiesActivityView>();

	public long getId() {
		return this.id;
	}

	public void setId(Schedule schedule) {
		this.id = schedule.getId();
	}

	public long getYear() {
		return this.year;
	}

	public void setYear(Schedule schedule) {
		this.year = schedule.getYear();
	}

	@JsonProperty("terms")
	public List<SummaryActivitiesTermView> getSummaryActivitiesTermViews() {
		return this.summaryActivitiesTermViews;
	}

	public void setSummaryActivitiesTermViews(List<SummaryActivitiesTermView> summaryActivitiesTermViews) {
		this.summaryActivitiesTermViews = summaryActivitiesTermViews;
	}

	public List<SummaryActivitiesActivityView> getActivities() {
		return this.activities;
	}

	public void setActivities(Schedule schedule) {
		List<SummaryActivitiesActivityView> activities = new ArrayList<SummaryActivitiesActivityView>();

		// Create activities for teaching call beginnings
		for (TeachingCall teachingCall : schedule.getTeachingCalls()) {
			Date startDate = teachingCall.getStartDate();

			String teachingCallUrl = SettingsConfiguration.getURL() + "/teachingCalls/#/" + teachingCall.getId();

			if (teachingCall.isSentToSenate()) {
				String description = "Teaching Call " + schedule.getYear() + " sent to Senate instructors. " + teachingCallUrl;
				activities.add(new SummaryActivitiesActivityView(description, startDate));
			}

			if (teachingCall.isSentToFederation()) {
				String description = "Teaching Call " + schedule.getYear() + " sent to Federation instructors. " + teachingCallUrl;
				activities.add(new SummaryActivitiesActivityView(description, startDate));
			}
		}

		this.activities = activities;
	}
}
