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

	public List<TeachingCallResponse> findByTeachingCallAndInstructorLoginId(TeachingCall teachingCall, String loginId);

	List<TeachingCallResponse> findByScheduleIdAndTermCode(Long scheduleId, String termCode);

	List<TeachingCallResponse> findByWorkgroupIdAndYearAndTermCode(long workgroupId, long year, String termCode);

	public TeachingCallResponse findOrCreateOneByTeachingCallIdAndInstructorIdAndTermCode(Long teachingCallId, long instructorId, String termCode);

	/**
	 * Overloaded version of {@link #getWorkgroupTeachingCallResponsesByInstructorId(Workgroup, Instructor, int)}.
	 * <p>
	 * Implementation defaults to returning a List of TeachingCallResponses for ten years for a
	 * given Workgroup/Instructor combination.
	 *
	 * @param workgroup Workgroup from which to get TeachingCallResponses
	 * @param instructor Instructor whose TeachingCallResponses should be retrieved
	 * @return List of TeachingCallResponses for (10) years
	 */
	List<TeachingCallResponse> getWorkgroupTeachingCallResponsesByInstructorId(
			Workgroup workgroup, Instructor instructor);

	List<TeachingCallResponse> getWorkgroupTeachingCallResponsesByInstructorId(
			Workgroup workgroup, Instructor instructor, int years);

	public List<TeachingCallResponse> findByScheduleId(long scheduleId);

	List<TeachingCallResponse> findBySectionGroup(SectionGroup sectionGroup);
}
