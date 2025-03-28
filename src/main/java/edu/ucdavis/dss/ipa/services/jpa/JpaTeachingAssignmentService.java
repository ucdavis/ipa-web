package edu.ucdavis.dss.ipa.services.jpa;

import jakarta.inject.Inject;

import edu.ucdavis.dss.ipa.entities.InstructorType;
import edu.ucdavis.dss.ipa.services.InstructorService;
import edu.ucdavis.dss.ipa.services.InstructorTypeService;
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
	@Inject InstructorTypeService instructorTypeService;

	/**
	 * If instructor exists, will attempt to fill in the instructorType of the instructor, based on relevant userRoles.
	 * InstructorType may remain null if nothing is found.
	 * @param teachingAssignment
	 * @return
     */
	@Override
	public TeachingAssignment saveAndAddInstructorType(TeachingAssignment teachingAssignment) {
		if (teachingAssignment.getInstructor() != null && teachingAssignment.getInstructorType() == null) {
			InstructorType instructorType = instructorTypeService.findByInstructorAndWorkgroup(teachingAssignment.getInstructor(), teachingAssignment.getSchedule().getWorkgroup());

			teachingAssignment.setInstructorType(instructorType);
		}

		return this.save(teachingAssignment);
	}

	public TeachingAssignment save(TeachingAssignment teachingAssignment) {
		return teachingAssignmentRepository.save(teachingAssignment);
	}

	@Override
	@Transactional
	public void delete(Long id) {
		teachingAssignmentRepository.deleteById(id);
	}

	@Override
	public TeachingAssignment findOneBySectionGroupAndInstructorAndTermCode(SectionGroup sectionGroup, Instructor instructor, String termCode) {
		return teachingAssignmentRepository.findOneBySectionGroupAndInstructorAndTermCode(sectionGroup, instructor, termCode);
	}
	public TeachingAssignment findOneBySectionGroupAndInstructorAndTermCodeAndApprovedTrue(SectionGroup sectionGroup, Instructor instructor, String termCode) {
		return teachingAssignmentRepository.findOneBySectionGroupAndInstructorAndTermCodeAndApprovedTrue(sectionGroup, instructor, termCode);
	}

	@Override
	public TeachingAssignment findOneById(Long id) {
		return teachingAssignmentRepository.findById(id).orElse(null);
	}

	@Override
	public TeachingAssignment findOrCreateOneBySectionGroupAndInstructor(SectionGroup sectionGroup, Instructor instructor) {
		TeachingAssignment teachingAssignment = this.findOneBySectionGroupAndInstructorAndTermCode(sectionGroup, instructor, sectionGroup.getTermCode());

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
	public List<TeachingAssignment> findApprovedByWorkgroupIdAndYear(long workgroupId, long year) {
		return teachingAssignmentRepository.findByScheduleWorkgroupIdAndScheduleYearAndApprovedTrue(workgroupId, year);
	}

	@Override
	public List<TeachingAssignment> findByInstructorIdAndScheduleIdAndTermCode(long instructorId, long scheduleId, String termCode) {
		return teachingAssignmentRepository.findByInstructorIdAndScheduleIdAndTermCode(instructorId, scheduleId, termCode);
	}

	@Override
	public TeachingAssignment update(TeachingAssignment newTeachingAssignment) {
		TeachingAssignment originalTeachingAssignment = this.findOneById(newTeachingAssignment.getId());
		originalTeachingAssignment.setFromInstructor(newTeachingAssignment.isFromInstructor());

		return this.save(originalTeachingAssignment);
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
			teachingAssignment = teachingAssignmentRepository.findOneBySectionGroupAndInstructorAndTermCode(teachingAssignmentDTO.getSectionGroup(), teachingAssignmentDTO.getInstructor(), teachingAssignmentDTO.getSectionGroup().getTermCode());
		} else {
			teachingAssignment = teachingAssignmentRepository.findOneByInstructorIdAndScheduleIdAndTermCodeAndBuyoutAndAndCourseReleaseAndSabbaticalAndInResidenceAndWorkLifeBalanceAndLeaveOfAbsenceAndSabbaticalInResidenceAndJointAppointmentAndInterdisciplinaryTeachingAndWorkLoadCredit(
				teachingAssignmentDTO.getInstructor().getId(),
				teachingAssignmentDTO.getSchedule().getId(),
				teachingAssignmentDTO.getTermCode(),
				teachingAssignmentDTO.isBuyout(),
				teachingAssignmentDTO.isCourseRelease(),
				teachingAssignmentDTO.isSabbatical(),
				teachingAssignmentDTO.isInResidence(),
				teachingAssignmentDTO.isWorkLifeBalance(),
				teachingAssignmentDTO.isWorkLifeBalance(),
				teachingAssignmentDTO.isSabbaticalInResidence(),
				teachingAssignmentDTO.isJointAppointment(),
				teachingAssignmentDTO.isInterdisciplinaryTeaching(),
				teachingAssignmentDTO.isWorkLoadCredit()
			);
		}

		return teachingAssignment;
	}

	@Override
	public List<TeachingAssignment> findAllByIds(List<Long> teachingAssignmentIds) {
		return teachingAssignmentRepository.findByIdIn(teachingAssignmentIds);
	}

	@Override
	public List<TeachingAssignment> updatePreferenceOrder(List<Long> sortedTeachingPreferenceIds) {
		List<TeachingAssignment> teachingAssignments = this.findAllByIds(sortedTeachingPreferenceIds);

		if (teachingAssignments == null) {
			return null;
		}

		Integer priority = 1;

		for (Long id : sortedTeachingPreferenceIds) {
			for(TeachingAssignment teachingAssignment : teachingAssignments) {
				if (id == teachingAssignment.getId()) {
					teachingAssignment.setPriority(priority);
					this.save(teachingAssignment);
					priority++;
				}
			}
		}

		return teachingAssignments;
	}
}
