package edu.ucdavis.dss.ipa.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import edu.ucdavis.dss.ipa.entities.InstructorWorkgroupRelationship;

public interface InstructorWorkgroupRelationshipRepository extends CrudRepository<InstructorWorkgroupRelationship, Long> {

	InstructorWorkgroupRelationship findOneById(Long id);

	InstructorWorkgroupRelationship findOneByWorkgroupIdAndInstructorId(long workgroupId, long instructorId);

	@Query("select count(e) > 0 from InstructorWorkgroupRelationship e where e.workgroup.id = :workgroupId and e.instructor.id = :instructorId")
	boolean existsByWorkgroupIdAndInstructorId(@Param("workgroupId") long workgroupId, @Param("instructorId") long instructorId);
}
