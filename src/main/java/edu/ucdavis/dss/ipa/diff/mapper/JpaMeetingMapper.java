package edu.ucdavis.dss.ipa.diff.mapper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.function.Function;

import edu.ucdavis.dss.ipa.diff.entities.DiffMeeting;
import edu.ucdavis.dss.ipa.entities.Activity;

/**
 * Functional class used by Stream::map() for converting IPA activities to
 * DiffMeetings for comparing meetings/activities with JaVers. Used by
 * JpaSectionMapper.
 * 
 * @author Eric Lin
 */
public class JpaMeetingMapper implements Function<Activity, DiffMeeting> {
	private String parentId;

	public JpaMeetingMapper(String parentId) {
		this.parentId = parentId;
	}

	@Override
	public DiffMeeting apply(Activity activity) {
		String buildingCode = null;
		if (activity.getBuilding() != null)
			buildingCode = activity.getBuilding().getName();

		// Convert date to LocalDateTime to avoid time zone issues
		LocalDateTime beginDate = LocalDateTime.ofInstant(activity.getBeginDate().toInstant(), ZoneId.systemDefault());
		LocalDateTime endDate = LocalDateTime.ofInstant(activity.getEndDate().toInstant(), ZoneId.systemDefault());

		return new DiffMeeting.Builder(parentId, buildingCode, activity.getRoom(), activity.getDayIndicator(), activity.getActivityTypeCode().getActivityTypeCode())
				.beginDate(beginDate)
				.endDate(endDate)
				.beginTime(activity.getStartTime())
				.endTime(activity.getEndTime())
				.frequency(activity.getFrequency())
				.build();
	}


}
