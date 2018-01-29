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

	List<TeachingAssignment> findByScheduleIdAndInstructorId(long scheduleId, long instructorId);

	List<TeachingAssignment> findByCourseId(long courseId);

	TeachingAssignment findByInstructorIdAndScheduleIdAndTermCodeAndSuggestedCourseNumberAndSuggestedSubjectCodeAndSuggestedEffectiveTermCode(long instructorId, long scheduleId, String termCode, String suggestedCourseNumber, String suggestedSubjectCode, String suggestedEffectiveTermCode);

    List<TeachingAssignment> findApprovedByWorkgroupIdAndYearAndTermCode(long workgroupId, long year, String termCode);

	List<TeachingAssignment> findByScheduleId(long scheduleId);

	TeachingAssignment findByTeachingAssignment(TeachingAssignment teachingAssignment);

	List<TeachingAssignment> findAllByIds(List<Long> teachingAssignmentIds);

	List<TeachingAssignment> updatePreferenceOrder(List<Long> sortedTeachingPreferenceIds);
}
