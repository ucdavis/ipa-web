package edu.ucdavis.dss.ipa.services.jpa;

import java.util.List;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.repositories.InstructorRepository;
import edu.ucdavis.dss.ipa.services.InstructorService;
import edu.ucdavis.dss.ipa.services.InstructorWorkgroupRelationshipService;
import edu.ucdavis.dss.ipa.services.WorkgroupService;

@Service
public class JpaInstructorService implements InstructorService {
	@Inject InstructorRepository instructorRepository;
	@Inject WorkgroupService workgroupService;
	@Inject InstructorWorkgroupRelationshipService instructorWorkgroupRelationshipService;

	private static final Logger log = LogManager.getLogger();

	@Override
	public Instructor saveInstructor(Instructor instructor) {
		return this.instructorRepository.save(instructor);
	}

	@Override
	public Instructor getInstructorById(Long id) {
		return this.instructorRepository.findById(id);
	}

	@Override
	public Instructor getInstructorByEmployeeId(String employeeId) {
		return this.instructorRepository.findByEmployeeId(employeeId);
	}

	@Override
	public Instructor getInstructorByLoginId(String loginId) {
		return this.instructorRepository.findByLoginIdIgnoreCase(loginId);
	}

	/**
	 * Overrides findOrCreateInstructor without employeeId.
	 * Currently, employeeId is desired for instructor creation but not reliably available from DW.
	 */
	@Override
	public Instructor findOrCreateInstructor(String firstName, String lastName, String email, String loginId, Long workgroupId) {
		return this.findOrCreateInstructor(firstName, lastName, email, loginId, workgroupId, "");
	}

	@Override
	public Instructor findOrCreateInstructor(String firstName, String lastName, String email, String loginId, Long workgroupId, String employeeId) {

		// 1) Attempt to find by loginId
		Instructor instructor = instructorRepository.findByLoginIdIgnoreCase(loginId);

		// 2) Attempt to find by employeeId
		if (instructor == null && employeeId.length() > 0) {
			instructor = instructorRepository.findByEmployeeId(employeeId);
		}

		// 3) Create new instructor
		if (instructor == null) {
			instructor = new Instructor();
			
			instructor.setFirstName(firstName);
			instructor.setLastName(lastName);
			instructor.setEmail(email);
			instructor.setLoginId(loginId);
		}
		
		instructorRepository.save(instructor);

		// Ensure instructor workgroup relationship already exists
		for (InstructorWorkgroupRelationship instructorWorkgroupRelationship : instructor.getInstructorWorkgroupRelationships()) {
			if (instructorWorkgroupRelationship.getWorkgroup().getId() == workgroupId) {
				return instructor;
			}
		}

		instructor = addInstructorWorkgroupRelationship(instructor.getId(), workgroupId);

		return instructor;
	}

	@Override
	public Instructor addInstructorWorkgroupRelationship(Long instructorId, Long workgroupId) {
		Instructor instructor = this.getInstructorById(instructorId);
		Workgroup workgroup = workgroupService.findOneById(workgroupId);

		InstructorWorkgroupRelationship instructorWorkgroupRelationship = new InstructorWorkgroupRelationship();
		instructorWorkgroupRelationship.setWorkgroup(workgroup);
		instructorWorkgroupRelationship.setInstructor(instructor);
		instructorWorkgroupRelationshipService.saveInstructorWorkgroupRelationship(instructorWorkgroupRelationship);

		List<InstructorWorkgroupRelationship> instructorWorkgroupRelationships  = instructor.getInstructorWorkgroupRelationships();
		instructorWorkgroupRelationships.add(instructorWorkgroupRelationship);
		instructor.setInstructorWorkgroupRelationships(instructorWorkgroupRelationships);
		instructor = instructorRepository.save(instructor);

		return instructor;
	}

	@Override
	public void removeOrphanedInstructorByLoginId(String loginId) {
		Instructor instructor = this.getInstructorByLoginId(loginId);

		if (instructor == null) {
			log.warn("Attempting to find instructor by loginId: " +loginId + ", was not found.");
			return;
		}

		if (instructor.getInstructorTeachingAssistantPreferences().size() == 0 &&
			instructor.getTeachingAssignments().size() == 0 &&
			instructor.getTeachingCallResponses().size() == 0 &&
			instructor.getTeachingCallReceipts().size() == 0 &&
			instructor.getInstructorTeachingAssistantPreferences().size() == 0 &&
			instructor.getTeachingPreferences().size() == 0) {

				// Remove workgroupRelationships
				for (InstructorWorkgroupRelationship instructorWorkgroupRelationship : instructor.getInstructorWorkgroupRelationships()) {
					instructorWorkgroupRelationshipService.deleteById(instructorWorkgroupRelationship.getId());
				}

				instructorRepository.delete(instructor.getId());
		}
	}
}