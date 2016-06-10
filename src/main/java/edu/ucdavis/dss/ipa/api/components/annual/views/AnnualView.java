package edu.ucdavis.dss.ipa.api.components.annual.views;

import java.util.ArrayList;
import java.util.List;

import edu.ucdavis.dss.ipa.entities.CourseOfferingGroup;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.ScheduleTermState;

public class AnnualView {
	private long id, year, workgroupId;
	private boolean isClosed;
	private List<ScheduleTermState> scheduleTermStates = new ArrayList<ScheduleTermState>();
	private List<AnnualCourseOfferingGroupView> courseOfferingGroups = new ArrayList<AnnualCourseOfferingGroupView>();
	private List<AnnualInstructorView> instructors = new ArrayList<AnnualInstructorView>();

	public AnnualView(Schedule schedule, boolean isClosed, List<ScheduleTermState> scheduleTermStates) {
		setId(schedule);
		setYear(schedule);
		setScheduleTermStates(scheduleTermStates);
		setCourseOfferingGroups(schedule);
		setInstructors(schedule);
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

	public List<AnnualCourseOfferingGroupView> getCourseOfferingGroups() {
		return this.courseOfferingGroups;
	}

	public List<AnnualInstructorView> getInstructors() {
		return this.instructors;
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

	private void setCourseOfferingGroups(Schedule schedule) {
		for (CourseOfferingGroup cog: schedule.getCourseOfferingGroups()) {
			this.courseOfferingGroups.add(new AnnualCourseOfferingGroupView(cog));
		}
	}

	private void setInstructors(Schedule schedule) {
//		List<Instructor> scheduleInstructors = new ArrayList<Instructor>();
//		for(TeachingPreference teachingPreference : schedule.getTeachingPreferences() ) {
//			if( teachingPreference.isApproved() == true && !scheduleInstructors.contains(teachingPreference.getInstructor())) {
//				scheduleInstructors.add(teachingPreference.getInstructor());
//			}
//		}
//
//		// Add the 'No instructor' to list courses with none assigned
//		Instructor noInstructor = new Instructor();
//		noInstructor.setFirstName(".");
//		noInstructor.setLastName("No Instructor");
//		scheduleInstructors.add(noInstructor);
//
//		for (Instructor instructor: scheduleInstructors) {
//			this.instructors.add(new AnnualInstructorView(instructor, schedule));
//		}
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
