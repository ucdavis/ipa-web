package edu.ucdavis.dss.ipa.services;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

import edu.ucdavis.dss.ipa.entities.CourseOffering;
import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.TeachingPreference;

@Validated
public interface TeachingPreferenceService {
	TeachingPreference saveTeachingPreference(@NotNull @Valid TeachingPreference teachingPreference);

	TeachingPreference findOneById(Long id);

	TeachingPreference findOrCreateOneBySectionIdAndInstructorId(Long sectionId, Long instructorId);
	
	void deleteTeachingPreferenceById(Long id);

	List<TeachingPreference> getTeachingPreferencesByTeachingCallIdAndInstructorId(Long teachingCallId, Long instructorId);

	List<TeachingPreference> getTeachingPreferencesByScheduleIdAndInstructorId(Long scheduleId, Long instructorId);

	List<TeachingPreference> getTeachingPreferencesByScheduleIdAndTermCode(long scheduleId, String termCode);

	TeachingPreference findOrCreateOneByCourseOfferingAndInstructorAndSchedule(CourseOffering courseOffering, Instructor instructor, Schedule schedule);

	List<TeachingPreference> getTeachingPreferencesByScheduleId(long scheduleId);

	List<TeachingPreference> getTeachingPreferencesByCourseOfferingGroupId(Long cogId);

}
