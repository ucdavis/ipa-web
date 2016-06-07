package edu.ucdavis.dss.ipa.web.components.summary.views;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.TeachingAssignment;

public class SummaryTermView {
	private String termCode;
	private Boolean isInstructorCallOpen;
	private List<SummaryCourseOfferingView> summaryCourseOfferingViews = new ArrayList<SummaryCourseOfferingView>();

	public Boolean isInstructorCallOpen() {
		return isInstructorCallOpen;
	}

	// Will be true unless schedule term is still in annual draft
	public void setInstructorCallOpen(Boolean isInstructorCallOpen) {
		this.isInstructorCallOpen = isInstructorCallOpen;
	}

	public String getTermCode() {
		return this.termCode;
	}

	public void setTermCode(String termCode) {
		this.termCode = termCode;
	}

	@JsonProperty("courseOfferings")
	public List<SummaryCourseOfferingView> getSummaryCourseOfferingViews() {
		return this.summaryCourseOfferingViews;
	}

	public void setSummaryCourseOfferingViews(Instructor instructor, Schedule schedule, String termCode) {
		List<SummaryCourseOfferingView> summaryCourseOfferingViews = new ArrayList<SummaryCourseOfferingView>();

		for (TeachingAssignment teachingAssignment : instructor.getTeachingAssignments()) {
			if (teachingAssignment.getSectionGroup().getTermCode().equals(termCode)) {
				SummaryCourseOfferingView summaryCourseOfferingView = new SummaryCourseOfferingView(teachingAssignment.getSectionGroup());
				summaryCourseOfferingViews.add(summaryCourseOfferingView);
			}
		}

		this.summaryCourseOfferingViews = summaryCourseOfferingViews;
	}
}
