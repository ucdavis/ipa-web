package edu.ucdavis.dss.ipa.api.components.annual.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.ucdavis.dss.ipa.entities.CensusSnapshot;
import edu.ucdavis.dss.ipa.entities.CourseOffering;
import edu.ucdavis.dss.ipa.entities.CourseOfferingGroup;
import edu.ucdavis.dss.ipa.entities.Section;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.entities.Track;

public class AnnualCourseOfferingGroupView {
	private long id, year;
	private String description;
	private List<Track> tracks = new ArrayList<Track>();
	private HashMap<String,Long> seatTotals = new HashMap<String,Long>(); // AC-set desired number of seats
	private HashMap<String,Long> currentSeats = new HashMap<String,Long>(); // from CDW
	private HashMap<String,Object> courseOfferingInfo = new HashMap<String,Object>();
	private AnnualCourseView course;

	public AnnualCourseOfferingGroupView(CourseOfferingGroup cog) {
		setId(cog.getId());
		setYear(cog.getYear());
		setCourseOfferingInfo(cog.getCourseOfferingInfo());
		setDescription(cog.getDescription());
		setTracks(cog);
		setSeatsTotal(cog.getCourseOfferings());
		setSectionSeatCounts(cog.getCourseOfferings());
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

	public List<Track> getTracks() {
		return this.tracks;
	}

	private void setTracks(CourseOfferingGroup cog) {
		for (Track track: cog.getTracks()) {
			if (!this.tracks.contains(track)) {
				this.tracks.add(track);
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

	public void setCourse(CourseOfferingGroup cog) {
		this.course = new AnnualCourseView(cog.getCourse());
	}

	public long getYear() {
		return year;
	}

	public void setYear(long year) {
		this.year = year;
	}

}
