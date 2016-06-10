package edu.ucdavis.dss.ipa.diff.mapper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.function.Function;


import edu.ucdavis.dss.dw.dto.DwMeeting;
import edu.ucdavis.dss.ipa.diff.entities.DiffMeeting;

/**
 * Functional class used by Stream::map() for converting DW/Banner meetings to
 * DiffMeetings for comparing meetings with JaVers. Used by DwSectionMapper.
 * 
 * @author Eric Lin
 */
public class DwMeetingMapper implements Function<DwMeeting, DiffMeeting> {
	/*
	 * This variable is used to help JaVers distinguish between meetings in
	 * different sections
	 */
	private String parentId;

	public DwMeetingMapper(String parentId) {
		this.parentId = parentId;
	}

	@Override
	public DiffMeeting apply(DwMeeting meeting) {
		int frequency = 0;

		// Calculate activity frequency (copied from JpaDwScheduleService)
		long diff = meeting.getEndDate().getTime() - meeting.getStartDate().getTime();
		double diffWeeks = Math.floor(diff / (7 * 24 * 60 * 60 * 1000));
		int timesInWeek = meeting.getDaysIndicated();

		if (meeting.getTotalMeetings() > 0)
			frequency = (int) Math.ceil(diffWeeks * timesInWeek / meeting.getTotalMeetings());

		LocalDateTime endDate = LocalDateTime.ofInstant(meeting.getEndDate().toInstant(), ZoneId.systemDefault());
		LocalDateTime beginDate = LocalDateTime.ofInstant(meeting.getStartDate().toInstant(), ZoneId.systemDefault());

		String bannerLocation = meeting.getBuildingCode() + " " + meeting.getRoomCode();

		return new DiffMeeting.Builder(parentId, bannerLocation, meeting.getDayIndicator(), meeting.getScheduleCode().getScheduleCode())
				.beginDate(beginDate)
				.endDate(endDate)
				.beginTime(meeting.getBeginTime())
				.endTime(meeting.getEndTime())
				.frequency(frequency)
				.build();
	}


}
