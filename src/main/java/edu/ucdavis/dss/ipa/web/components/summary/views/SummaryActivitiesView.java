package edu.ucdavis.dss.ipa.web.components.summary.views;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SummaryActivitiesView {
	private List<SummaryActivitiesScheduleView> summaryActivitiesScheduleViews = new ArrayList<SummaryActivitiesScheduleView>();

	@JsonProperty("schedules")
	public List<SummaryActivitiesScheduleView> getSummaryActivitiesScheduleViews() {
		return this.summaryActivitiesScheduleViews;
	}
	
	public void setSummaryActivitiesScheduleViews(List<SummaryActivitiesScheduleView> summaryActivitiesScheduleViews) {
		this.summaryActivitiesScheduleViews = summaryActivitiesScheduleViews;
	}
}
