package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.InstructorCost;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.entities.SectionGroupCost;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Set;

@Validated
public interface InstructorService {

	Instructor save(Instructor instructor);

	Instructor getOneById(Long instructorId);

	Instructor getOneByUcdStudentSID(String ucdStudentSID);

	/**
	 * Finds instructor by case-insensitive 'loginId', if exists.
	 * 
	 * @param loginId
	 * @return
	 */
	Instructor getOneByLoginId(String loginId);

	Instructor findOrCreate(String firstName, String lastName, String email, String loginId, Long workgroupId);

	Instructor findOrCreate(String firstName, String lastName, String email, String loginId, Long workgroupId, String ucdStudentSID);

	void removeOrphanedByLoginId(String loginId);

	/**
	 * Find all instructors associated to active 'instructor' type users in the workgroup.
	 * @param workgroupId
	 * @return
	 */
	List<Instructor> findActiveByWorkgroupId(long workgroupId);

	List<Instructor> findAssignedByScheduleId(long scheduleId);

	List<Instructor> findByInstructorCosts(List<InstructorCost> instructorCosts);

	List<Instructor> findBySectionGroups(List<SectionGroup> sectionGroups);

	Set<Instructor> findBySectionGroupCosts(List<SectionGroupCost> sectionGroupCosts);
}