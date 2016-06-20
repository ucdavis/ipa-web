package edu.ucdavis.dss.ipa.api.components.annual.views;

import java.util.ArrayList;
import java.util.List;

import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.ScheduleTermState;

public class AnnualView {
	private long id, year, workgroupId;
	private boolean isClosed;
	private List<ScheduleTermState> scheduleTermStates = new ArrayList<ScheduleTermState>();
	private List<AnnualCourseView> courses = new ArrayList<AnnualCourseView>();

	public AnnualView(Schedule schedule, boolean isClosed, List<ScheduleTermState> scheduleTermStates) {
		setId(schedule);
		setYear(schedule);
		setScheduleTermStates(scheduleTermStates);
		setCourses(schedule);
		setClosed(isClosed);
		setWorkgroupId(schedule);
	}

	public long getId() {
		return this.id;
	}

	public long getYear() {
		return this.year;
	}

	public List<ScheduleTermState> getScheduleTermStates() {
		return this.scheduleTermStates;
	}

	public List<AnnualCourseView> getCourses() {
		return this.courses;
	}

	private void setId(Schedule schedule) {
		this.id = schedule.getId();
	}

	private void setYear(Schedule schedule) {
		this.year = schedule.getYear();
	}

	private void setScheduleTermStates(List<ScheduleTermState> scheduleTermStates) {
		this.scheduleTermStates = scheduleTermStates;
	}

	private void setCourses(Schedule schedule) {
		for (Course cog: schedule.getCourses()) {
			this.courses.add(new AnnualCourseView(cog));
		}
	}

	public boolean isClosed() {
		return isClosed;
	}

	public void setClosed(boolean isClosed) {
		this.isClosed = isClosed;
	}

	public long getWorkgroupId() {
		return this.workgroupId;
	}

	public void setWorkgroupId(Schedule schedule) {
		this.workgroupId = schedule.getWorkgroup().getId();
	}
}
