package edu.ucdavis.dss.ipa.web.components.summary.views;

import java.sql.Date;

public class SummaryActivitiesActivityView {
	private String description;
	private Date date;

	public SummaryActivitiesActivityView(String description, Date date) {
		setDescription(description);
		setDate(date);
	}

	public String getDescription() {
		return this.description;
	}
	private void setDescription(String description) {
		this.description = description;
	}

	public Date getDate() {
		return this.date;
	}
	private void setDate(Date date) {
		this.date = date;
	}
}
