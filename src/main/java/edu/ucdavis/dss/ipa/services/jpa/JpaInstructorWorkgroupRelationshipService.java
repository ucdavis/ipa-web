package edu.ucdavis.dss.ipa.services.jpa;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.InstructorWorkgroupRelationship;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.repositories.InstructorWorkgroupRelationshipRepository;
import edu.ucdavis.dss.ipa.services.InstructorWorkgroupRelationshipService;

@Service
public class JpaInstructorWorkgroupRelationshipService implements InstructorWorkgroupRelationshipService {
	@Inject InstructorWorkgroupRelationshipRepository instructorWorkgroupRelationshipRepository;

	@Override
	public InstructorWorkgroupRelationship saveInstructorWorkgroupRelationship(InstructorWorkgroupRelationship idr) {
		return this.instructorWorkgroupRelationshipRepository.save(idr);
	}

	@Override
	public boolean deleteById(Long id) {
		InstructorWorkgroupRelationship instructorWorkgroupRelationship = this.findById(id);
		if(instructorWorkgroupRelationship == null) {
			return false;
		}

		this.instructorWorkgroupRelationshipRepository.delete(id);
		return true;
	}

	@Override
	public InstructorWorkgroupRelationship findById(Long id) {
		return this.instructorWorkgroupRelationshipRepository.findOneById(id);
	}

	@Override
	public InstructorWorkgroupRelationship findOrCreateOneByWorkgroupAndInstructor(Workgroup workgroup,
			Instructor instructor) {
		InstructorWorkgroupRelationship iwr = this.instructorWorkgroupRelationshipRepository.findOneByWorkgroupIdAndInstructorId(workgroup.getId(), instructor.getId());
		
		if(iwr == null) {
			iwr = createOneByWorkgroupAndInstructor(workgroup, instructor);
		}

		return iwr;
	}

	@Override
	public InstructorWorkgroupRelationship createOneByWorkgroupAndInstructor(Workgroup workgroup, Instructor instructor) {
		InstructorWorkgroupRelationship iwr = new InstructorWorkgroupRelationship();
		
		iwr.setWorkgroup(workgroup);
		iwr.setInstructor(instructor);
		
		return this.saveInstructorWorkgroupRelationship(iwr);
	}

	@Override
	public boolean existsByWorkgroupIdAndInstructorId(long workgroupId, long instructorId) {
		return this.instructorWorkgroupRelationshipRepository.existsByWorkgroupIdAndInstructorId(workgroupId, instructorId);
	}
}
