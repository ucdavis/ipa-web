package edu.ucdavis.dss.ipa.api.components.annual.views;

import java.util.ArrayList;
import java.util.List;

public class AnnualCourseView {

	private long id;
	private String subjectCode, courseNumber, title;
	private List<AnnualCourseView> courseOverlaps = new ArrayList<AnnualCourseView>();

	public AnnualCourseView(Course course) {
		setId(course.getId());
		setSubjectCode(course.getSubjectCode());
		setCourseNumber(course.getCourseNumber());
		setTitle(course.getTitle());
		setCourseOverlaps(course.getCourseOverlaps());
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getSubjectCode() {
		return subjectCode;
	}

	public void setSubjectCode(String subjectCode) {
		this.subjectCode = subjectCode;
	}

	public String getCourseNumber() {
		return courseNumber;
	}

	public void setCourseNumber(String courseNumber) {
		this.courseNumber = courseNumber;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<AnnualCourseView> getCourseOverlaps() {
		return courseOverlaps;
	}

	public void setCourseOverlaps(List<Course> courseOverlaps) {
		List<AnnualCourseView> viewCourseOverlaps = new ArrayList<AnnualCourseView>();
		for (Course courseOverlap: courseOverlaps) {
			viewCourseOverlaps.add(new AnnualCourseView(courseOverlap));
		}
		this.courseOverlaps = viewCourseOverlaps;
	}

}
