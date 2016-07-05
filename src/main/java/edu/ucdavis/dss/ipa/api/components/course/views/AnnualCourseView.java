package edu.ucdavis.dss.ipa.api.components.course.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.entities.Tag;

public class AnnualCourseView {
	private long id, year;
	private String description;
	private List<Tag> tags = new ArrayList<Tag>();
	private HashMap<String,Integer> plannedSeats = new HashMap<String,Integer>(); // AC-set desired number of seats
	private HashMap<String,Long> currentSeats = new HashMap<String,Long>(); // from CDW
	private HashMap<String,Object> courseOfferingInfo = new HashMap<String,Object>();
	private List<SectionGroup> sectionGroups = new ArrayList<>();

	public AnnualCourseView(Course course) {
		setId(course.getId());
		setYear(course.getYear());
		setDescription(course.getTitle());
		setTags(course);
		setSeatsTotal(course.getSectionGroups());
		setSectionSeatCounts(course.getSectionGroups());
		setSectionGroups(course.getSectionGroups());
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

	public HashMap<String,Integer> getSeatsTotal() {
		return this.plannedSeats;
	}

	public HashMap<String,Long> getCurrentSeatCounts() {
		return this.currentSeats;
	}

	private void setSectionSeatCounts(List<SectionGroup> sectionGroups) {
		for (SectionGroup sectionGroup: sectionGroups) {
			for (Section section: sectionGroup.getSections()) {
				// Get the 2 digit termCode
				String termCode = sectionGroup.getTermCode().substring(Math.max(sectionGroup.getTermCode().length() - 2, 0));

				// Find the current seats from the census snapshots
				Integer sectionCurrentSeats = 0;
				// TODO: Source?
//				for (CensusSnapshot censusSnapshot: section.getCensusSnapshots()) {
//					if (censusSnapshot.getSnapshotCode().equals("CURRENT")) {
//						sectionCurrentSeats = censusSnapshot.getCurrentEnrollmentCount();
//					}
//				}

				// Initialize currentSeats for the termCode if null, then add to the currentSeats
				if (this.currentSeats.get(termCode) == null) this.currentSeats.put(termCode, 0L);
				Long totalCurrentSeats = this.currentSeats.get(termCode) + sectionCurrentSeats;
				this.currentSeats.put(termCode, totalCurrentSeats);
			}
		}
	}

	private void setSeatsTotal(List<SectionGroup> sectionGroups) {
		for (SectionGroup sectionGroup: sectionGroups) {
			// Get the 2 digit termCode
			String termCode = sectionGroup.getTermCode().substring(Math.max(sectionGroup.getTermCode().length() - 2, 0));

			this.plannedSeats.put(termCode, sectionGroup.getPlannedSeats());
		}
	}

	public HashMap<String,Object> getCourseOfferingInfo() {
		return courseOfferingInfo;
	}

	public void setCourseOfferingInfo(HashMap<String,Object> courseOfferingInfo) {
		this.courseOfferingInfo = courseOfferingInfo;
	}

	public long getYear() {
		return year;
	}

	public void setYear(long year) {
		this.year = year;
	}

	public List<SectionGroup> getSectionGroups() {
		return sectionGroups;
	}

	public void setSectionGroups(List<SectionGroup> sectionGroups) {
		this.sectionGroups = sectionGroups;
	}
}
