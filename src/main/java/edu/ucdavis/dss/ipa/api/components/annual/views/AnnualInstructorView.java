package edu.ucdavis.dss.ipa.api.components.annual.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringJoiner;

import edu.ucdavis.dss.ipa.entities.CourseOffering;
import edu.ucdavis.dss.ipa.entities.CourseOfferingGroup;
import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.Section;
import edu.ucdavis.dss.ipa.entities.SectionGroup;

public class AnnualInstructorView {

	private long id;
	private String fullName;
	private List<HashMap<String,Object>> courses = new ArrayList<HashMap<String,Object>>();

	public AnnualInstructorView(Instructor instructor, Schedule schedule) {
		setId(instructor.getId());
		setFullName(instructor);
		setCourses(instructor, schedule);
	}

	public long getId() {
		return id;
	}

	public void setId(long instructorId) {
		this.id = instructorId;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(Instructor instructor) {
		StringJoiner joiner = new StringJoiner(", ");
		if (instructor.getLastName() != null && !instructor.getLastName().trim().isEmpty() && !instructor.getLastName().equals("."))
			joiner.add(instructor.getLastName());
		if (instructor.getFirstName() != null && !instructor.getFirstName().trim().isEmpty() && !instructor.getFirstName().equals("."))
			joiner.add(instructor.getFirstName());

		this.fullName = joiner.toString();
	}

	public List<HashMap<String,Object>> getCourses() {
		return courses;
	}

	@SuppressWarnings("unchecked")
	public void setCourses(Instructor instructor, Schedule schedule) {
		List<Section> sections = new ArrayList<Section>();
		for (CourseOfferingGroup cog: schedule.getCourseOfferingGroups()) {
			for (CourseOffering courseOffering: cog.getCourseOfferings()) {
				for (SectionGroup sg: courseOffering.getSectionGroups()) {
					for (Section section: sg.getSections()) {
						if (section.getInstructors().contains(instructor) ||
								// The case when no one is assigned, attach to the 'No instructor' DTO
								( section.getInstructors().size() == 0 && instructor.getId() == 0)) {
							sections.add(section);
						}
					}
				}
			}
		}

		for (Section section: sections) {
			HashMap<String,Object> sec = getCourseByCourseIdAndSequenceNumber(
					section.getSectionGroup().getCourseId(),
					section.getSequenceNumber());
			if (sec == null) {
				sec = new HashMap<String,Object>();
				sec.put("courseId", section.getSectionGroup().getCourseId());
				String description = section.getSectionGroup().getSubjectCode() +
						" " + section.getSectionGroup().getCourseNumber() +
						" " + section.getSectionGroup().getTitle();
				sec.put("description", description);

				sec.put("sequenceNumber", section.getSequenceNumber());

				HashMap<String, Long> termSeats = new HashMap<String, Long>();
				if (section.getSectionGroup().getTermCode() != null) {
					termSeats.put(
							section.getSectionGroup().getTermCode().substring(
									Math.max(section.getSectionGroup().getTermCode().length() - 2, 0)),
							section.getSeats());
				}
				sec.put("termSeats", termSeats);

				this.courses.add(sec);
			} else {
				HashMap<String,Long> termSeats = (HashMap<String, Long>) sec.get("termSeats");
				if (section.getSectionGroup().getTermCode() != null) {
					termSeats.put(
							section.getSectionGroup().getTermCode().substring(
									Math.max(section.getSectionGroup().getTermCode().length() - 2, 0)),
							section.getSeats());
				}
				sec.put("termSeats", termSeats);
			}
		}
	}

	private HashMap<String,Object> getCourseByCourseIdAndSequenceNumber(long courseId, String sequenceNumber) {
		for (HashMap<String,Object> course : this.courses) {
			if (course.get("courseId").equals(courseId) && course.get("sequenceNumber").equals(sequenceNumber)) {
				return course;
			}
		}
		return null;
	}
}
