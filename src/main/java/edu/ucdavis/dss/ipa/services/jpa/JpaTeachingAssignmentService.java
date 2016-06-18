package edu.ucdavis.dss.ipa.services.jpa;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.entities.TeachingAssignment;
import edu.ucdavis.dss.ipa.repositories.TeachingAssignmentRepository;
import edu.ucdavis.dss.ipa.repositories.TeachingAssistantPreferenceRepository;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.TeachingAssignmentService;

@Service
public class JpaTeachingAssignmentService implements TeachingAssignmentService {
	@Inject TeachingAssistantPreferenceRepository teachingAssistantPreferenceRepository;
	@Inject GraduateStudentService graduateStudentService;
	@Inject ScheduleService scheduleService;
	@Inject TeachingAssignmentRepository teachingAssignmentRepository;
	@Override
	public TeachingAssignment saveTeachingAssignment(TeachingAssignment teachingAssignment) {
		return teachingAssignmentRepository.save(teachingAssignment);
	}
	
	@Override
	public void deleteTeachingAssignmentById(Long id) {
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

			this.saveTeachingAssignment(teachingAssignment);
		}

		return teachingAssignment;
	}
}