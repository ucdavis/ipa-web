package edu.ucdavis.dss.ipa.api.components.summary.views;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.TeachingCall;
import edu.ucdavis.dss.ipa.entities.TeachingCallReceipt;

/**
 * View DTO embedded in SummaryInstructorView representing ???
 *
 */
public class SummaryScheduleView {
	private long id, year, teachingCallId;
	private List<SummaryTermView> summaryTermViews = new ArrayList<SummaryTermView>();

	public long getId() {
		return this.id;
	}

	public void setId(Schedule schedule) {
		this.id = schedule.getId();
	}

	public long getYear() {
		return this.year;
	}

	public void setYear(Schedule schedule) {
		this.year = schedule.getYear();
	}

	@JsonProperty("terms")
	public List<SummaryTermView> getSummaryTermViews() {
		return this.summaryTermViews;
	}

	public void setSummaryTermViews(List<SummaryTermView> summaryTermViews) {
		this.summaryTermViews = summaryTermViews;
	}

	public long getTeachingCallId() {
		return teachingCallId;
	}

	public void setTeachingCallId(Schedule schedule, Instructor instructor) {
		this.teachingCallId = 0;
		for (TeachingCall teachingCall : schedule.getTeachingCalls()) {
			for (TeachingCallReceipt teachingCallReceipt : teachingCall.getTeachingCallReceipts()) {
				if (instructor.equals(teachingCallReceipt.getInstructor())) {
					this.teachingCallId = teachingCall.getId();
				}
			}
		}
	}
}
