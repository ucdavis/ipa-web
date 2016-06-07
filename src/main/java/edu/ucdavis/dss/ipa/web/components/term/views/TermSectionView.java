package edu.ucdavis.dss.ipa.web.components.term.views;

import java.util.ArrayList;
import java.util.List;

import edu.ucdavis.dss.ipa.entities.Activity;
import edu.ucdavis.dss.ipa.entities.Section;

public class TermSectionView {
	private long id;
	private long seats;
	private String crn;
	private String sequenceNumber;
	private List<TermActivityView> activities = new ArrayList<TermActivityView>();
	private Boolean visible, crnRestricted;

	public TermSectionView(Section section) {
		if (section == null) return;
		setId(section.getId());
		setSeats(section.getSeats());
		setCrn(section.getCrn());
		setSequenceNumber(section.getSequenceNumber());
		setActivities(section.getActivities());
		setVisible(section.isVisible());
		setCrnRestricted(section.isCrnRestricted());
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getSeats() {
		return seats;
	}

	public void setSeats(long seats) {
		this.seats = seats;
	}

	public String getCrn() {
		return crn;
	}

	public void setCrn(String crn) {
		this.crn = crn;
	}

	public String getSequenceNumber() {
		return sequenceNumber;
	}

	public void setSequenceNumber(String sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	public List<TermActivityView> getActivities() {
		return activities;
	}

	public void setActivities(List<Activity> activities) {
		for (Activity activity : activities) {
			if (activity.isShared() == false) {
				this.activities.add(new TermActivityView(activity));
			}
		}
	}

	public Boolean getVisible() {
		return visible;
	}

	public void setVisible(Boolean visible) {
		this.visible = visible;
	}

	public Boolean getCrnRestricted() {
		return crnRestricted;
	}

	public void setCrnRestricted(Boolean crnRestricted) {
		this.crnRestricted = crnRestricted;
	}

}
