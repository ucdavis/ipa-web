package edu.ucdavis.dss.ipa.web.components.term.views;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.ucdavis.dss.ipa.entities.Activity;
import edu.ucdavis.dss.ipa.entities.ActivityType;
import edu.ucdavis.dss.ipa.entities.Building;
import edu.ucdavis.dss.ipa.entities.Track;
import edu.ucdavis.dss.ipa.enums.ActivityState;

public class TermActivityView {
	private long id, sectionId, sectionGroupId, courseOfferingId;
	private TermBuildingView building;
	private Date beginDate, endDate;
	private Time startTime, endTime;
	private String room, dayIndicator, title, sequenceNumber, codeDescription, termCode, crn, description;
	private ActivityState activityState;
	private int frequency;
	private boolean virtual, shared;
	private ActivityType activityTypeCode;
	private List<TermTrackView> tracks = new ArrayList<TermTrackView>();

	public TermActivityView(Activity activity) {
		if (activity == null) return;
		setId(activity.getId());
		setBuilding(activity.getBuilding());
		setBeginDate(activity.getBeginDate());
		setEndDate(activity.getEndDate());
		setStartTime(activity.getStartTime());
		setEndTime(activity.getEndTime());
		setRoom(activity.getRoom());
		setDayIndicator(activity.getDayIndicator());
		setActivityState(activity.getActivityState());
		setFrequency(activity.getFrequency());
		setVirtual(activity.isVirtual());
		setActivityTypeCode(activity.getActivityTypeCode());
		setTitle(activity);
		setDescription(activity);
		setCodeDescription(activity);
		setSectionId(activity);
		setSectionGroupId(activity);
		setCourseOfferingId(activity);
		setTracks(activity);
		setTermCode(activity);
		setCrn(activity);
		setShared(activity.isShared());
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public TermBuildingView getBuilding() {
		return building;
	}

	public void setBuilding(Building building) {
		if (building == null) return;
		this.building = new TermBuildingView(building);
	}

	public Date getBeginDate() {
		return beginDate;
	}

	public void setBeginDate(Date beginDate) {
		this.beginDate = beginDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Time getStartTime() {
		return startTime;
	}

	public void setStartTime(Time startTime) {
		this.startTime = startTime;
	}

	public Time getEndTime() {
		return endTime;
	}

	public void setEndTime(Time endTime) {
		this.endTime = endTime;
	}

	public String getRoom() {
		return room;
	}

	public void setRoom(String room) {
		this.room = room;
	}

	public String getDayIndicator() {
		return dayIndicator;
	}

	public void setDayIndicator(String dayIndicator) {
		this.dayIndicator = dayIndicator;
	}

	public ActivityState getActivityState() {
		return activityState;
	}

	public void setActivityState(ActivityState activityState) {
		this.activityState = activityState;
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public boolean isVirtual() {
		return virtual;
	}

	public void setVirtual(boolean virtual) {
		this.virtual = virtual;
	}

	public boolean isShared() {
		return shared;
	}

	public void setShared(boolean shared) {
		this.shared = shared;
	}

	public ActivityType getActivityTypeCode() {
		return activityTypeCode;
	}

	public void setActivityTypeCode(ActivityType activityTypeCode) {
		this.activityTypeCode = activityTypeCode;
	}

	public long getSectionId() {
		return sectionId;
	}

	public void setSectionId(Activity activity) {
		this.sectionId = activity.getSection().getId();
	}

	public long getSectionGroupId() {
		return sectionGroupId;
	}

	public void setSectionGroupId(Activity activity) {
		this.sectionGroupId = activity.getSectionGroupId();
	}

	public long getCourseOfferingId() {
		return courseOfferingId;
	}

	public void setCourseOfferingId(Activity activity) {
		this.courseOfferingId = activity.getSection().getSectionGroup().getCourseOffering().getId();
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(Activity activity) {
		String sequenceDescription = activity.getSection().getSequenceNumber();
		if (activity.isShared()) {
			sequenceDescription = sequenceDescription.charAt(0) + "X";
		}

		this.title = activity.getSection().getSectionGroup().getCourse().getSubjectCode()
			+ " " + activity.getSection().getSectionGroup().getCourse().getCourseNumber()
			+ " - " + sequenceDescription;
	}

	public String getSequenceNumber() {
		return sequenceNumber;
	}

	public void setSequenceNumber(Activity activity) {
		this.sequenceNumber = activity.getSection().getSequenceNumber();
	}

	public String getCodeDescription() {
		return codeDescription;
	}

	public void setCodeDescription(Activity activity) {
		this.codeDescription = activity.getActivityTypeCode().GetDescription(activity.getActivityTypeCode().getActivityTypeCode())
				+ " (" + activity.getActivityTypeCode().getActivityTypeCode() + ")";
	}

	public String getTermCode() {
		return termCode;
	}

	public void setTermCode(Activity activity) {
		this.termCode = activity.getSection().getSectionGroup().getTermCode();
	}

	public String getCrn() {
		return crn;
	}

	public void setCrn(Activity activity) {
		this.crn = activity.getSection().getCrn();
	}

	public List<TermTrackView> getTracks() {
		return tracks;
	}

	public void setTracks(Activity activity) {
		for(Track track :activity.getSection().getSectionGroup().getCourseOfferingGroup().getTracks()) {
			this.tracks.add(new TermTrackView(track));
		}
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(Activity activity) {
		this.description = activity.getSection().getSectionGroup().getCourse().getSubjectCode()
			+ " " + activity.getSection().getSectionGroup().getCourse().getCourseNumber();
	}
}
