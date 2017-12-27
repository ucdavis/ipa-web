package edu.ucdavis.dss.ipa.services.jpa;

import javax.inject.Inject;

import edu.ucdavis.dss.ipa.services.InstructorService;
import org.springframework.stereotype.Service;

import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.entities.TeachingAssignment;
import edu.ucdavis.dss.ipa.repositories.TeachingAssignmentRepository;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.TeachingAssignmentService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class JpaTeachingAssignmentService implements TeachingAssignmentService {
	@Inject TeachingAssignmentRepository teachingAssignmentRepository;

	@Inject ScheduleService scheduleService;
	@Inject InstructorService instructorService;

	@Override
	public TeachingAssignment save(TeachingAssignment teachingAssignment) {
		return teachingAssignmentRepository.save(teachingAssignment);
	}
	
	@Override
	@Transactional
	public void delete(Long id) {
		teachingAssignmentRepository.deleteById(id);
	}

	@Override
	public TeachingAssignment findOneBySectionGroupAndInstructor(SectionGroup sectionGroup, Instructor instructor) {
		return teachingAssignmentRepository.findOneBySectionGroupAndInstructor(sectionGroup, instructor);
	}

	@Override
	public TeachingAssignment findOneById(Long id) {
		return teachingAssignmentRepository.findById(id);
	}

	@Override
	public TeachingAssignment findOrCreateOneBySectionGroupAndInstructor(SectionGroup sectionGroup, Instructor instructor) {
		TeachingAssignment teachingAssignment = this.findOneBySectionGroupAndInstructor(sectionGroup, instructor);

		if (teachingAssignment == null) {
			teachingAssignment = new TeachingAssignment();
			teachingAssignment.setTermCode(sectionGroup.getTermCode());
			teachingAssignment.setSchedule(sectionGroup.getCourse().getSchedule());
			teachingAssignment.setInstructor(instructor);
			teachingAssignment.setSectionGroup(sectionGroup);

			this.save(teachingAssignment);
		}

		return teachingAssignment;
	}

	@Override
	public List<TeachingAssignment> findByScheduleIdAndInstructorId(long scheduleId, long instructorId) {
		return teachingAssignmentRepository.findByScheduleIdAndInstructorId(scheduleId, instructorId);
	}

	@Override
	public List<TeachingAssignment> findByCourseId(long courseId) {
		return teachingAssignmentRepository.findByCourseId(courseId);
	}

	@Override
	public TeachingAssignment findByInstructorIdAndScheduleIdAndTermCodeAndSuggestedCourseNumberAndSuggestedSubjectCodeAndSuggestedEffectiveTermCode(
			long instructorId, long scheduleId, String termCode, String suggestedCourseNumber, String suggestedSubjectCode, String suggestedEffectiveTermCode) {

		Schedule schedule = scheduleService.findById(scheduleId);
		Instructor instructor = instructorService.getOneById(instructorId);
		TeachingAssignment teachingAssignment = null;

		if (schedule != null && instructor != null) {
			teachingAssignment = teachingAssignmentRepository.findOneByScheduleAndInstructorAndTermCodeAndSuggestedCourseNumberAndSuggestedSubjectCodeAndSuggestedEffectiveTermCode(
					schedule, instructor, termCode, suggestedCourseNumber, suggestedSubjectCode, suggestedEffectiveTermCode);
			if (teachingAssignment != null) {
				return teachingAssignment;
			}
		}

		return null;
	}

	@Override
	public List<TeachingAssignment> findApprovedByWorkgroupIdAndYearAndTermCode(long workgroupId, long year, String termCode) {
		return teachingAssignmentRepository.findByScheduleWorkgroupIdAndScheduleYearAndTermCodeAndApprovedTrue(workgroupId, year, termCode);
	}

	@Override
	public List<TeachingAssignment> findByScheduleId(long scheduleId) {
		return teachingAssignmentRepository.findByScheduleId(scheduleId);
	}

	/**
	 * Will return an existing teachingAssignment that matches on all fields, otherwise returns null;
	 *
	 * @param teachingAssignmentDTO
	 * @return
	 */
	@Override
	public TeachingAssignment findByTeachingAssignment(TeachingAssignment teachingAssignmentDTO) {

		if (teachingAssignmentDTO.getInstructor() == null) {
			return null;
		}

		TeachingAssignment teachingAssignment = null;

		if (teachingAssignmentDTO.getSectionGroup() != null) {
			teachingAssignment = teachingAssignmentRepository.findOneBySectionGroupAndInstructor(teachingAssignmentDTO.getSectionGroup(), teachingAssignmentDTO.getInstructor());
		} else {
			teachingAssignment = teachingAssignmentRepository.findOneByInstructorIdAndScheduleIdAndTermCodeAndBuyoutAndAndCourseReleaseAndSabbaticalAndInResidenceAndWorkLifeBalanceAndLeaveOfAbsence(
				teachingAssignmentDTO.getInstructor().getId(),
				teachingAssignmentDTO.getSchedule().getId(),
				teachingAssignmentDTO.getTermCode(),
				teachingAssignmentDTO.isBuyout(),
				teachingAssignmentDTO.isCourseRelease(),
				teachingAssignmentDTO.isSabbatical(),
				teachingAssignmentDTO.isInResidence(),
				teachingAssignmentDTO.isWorkLifeBalance(),
				teachingAssignmentDTO.isWorkLifeBalance()
			);
		}

		return teachingAssignment;
	}

	@Override
	public List<TeachingAssignment> findAllByIds(List<Long> teachingAssignmentIds) {
		return teachingAssignmentRepository.findByIdIn(teachingAssignmentIds);
	}

}