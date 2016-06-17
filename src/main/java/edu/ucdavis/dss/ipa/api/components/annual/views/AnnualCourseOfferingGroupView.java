package edu.ucdavis.dss.ipa.api.components.annual.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.entities.Tag;

public class AnnualCourseOfferingGroupView {
	private long id, year;
	private String description;
	private List<Tag> tags = new ArrayList<Tag>();
	private HashMap<String,Long> seatTotals = new HashMap<String,Long>(); // AC-set desired number of seats
	private HashMap<String,Long> currentSeats = new HashMap<String,Long>(); // from CDW
	private HashMap<String,Object> courseOfferingInfo = new HashMap<String,Object>();
	private AnnualCourseView course;

	public AnnualCourseOfferingGroupView(Course cog) {
		setId(cog.getId());
		setYear(cog.getYear());
		setCourseOfferingInfo(cog.getCourseOfferingInfo());
		setDescription(cog.getDescription());
		setTags(cog);
		setSeatsTotal(cog.getSectionGroups());
		setSectionSeatCounts(cog.getSectionGroups());
		setCourse(cog);
	}

	public Long getId() {
		return this.id;
	}

	private void setId(Long courseOfferingGroupId) {
		this.id = courseOfferingGroupId;
	}

	public String getDescription() {
		return this.description;
	}

	private void setDescription(String description) {
		this.description = description;
	}

	public List<Tag> getTags() {
		return this.tags;
	}

	private void setTags(Course cog) {
		for (Tag tag : cog.getTags()) {
			if (!this.tags.contains(tag)) {
				this.tags.add(tag);
			}
		}
	}

	public HashMap<String,Long> getSeatsTotal() {
		return this.seatTotals;
	}

	public HashMap<String,Long> getCurrentSeatCounts() {
		return this.currentSeats;
	}

	private void setSectionSeatCounts(List<CourseOffering> courseOfferings) {
		for (CourseOffering courseOffering: courseOfferings) {
			for (SectionGroup sectionGroup: courseOffering.getSectionGroups()) {
				for (Section section: sectionGroup.getSections()) {
					// Get the 2 digit termCode
					String termCode = courseOffering.getTermCode().substring(Math.max(courseOffering.getTermCode().length() - 2, 0));

					// Find the current seats from the census snapshots
					Integer sectionCurrentSeats = 0;
					for (CensusSnapshot censusSnapshot: section.getCensusSnapshots()) {
						if (censusSnapshot.getSnapshotCode().equals("CURRENT")) {
							sectionCurrentSeats = censusSnapshot.getCurrentEnrollmentCount();
						}
					}

					// Initialize currentSeats for the termCode if null, then add to the currentSeats
					if (this.currentSeats.get(termCode) == null) this.currentSeats.put(termCode, 0L);
					Long totalCurrentSeats = this.currentSeats.get(termCode) + sectionCurrentSeats;
					this.currentSeats.put(termCode, totalCurrentSeats);
				}
			}
		}
	}

	private void setSeatsTotal(List<CourseOffering> courseOfferings) {
		for (CourseOffering courseOffering: courseOfferings) {
			// Get the 2 digit termCode
			String termCode = courseOffering.getTermCode().substring(Math.max(courseOffering.getTermCode().length() - 2, 0));

			this.seatTotals.put(termCode, courseOffering.getSeatsTotal());
		}
	}

	public HashMap<String,Object> getCourseOfferingInfo() {
		return courseOfferingInfo;
	}

	public void setCourseOfferingInfo(HashMap<String,Object> courseOfferingInfo) {
		this.courseOfferingInfo = courseOfferingInfo;
	}

	public AnnualCourseView getCourse() {
		return course;
	}

	public void setCourse(Course cog) {
		this.course = new AnnualCourseView(cog.getCourse());
	}

	public long getYear() {
		return year;
	}

	public void setYear(long year) {
		this.year = year;
	}

}
