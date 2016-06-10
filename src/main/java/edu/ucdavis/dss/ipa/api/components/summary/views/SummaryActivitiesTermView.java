package edu.ucdavis.dss.ipa.api.components.summary.views;

import java.util.ArrayList;
import java.util.List;

import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.Term;

public class SummaryActivitiesTermView {
	private String termCode;
	private List<SummaryActivitiesActivityView> activities = new ArrayList<SummaryActivitiesActivityView>();

	public SummaryActivitiesTermView(Schedule schedule, String termCode, List<Term> termReferences) {
		setTermCode(termCode);
		setActivities(schedule, termCode, termReferences);
	}

	public String getTermCode() {
		return this.termCode;
	}

	private void setTermCode(String termCode) {
		this.termCode = termCode;
	}

	public List<SummaryActivitiesActivityView> getActivities() {
		return this.activities;
	}

	private void setActivities(Schedule schedule, String termCode, List<Term> termReferences) {
		List<SummaryActivitiesActivityView> activities = new ArrayList<SummaryActivitiesActivityView>();

		// Get term data for the current term of interest
		Term activityTerm = null;

		for (Term term : termReferences) {
			if (term.getTermCode().equals(termCode) ) {
				activityTerm = term;
			}
		}

		// ActivityLog cannot describe activities in a term without knowing the start and end dates of that term.
		if(activityTerm == null || activityTerm.getStartDate() == null) {
			return;
		}

		this.activities = activities;
	}
}
