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
import edu.ucdavis.dss.ipa.services.WorkgroupService;

@Service
public class JpaInstructorService implements InstructorService {
	@Inject InstructorRepository instructorRepository;
	@Inject WorkgroupService workgroupService;

	private static final Logger log = LogManager.getLogger();

	@Override
	public Instructor save(Instructor instructor) {
		return this.instructorRepository.save(instructor);
	}

	@Override
	public Instructor getOneById(Long id) {
		return this.instructorRepository.findById(id);
	}

	@Override
	public Instructor getOneByEmployeeId(String employeeId) {
		return this.instructorRepository.findByEmployeeId(employeeId);
	}

	@Override
	public Instructor getOneByLoginId(String loginId) {
		return this.instructorRepository.findByLoginIdIgnoreCase(loginId);
	}

	/**
	 * Overrides findOrCreate without employeeId.
	 * Currently, employeeId is desired for instructor creation but not reliably available from DW.
	 */
	@Override
	public Instructor findOrCreate(String firstName, String lastName, String email, String loginId, Long workgroupId) {
		return this.findOrCreate(firstName, lastName, email, loginId, workgroupId, "");
	}

	@Override
	public Instructor findOrCreate(String firstName, String lastName, String email, String loginId, Long workgroupId, String employeeId) {

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

		return instructor;
	}

	@Override
	public void removeOrphanedByLoginId(String loginId) {
		Instructor instructor = this.getOneByLoginId(loginId);

		if (instructor == null) {
			log.warn("Attempting to find instructor by loginId: " +loginId + ", was not found.");
			return;
		}

		if (instructor.getTeachingAssignments().size() == 0 &&
			instructor.getTeachingCallResponses().size() == 0 &&
			instructor.getTeachingCallReceipts().size() == 0) {
				instructorRepository.delete(instructor.getId());
		}
	}

}