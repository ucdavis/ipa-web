package edu.ucdavis.dss.ipa.services;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.entities.TeachingAssignment;

import java.util.List;

@Validated
public interface TeachingAssignmentService {

	TeachingAssignment findOneById(Long id);

	TeachingAssignment save(@NotNull @Valid TeachingAssignment teachingAssignment);

	void delete(Long id);

	TeachingAssignment findOneBySectionGroupAndInstructor(SectionGroup sectionGroup, Instructor instructor);

	TeachingAssignment findOrCreateOneBySectionGroupAndInstructor(SectionGroup sectionGroup, Instructor instructor);

	TeachingAssignment findBySectionGroupIdAndInstructorIdAndScheduleIdAndTermCodeAndBuyoutAndCourseReleaseAndSabbatical(
		Long sectionGroupId, Long instructorId, Long scheduleId, String termCode, Boolean buyout, Boolean courseRelease, Boolean sabbatical);

	List<TeachingAssignment> findByScheduleIdAndInstructorId(long scheduleId, long instructorId);

	List<TeachingAssignment> findByCourseId(long courseId);

	TeachingAssignment findByInstructorIdAndScheduleIdAndTermCodeAndSuggestedCourseNumberAndSuggestedSubjectCodeAndSuggestedEffectiveTermCode(long instructorId, long scheduleId, String termCode, String suggestedCourseNumber, String suggestedSubjectCode, String suggestedEffectiveTermCode);
}
