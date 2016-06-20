package edu.ucdavis.dss.ipa.services.jpa;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.entities.TeachingAssignment;
import edu.ucdavis.dss.ipa.repositories.TeachingAssignmentRepository;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.TeachingAssignmentService;

import java.util.List;

@Service
public class JpaTeachingAssignmentService implements TeachingAssignmentService {

	@Inject ScheduleService scheduleService;
	@Inject TeachingAssignmentRepository teachingAssignmentRepository;

	@Override
	public TeachingAssignment save(TeachingAssignment teachingAssignment) {
		return teachingAssignmentRepository.save(teachingAssignment);
	}
	
	@Override
	public void delete(Long id) {
		teachingAssignmentRepository.delete(id);
	}

	@Override
	public TeachingAssignment findOneById(Long id) {
		return teachingAssignmentRepository.findById(id);
	}

	@Override
	public TeachingAssignment findOrCreateOneBySectionGroupAndInstructor(SectionGroup sectionGroup, Instructor instructor) {
		TeachingAssignment teachingAssignment = teachingAssignmentRepository.findOneBySectionGroupAndInstructor(sectionGroup, instructor);

		if (teachingAssignment == null) {
			teachingAssignment = new TeachingAssignment();

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
}