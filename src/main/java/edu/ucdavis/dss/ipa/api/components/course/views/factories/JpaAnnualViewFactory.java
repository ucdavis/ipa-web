package edu.ucdavis.dss.ipa.api.components.course.views.factories;

import edu.ucdavis.dss.ipa.api.components.course.views.CourseExcelView;
import edu.ucdavis.dss.ipa.api.components.course.views.CourseView;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.View;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Service
public class JpaAnnualViewFactory implements AnnualViewFactory {
	@Inject ScheduleTermStateService scheduleTermStateService;
	@Inject SectionGroupService sectionGroupService;
	@Inject ScheduleService scheduleService;
	@Inject WorkgroupService workgroupService;
	@Inject CourseService courseService;
	@Inject TermService termService;

	@Override
	public CourseView createCourseView(long workgroupId, long year, Boolean showDoNotPrint) {
		Workgroup workgroup = workgroupService.findOneById(workgroupId);
		if(workgroup == null) { return null; }

		Schedule schedule = scheduleService.findOrCreateByWorkgroupIdAndYear(workgroupId, year);
		List<ScheduleTermState> scheduleTermStates = scheduleTermStateService.getScheduleTermStatesBySchedule(schedule);
		List<SectionGroup> sectionGroups = sectionGroupService.findByWorkgroupIdAndYear(workgroupId, year);
		List<InstructorNote> instructorNotes = schedule.getInstructorNotes();

		// TODO: make sure banner has terms after 2099, not urgent, just fix before 2099!
		List<Term> terms = termService.findByYear(year);

		List<Course> courses;
		if (showDoNotPrint != null && showDoNotPrint) {
			courses = schedule.getCourses();
		} else {
			courses = courseService.findVisibleByWorkgroupIdAndYear(workgroupId, year);
		}

		return new CourseView(courses, sectionGroups, workgroup.getTags(), terms, instructorNotes);
	}

    @Override
    public View createAnnualScheduleExcelView(long workgroupId, long year, Boolean showDoNotPrint) {
    	CourseView courseView = createCourseView(workgroupId, year, showDoNotPrint);
		return new CourseExcelView(courseView);
    }

	@Override
	public List<HistoricalCourse> createCourseQueryView(long workgroupId, long year, Boolean showDoNotPrint) {
		List<HistoricalCourse> queriedCourses = new ArrayList<>();

		Schedule schedule = scheduleService.findOrCreateByWorkgroupIdAndYear(workgroupId, year);

		List<Course> courses;
		if (showDoNotPrint != null && showDoNotPrint) {
			courses = schedule.getCourses();
		} else {
			courses = courseService.findVisibleByWorkgroupIdAndYear(workgroupId, year);
		}

		for (Course course : courses) {
			for (SectionGroup sectionGroup : course.getSectionGroups()) {

				// For each sectionGroup, build a 'historicalCourse' by combining the course and sectionGroup data.
				HistoricalCourse historicalCourse = new HistoricalCourse();
				historicalCourse.setCourseNumber(course.getCourseNumber());
				historicalCourse.setCreditHoursHigh(course.getUnitsHigh());
				historicalCourse.setCreditHoursLow(course.getUnitsLow());
				historicalCourse.setEffectiveTermCode(course.getEffectiveTermCode());
				historicalCourse.setSequencePattern(course.getSequencePattern());
				historicalCourse.setSubjectCode(course.getSubjectCode());
				historicalCourse.setTitle(course.getTitle());

				historicalCourse.setTermCode(sectionGroup.getTermCode());
				historicalCourse.setPlannedSeats(sectionGroup.getPlannedSeats());

				queriedCourses.add(historicalCourse);
			}
		}



		return queriedCourses;
	}


	public class HistoricalCourse {
		private long id;

		private String courseNumber, subjectCode, sequencePattern, termCode, effectiveTermCode, title;
		private Integer plannedSeats;
		private float creditHoursHigh, creditHoursLow;
		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

		public String getCourseNumber() {
			return courseNumber;
		}

		public void setCourseNumber(String courseNumber) {
			this.courseNumber = courseNumber;
		}

		public String getSubjectCode() {
			return subjectCode;
		}

		public void setSubjectCode(String subjectCode) {
			this.subjectCode = subjectCode;
		}

		public String getSequencePattern() {
			return sequencePattern;
		}

		public void setSequencePattern(String sequencePattern) {
			this.sequencePattern = sequencePattern;
		}

		public String getTermCode() {
			return termCode;
		}

		public void setTermCode(String termCode) {
			this.termCode = termCode;
		}

		public String getEffectiveTermCode() {
			return effectiveTermCode;
		}

		public void setEffectiveTermCode(String effectiveTermCode) {
			this.effectiveTermCode = effectiveTermCode;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public Integer getPlannedSeats() {
			return plannedSeats;
		}

		public void setPlannedSeats(Integer plannedSeats) {
			this.plannedSeats = plannedSeats;
		}

		public float getCreditHoursHigh() {
			return creditHoursHigh;
		}

		public void setCreditHoursHigh(float creditHoursHigh) {
			this.creditHoursHigh = creditHoursHigh;
		}

		public float getCreditHoursLow() {
			return creditHoursLow;
		}

		public void setCreditHoursLow(float creditHoursLow) {
			this.creditHoursLow = creditHoursLow;
		}
	}
}
