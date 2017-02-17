package edu.ucdavis.dss.ipa.services;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import edu.ucdavis.dss.ipa.entities.*;
import org.springframework.validation.annotation.Validated;

// The availabilityBlob on a teachingCallResponse is a comma delimited string
// It represents availability within a 15 hour window (7am-10pm) over 5 days
// 1 for available, 0 for not
@Validated
public interface TeachingCallResponseService {

	public TeachingCallResponse save(@NotNull @Valid TeachingCallResponse teachingCallResponse);

	public TeachingCallResponse getOneById(Long id);

	public void delete(Long id);

	List<TeachingCallResponse> findByWorkgroupIdAndYearAndTermCode(long workgroupId, long year, String termCode);

	public TeachingCallResponse findOrCreateOneByScheduleIdAndInstructorIdAndTermCode(Long scheduleId, long instructorId, String termCode);
}
